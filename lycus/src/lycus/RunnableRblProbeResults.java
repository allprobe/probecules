package lycus;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RunnableRblProbeResults extends RunnableProbeResults {
	
	private Boolean IsListed=null;

	public RunnableRblProbeResults(RunnableProbe rp) {
		super(rp);
	
	}

	public Boolean isIsListed() {
		return IsListed;
	}

	public void setIsListed(Boolean isListed) {
		IsListed = isListed;
	}
	

	@Override
	public void acceptResults(ArrayList<Object> results) throws Exception
	{
		long lastTimestamp=(long)results.get(0);
		boolean isListed=(boolean)results.get(1);
		
		this.setLastTimestamp(lastTimestamp);
		this.setIsListed(isListed);
		
		try{
		checkIfTriggerd();
		}
		catch(Exception e)
		{
			SysLogger.Record(new Log("Error triggering RunnableProbe: "+this.getRp(),LogType.Warn,e));
		}
		
		
	}
	
	private void checkIfTriggerd() throws Exception {
		HashMap<String,Trigger> triggers = this.getRp().getProbe().getTriggers();
		for (Trigger trigger : triggers.values()) {
			boolean triggered = false;
			triggered = checkForRblTrigger(trigger);

			TriggerEvent lastEvent = this.getEvents().get(trigger);
			if (lastEvent != null && !triggered) {
				lastEvent.setStatus(true);
				lastEvent.setSent(false);
				SysLogger.Record(new Log("Trigger "+trigger.getTriggerId()+" of Runnable Probe: "+this.getRp().getRPString()+" deactivated, will send event to API...",LogType.Debug));
			} else if (lastEvent == null) {
				TriggerEvent event = new TriggerEvent(this.getRp(), trigger, false);
				event.setSent(false);
				this.getEvents().put(trigger, event);
			}

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

	@Override
	public HashMap<String,String> getResults() throws Throwable
	{
		HashMap<String, String> results = super.getResults();
		JSONArray rawResults = new JSONArray();
		rawResults.add(5);
		rawResults.add(this.isIsListed());
		this.setIsListed(null);

		results.put("RAW@isListed@" + this.getLastTimestamp(), rawResults.toJSONString());
		
		this.setLastTimestamp((long)0);
		return results;
	}
}
