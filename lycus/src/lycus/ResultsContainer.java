package lycus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lycus.DAL.ApiInterface;
import lycus.GlobalConstants.Enums;
import lycus.GlobalConstants.LogType;
import lycus.Results.BaseResult;
import lycus.Utils.GeneralFunctions;
import lycus.Utils.Logit;
import sun.misc.Queue;
import lycus.Interfaces.IResultsContainer;

public class ResultsContainer implements IResultsContainer {
	private static ResultsContainer instance;
	private List<BaseResult> results;

	private ResultsContainer() {
		results = new ArrayList<BaseResult>();
	}

	public static ResultsContainer getInstance() {
		if (instance == null)
			instance = new ResultsContainer();
		return instance;
	}

	private HashMap<String, String> rollupResultsDBFormat(BaseResult rpr, String resultkey, String resultvalue) {

		HashMap<String, String> tableResults;
		tableResults = new HashMap<String, String>();
		RunnableProbe rp = rpr.getRp();
		tableResults.put("USER_ID", rp.getProbe().getUser().getUserId().toString());
		// try {
		if (rp.getProbeType() != null)
			tableResults.put("PROBE_TYPE", rp.getProbeType().name());
		else
			return null;

		tableResults.put("RESULTS_TIME", resultkey.split("@")[2]);
		tableResults.put("RESULTS_NAME", resultkey.split("@")[1]);
		tableResults.put("RESULTS", resultvalue);
		return tableResults;

	}

	private HashMap<String, String> rawResultsDBFormat(BaseResult rpr, String resultkey, String resultvalue) {
		HashMap<String, String> tableResults;
		tableResults = new HashMap<String, String>();
		RunnableProbe rp = rpr.getRp();
		tableResults.put("USER_ID", rp.getProbe().getUser().getUserId().toString());
		// try {
		if (rp.getProbeType() != null)
			tableResults.put("PROBE_TYPE", rp.getProbeType().name());
		else
			return null;

		tableResults.put("RESULTS_TIME", resultkey.split("@")[2]);
		tableResults.put("RESULTS_NAME", resultkey.split("@")[1]);
		tableResults.put("RESULTS", resultvalue);
		return tableResults;
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
			SysLogger.Record(new Log("Retrieving existing live eventsHandler from REDIS...", LogType.Debug));
			Object eventsObject = ApiInterface.executeRequest(Enums.ApiAction.GetServerLiveEvents, "GET", null);

			if (eventsObject == null) {
				SysLogger.Record(
						new Log("Unable to retrieve existing live eventsHandler, trying again in about 30 secs...",
								LogType.Warn));
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					SysLogger.Record(new Log("Main thread interrupted!", LogType.Error, e));
					continue;
				}
				continue;
			}

			JSONObject events = (JSONObject) eventsObject;

			if (events.size() == 0)
				return;

