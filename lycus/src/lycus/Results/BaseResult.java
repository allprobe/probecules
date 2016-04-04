package lycus.Results;

import java.util.HashMap;
import lycus.Utils.Logit;
import lycus.Trigger;
import lycus.GlobalConstants.ProbeTypes;
import lycus.Interfaces.IResult;
import lycus.Event;
import lycus.ResultsContainer;
import lycus.RunnableProbeContainer;

public class BaseResult implements IResult {
	private Long lastTimestamp;
//	private HashMap<Trigger, Event> events;
	private boolean isSent;
	private String runnableProbeId;
	protected ProbeTypes probeType;
	
//	public BaseResult() {
//		this.lastTimestamp = null;
//		this.setEvents(new HashMap<Trigger, TriggerEvent>());
//		setSent(false);
//	}
 	
	public BaseResult(String runnableProbeId,long timestamp) {
		
		this.runnableProbeId = runnableProbeId;
		this.lastTimestamp = timestamp;
		setSent(false);
	}
	public BaseResult(String runnableProbeId) {
		this.runnableProbeId = runnableProbeId;
		setSent(false);
	}

	public Long getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(Long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public int getNumberOfRollupTables()
	{
		long interval=RunnableProbeContainer.getInstanece().get(runnableProbeId).getProbe().getInterval();
		if(interval<240)
			return 6;
		if(interval>=240&&interval<1200)
			return 5;
		if(interval>=1200&&interval<3600)
			return 4;
		if(interval>=3600&&interval<21600)
			return 3;
		if(interval>=21600)
			return 2;
		Logit.LogError("ProbeRollup - getNumberOfRollupTables", "Wrong interval at Runnable Probe:"+runnableProbeId);
		return 0;
	}
	/**
	 * return -1 if no change return n if new is the new number of rollup tables
	 */
//	public int isNumberOfRollupTablesChanged(DataPointsRollup[][] dprs) {
//		int c = 0;
//		for (int i = 0; i < dprs[0].length; i++) {
//			if (dprs[0][i] != null)
//				c++;
//		}
//		int currentNumberOfRollupTables = this.getNumberOfRollupTables();
//		if (currentNumberOfRollupTables != c)
//			return currentNumberOfRollupTables;
//		return -1;
//	}

//	public void changeNumberRollupTables(DataPointsRollup[][] dprs, int newNumber) {
//		// TODO Auto-generated method stub
//	}

//	public void insertExistingRollups(DataPointsRollup[][] existing) {
//		// TODO Auto-generated method stub
//
//	}
//
//	public DataPointsRollup[][] retrieveExistingRollups() {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	public DataPointsRollup[] initRollupSeries(DataPointsRollup[] rollups) {
//		int n = this.getNumberOfRollupTables();
//		if (n == 0) {
//			Logit.LogError("BaseResult - initRollupSeries", "Unable to init rtt Rollups of Runnable Probe Results: "
//					+ getRunnableProbeId() + " check interval!");
//			return null;
//		}
//		for (int i = 0; i < n; i++) {
//			if (rollups[i] == null) {
//				if (i == 0)
//					rollups[0] = new DataPointsRollup(getRunnableProbeId(), DataPointsRollupSize._11day);
//				if (i == 1)
//					rollups[1] = new DataPointsRollup(getRunnableProbeId(), DataPointsRollupSize._36hour);
//				if (i == 2)
//					rollups[2] = new DataPointsRollup(getRunnableProbeId(), DataPointsRollupSize._6hour);
//				if (i == 3)
//					rollups[3] = new DataPointsRollup(getRunnableProbeId(), DataPointsRollupSize._1hour);
//				if (i == 4)
//					rollups[4] = new DataPointsRollup(getRunnableProbeId(), DataPointsRollupSize._20minutes);
//				if (i == 5)
//					rollups[5] = new DataPointsRollup(getRunnableProbeId(), DataPointsRollupSize._4minutes);
//			}
//
//		}
//		return rollups;
//	}

	/**
	 * @param results
	 *            - results in arrayList.
	 * @throws Exception
	 */
//	public synchronized void acceptResults(ArrayList<Object> results) throws Exception {
//		SysLogger
//				.Record(new Log("Processing results for Runnable Probe: " + this.getRp().getRPString(), LogType.Debug));
//	}

	/**
	 * 
	 * @return
	 * @throws Throwable
	 */

//	public HashMap<String, String> getResults() throws Throwable {
//		Logit.LogDebug("Collecting DATA for Runnable Probe: " + getRunnableProbeId());
//
//		String rpStr = getRunnableProbeId();
//		if (rpStr.contains("7352a46f-5189-428c-b4c0-fb98dedd10b1@inner_036f81e0-4ec0-468a-8396-77c21dd9ae5a"))
//			System.out.println("BREAKPOINT");
//
//		HashMap<String, String> results = new HashMap<String, String>();
//		results.put("rpID", getRunnableProbeId());
//		return results;
//	}

	public HashMap<String, String> getRaw() throws Throwable {
		return null;
	}

//	public HashMap<String, String> getRollups() throws Throwable {
//		return null;
//	}

//	public void resetRollups() {
//
//	}

//	protected void addRollupsFromExistingMemoryDump(DataPointsRollup[] original, DataPointsRollup[] memoryDump) {
//		Logit.LogDebug("Merging existing rollup for => " + getRunnableProbeId());
//		for (int i = 0; i < 6; i++) {
//			if (original[i] != null) {
//				original[i].mergeRollup(memoryDump[i]);
//			}
//		}
//	}

	public void checkIfTriggerd(HashMap<String,Trigger> triggers) throws Exception {
		Logit.LogInfo("Triggering Runnable Probe: " + getRunnableProbeId());
	}

	public void processTriggerResult(Trigger trigger, boolean triggered) {
		Event lastEvent = ResultsContainer.getInstance().getEvent(getRunnableProbeId(), trigger.getTriggerId());
		if (lastEvent != null && !triggered) {
			// if trigger event became true and normal again send event to api
			lastEvent.setStatus(true);
			lastEvent.setSent(false);
			Logit.LogInfo("Trigger " + trigger.getTriggerId() + " of Runnable Probe: "
					+ getRunnableProbeId() + " deactivated, will send event to API...");
		} else if (lastEvent == null && triggered) {
			Event event = new Event(trigger, false);
			ResultsContainer.getInstance().addEvent(runnableProbeId, trigger.getTriggerId(), event);
		}
	}

	public boolean isSent() {
		return isSent;
	}

	public void setSent(boolean isSentOK) {
		this.isSent = isSentOK;
	}

	public String getRunnableProbeId() {
		return runnableProbeId;
	}

	public void setRunnableProbeId(String runnableProbeId) {
		this.runnableProbeId = runnableProbeId;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getResultString() {
		// TODO Auto-generated method stub
		return null;
	}


}
