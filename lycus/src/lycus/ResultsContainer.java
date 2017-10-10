package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import Triggers.Trigger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import DAL.DAL;
import Events.EvenetsQueue;
import Events.Event;
import GlobalConstants.Enums.ApiAction;
import Interfaces.IResultsContainer;
import Results.BaseResult;
import Results.DiscoveryResult;
import Utils.GeneralFunctions;
import Utils.Logit;

public class ResultsContainer implements IResultsContainer {
	private static ResultsContainer instance;
	private List<BaseResult> results;
	private ConcurrentHashMap<String, ConcurrentHashMap<String, Event>> eventsPerRunnableProbe; // HashMap<runnableProbeId,HashMap<triggerId,event>>
	// private ConcurrentHashMap<String, ConcurrentHashMap<String, Event>>
	// eventsPerTrigger; // HashMap<triggerId,HashMap<runnableProbeId,event>>
	private Object lockResults = new Object();
	private Object lockEvents = new Object();

	private ResultsContainer() {
		results = new ArrayList<BaseResult>();
		eventsPerRunnableProbe = new ConcurrentHashMap<String, ConcurrentHashMap<String, Event>>();
	}

	public static ResultsContainer getInstance() {
		if (instance == null)
			instance = new ResultsContainer();
		return instance;
	}

	public Event getEvent(String runnableProbeId, String triggerId) {
		ConcurrentHashMap<String, Event> runnableProbeEvents = eventsPerRunnableProbe.get(runnableProbeId);
		if (runnableProbeEvents == null)
			return null;
		return runnableProbeEvents.get(triggerId);
	}

	public ConcurrentHashMap<String, Event> getEvent(String runnableProbeId) {
		return eventsPerRunnableProbe.get(runnableProbeId);
	}

	// userId@bucket@hostId@trigger_id
	public Boolean removeEventsById(String eventId) {
		String[] splittedId = eventId.split("@");
		String userId = splittedId[0];
		String bucketId = splittedId[1];
		String hostd = splittedId[2];
		String triggerId = splittedId[3] + "@" + splittedId[4] + "@" + splittedId[5];

		ConcurrentHashMap<String, RunnableProbe> runnableProbes = RunnableProbeContainer.getInstanece()
				.getByUser(userId);

		for (RunnableProbe runnableProbe : runnableProbes.values()) {
			if (runnableProbe.getHost().getHostId().toString().equals(hostd)
					&& runnableProbe.getProbe().getTrigger(triggerId) != null) {
				ConcurrentHashMap<String, Event> runnableProbeEvents = eventsPerRunnableProbe
						.get(runnableProbe.getId());

				List<String> triggersIds = new ArrayList();
				for (Event event : runnableProbeEvents.values()) {
					if (event.getBucketId().equals(bucketId))
						triggersIds.add(event.getTriggerId());
				}

				for (String triggId : triggersIds) {
					synchronized (lockEvents) {
						runnableProbeEvents.remove(triggId);
					}
				}
			}
		}

		return true;
	}

	public boolean addEvent(String runnableProbeId, String triggerId, Event event) {

		Logit.LogDebug("Event for RunnableProbeID: " + runnableProbeId + ", added (Container) = " + event.toString()
				+ ", trace: " + stackTraceToString(Thread.currentThread().getStackTrace()));

		ConcurrentHashMap<String, Event> runnableProbeEvents = null;
		if (eventsPerRunnableProbe.containsKey(runnableProbeId)) {
			runnableProbeEvents = eventsPerRunnableProbe.get(runnableProbeId);
			if (!runnableProbeEvents.containsKey(triggerId))
				runnableProbeEvents.put(triggerId, event);
		} else {
			runnableProbeEvents = new ConcurrentHashMap<String, Event>();
			runnableProbeEvents.put(triggerId, event);
			synchronized (lockEvents) {
				eventsPerRunnableProbe.put(runnableProbeId, runnableProbeEvents);
			}
		}
		return true;
	}

