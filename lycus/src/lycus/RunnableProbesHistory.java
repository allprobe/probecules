package lycus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RunnableProbesHistory implements Runnable {
	private HashMap<String, RunnableProbeResults> results;
	private Gson gson;
	private RollupsMemoryDump memDump;
	private EventHandler eventsHandler;
	private ScheduledExecutorService rollupsDumpExecuter;
	private ScheduledFuture<?> rollupsDumpExecuterThread;
	private ScheduledExecutorService resultsInsertorExecuter;
	private ScheduledFuture<?> resultsInsertorExecuterThread;
	private ScheduledExecutorService eventsInsertorExecuter;
	private ScheduledFuture<?> eventsInsertorExecuterThread;

	public RunnableProbesHistory(ArrayList<User> allUsers, String existingRollups) {
		this.results = this.getAllResultsUsers(allUsers);
//		this.setGson(new GsonBuilder().setPrettyPrinting().create());
		this.setGson(new GsonBuilder().create());
		this.memDump = new RollupsMemoryDump(this);
		this.eventsHandler = new EventHandler(this);
		this.setRollupsDumpExecuter(Executors.newSingleThreadScheduledExecutor());
		this.setResultsInsertorExecuter(Executors.newSingleThreadScheduledExecutor());
		this.setEventsInsertorExecuter(Executors.newSingleThreadScheduledExecutor());
	}

	public Gson getGson() {
		return gson;
	}

	public void setGson(Gson gson) {
		this.gson = gson;
	}

	public EventHandler getEvents() {
		return eventsHandler;
	}

	public void setEvents(EventHandler events) {
		this.eventsHandler = events;
	}

	

	public void run() {
		try {
			SysLogger.Record(new Log("Sending collected data to API...", LogType.Info));
			String results = this.getResultsDBFormat();
			
			String rpStr = results;
			if (rpStr.contains(
					"788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_e69656ce-6835-41ec-8399-18bcf939fa9b"))
				System.out.println("TEST");
			
			String encodedResults = GeneralFunctions.Base64Encode(results);
			
			String sendString = "{\"results\" : \"" + encodedResults + "\"}";
			
			
			if(results.contains("788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_10e61538-b4e1-44c6-aa12-b81ef6a5528d"))
				System.out.println("BREAKPOINT - RunnableProbesHistory");
			ApiInterface.executeRequest(Enums.ApiAction.InsertDatapointsBatches, "PUT",sendString);
		
		
		} catch (Throwable thrown) {
			SysLogger.Record(new Log("Sending collected data to API failed!", LogType.Error));
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			thrown.printStackTrace(pw);

			SysLogger.Record(new Log(sw.toString(), LogType.Error));
			SysLogger.Record(new Log(thrown.getMessage().toString(), LogType.Error));
			SysLogger.Record(new Log("Results insertion failed! does not run anymore!", LogType.Error));
		}
	}

	private String getResultsDBFormat() {

		HashMap<String, RunnableProbeResults> rprs = this.getResults();

		HashMap<String, HashMap<String, HashMap<String, String>>> results = new HashMap<String, HashMap<String, HashMap<String, String>>>();
		results.put("RAW", new HashMap<String, HashMap<String, String>>());
		results.put("4mRollups", new HashMap<String, HashMap<String, String>>());
		results.put("20mRollups", new HashMap<String, HashMap<String, String>>());
		results.put("1hRollups", new HashMap<String, HashMap<String, String>>());
		results.put("6hRollups", new HashMap<String, HashMap<String, String>>());
		results.put("36hRollups", new HashMap<String, HashMap<String, String>>());
		results.put("11dRollups", new HashMap<String, HashMap<String, String>>());

		for (RunnableProbeResults rpr : rprs.values()) {

			RunnableProbe rp = rpr.getRp();

			String rpStr = rp.getRPString();
			if (rpStr.contains("788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_10e61538-b4e1-44c6-aa12-b81ef6a5528d"))
				System.out.println("BREAKPOINT - RunnableProbesHistory");
			
			// SysLogger.Record(new Log("Delay of
			// "+RunInnerProbesChecks.getRunnableProbeThread(rp).getDelay(TimeUnit.SECONDS)+"s
			// for last check of Runnable Probe:
			// "+rp.getRPString(),LogType.Debug));

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
						results.get("RAW").put(rp.getRPString(), rawResultsDBFormat(rpr, resultKey, resultValue));
					}
					if (resultKey.contains("ROLLUP_4minutes")) {
						results.get("4mRollups").put(rp.getRPString(),
								rollupResultsDBFormat(rpr, resultKey, resultValue));
					}
					if (resultKey.contains("ROLLUP_20minutes")) {
						results.get("20mRollups").put(rp.getRPString(),
								rollupResultsDBFormat(rpr, resultKey, resultValue));
					}
					if (resultKey.contains("ROLLUP_1hour")) {
						results.get("1hRollups").put(rp.getRPString(),
								rollupResultsDBFormat(rpr, resultKey, resultValue));
					}
					if (resultKey.contains("ROLLUP_6hour")) {
						results.get("6hRollups").put(rp.getRPString(),
								rollupResultsDBFormat(rpr, resultKey, resultValue));
					}
					if (resultKey.contains("ROLLUP_36hour")) {
						results.get("36hRollups").put(rp.getRPString(),
								rollupResultsDBFormat(rpr, resultKey, resultValue));
					}
					if (resultKey.contains("ROLLUP_11day")) {
						results.get("11dRollups").put(rp.getRPString(),
								rollupResultsDBFormat(rpr, resultKey, resultValue));
					}
				}
			} catch (Throwable th) {

				SysLogger.Record(
						new Log("Error collecting runnable probes results! stopped at: " + rpr.getRp().getRPString(),
								LogType.Error));
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				th.printStackTrace(pw);

				SysLogger.Record(new Log(sw.toString(), LogType.Error));
			}
		}

		return (this.getGson().toJson(results));
	}

	private void startMemoryDump() {
		this.setRollupsDumpExecuterThread(
				this.getRollupsDumpExecuter().scheduleWithFixedDelay(this.getMemDump(), 0, 5, TimeUnit.MINUTES));
	}

	private void startHistoryInsertion() {
		this.setResultsInsertorExecuterThread(
				this.getResultsInsertorExecuter().scheduleAtFixedRate(this, 0, 30, TimeUnit.SECONDS));
	}

	private void startEventsInsertion() {

		pullCurrentLiveEvents();
		this.setEventsInsertorExecuterThread(
				this.getEventsInsertorExecuter().scheduleAtFixedRate(this.getEvents(), 0, 15, TimeUnit.SECONDS));
	}

	public void startHistory() {
		this.startHistoryInsertion();
		this.startMemoryDump();
		this.startEventsInsertion();

	}

	private HashMap<String, String> rollupResultsDBFormat(RunnableProbeResults rpr, String resultkey,
			String resultvalue) {

		HashMap<String, String> tableResults;
		tableResults = new HashMap<String, String>();
		RunnableProbe rp = rpr.getRp();
		tableResults.put("USER_ID", rp.getProbe().getUser().getUserId().toString());
//		try {
		if (rp.getProbeType() != null) 
			tableResults.put("PROBE_TYPE", rp.getProbeType().name());
		else return null;
		
//		} catch (Exception e) {
//			SysLogger
//					.Record(new Log("Wrong RP type: " + rp.getRPString() + ", unable to save results!", LogType.Error));
//			return null;
//		}
		tableResults.put("RESULTS_TIME", resultkey.split("@")[2]);
		tableResults.put("RESULTS_NAME", resultkey.split("@")[1]);
		tableResults.put("RESULTS", resultvalue);
		return tableResults;

	}

	private HashMap<String, String> rawResultsDBFormat(RunnableProbeResults rpr, String resultkey, String resultvalue) {
		HashMap<String, String> tableResults;
		tableResults = new HashMap<String, String>();
		RunnableProbe rp = rpr.getRp();
		tableResults.put("USER_ID", rp.getProbe().getUser().getUserId().toString());
//		try {
			if (rp.getProbeType() != null) 
				tableResults.put("PROBE_TYPE", rp.getProbeType().name());
			else return null;
//		} catch (Exception e) {
//			SysLogger
//					.Record(new Log("Wrong RP type: " + rp.getRPString() + ", unable to save results!", LogType.Error));
//			return null;
//		}
		tableResults.put("RESULTS_TIME", resultkey.split("@")[2]);
		tableResults.put("RESULTS_NAME", resultkey.split("@")[1]);
		tableResults.put("RESULTS", resultvalue);
		return tableResults;
	}

	public HashMap<String, RunnableProbeResults> getResults() {
		return results;
	}

	public void setResults(HashMap<String, RunnableProbeResults> results) {
		this.results = results;
	}

	public ScheduledExecutorService getRollupsDumpExecuter() {
		return rollupsDumpExecuter;
	}

	public void setRollupsDumpExecuter(ScheduledExecutorService rollupsDumpExecuter) {
		this.rollupsDumpExecuter = rollupsDumpExecuter;
	}

	public ScheduledExecutorService getResultsInsertorExecuter() {
		return resultsInsertorExecuter;
	}

	public void setResultsInsertorExecuter(ScheduledExecutorService resultsInsertorExecuter) {
		this.resultsInsertorExecuter = resultsInsertorExecuter;
	}

	public ScheduledExecutorService getEventsInsertorExecuter() {
		return eventsInsertorExecuter;
	}

	public void setEventsInsertorExecuter(ScheduledExecutorService eventsInsertorExecuter) {
		this.eventsInsertorExecuter = eventsInsertorExecuter;
	}

	public ScheduledFuture<?> getResultsInsertorExecuterThread() {
		return resultsInsertorExecuterThread;
	}

	public void setResultsInsertorExecuterThread(ScheduledFuture<?> resultsInsertorExecuterThread) {
		this.resultsInsertorExecuterThread = resultsInsertorExecuterThread;
	}

	public ScheduledFuture<?> getEventsInsertorExecuterThread() {
		return eventsInsertorExecuterThread;
	}

	public void setEventsInsertorExecuterThread(ScheduledFuture<?> eventsInsertorExecuterThread) {
		this.eventsInsertorExecuterThread = eventsInsertorExecuterThread;
	}

	public ScheduledFuture<?> getRollupsDumpExecuterThread() {
		return rollupsDumpExecuterThread;
	}

	public void setRollupsDumpExecuterThread(ScheduledFuture<?> rollupsDumpExecuterThread) {
		this.rollupsDumpExecuterThread = rollupsDumpExecuterThread;
	}

	public RollupsMemoryDump getMemDump() {
		return memDump;
	}

	public void setMemDump(RollupsMemoryDump memDump) {
		this.memDump = memDump;
	}

	private HashMap<String, RunnableProbeResults> getAllResultsUsers(ArrayList<User> users) {
		HashMap<String, RunnableProbeResults> rprs = new HashMap<String, RunnableProbeResults>();
		for (User u : users) {
			Collection<RunnableProbe> usersRPs = u.getAllRunnableProbes().values();
			for (RunnableProbe rp : usersRPs) {

				rprs.put(rp.getRPString(), rp.getResult());
			}
		}
		return rprs;
	}


	public void pullCurrentLiveEvents() {
		while (true) {
			SysLogger.Record(new Log("Retrieving existing live eventsHandler from REDIS...", LogType.Debug));
			Object eventsObject = ApiInterface.executeRequest(Enums.ApiAction.GetServerLiveEvents, "GET", null);

			if (eventsObject == null) {
				SysLogger.Record(new Log("Unable to retrieve existing live eventsHandler, trying again in about 30 secs...",
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

					RunnableProbeResults rpr = this.getResults()
							.get(templateId.toString() + "@" + hostId.toString() + "@" + probeId);
					Trigger trigger = rpr.getRp().getProbe().getTriggers()
							.get(templateId.toString() + "@" + probeId + "@" + triggerId.toString());

					TriggerEvent event = new TriggerEvent(rpr.getRp(), trigger, false);
					event.setTime(timestamp);
					event.setSent(true);

					rpr.getEvents().put(trigger, event);
				} catch (Exception e) {
					SysLogger.Record(new Log("Unable to process live event: " + it, LogType.Error));
				}
			}
			return;
		}
	}

}
