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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import DAL.DAL;
import GlobalConstants.Enums.ApiAction;
import Interfaces.IResultsContainer;
import Results.BaseResult;
import Results.DiscoveryResult;
import Utils.GeneralFunctions;
import Utils.Logit;

public class ResultsContainer implements IResultsContainer {
	private static ResultsContainer instance;
	private List<BaseResult> results;
	private ConcurrentHashMap<String, ConcurrentHashMap<String, Event>> events; // HashMap<runnableProbeId,

	private Object lockResults = new Object();
	private Object lockEvents = new Object();

	private ResultsContainer() {
		results = new ArrayList<BaseResult>();
		events = new ConcurrentHashMap<String, ConcurrentHashMap<String, Event>>();
	}

	public static ResultsContainer getInstance() {
		if (instance == null)
			instance = new ResultsContainer();
		return instance;
	}

	public Event getEvent(String runnableProbeId, String triggerId) {
		ConcurrentHashMap<String, Event> runnableProbeEvents = events.get(runnableProbeId);
		if (runnableProbeEvents == null)
			return null;
		return runnableProbeEvents.get(triggerId);
	}

	public ConcurrentHashMap<String, Event> getEvent(String runnableProbeId) {
		return events.get(runnableProbeId);
	}

	public boolean addEvent(String runnableProbeId, String triggerId, Event event) {
		ConcurrentHashMap<String, Event> runnableProbeEvents = null;
		if (events.containsKey(runnableProbeId)) {
			runnableProbeEvents = events.get(runnableProbeId);
			if (!runnableProbeEvents.containsKey(triggerId))
				runnableProbeEvents.put(triggerId, event);
		} else {
			runnableProbeEvents = new ConcurrentHashMap<String, Event>();
			runnableProbeEvents.put(triggerId, event);
			synchronized (lockEvents) {
				events.put(runnableProbeId, runnableProbeEvents);
			}
		}
		return true;
	}

	public boolean removeEvent(String runnableProbeId, String triggerId) {
		ConcurrentHashMap<String, Event> runnableProbeEvents = null;
		if (events.containsKey(runnableProbeId))
			runnableProbeEvents = events.get(runnableProbeId);

		synchronized (lockEvents) {
			runnableProbeEvents.remove(triggerId);
		}
		return true;
	}

	public boolean clear() {
		eventsClear();
		// TODO: Leave 10 last results from each kind on the list
		// results.clear();
		removeSentResults();
		return true;
	}

