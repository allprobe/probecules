package lycus;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;

public class RunnableWeberProbeResults extends RunnableProbeResults {

	private Integer statusCode;
	private Long responseTime;
	private DataPointsRollup[] responseTimeRollups;
	private Long pageSize;
	
	public RunnableWeberProbeResults(RunnableProbe rp) {
		super(rp);
		this.responseTimeRollups=this.initRollupSeries(new DataPointsRollup[6]);
	}

	
	public Integer getStatusCode() {
		return statusCode;
	}


	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
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


	public Long getPageSize() {
		return pageSize;
	}


	public void setPageSize(Long pageSize) {
		this.pageSize = pageSize;
	}


	@Override
	public synchronized void acceptResults(ArrayList<Object> results) throws Exception{
		
		String rpStr = this.getRp().getRPString();
		if (rpStr.contains(
				"d934aa3b-f703-4d4b-99c6-66b470c782f2@inner_62baf829-81fd-4184-b7d7-9d44616386f4"))
			System.out.println("BREAKPOINT - RunnableWeberProbeResults");
		
		long lastTimestamp=(long)results.get(0);
		int statusCode=(int)results.get(1);
		long responseTime=(long)results.get(2);
		long pageSize=(long)results.get(3);
		
		this.setLastTimestamp(lastTimestamp);
		this.setStatusCode(statusCode);
		this.setResponseTime(responseTime);
		this.setPageSize(pageSize);
		
		for(int i=0;i<this.getNumberOfRollupTables();i++)
		{
			DataPointsRollup responseTimeRollup=this.getResponseTimeRollups()[i];
			responseTimeRollup.add(lastTimestamp, responseTime);
		}
		try{
		checkIfTriggerd();
		}
		catch(Exception e)
		{
			SysLogger.Record(new Log("Error triggering RunnableProbe: "+this.getRp(),LogType.Warn,e));
		}
	}
	
	@Override
	protected void checkIfTriggerd() throws Exception {
		super.checkIfTriggerd();
		HashMap<String,Trigger> triggers = this.getRp().getProbe().getTriggers();
		for (Trigger trigger : triggers.values()) {
			boolean triggered = false;
			switch (trigger.getElementType()) {
			case "rc":
				triggered = checkForResponseCodeTrigger(trigger);
				break;
			case "rt":
				triggered = checkForResponseTimeTrigger(trigger);
				break;
			case "ps":
				triggered = checkForPageSizeTrigger(trigger);
				break;
			}
			
			super.processTriggerResult(trigger, triggered);
		}
	}

	private boolean checkForResponseCodeTrigger(Trigger trigger) {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			int x = Integer.parseInt(condition.getxValue());
			int lastValue = this.getStatusCode();
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
			if(flag&&condition.getAndOr().equals("or"))
				return true;
			else if(!flag&&condition.getAndOr().equals("and"))
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
			if(flag&&condition.getAndOr().equals("or"))
				return true;
			else if(!flag&&condition.getAndOr().equals("and"))
				return false;
		}
		return flag;
	}
	private boolean checkForPageSizeTrigger(Trigger trigger) {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			long x = Long.parseLong(condition.getxValue());
			long lastValue = this.getPageSize();
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
			if(flag&&condition.getAndOr().equals("or"))
				return true;
			else if(!flag&&condition.getAndOr().equals("and"))
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
	public HashMap<String,String> getResults() throws Throwable {
		HashMap<String, String> results = super.getResults();
		JSONArray rawResults = new JSONArray();
		rawResults.add(3);
		rawResults.add(this.getStatusCode());
		this.setStatusCode(null);
		rawResults.add(this.getResponseTime());
		this.setResponseTime(null);
		rawResults.add(this.getPageSize());
		this.setPageSize(null);

		results.put("RAW@responseCode_responseTime_responseSize@" + this.getLastTimestamp(), rawResults.toJSONString());
		int rollupsNumber = this.getNumberOfRollupTables();
		for (int i = 0; i < rollupsNumber; i++) {
			
			DataPointsRollup currentResponseTimeRollup=this.getResponseTimeRollups()[i];
			DataPointsRollup finishedResponseTimeRollup = currentResponseTimeRollup.getLastFinishedRollup();
			
			if(currentResponseTimeRollup==null)
			{
				SysLogger.Record(new Log("Wrong Rollup Tables Number Of: "+this.getRp().getRPString(),LogType.Debug));
				continue;
			}
				if (finishedResponseTimeRollup!=null) {
					JSONArray responseTimeRollupResults = new JSONArray();
					responseTimeRollupResults.add(finishedResponseTimeRollup.getMin());
					responseTimeRollupResults.add(finishedResponseTimeRollup.getMax());
					responseTimeRollupResults.add(finishedResponseTimeRollup.getAvg());
					responseTimeRollupResults.add(finishedResponseTimeRollup.getResultsCounter());
					
					
					JSONArray fullRollupResults= new JSONArray();
					fullRollupResults.add(responseTimeRollupResults);
					
				results.put("ROLLUP" + finishedResponseTimeRollup.getTimePeriod().getName() + "@responseTime@"
						+ finishedResponseTimeRollup.getEndTime(), fullRollupResults.toJSONString());
				
				currentResponseTimeRollup.setLastFinishedRollup(null);
			}
		}
		this.setLastTimestamp((long)0);
		
		
		return results;
	}
}
