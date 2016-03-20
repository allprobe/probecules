package lycus.Results;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;

import lycus.GlobalConstants.LogType;
import lycus.Utils.Logit;
import lycus.DataPointsRollup;
import lycus.Log;
import lycus.RunnableProbe;
import lycus.SysLogger;
import lycus.Trigger;
import lycus.TriggerCondition;

public class PorterResults extends BaseResult {
	private Boolean portStatus;
	private Long responseTime;
	private DataPointsRollup[] responseTimeRollups;

	public PorterResults(RunnableProbe rp) {
		super(rp);
		this.responseTimeRollups = this.initRollupSeries(new DataPointsRollup[6]);
	}

	public Boolean isPortStatus() {
		return portStatus;
	}

	public void setPortStatus(Boolean portStatus) {
		this.portStatus = portStatus;
	}

	public Long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(Long responseTime) {
		this.responseTime = responseTime;
	}

	public DataPointsRollup[] getResponseTimeRollups() {
		return responseTimeRollups;
	}

	public void setResponseTimeRollups(DataPointsRollup[] responseTimeRollups) {
		this.responseTimeRollups = responseTimeRollups;
	}

	@Override
	public synchronized void acceptResults(ArrayList<Object> results) throws Exception {
		long lastTimestamp = (long) results.get(0);
		boolean status = (boolean) results.get(1);
		long time = Long.parseLong(String.valueOf(results.get(2)));

		this.setLastTimestamp(lastTimestamp);
		this.setPortStatus(status);
		this.setResponseTime(time);

		for (int i = 0; i < this.getNumberOfRollupTables(); i++) {
			DataPointsRollup responseTimeRollups = this.getResponseTimeRollups()[i];
			if (responseTimeRollups == null)
				continue;
			responseTimeRollups.add(lastTimestamp, time);
		}

		try {
			checkIfTriggerd();
		} catch (Exception e) {
			Logit.LogError("PorterResults - acceptResults", "Error triggering RunnableProbe: " + this.getRp());
		}
	}

	@Override
	protected void checkIfTriggerd() throws Exception {
		super.checkIfTriggerd();
		HashMap<String, Trigger> triggers = this.getRp().getProbe().getTriggers();
		for (Trigger trigger : triggers.values()) {
			boolean triggered = false;
			switch (trigger.getElementType()) {
			case "st":
				triggered = checkForStatusTrigger(trigger);
				break;
			case "rt":
				triggered = checkForResponseTimeTrigger(trigger);
				break;
			}
			
			super.processTriggerResult(trigger, triggered);

		}
	}

	private boolean checkForStatusTrigger(Trigger trigger) {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			boolean x = Boolean.parseBoolean((condition.getxValue()));
			boolean lastValue = this.isPortStatus();
			switch (condition.getCode()) {
			case 3:
				if (lastValue == x)
					flag = true;
				break;
			case 4:
				if (lastValue != x)
					flag = true;
				break;
			}
			if (flag && condition.getAndOr().equals("or"))
				return true;
			else if (!flag && condition.getAndOr().equals("and"))
				return false;
		}
		return flag;
	}

	private boolean checkForResponseTimeTrigger(Trigger trigger) {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			long x = Long.parseLong(condition.getxValue());
			long lastValue = this.getResponseTime();
			switch (condition.getCode()) {
			case 1:
				if (lastValue > x)
					flag = true;
				break;
			case 2:
				if (lastValue < x)
					flag = true;
				break;
			case 3:
				if (lastValue == x)
					flag = true;
				break;
			case 4:
				if (lastValue != x)
					flag = true;
				break;
			}
			if (flag && condition.getAndOr().equals("or"))
				return true;
			else if (!flag && condition.getAndOr().equals("and"))
				return false;
		}
		return flag;
	}

	@Override
	public void insertExistingRollups(DataPointsRollup[][] existing) {
		this.addRollupsFromExistingMemoryDump(this.getResponseTimeRollups(), existing[0]);
	}

	@Override
	public DataPointsRollup[][] retrieveExistingRollups() {
		DataPointsRollup[][] existing = new DataPointsRollup[1][6];
		existing[0] = this.getResponseTimeRollups();
		return existing;
	}

	@Override
	public HashMap<String, String> getResults() throws Throwable {
		HashMap<String, String> results = super.getResults();
		JSONArray rawResults = new JSONArray();
		rawResults.add(2);
		rawResults.add(this.isPortStatus());
		this.setPortStatus(null);
		rawResults.add(this.getResponseTime());
		this.setResponseTime(null);

		results.put("RAW@portState_responseTime@" + this.getLastTimestamp(), rawResults.toJSONString());
		int rollupsNumber = this.getNumberOfRollupTables();
		for (int i = 0; i < rollupsNumber; i++) {

			DataPointsRollup currentResponseTimeRollup = this.getResponseTimeRollups()[i];
			DataPointsRollup finishedResponseTimeRollup = currentResponseTimeRollup.getLastFinishedRollup();
			if (currentResponseTimeRollup == null) {
				SysLogger
						.Record(new Log("Wrong Rollup Tables Number Of: " + this.getRp().getRPString(), LogType.Debug));
				continue;
			}
			if (finishedResponseTimeRollup != null) {
				JSONArray responseTimeRollupResults = new JSONArray();
				responseTimeRollupResults.add(finishedResponseTimeRollup.getMin());
				responseTimeRollupResults.add(finishedResponseTimeRollup.getMax());
				responseTimeRollupResults.add(finishedResponseTimeRollup.getAvg());
				responseTimeRollupResults.add(finishedResponseTimeRollup.getResultsCounter());

				JSONArray fullRollupResults = new JSONArray();
				fullRollupResults.add(responseTimeRollupResults);

				results.put("ROLLUP" + finishedResponseTimeRollup.getTimePeriod().getName() + "@responseTime@"
						+ finishedResponseTimeRollup.getEndTime(), fullRollupResults.toJSONString());

				currentResponseTimeRollup.setLastFinishedRollup(null);
			}
		}
		this.setLastTimestamp((long) 0);

		return results;
	}
}