	private void eventsClear() {
		for (Map.Entry<String, ConcurrentHashMap<String, Event>> runnableProbeEvents : events.entrySet()) {
			// String runnableProbeId = runnableProbeEvents.getKey();
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
						"	721feef6-504b-4fe3-81e3-089ab33d53a1@6b999cd6-fcbb-4ca8-9936-5529b4c66976@snmp_986e5c7a-5382-44ce-8421-dea5c02ae6aa"))
					Logit.LogDebug("BREAKPOINT");
				try {
					UUID hostId = UUID.fromString(it.split("@")[0]);
					UUID templateId = UUID.fromString(it.split("@")[1]);
					String probeId = it.split("@")[2];
					UUID triggerId = UUID.fromString(it.split("@")[3]);
					long timestamp = Long.parseLong((String) events.get(it));

					RunnableProbe runnableProbe = RunnableProbeContainer.getInstanece()
							.get(GeneralFunctions.getRunnableProbeId(templateId, hostId, probeId));

					if (runnableProbe == null) {
						Logit.LogWarn(
								"Runnable Probe: " + GeneralFunctions.getRunnableProbeId(templateId, hostId, probeId)
										+ " for existing live event doesnt exists so doesnt added!");
						continue;
					}

					Trigger trigger = runnableProbe.getProbe().getTriggers()
							.get(templateId.toString() + "@" + probeId + "@" + triggerId.toString());

					Event event = new Event(trigger);
					event.setTime(timestamp);
					event.setSent(true);

					addEvent(runnableProbe.getId(), triggerId.toString(), event);
					// result.getEvents().put(trigger, event);
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
				return false;
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

	// @Override
	// public String getRollups() {
	// return RollupsContainer.getInstance().getAllFinsihedRollups();
	// }

	@Override
	public String getEvents() {
		ArrayList<HashMap<String, HashMap<String, String>>> eventsToSend = new ArrayList<HashMap<String, HashMap<String, String>>>();
		for (Map.Entry<String, ConcurrentHashMap<String, Event>> runnableProbeEventsEntry : events.entrySet()) {
			String runnableProbeId = runnableProbeEventsEntry.getKey();

			ConcurrentHashMap<String, Event> runnableProbeEvents = runnableProbeEventsEntry.getValue();

			for (Map.Entry<String, Event> triggerEvent : runnableProbeEvents.entrySet()) {
				String triggerId = triggerEvent.getKey();
				Event event = triggerEvent.getValue();
				RunnableProbe runnableProbe = RunnableProbeContainer.getInstanece().get(runnableProbeId);
				Trigger trigger = runnableProbe.getProbe().getTrigger(triggerId);
				try {
					String rpStr = runnableProbeId;
					if (rpStr.contains(
							"ff00ff2c-0f40-4616-9ac4-a71447b22431@inner_33695a83-654d-4177-b90d-0a89c5f0120d"))
						Logit.LogDebug("BREAKPOINT");

					if (!event.isSent() || (event.isSent() && event.getIsStatus())) {
						HashMap<String, HashMap<String, String>> sendingEvents = eventDBFormat(triggerId, event,
								runnableProbe, trigger);

						eventsToSend.add(sendingEvents);
						event.setSent(true);
					}
				} catch (Exception e) {
					Logit.LogError(null, "Unable to process evsent for triggerId: " + triggerId + ", RunnableProbeId: "
							+ runnableProbeId);
					continue;
				}
			}
		}

		for (Map.Entry<String, ConcurrentHashMap<String, Event>> runnableProbeEventsEntry : events.entrySet()) {
			String runnableProbeId = runnableProbeEventsEntry.getKey();

			ConcurrentHashMap<String, Event> runnableProbeEvents = runnableProbeEventsEntry.getValue();
			for (Map.Entry<String, Event> triggerEvent : runnableProbeEvents.entrySet()) {
				Event event = triggerEvent.getValue();
				if (event.isSent() && event.getIsStatus()) {
					synchronized (lockEvents) {
						this.events.get(runnableProbeId).remove(triggerEvent.getKey());
					}
				}
			}
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return (gson.toJson(eventsToSend));
	}

	private HashMap<String, HashMap<String, String>> eventDBFormat(String triggerId, Event event,
			RunnableProbe runnableProbe, Trigger trigger) {
		HashMap<String, HashMap<String, String>> sendingEvents = new HashMap<String, HashMap<String, String>>();
		HashMap<String, String> eventValues = new HashMap<String, String>();
		eventValues.put("trigger_id", triggerId);
		eventValues.put("host_id", runnableProbe.getHost().getHostId().toString());
		eventValues.put("host_name", runnableProbe.getHost().getName());
		eventValues.put("user_id", runnableProbe.getProbe().getUser().getUserId().toString());
		eventValues.put("trigger_name", trigger.getName());
		eventValues.put("trigger_severity", trigger.getSvrty().toString());
		eventValues.put("event_timestamp", String.valueOf(event.getTime()));
		eventValues.put("event_status", String.valueOf(event.getIsStatus()));
		eventValues.put("host_bucket", runnableProbe.getHost().getBucket());
		if (runnableProbe.getHost().getNotificationGroups() != null)
			eventValues.put("host_notifs_groups", runnableProbe.getHost().getNotificationGroups().toString());
		else
			eventValues.put("host_notifs_groups", null);
		sendingEvents.put(runnableProbe.getId(), eventValues);
		return sendingEvents;
	}
}
