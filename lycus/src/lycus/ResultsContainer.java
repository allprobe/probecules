package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import DAL.ApiInterface;
import DAL.DAL;
import GlobalConstants.Enums;
import GlobalConstants.ProbeTypes;
import Interfaces.IResultsContainer;
import Results.BaseResult;
import Results.DiscoveryResult;
import Rollups.RollupsContainer;
import SLA.SLAContainer;
import Utils.GeneralFunctions;
import Utils.Logit;

public class ResultsContainer implements IResultsContainer {
	private static ResultsContainer instance;
	private List<BaseResult> results;
	private ConcurrentHashMap<String, ConcurrentHashMap<String, Event>> events; // HashMap<runnableProbeId,
	// HashMap<triggerId,
	// Event>>
	private SLAContainer slaContainer;

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
		if (events.containsKey(runnableProbeId))
			runnableProbeEvents = events.get(runnableProbeId);
		else {
			runnableProbeEvents = new ConcurrentHashMap<String, Event>();
		}

		runnableProbeEvents.put(triggerId, event);
		synchronized (lockEvents) {
			events.put(runnableProbeId, runnableProbeEvents);
		}
		return true;
	}

	public boolean clear() {
		// synchronized (lockEvents) {
		//
		// events.clear();
		// }
		eventsClear();
		// TODO: Leave 10 last results from each kind on the list
		results.clear();
		
		return true;
	}

	private void eventsClear() {
		for (Map.Entry<String, ConcurrentHashMap<String, Event>> runnableProbeEvents : events.entrySet()) {
			String runnableProbeId = runnableProbeEvents.getKey();
			for (Map.Entry<String, Event> triggerEvent : runnableProbeEvents.getValue().entrySet()) {
				String triggerId = triggerEvent.getKey();
				Event event = triggerEvent.getValue();
				if (event.isStatus() && event.isSent()) {
					runnableProbeEvents.getValue().remove(triggerId);
				}
			}
		}
	}

	private JSONObject rawResultsDBFormat(BaseResult rpr) {
		JSONObject result = new JSONObject();

		String rpStr = rpr.getRunnableProbeId();
		if (rpStr.contains(
				"discovery_45035c45-2679-4af6-84ca-e924e78dd7bc"))
			Logit.LogDebug("BREAKPOINT");
		
		RunnableProbe rp = RunnableProbeContainer.getInstanece().get(rpr.getRunnableProbeId());
		
		if(rp.getProbeType().equals(ProbeTypes.DISCDISK))
			Logit.LogDebug("BREAKPOINT");
		
		if (rp == null)
			return null;
		result.put("USER_ID", rp.getProbe().getUser().getUserId().toString());
		result.put("PROBE_TYPE", rp.getProbeType().name());
		result.put("RESULTS_TIME", rpr.getLastTimestamp());
		result.put("RESULTS_NAME", rpr.getName());
		result.put("RESULTS", rpr.getResultString());
		result.put("RUNNABLE_PROBE_ID", rp.getId());

		return result;
	}

	// private HashMap<String, BaseResults> getAllResultsUsers(ArrayList<User>
	// users) {
	// HashMap<String, BaseResults> rprs = new HashMap<String, BaseResults>();
	// for (User u : users) {
	// Collection<RunnableProbe> usersRPs = u.getAllRunnableProbes().values();
	// for (RunnableProbe rp : usersRPs) {
	//
	// rprs.put(rp.getRPString(), rp.getResult());
	// }
	// }
	// return rprs;
	// }

	public void pullCurrentLiveEvents() {
		while (true) {
			Logit.LogInfo("Retrieving existing live eventsHandler from REDIS...");
			Object eventsObject = ApiInterface.executeRequest(Enums.ApiAction.GetServerLiveEvents, "GET", null);

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
				
				if(it.contains("c3f052eb-d8e3-4672-9bab-cb25fc6e702f@0b05919c-6cc0-42cc-a74b-de3b0dcd4a2a@port_667b9da7-1d9b-46b7-8299-1cc981cb8cc8@07d1af43-1d78-4ddc-9d3b-ee7e3cf8eb50"))
					Logit.LogDebug("BREAKPOINT");
				try {
					UUID hostId = UUID.fromString(it.split("@")[0]);
					UUID templateId = UUID.fromString(it.split("@")[1]);
					String probeId = it.split("@")[2];
					UUID triggerId = UUID.fromString(it.split("@")[3]);
					long timestamp = Long.parseLong((String) events.get(it));

					RunnableProbe runnableProbe = RunnableProbeContainer.getInstanece()
							.get(GeneralFunctions.getRunnableProbeId(templateId, hostId, probeId));

					if(runnableProbe==null)
					{
						Logit.LogWarn( "Runnable Probe: "+GeneralFunctions.getRunnableProbeId(templateId, hostId, probeId)+" for existing live event doesnt exists so doesnt added!");
						continue;
					}
					
					Trigger trigger = runnableProbe.getProbe().getTriggers()
							.get(templateId.toString() + "@" + probeId + "@" + triggerId.toString());

					Event event = new Event(trigger, false);
					event.setTime(timestamp);
					event.setSent(true);

					addEvent(runnableProbe.getId(), triggerId.toString(), event);
					// result.getEvents().put(trigger, event);
				} catch (Exception e) {
					Logit.LogError("ResultsContainer - pullCurrentLiveEvents()", "Unable to process live event: "+it);
					Logit.LogError("ResultsContainer - pullCurrentLiveEvents()", "E: "+e.getMessage());
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
		
		if(result.getRunnableProbeId().contains("discovery_45035c45-2679-4af6-84ca-e924e78dd7bc"))
			Logit.LogDebug("BREAKPOINT");
			synchronized (lockResults) {
				results.add(result);
		}
		return true;
	}

	@Override
	public boolean removeSentResults() {
		for (BaseResult result : results) {
			if (result.isSent())
				synchronized (lockResults) {
					results.remove(result);
				}
			result = null;
		}
		return true;
	}

	@Override
	public String getResults() {
		// HashMap<String, HashMap<String, HashMap<String, String>>> newResults
		// = new HashMap<String, HashMap<String, HashMap<String, String>>>();
		// newResults.put("RAW", new HashMap<String, HashMap<String,
		// String>>());
		//
		// newResults.put("4mRollups", new HashMap<String, HashMap<String,
		// String>>());
		// newResults.put("20mRollups", new HashMap<String, HashMap<String,
		// String>>());
		// newResults.put("1hRollups", new HashMap<String, HashMap<String,
		// String>>());
		// newResults.put("6hRollups", new HashMap<String, HashMap<String,
		// String>>());
		// newResults.put("36hRollups", new HashMap<String, HashMap<String,
		// String>>());
		// newResults.put("11dRollups", new HashMap<String, HashMap<String,
		// String>>());
		//
		// for (BaseResult result : results) {
		//
		// if
		// (result.getRunnableProbeId().contains("fc46cf87-0872-4e5d-9b83-c44a3d1f3ea6@icmp_1f1aed08-7331-4126-97ef-225e90b4a969"))
		// System.out.println("BREAKPOINT - RunnableProbesHistory");
		//
		// if (result.getLastTimestamp() == null || result.getLastTimestamp() ==
		// 0)
		// continue;
		//
		// HashMap<String, String> probeResults;
		//
		// try {
		//
		// probeResults = result.getResults();
		//
		// if (probeResults.containsKey("error")) {
		// Logit.LogError("ResultsContainer - getResults()", "Seriious error
		// getting runnable probe results of: " + result.getRunnableProbeId());
		// continue;
		// }
		// for (Map.Entry<String, String> probeResult :
		// probeResults.entrySet()) {
		// String resultKey = probeResult.getKey();
		// String resultValue = probeResult.getValue();
		//
		// if (resultKey.contains("RAW")) {
		// HashMap<String, String> test = rawResultsDBFormat(result, resultKey,
		// resultValue);
		// newResults.get("RAW").put(result.getRunnableProbeId(),
		// rawResultsDBFormat(result, resultKey, resultValue));
		// }
		// if (resultKey.contains("ROLLUP_4minutes")) {
		// newResults.get("4mRollups").put(rp.getRPString(),
		// rollupResultsDBFormat(result, resultKey, resultValue));
		// }
		// if (resultKey.contains("ROLLUP_20minutes")) {
		// newResults.get("20mRollups").put(rp.getRPString(),
		// rollupResultsDBFormat(result, resultKey, resultValue));
		// }
		// if (resultKey.contains("ROLLUP_1hour")) {
		// newResults.get("1hRollups").put(rp.getRPString(),
		// rollupResultsDBFormat(result, resultKey, resultValue));
		// }
		// if (resultKey.contains("ROLLUP_6hour")) {
		// newResults.get("6hRollups").put(rp.getRPString(),
		// rollupResultsDBFormat(result, resultKey, resultValue));
		// }
		// if (resultKey.contains("ROLLUP_36hour")) {
		// newResults.get("36hRollups").put(rp.getRPString(),
		// rollupResultsDBFormat(result, resultKey, resultValue));
		// }
		// if (resultKey.contains("ROLLUP_11day")) {
		// newResults.get("11dRollups").put(rp.getRPString(),
		// rollupResultsDBFormat(result, resultKey, resultValue));
		// }
		// }
		// } catch (Throwable th) {
		//
		// Logit.LogError("ResultsContainer - getResults()",
		// "Error collecting runnable probes results! stopped at: " +
		// result.getRunnableProbeId());
		// StringWriter sw = new StringWriter();
		// PrintWriter pw = new PrintWriter(sw);
		// th.printStackTrace(pw);
		//
		// Logit.LogError("ResultsContainer - getResults()", sw.toString());
		// }
		// }
		//
		// Run
		// RuntimeTypeAdapterFactory<BaseResult> adapter =
		// RuntimeTypeAdapterFactory
		// .of(ObixBaseObj.class)
		// .registerSubtype(ObixBaseObj.class)
		// .registerSubtype(ObixOp.class);
		try{
		JSONArray resultsDBFormat = new JSONArray();
		for (int i = 0; i < results.size(); i++) {
			
			String rpStr = results.get(i).getRunnableProbeId();
			if (rpStr.contains(
					"discovery_777938b0-e4b0-4ec6-b0f2-ea880a0c09ef"))
				Logit.LogDebug("BREAKPOINT");
			
			JSONObject resultDBFormat = rawResultsDBFormat(results.get(i));
			if (resultDBFormat == null)
				continue;
			resultsDBFormat.add(resultDBFormat);
		}
		return resultsDBFormat.toString();
		}
		catch(Exception e)
		{
			Logit.LogFatal("ResultsContainer - getResults()", "Error getting results from resultsContainer! E: "+e.getMessage(),e);
//			Logit.LogFatal("ResultsContainer - getResults()", "trace: "+e.getStackTrace().toString(),e);
			return null;
		}
		// try {
		// String jsonString = null;
		// synchronized(lock1) {
		// jsonString = JsonUtil.ToJson(this.results);
		// }
		// return jsonString;
		// } catch (Exception e) {
		// Logit.LogFatal("ResultsContainer - getResults()",
		// "Unable to parse results to json format! not sent!, E: " +
		// e.getMessage());
		//
		// return null;
		// }
	}

	@Override
	public String getRollups() {
		return RollupsContainer.getInstance().getAllFinsihedRollups();
	}

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
					if (!event.isSent()) {

						String rpStr = runnableProbeId;
						if (rpStr.contains(
								"ff00ff2c-0f40-4616-9ac4-a71447b22431@inner_33695a83-654d-4177-b90d-0a89c5f0120d"))
							Logit.LogDebug("BREAKPOINT");

						HashMap<String, HashMap<String, String>> sendingEvents = eventDBFormat(triggerId, event,
								runnableProbe, trigger);

						eventsToSend.add(sendingEvents);
						event.setSent(true);
					}
				} catch (Exception e) {
					Logit.LogError(null, "Unable to process event for triggerId: " + triggerId + ", RunnableProbeId: "
							+ runnableProbeId);
					continue;
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
		eventValues.put("event_status", String.valueOf(event.isStatus()));
		eventValues.put("host_bucket", runnableProbe.getHost().getBucket());
		if (runnableProbe.getHost().getNotificationGroups() != null)
			eventValues.put("host_notifs_groups", runnableProbe.getHost().getNotificationGroups().toString());
		else
			eventValues.put("host_notifs_groups", null);
		sendingEvents.put(runnableProbe.getId(), eventValues);
		return sendingEvents;
	}
}
