package lycus.Results;

import java.util.HashMap;

import lycus.Trigger;
import lycus.Probes.SnmpProbe;
import lycus.DataPointsRollup;
import lycus.TriggerCondition;
import lycus.GlobalConstants.Constants;
import lycus.GlobalConstants.ProbeTypes;

public class SnmpResult extends BaseResult {

	private String oid;
	private String data;
	private DataPointsRollup[] dataRollups;
	private String snmpResultError;
	private Double tmpDeltaVar;// last results when probe "save results as"
								// delta
	private Long tmpDeltaTimestamp;

	public SnmpResult(String runnableProbeId,long timestamp,String data) {
		super(runnableProbeId,timestamp);
		this.probeType=ProbeTypes.SNMP;
		this.data=data;
//		this.setSnmpResultError(null);
		
//		switch (((SnmpProbe) rp.getProbe()).getDataType()) {
//		case Numeric: {
//			this.dataRollups = this.initRollupSeries(new DataPointsRollup[6]);
//			break;
//		}
//		case Text: {
//			this.dataRollups = null;
//			this.stringData = null;
//			break;
//		}
//		default:
//			Logit.LogError("SnmpResults - SnmpResults", "Unable to determine snmp probe type: " + runnableProbeId + ", RP results didn't initialized!");
//		}
	}
	public SnmpResult(String runnableProbeId) {
		super(runnableProbeId);
	}
	
	public String getStringData() {
		return data;
	}

//	public void setStringData(String stringData) {
//		this.data = stringData;
//	}

	public boolean isValidNumber()
	{
		return this.getNumData()!=null;
	}
	public Double getNumData() {
		Double numData=null;
		
		try
		{
			numData=Double.parseDouble(data);
		}
		catch(Exception e)
		{
		}

		return numData;
	}

	public Double getTmpDeltaVar() {
		return tmpDeltaVar;
	}

	public void setTmpDeltaVar(Double tmpDeltaVar) {
		this.tmpDeltaVar = tmpDeltaVar;
	}

	public Long getTmpDeltaTimestamp() {
		return tmpDeltaTimestamp;
	}