	public String stackTraceToString(StackTraceElement[] stackTrace) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : stackTrace) {
			sb.append(element.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public boolean removeEvent(String runnableProbeId, String triggerId) {
		ConcurrentHashMap<String, Event> runnableProbeEvents = null;
		if (eventsPerRunnableProbe.containsKey(runnableProbeId))
			runnableProbeEvents = eventsPerRunnableProbe.get(runnableProbeId);

		synchronized (lockEvents) {
			runnableProbeEvents.remove(triggerId);
		}
		return true;
	}

	public boolean clear() {
		eventsClear();
		removeSentResults();
		return true;
	}

	private void eventsClear() {
		for (Map.Entry<String, ConcurrentHashMap<String, Event>> runnableProbeEvents : eventsPerRunnableProbe
				.entrySet()) {
			for (Map.Entry<String, Event> triggerEvent : runnableProbeEvents.getValue().entrySet()) {
				String triggerId = triggerEvent.getKey();
				Event event = triggerEvent.getValue();
				if (event.getIsStatus() && event.isSent()) {
					runnableProbeEvents.getValue().remove(triggerId);
				}
			}
		}
	}

	private JSONObject rawResultsDBFormat(BaseResult rpr) {
		JSONObject result = new JSONObject();

		String rpStr = rpr.getRunnableProbeId();
		if (rpStr.contains("discovery_45035c45-2679-4af6-84ca-e924e78dd7bc"))
			Logit.LogDebug("BREAKPOINT");

		RunnableProbe rp = RunnableProbeContainer.getInstanece().get(rpr.getRunnableProbeId());

		Object resultObject = rpr.getResultObject();
		// if (resultObject== null)
		// return null;

		if (rp == null)
			return null;
		result.put("USER_ID", rp.getProbe().getUser().getUserId().toString());
		result.put("PROBE_TYPE", rp.getProbeType().name());
		result.put("RESULTS_TIME", rpr.getLastTimestamp());
		result.put("RESULTS_NAME", rpr.getName());
		result.put("RESULTS", resultObject);
		result.put("RUNNABLE_PROBE_ID", rp.getId());

		return result;
	}

	public void pullCurrentLiveEvents() {
		while (true) {
			Logit.LogInfo("Retrieving existing live eventsHandler from REDIS...");
			Object eventsObject = DAL.getInstanece().get(ApiAction.GetServerLiveEvents);

			if (eventsObject == null) {
				Logit.LogWarn("Unable to retrieve existing live eventsHandler, trying again in about 30 secs...");
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					Logit.LogError("ResultsContainer - pullCurrentLiveEvents()",
							"Main thread interrupted!" + e.getMessage());
					continue;
				}
				continue;
			}

			JSONObject events = (JSONObject) eventsObject;

			if (events.size() == 0)
				return;

			for (Iterator iterator = events.keySet().iterator(); iterator.hasNext();) {
				String it = (String) iterator.next();

				if (it.contains(
						"0eb888bc-ba24-49f0-8468-da89ca830c77@4031d0c0-39da-4bd3-bcf1-6d081148680f@discovery_35667a76-cb01-4108-9429-cac2dbcf933e@e99ebb11-5e5c-4db3-9573-b54eda1f5ba9"))
					Logit.LogDebug("BP");

				try {
					String userId = it.split("@")[0];
					String bucketId = it.split("@")[1];
					UUID hostId = UUID.fromString(it.split("@")[2]);
					UUID templateId = UUID.fromString(it.split("@")[3]);
					String probeId = it.split("@")[4];
					String triggerId = templateId + "@" + probeId + "@" + it.split("@")[5];
					if (it.split("@").length == 7)
						probeId += "@" + it.split("@")[6];
					String hostName = "";
					String hostNotificationGroup = "";
					String triggerName = "";
					String triggerSeverity = "";

					String runnableProbeId = GeneralFunctions.getRunnableProbeId(templateId, hostId, probeId);
					if (runnableProbeId
							.contains("e339d292-e724-4098-897e-758eeb075978@icmp_91b0eac5-d25c-4d93-a061-66572942ad7f"))
						Logit.LogDebug("BREAKPOINT");

					RunnableProbe runnableProbe = RunnableProbeContainer.getInstanece().get(runnableProbeId);
					long timestamp = Long.parseLong((String) events.get(it));

					User user = UsersManager.getUser(userId);
					if (user != null) {
						Host host = user.getHost(hostId);
						if (host != null) {
							hostName = host.getName();
							if (host.getNotificationGroups() != null)
								hostNotificationGroup = host.getNotificationGroups();
						}
					}

					if (runnableProbe != null) {
						Trigger trigger = runnableProbe.getProbe().getTrigger(triggerId);
						if (trigger != null) {
							triggerName = trigger.getName();
							triggerSeverity = trigger.getSvrty().toString();
						}
					}
					Event event = new Event(triggerId, userId, bucketId, hostName, hostNotificationGroup, triggerName,
							triggerSeverity, runnableProbeId);
					event.setTime(timestamp);
					event.setSent(true);

					if (runnableProbe == null || !runnableProbe.getProbe().getTriggers().containsKey(triggerId)) {
						event.setIsStatus(true);
						event.setDeleted(true);
						EvenetsQueue.getInstance().add(event, null);
						Logit.LogDebug(
								"Event for RunnableProbeID: " + runnableProbeId + ", added = " + event.toString());

					} else
						addEvent(runnableProbeId, triggerId, event);

				} catch (Exception e) {
					Logit.LogError("ResultsContainer - pullCurrentLiveEvents()", "Unable to process live event: " + it);
					Logit.LogError("ResultsContainer - pullCurrentLiveEvents()", "E: " + e.getMessage());
				}
			}
			return;
		}
	}

	public List<BaseResult> getResult(String runnableProbeId) {

		List<BaseResult> runnableProbeResults = new ArrayList<BaseResult>();

		for (BaseResult result : results) {
			if (result.getRunnableProbeId().equals(runnableProbeId)) {
				runnableProbeResults.add(result);
			}
		}
		if (runnableProbeResults.size() == 0)
			return null;
		return runnableProbeResults;
	}

	@Override
	public boolean addResult(BaseResult result) {

		if (result.getRunnableProbeId().equals(
				"8b0104e7-5902-4419-933f-668582fc3acd@6975cb58-8aa4-4ecd-b9fc-47b78c0d7af8@snmp_5d937636-eb75-4165-b339-38a729aa2b7d"))
			Logit.LogDebug("BREAKPOINT");

		if (result instanceof DiscoveryResult) {
			DiscoveryResult discoveryResult = (DiscoveryResult) result;
			switch (discoveryResult.getElementsType()) {
			case bw:
				if (!ElementsContainer.getInstance().isNicElementsChanged(discoveryResult)) {
					// return true;
				}
				break;
			case ds:
				if (!ElementsContainer.getInstance().isDiskElementsChanged(discoveryResult)) {
					// return true;
				}
				break;
			default:
				Logit.LogDebug("unknown elements found for RP: "+result.getRunnableProbeId());
			}
		}

		synchronized (lockResults) {
			results.add(result);
		}
		return true;
	}

	@Override
	public boolean removeSentResults() {
		List<BaseResult> results = new ArrayList<BaseResult>();
		for (BaseResult result : results) {
			if (result.isSent())
				results.add(result);
		}

		for (BaseResult result : results) {
			synchronized (lockResults) {
				this.results.remove(result);
			}
		}
		return true;
	}

	@Override
	public String getResults() {
		try {
			List<BaseResult> resultsToDelete = new ArrayList<BaseResult>();
			JSONArray resultsDBFormat = new JSONArray();
			synchronized (lockResults) {
				for (BaseResult result : results) {
					if (!result.isSent()) {

						// String rpStr = result.getRunnableProbeId();
						// if (rpStr.contains(
						// "8b0104e7-5902-4419-933f-668582fc3acd@6975cb58-8aa4-4ecd-b9fc-47b78c0d7af8@snmp_5d937636-eb75-4165-b339-38a729aa2b7d"))
						// Logit.LogDebug("BREAKPOINT");
						result.setSent(true);
						JSONObject resultDBFormat = rawResultsDBFormat(result);
						if (resultDBFormat == null)
							continue;
						resultsDBFormat.add(resultDBFormat);
						resultsToDelete.add(result);
					}
				}
			}

			for (BaseResult result : resultsToDelete) {
				synchronized (lockResults) {
					this.results.remove(result);
				}
			}

			return resultsDBFormat.toString();

		} catch (Exception e) {
			Logit.LogFatal("ResultsContainer - getResults()",
					"Error getting results from resultsContainer! E: " + e.getMessage(), e);
			return null;
		}
	}

	// public EventsObject getEventsPerRunnableProbe() {
	// int countEventsToSend = 0;
	// ArrayList<HashMap<String, HashMap<String, String>>> eventsToSend = new
	// ArrayList<HashMap<String, HashMap<String, String>>>();
	// for (Map.Entry<String, ConcurrentHashMap<String, Event>>
	// runnableProbeEventsEntry : eventsPerRunnableProbe
	// .entrySet()) {
	// String runnableProbeId = runnableProbeEventsEntry.getKey();
	//
	// ConcurrentHashMap<String, Event> runnableProbeEvents =
	// runnableProbeEventsEntry.getValue();
	//
	// for (Map.Entry<String, Event> triggerEvent :
	// runnableProbeEvents.entrySet()) {
	// String triggerId = triggerEvent.getKey();
	// Event event = triggerEvent.getValue();
	//
	// Trigger trigger = null;
	//
	// String rpStr = runnableProbeId;
	// if
	// (rpStr.contains("ff00ff2c-0f40-4616-9ac4-a71447b22431@inner_33695a83-654d-4177-b90d-0a89c5f0120d"))
	// Logit.LogDebug("BREAKPOINT");
	//
	// try {
	// // RunnableProbe runnableProbe =
	// // RunnableProbeContainer.getInstanece().get(runnableProbeId);
	// // if (runnableProbe != null)
	// // trigger = runnableProbe.getProbe().getTrigger(triggerId);
	//
	// if (!event.isSent() || (event.isSent() && event.getIsStatus())) {
	// HashMap<String, HashMap<String, String>> sendingEvents =
	// eventDBFormat(runnableProbeId,
	// triggerId, event);
	//
	// eventsToSend.add(sendingEvents);
	// String status = triggerEvent.getValue().getIsStatus() ? "true" : "false";
	// event.setSent(true);
	// countEventsToSend++;
	// Logit.LogInfo("Event in bucketId: " +
	// triggerEvent.getValue().getBucketId() + ", triggerId: "
	// + triggerEvent.getValue().getTriggerId() + ", host: "
	// + triggerEvent.getValue().getHostName() + ", status: " + status
	// + " is ready for sending");
	// }
	// } catch (Exception e) {
	// Logit.LogError(null, "Unable to process event for triggerId: " +
	// triggerId + ", RunnableProbeId: "
	// + runnableProbeId, e);
	// continue;
	// }
	// }
	// }
	//
	// Gson gson = new GsonBuilder().setPrettyPrinting().create();
	// return new EventsObject(gson.toJson(eventsToSend), countEventsToSend);
	// }

	// public void cleanEvents() {
	// for (Map.Entry<String, ConcurrentHashMap<String, Event>>
	// runnableProbeEventsEntry : eventsPerRunnableProbe
	// .entrySet()) {
	// String runnableProbeId = runnableProbeEventsEntry.getKey();
	//
	// ConcurrentHashMap<String, Event> runnableProbeEvents =
	// runnableProbeEventsEntry.getValue();
	// for (Map.Entry<String, Event> triggerEvent :
	// runnableProbeEvents.entrySet()) {
	// Event event = triggerEvent.getValue();
	// if (event.isSent() && event.getIsStatus()) {
	// synchronized (lockEvents) {
	// this.eventsPerRunnableProbe.get(runnableProbeId).remove(triggerEvent.getKey());
	// }
	// }
	// }
	// }
	// }

	private HashMap<String, HashMap<String, String>> eventDBFormat(String runnableProbeId, String triggerId,
			Event event) {
		HashMap<String, HashMap<String, String>> sendingEvents = new HashMap<String, HashMap<String, String>>();
		HashMap<String, String> eventValues = new HashMap<String, String>();
		eventValues.put("trigger_id", triggerId);
		eventValues.put("event_timestamp", String.valueOf(event.getTime()));
		eventValues.put("event_status", String.valueOf(event.getIsStatus()));
		eventValues.put("user_id", event.getUserId());
		eventValues.put("host_id", runnableProbeId.split("@")[1]);
		eventValues.put("host_bucket", event.getBucketId());
		eventValues.put("extra_info", event.getExtraInfo());
		eventValues.put("event_sub_type", event.getSubType());
		eventValues.put("trigger_name", event.getTriggerName());
		eventValues.put("trigger_severity", event.getTriggerSeverity());
		eventValues.put("host_name", event.getHostName());
		eventValues.put("host_notifs_groups", event.getHostNotificationGroup());

		if (!event.getSubType().contains("regular"))
			Logit.LogDebug("Breakpoint");

		if (event.isDeleted())
			eventValues.put("remove_object", "true");

		if (event.getIsStatus())
			eventValues.put("origin_timestamp", String.valueOf(event.getOriginalTimeStamp()));

		sendingEvents.put(runnableProbeId, eventValues);
		return sendingEvents;
	}

	public void resendEvents(String triggerId, String eventInfo, String newTriggerName, String newSeverity) {
		for (ConcurrentHashMap<String, Event> events : eventsPerRunnableProbe.values()) {
			if (events.containsKey(triggerId) && events.get(triggerId).isSent()) {
				// events.get(triggerId).setTime(System.currentTimeMillis());
				Event event = events.get(triggerId);
				event.setSent(false);
				event.setExtraInfo(eventInfo);
				if (newTriggerName != null)
					event.setTriggerName(newTriggerName);
				if (newSeverity != null)
					event.setTriggerSeverity(newSeverity);

				RunnableProbeContainer.getInstanece().get(event.getRunnableProbeId()).getEventTrigger()
						.appendSubType(event);

				Logit.LogDebug("Event for RunnableProbeID: " + event.getRunnableProbeId() + ", added (resend) = "
						+ event.toString());
				EvenetsQueue.getInstance().add(event, null);
			}
		}
	}
}
