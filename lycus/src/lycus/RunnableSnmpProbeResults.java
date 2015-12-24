package lycus;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;

public class RunnableSnmpProbeResults extends RunnableProbeResults {

	private String stringData;
	private Double numData;
	private DataPointsRollup[] dataRollups;
	private String snmpResultError;

	public RunnableSnmpProbeResults(RunnableProbe rp) {
		super(rp);
		this.setSnmpResultError(null);
		switch (((SnmpProbe) rp.getProbe()).getDataType()) {
		case Numeric: {
			this.dataRollups = this.initRollupSeries(new DataPointsRollup[6]);
			this.stringData = null;
			break;
		}
		case Text: {
			this.dataRollups = null;
			this.stringData = null;
			break;
		}
		default:
			SysLogger.Record(new Log(
					"Unable to determine snmp probe type: " + rp.getRPString() + ", RP results didn't initialized!",
					LogType.Error));
		}
	}

	public String getStringData() {
		return stringData;
	}

	public void setStringData(String stringData) {
		this.stringData = stringData;
	}

	public Double getNumData() {
		return numData;
	}

	public void setNumData(Double numData) {
		this.numData = numData;
	}

	public synchronized DataPointsRollup[] getDataRollups() {
		return dataRollups;
	}

	public synchronized void setDataRollups(DataPointsRollup[] dataRollups) {
		this.dataRollups = dataRollups;
	}

	public String getSnmpResultError() {
		return snmpResultError;
	}

	public void setSnmpResultError(String snmpResultError) {
		this.snmpResultError = snmpResultError;
	}

	@Override
	public void acceptResults(ArrayList<Object> results) throws Exception{
		
		

		long lastTimestamp = (long) results.get(0);
		this.setLastTimestamp(lastTimestamp);

		switch (((SnmpProbe) (this.getRp().getProbe())).getDataType()) {
		case Numeric:
			Double data = null;
			try {
				data = Double.parseDouble((String) results.get(1));
			} catch (/* NumberFormat */Exception nfe) {
				SysLogger.Record(
						new Log("Error parsing snmp probe results: " + this.getRp().getRPString(), LogType.Warn));
				if (((String) results.get(1)).equals("WRONG_OID")) {
					this.setSnmpResultError("WRONG_OID");
				} else {
					this.setSnmpResultError("WRONG_VALUE_FORMAT");
				}
				this.setNumData(null);
				data = null;
				return;
			}
			this.setSnmpResultError(null);
			this.setNumData(data);
			this.setStringData(null);
			for (int i = 0; i <this.getNumberOfRollupTables(); i++) {
				DataPointsRollup numDataRollup = this.getDataRollups()[i];
				numDataRollup.add(lastTimestamp, data);
			}
			break;
		case Text:
			String stringData = (String) results.get(1);
			if (stringData == null) {
				SysLogger.Record(
						new Log("Error parsing snmp probe results: " + this.getRp().getRPString(), LogType.Warn));
				this.setStringData(null);
				return;
			} else if (stringData.equals("WRONG_OID")) {
				this.setSnmpResultError("WRONG_OID");
				return;
			} else {
				this.setSnmpResultError(null);
				this.setNumData(null);
				this.setStringData(stringData);
				break;
			}
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
			switch (((SnmpProbe) this.getRp().getProbe()).getDataType()) {
			case Numeric:
				triggered = checkForNumberTrigger(trigger);
				break;
			case Text:
				triggered = checkForTextTrigger(trigger);
				break;
			}
			TriggerEvent lastEvent = this.getEvents().get(trigger);
			if (lastEvent != null && !triggered) {
				//if trigger event became true and normal again send event to api
				lastEvent.setStatus(true);
				lastEvent.setSent(false);
				SysLogger
						.Record(new Log(
								"Trigger " + trigger.getTriggerId() + " of Runnable Probe: "
										+ this.getRp().getRPString() + " deactivated, will send event to API...",
								LogType.Debug));
			} else if (lastEvent == null && triggered) {
				TriggerEvent event = new TriggerEvent(this.getRp(), trigger, false);
				event.setSent(false);
				this.getEvents().put(trigger, event);
			}

		}
	}

