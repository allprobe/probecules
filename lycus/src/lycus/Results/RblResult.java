package lycus.Results;

import java.util.HashMap;

import org.json.simple.JSONArray;

import lycus.Trigger;
import lycus.TriggerCondition;
import lycus.GlobalConstants.ProbeTypes;

public class RblResult extends BaseResult {
	
	private Boolean IsListed=null;

	public RblResult(String runnableProbeId, long timestamp, boolean isListed2) {
		super(runnableProbeId,timestamp);
		this.probeType=ProbeTypes.RBL;

		this.IsListed=isListed2;
	}
	public RblResult(String runnableProbeId) {
		super(runnableProbeId);
	}
	
	public Boolean isIsListed() {
		return IsListed;
	}

	public void setIsListed(Boolean isListed) {
		IsListed = isListed;
	}
	

//	@Override
//	public synchronized void acceptResults(ArrayList<Object> results) throws Exception
//	{
//		super.acceptResults(results);
//		long lastTimestamp=(long)results.get(0);
//		boolean isListed=(boolean)results.get(1);
//		
//		this.setLastTimestamp(lastTimestamp);
//		this.setIsListed(isListed);
//		
//		try{
//		checkIfTriggerd();
//		}
//		catch(Exception e)
//		{
//			Logit.LogError("RblResults - acceptResults","Error triggering RunnableProbe: "+this.getRp() );
//		}
//	}
	
	@Override
	public void checkIfTriggerd(HashMap<String,Trigger> triggers) throws Exception {
		super.checkIfTriggerd(triggers);
		for (Trigger trigger : triggers.values()) {
			boolean triggered = false;
			triggered = checkForRblTrigger(trigger);

			super.processTriggerResult(trigger, triggered);

		}
	}

	private boolean checkForRblTrigger(Trigger trigger) throws Exception {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			boolean x = Boolean.parseBoolean(condition.getxValue());
			boolean lastValue = this.isIsListed();
			
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
			
			if(flag&&condition.getAndOr().equals("or"))
				return true;
			else if(!flag&&condition.getAndOr().equals("and"))
				return false;
		}
		return flag;
	}

//	@Override
//	public HashMap<String,String> getResults() throws Throwable
//	{
//		HashMap<String, String> results = super.getResults();
//		JSONArray rawResults = new JSONArray();
//		rawResults.add(5);
//		rawResults.add(this.isIsListed());
//		this.setIsListed(null);
//
//		results.put("RAW@isListed@" + this.getLastTimestamp(), rawResults.toJSONString());
//		
//		this.setLastTimestamp((long)0);
//		return results;
//	}
	@Override
	public String getResultString() {
		JSONArray result=new JSONArray();
		result.add(5);
		result.add(IsListed);
		return result.toString();
	}
}