			for (Iterator iterator = events.keySet().iterator(); iterator.hasNext();) {
				String it = (String) iterator.next();
				try {
					UUID hostId = UUID.fromString(it.split("@")[0]);
					UUID templateId = UUID.fromString(it.split("@")[1]);
					String probeId = it.split("@")[2];
					UUID triggerId = UUID.fromString(it.split("@")[3]);
					long timestamp = Long.parseLong((String) events.get(it));
					
					BaseResult rpr = getResult(templateId.toString() + "@" + hostId.toString() + "@" + probeId);
					if (rpr == null)
						continue;
					
					Trigger trigger = rpr.getRp().getProbe().getTriggers()
							.get(templateId.toString() + "@" + probeId + "@" + triggerId.toString());

					TriggerEvent event = new TriggerEvent(rpr.getRp(), trigger, false);
					event.setTime(timestamp);
					event.setSent(true);

					rpr.getEvents().put(trigger, event);
				} catch (Exception e) {
					Logit.LogError("ResultsContainer - pullCurrentLiveEvents()", "Unable to process live event: ");
				}
			}
			return;
		}
	}

	public BaseResult getResult(String rpId)
	{
		for (BaseResult result : results)
		{
			if (result.getRp().getRPString().equals(rpId))
			{
				return  result;
			}
		}
		return null;
	}
	
	@Override
	public boolean addResult(BaseResult result) {
		results.add(result);
		return true;
	}

	@Override
	public boolean removeSentResults() {
		for (BaseResult result : results) {
			if (result.isSent())
				results.remove(result);
			result = null;
		}
		return true;
	}

	@Override
	public String getResults() {
		HashMap<String, HashMap<String, HashMap<String, String>>> newResults = new HashMap<String, HashMap<String, HashMap<String, String>>>();
		newResults.put("RAW", new HashMap<String, HashMap<String, String>>());
		newResults.put("4mRollups", new HashMap<String, HashMap<String, String>>());
		newResults.put("20mRollups", new HashMap<String, HashMap<String, String>>());
		newResults.put("1hRollups", new HashMap<String, HashMap<String, String>>());
		newResults.put("6hRollups", new HashMap<String, HashMap<String, String>>());
		newResults.put("36hRollups", new HashMap<String, HashMap<String, String>>());
		newResults.put("11dRollups", new HashMap<String, HashMap<String, String>>());

		for (BaseResult rpr : results) {

			RunnableProbe rp = rpr.getRp();

			String rpStr = rp.getRPString();
			if (rpStr.contains("discovery_6b54463e-fe1c-4e2c-a090-452dbbf2d510"))
				System.out.println("BREAKPOINT - RunnableProbesHistory");

			if (rpr.getLastTimestamp() == null || rpr.getLastTimestamp() == 0)
				continue;

			HashMap<String, String> probeResults;

			try {

				probeResults = rpr.getResults();

				if (probeResults.containsKey("error")) {
					SysLogger.Record(
							new Log("Seriious error getting runnable probe results of: " + rpr.getRp().getRPString(),
									LogType.Error));
					continue;
				}
				for (Map.Entry<String, String> probeResult : probeResults.entrySet()) {
					String resultKey = probeResult.getKey();
					String resultValue = probeResult.getValue();

					if (resultKey.contains("RAW")) {
						HashMap<String, String> test = rawResultsDBFormat(rpr, resultKey, resultValue);
						newResults.get("RAW").put(rp.getRPString(), rawResultsDBFormat(rpr, resultKey, resultValue));
					}
					if (resultKey.contains("ROLLUP_4minutes")) {
						newResults.get("4mRollups").put(rp.getRPString(),
								rollupResultsDBFormat(rpr, resultKey, resultValue));
					}
					if (resultKey.contains("ROLLUP_20minutes")) {
						newResults.get("20mRollups").put(rp.getRPString(),
								rollupResultsDBFormat(rpr, resultKey, resultValue));
					}
					if (resultKey.contains("ROLLUP_1hour")) {
						newResults.get("1hRollups").put(rp.getRPString(),
								rollupResultsDBFormat(rpr, resultKey, resultValue));
					}
					if (resultKey.contains("ROLLUP_6hour")) {
						newResults.get("6hRollups").put(rp.getRPString(),
								rollupResultsDBFormat(rpr, resultKey, resultValue));
					}
					if (resultKey.contains("ROLLUP_36hour")) {
						newResults.get("36hRollups").put(rp.getRPString(),
								rollupResultsDBFormat(rpr, resultKey, resultValue));
					}
					if (resultKey.contains("ROLLUP_11day")) {
						newResults.get("11dRollups").put(rp.getRPString(),
								rollupResultsDBFormat(rpr, resultKey, resultValue));
					}
				}
			} catch (Throwable th) {

				Logit.LogError("ResultsContainer - getResults()",
						"Error collecting runnable probes results! stopped at: " + rpr.getRp().getRPString());
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				th.printStackTrace(pw);

				Logit.LogError("ResultsContainer - getResults()", sw.toString());
			}
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return (gson.toJson(results));
	}

	@Override
	public String getRollups() {
		ArrayList<DataPointsRollup[][]> dataRollups = new ArrayList<DataPointsRollup[][]>();
		for (BaseResult result : results) {
			dataRollups.add(result.retrieveExistingRollups());
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(dataRollups);
	}

	@Override
	public String getEvents() {
		ArrayList<HashMap<String, HashMap<String, String>>> events = new ArrayList<HashMap<String, HashMap<String, String>>>();
		for (BaseResult rpr : results) {
			try {
				HashMap<Trigger, TriggerEvent> rprEvents = rpr.getEvents();
				if (rprEvents.size() == 0)
					continue;
				Iterator mapIterator = rprEvents.entrySet().iterator();
				while (mapIterator.hasNext()) {
					Map.Entry<Trigger, TriggerEvent> pair = (Map.Entry<Trigger, TriggerEvent>) mapIterator.next();
					Trigger trigger = pair.getKey();
					TriggerEvent event = pair.getValue();
					if (!event.isSent() && event.isStatus()) {
						HashMap<String, HashMap<String, String>> sendingEvents = new HashMap<String, HashMap<String, String>>();
						HashMap<String, String> eventValues = new HashMap<String, String>();
						eventValues.put("trigger_id", trigger.getTriggerId().toString());
						eventValues.put("host_id", rpr.getRp().getHost().getHostId().toString());
						eventValues.put("host_name", rpr.getRp().getHost().getName());
						eventValues.put("user_id", rpr.getRp().getProbe().getUser().getUserId().toString());
						eventValues.put("trigger_name", trigger.getName());
						eventValues.put("trigger_severity", trigger.getSvrty().toString());
						eventValues.put("event_timestamp", String.valueOf(event.getTime()));
						eventValues.put("event_status", String.valueOf(event.isStatus()));
						eventValues.put("host_bucket", rpr.getRp().getHost().getBucket());
						if (rpr.getRp().getHost().getNotificationGroups() != null)
							eventValues.put("host_notifs_groups",
									rpr.getRp().getHost().getNotificationGroups().toString());
						else
							eventValues.put("host_notifs_groups", null);
						sendingEvents.put(rpr.getRp().getRPString(), eventValues);

						events.add(sendingEvents);

						mapIterator.remove();
					} else if (!event.isSent() && !event.isStatus()) {
						HashMap<String, HashMap<String, String>> sendingEvents = new HashMap<String, HashMap<String, String>>();
						HashMap<String, String> eventValues = new HashMap<String, String>();
						eventValues.put("trigger_id", trigger.getTriggerId().toString());
						eventValues.put("host_id", rpr.getRp().getHost().getHostId().toString());
						eventValues.put("host_name", rpr.getRp().getHost().getName());
						eventValues.put("user_id", rpr.getRp().getProbe().getUser().getUserId().toString());
						eventValues.put("trigger_name", trigger.getName());
						eventValues.put("trigger_severity", trigger.getSvrty().toString());
						eventValues.put("event_timestamp", String.valueOf(event.getTime()));
						eventValues.put("event_status", String.valueOf(event.isStatus()));
						eventValues.put("host_bucket", rpr.getRp().getHost().getBucket());
						if (rpr.getRp().getHost().getNotificationGroups() != null)
							eventValues.put("host_notifs_groups",
									rpr.getRp().getHost().getNotificationGroups().toString());
						else
							eventValues.put("host_notifs_groups", null);
						sendingEvents.put(rpr.getRp().getRPString(), eventValues);

						events.add(sendingEvents);

						event.setSent(true);
					}
				}
			} catch (Exception e) {
				Logit.LogError(null, "Unable to process event for RP:" + rpr.getRp().getRPString());
				continue;
			}
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return (gson.toJson(events));
	}
}