	private boolean checkForNumberTrigger(Trigger trigger) {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			int x = Integer.parseInt(condition.getxValue());
			double lastValue = this.getNumData();
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

	private boolean checkForTextTrigger(Trigger trigger) {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			String x = condition.getxValue();
			String lastValue = this.getStringData();
			switch (condition.getCode()) {
			case 3:
				if (lastValue.equals(x))
					flag = true;
				break;
			case 4:
				if (!lastValue.equals(x))
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
		this.addRollupsFromExistingMemoryDump(this.getDataRollups(), existing[0]);
	}

	@Override
	public DataPointsRollup[][] retrieveExistingRollups() {
		DataPointsRollup[][] existing = new DataPointsRollup[1][6];
		existing[0] = this.getDataRollups();
		return existing;
	}

	@Override
	public HashMap<String, String> getResults() throws Throwable {
		HashMap<String, String> results = super.getResults();
		
		String rpStr = this.getRp().getRPString();
		if (rpStr.contains(
				"788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_10e61538-b4e1-44c6-aa12-b81ef6a5528d"))
			System.out.println("TEST");
		
		
		HashMap<String,String> rawResults=this.getRaw();
		if(rawResults!=null)
			results.putAll(rawResults);
		
		if(((SnmpProbe)this.getRp().getProbe()).getDataType() == SnmpDataType.Numeric)
		{	HashMap<String,String> rollupsResults=this.getRollups();
		results.putAll(rollupsResults);
		}
		
		
		this.setLastTimestamp((long) 0);
			return results;
		
		
	}
	
	public HashMap<String,String> getRaw() throws Throwable
	{
		HashMap<String,String> results=new HashMap<String,String>();
		JSONArray rawResults = new JSONArray();
		rawResults.add(4);
		
		
		
		switch (((SnmpProbe) (this.getRp().getProbe())).getDataType()) {
		case Text:if(this.getStringData()==null&&this.getSnmpResultError()==null)
			return null;
		if (this.getSnmpResultError() == null) {
			rawResults.add(this.getStringData());
			this.setStringData(null);
		} else {
			rawResults.add(this.getSnmpResultError());
			this.setStringData(null);
		}
			results.put("RAW@data@" + this.getLastTimestamp(), rawResults.toJSONString());
		break;
		case Numeric:if(this.getNumData()==null&&this.getSnmpResultError()==null)
			return null;
			if (this.getSnmpResultError() == null) {
				rawResults.add(this.getNumData());
				this.setNumData(null);
			} else {
				rawResults.add(this.getSnmpResultError());
				this.setNumData(null);
			}
			results.put("RAW@data@" + this.getLastTimestamp(), rawResults.toJSONString());
			break;
		default: return null ;
		}
		return results; 
	}
	
	//Numerics ONLY
	public HashMap<String,String> getRollups() throws Throwable
	{
		HashMap<String,String> results=new HashMap<String,String>();
		int rollupsNumber = this.getNumberOfRollupTables();
		for (int i = 0; i < rollupsNumber; i++) {
			
			DataPointsRollup currentDataRollup = this.getDataRollups()[i];
			DataPointsRollup finishedDataRollup = currentDataRollup.getLastFinishedRollup();
			
			if (currentDataRollup == null) {
				SysLogger
				.Record(new Log("Wrong Rollup Tables Number Of: " + this.getRp().getRPString(), LogType.Debug));
				continue;
			}
			if (finishedDataRollup != null) {
				JSONArray dataRollupResults = new JSONArray();
				dataRollupResults.add(finishedDataRollup.getMin());
				dataRollupResults.add(finishedDataRollup.getMax());
				dataRollupResults.add(finishedDataRollup.getAvg());
				dataRollupResults.add(finishedDataRollup.getResultsCounter());
				
				JSONArray fullRollupResults = new JSONArray();
				fullRollupResults.add(dataRollupResults);
				
				results.put("ROLLUP" + finishedDataRollup.getTimePeriod().getName() + "@data@"
						+ finishedDataRollup.getEndTime(), fullRollupResults.toJSONString());
				
				currentDataRollup.setLastFinishedRollup(null);
			}
		}
		return results;
	}
	
}