	public void setTmpDeltaTimestamp(Long tmpDeltaTimestamp) {
		this.tmpDeltaTimestamp = tmpDeltaTimestamp;
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

//	@Override
//	public synchronized void acceptResults(ArrayList<Object> results) throws Exception {
//		super.acceptResults(results);
//
//		long lastTimestamp = (long) results.get(0);
//		this.setLastTimestamp(lastTimestamp);
//
//		String rpStr = this.getRp().getRPString();
//		if (rpStr.contains(
//				"0b05919c-6cc0-42cc-a74b-de3b0dcd4a2a@788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_b0fb65d1-c50d-4639-a728-0f173588f56b"))
//			System.out.println("BREAKPOINT");
//
//		switch (((SnmpProbe) (this.getRp().getProbe())).getDataType()) {
//		case Numeric:
//			acceptNumericResults(lastTimestamp, results);
//
//
//			break;
//		case Text:
//			acceptTextResults(results);
//			break;
//		}
//		try {
//			checkIfTriggerd();
//		} catch (Exception e) {
//			Logit.LogError("SnmpResults - acceptResults", "Error triggering RunnableProbe: " + this.getRp());
//		}
//	}
//
//	private void acceptTextResults(ArrayList<Object> results) {
//		String stringData = (String) results.get(1);
//		if (stringData == null) {
//			Logit.LogError("SnmpResults - acceptTextResults", "Error parsing snmp probe results: " + getRunnableProbeId());
//			this.setStringData(null);
//			return;
//		} else if (stringData.equals(Constants.WRONG_OID)) {
//			this.setSnmpResultError(Constants.WRONG_OID);
//			return;
//		} else {
//			this.setSnmpResultError(null);
//			this.setNumData(null);
//			this.setStringData(stringData);
//		}
//	}

//	private void acceptNumericResults(long lastTimestamp, ArrayList<Object> results) {
//		Double data = null;
//		try {
//			data = Double.parseDouble((String) results.get(1));
//		} catch (Exception nfe) {
//			Logit.LogError("SnmpResults - acceptNumericResults", "Failed parsing snmp probe results: " + getRunnableProbeId());
//			if (((String) results.get(1)).equals(Constants.WRONG_OID)) {
//				this.setSnmpResultError(Constants.WRONG_OID);
//			} else {
//				this.setSnmpResultError(Constants.WRONG_VALUE_FORMAT);
//			}
//			this.setNumData(null);
//			return;
//		}
//		this.setSnmpResultError(null);
//
//		switch (((SnmpProbe) this.getRp().getProbe()).getStoreAs()) {
//		case asIs:
//			this.setNumData(data);
//			break;
//		case delta:
//			if (this.getTmpDeltaVar() == null) {
//				this.setTmpDeltaVar(data);
//				return;
//			}
//			this.setNumData(data - this.getTmpDeltaVar());
//			this.setTmpDeltaVar(data);
//			break;
//		case deltaBytesPerSecond:
//			if (this.getTmpDeltaVar() == null) {
//				this.setTmpDeltaVar(data);
//				this.setTmpDeltaTimestamp(lastTimestamp);
//				return;
//			}
//			long ifSpeed =  (long)results.get(2);
//			this.setNumData(
//					this.getBytesPerSecond(lastTimestamp, data,ifSpeed));
//			this.setTmpDeltaVar(data);
//			this.setTmpDeltaTimestamp(lastTimestamp);
//
//			break;
//
//		}
//
//		this.setStringData(null);
//		if (this.getNumData() == null)
//			return;
//		for (int i = 0; i < this.getNumberOfRollupTables(); i++) {
//			DataPointsRollup numDataRollup = this.getDataRollups()[i];
//			numDataRollup.add(lastTimestamp, this.getNumData());
//		}
//	}

	private Double getBytesPerSecond(long lastTimestamp, Double data, long ifSpeed) {
		Double oldOctets = this.getTmpDeltaVar();
		Double newOctets = data;
		long oldTimestamp = this.getTmpDeltaTimestamp();
		long newTimestamp = lastTimestamp;

		Double bandwidth = ((newOctets - oldOctets) * 8 * 100) / (((newTimestamp - oldTimestamp) / 1000) * ifSpeed);

		return bandwidth;
	}

	private void setTmpDeltaTimestamp(long lastTimestamp) {
		this.tmpDeltaTimestamp = lastTimestamp;
	}

	@Override
	public void checkIfTriggerd(HashMap<String,Trigger> triggers) throws Exception {
		super.checkIfTriggerd(triggers);
		for (Trigger trigger : triggers.values()) {
			
			boolean triggered = false;
			//TODO: Check with Roi
			switch (((SnmpProbe)trigger.getProbe()).getDataType()) {
			case Numeric:
				triggered = checkForNumberTrigger(trigger);
				break;
			case Text:
				triggered = checkForTextTrigger(trigger);
				break;
			}

			super.processTriggerResult(trigger, triggered);

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
			if (flag && condition.getAndOr().equals(Constants.or))
				return true;
			else if (!flag && condition.getAndOr().equals(Constants.and))
				return false;
		}
		return flag;
	}

//	@Override
//	public void insertExistingRollups(DataPointsRollup[][] existing) {
//		this.addRollupsFromExistingMemoryDump(this.getDataRollups(), existing[0]);
//	}

//	@Override
//	public DataPointsRollup[][] retrieveExistingRollups() {
//		DataPointsRollup[][] existing = new DataPointsRollup[1][6];
//		existing[0] = this.getDataRollups();
//		return existing;
//	}

//	@Override
//	public HashMap<String, String> getResults() throws Throwable {
//		HashMap<String, String> results = super.getResults();
//		String rpStr = getRunnableProbeId();
//		if (rpStr.contains("0b05919c-6cc0-42cc-a74b-de3b0dcd4a2a@788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_b0fb65d1-c50d-4639-a728-0f173588f56b"))
//			System.out.println("BREAKPOINT");
//
//		HashMap<String, String> rawResults = this.getRaw();
//		if (rawResults != null)
//			results.putAll(rawResults);
//
//		if (((SnmpProbe) this.getRp().getProbe()).getDataType() == SnmpDataType.Numeric) {
//			HashMap<String, String> rollupsResults = this.getRollups();
//			results.putAll(rollupsResults);
//		}
//
//		this.setLastTimestamp((long) 0);
//
//
//		return results;
//
//	}

//	public HashMap<String, String> getRaw() throws Throwable {
//		HashMap<String, String> results = new HashMap<String, String>();
//		JSONArray rawResults = new JSONArray();
//		rawResults.add(4);
//
//		switch (((SnmpProbe) (this.getRp().getProbe())).getDataType()) {
//		case Text:
//			if (this.getStringData() == null && this.getSnmpResultError() == null)
//				return null;
//			if (this.getSnmpResultError() == null) {
//				rawResults.add(this.getStringData());
//				this.setStringData(null);
//			} else {
//				rawResults.add(this.getSnmpResultError());
//				this.setStringData(null);
//			}
//			results.put("RAW@data@" + this.getLastTimestamp(), rawResults.toJSONString());
//			break;
//		case Numeric:
//			if (this.getNumData() == null && this.getSnmpResultError() == null)
//				return null;
//			if (this.getSnmpResultError() == null) {
//				rawResults.add(this.getNumData());
//				this.setNumData(null);
//			} else {
//				rawResults.add(this.getSnmpResultError());
//				this.setNumData(null);
//			}
//			results.put("RAW@data@" + this.getLastTimestamp(), rawResults.toJSONString());
//			this.setLastTimestamp(null);
//			break;
//		default:
//			return null;
//		}
//		return results;
//	}

	// Numerics ONLY
//	public HashMap<String, String> getRollups() throws Throwable {
//		HashMap<String, String> results = new HashMap<String, String>();
//		int rollupsNumber = this.getNumberOfRollupTables();
//		for (int i = 0; i < rollupsNumber; i++) {
//
//			DataPointsRollup currentDataRollup = this.getDataRollups()[i];
//			DataPointsRollup finishedDataRollup = currentDataRollup.getLastFinishedRollup();
//
//			if (currentDataRollup == null) {
//				SysLogger
//						.Record(new Log("Wrong Rollup Tables Number Of: " + getRunnableProbeId(), LogType.Debug));
//				continue;
//			}
//			if (finishedDataRollup != null) {
//				JSONArray dataRollupResults = new JSONArray();
//				dataRollupResults.add(finishedDataRollup.getMin());
//				dataRollupResults.add(finishedDataRollup.getMax());
//				dataRollupResults.add(finishedDataRollup.getAvg());
//				dataRollupResults.add(finishedDataRollup.getResultsCounter());
//
//				JSONArray fullRollupResults = new JSONArray();
//				fullRollupResults.add(dataRollupResults);
//
//				results.put("ROLLUP" + finishedDataRollup.getTimePeriod().getName() + "@data@"
//						+ finishedDataRollup.getEndTime(), fullRollupResults.toJSONString());
//
//				currentDataRollup.setLastFinishedRollup(null);
//			}
//		}
//		return results;
//	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

}
