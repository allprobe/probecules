package lycus.Results;

import java.util.HashMap;

import org.json.simple.JSONArray;

import lycus.Trigger;
import lycus.TriggerCondition;
import lycus.GlobalConstants.ProbeTypes;

public class PingResult extends BaseResult {

	private Integer packetLoss;
//	private DataPointsRollup[] packetLostRollups;
	private Double rtt;
//	private DataPointsRollup[] rttRollups;
	private Integer ttl;

	public PingResult(String runnableProbeId,long timestamp, int packetLoss, double rtt, int ttl) {
		super(runnableProbeId,timestamp);
		this.probeType=ProbeTypes.PING;
		this.packetLoss=packetLoss;
		this.rtt=rtt;
		this.ttl=ttl;
//		this.packetLostRollups = this.initRollupSeries(new DataPointsRollup[6]);
//		this.rttRollups = this.initRollupSeries(new DataPointsRollup[6]);
	}
	public PingResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public Integer getPacketLost() {
		return packetLoss;
	}

	public void setPacketLost(Integer packetLost) {
		this.packetLoss = packetLost;
	}

//	public DataPointsRollup[] getPacketLostRollups() {
//		return packetLostRollups;
//	}
//
//	public void setPacketLostRollups(DataPointsRollup[] packetLostRollups) {
//		this.packetLostRollups = packetLostRollups;
//	}

	public Double getRtt() {
		return rtt;
	}

	public void setRtt(Double rtt) {
		this.rtt = rtt;
	}

//	public DataPointsRollup[] getRttRollups() {
//		return rttRollups;
//	}
//
//	public void setRttRollups(DataPointsRollup[] rttRollups) {
//		this.rttRollups = rttRollups;
//	}

	public Integer getTtl() {
		return ttl;
	}

	public void setTtl(Integer ttl) {
		this.ttl = ttl;
	}

//	@Override
//	public void acceptResults(ArrayList<Object> results) throws Exception {
//		
////		super.acceptResults(results);
//		String rpStr = this.getRp().getRPString();
//		if (rpStr.contains(
//				"ae1981c3-c157-4ce2-9086-11e869d4a344@icmp_41468c4c-c7d4-4dae-bd03-a5b2ca0b44d6"))
//			System.out.println("BREAKPOINT - RunnablePingerProbeResults");
////		if(results==null)
////			return;
////		
////		long lastTimestamp = (long) results.get(0);
////		int packetLost = (int) results.get(1);
////		double rtt = (double) results.get(2);
////		int ttl = (int) results.get(3);
//
//		this.setLastTimestamp(lastTimestamp);
//		this.setPacketLost(packetLost);
//		this.setRtt(rtt);
//		this.setTtl(ttl);
//
//		for (int i = 0; i < this.getNumberOfRollupTables(); i++) {
//			DataPointsRollup packetLostRollup = this.getPacketLostRollups()[i];
//			DataPointsRollup rttRollup = this.getRttRollups()[i];
//
//			packetLostRollup.add(lastTimestamp, packetLost);
//			rttRollup.add(lastTimestamp, rtt);
//		}
//
//		try{
//		checkIfTriggerd();
//		}
//		catch(Exception e)
//		{
//			Logit.LogError("PingerResults - acceptResults", "Error triggering RunnableProbe: "+this.getRp());
//		}
//	}
	
	@Override
	public synchronized void checkIfTriggerd(HashMap<String,Trigger> triggers) throws Exception {
		super.checkIfTriggerd(triggers);
		for (Trigger trigger : triggers.values()) {
			boolean triggered = false;
			if(trigger.getElementType()==null)
				return;
			switch (trigger.getElementType()) {
			case "pl":
				triggered = checkForPacketLostTrigger(trigger);
				break;
			case "rta":
				triggered = checkForRttTrigger(trigger);
				break;
			}
			super.processTriggerResult(trigger,triggered);

		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return super.getName();
	}
	@Override
	public String getResultString() {
		JSONArray result=new JSONArray();
		result.add(1);
		result.add(packetLoss);
		result.add(rtt);
		result.add(ttl);
		return result.toString();
	}
	private boolean checkForPacketLostTrigger(Trigger trigger) {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			int x = Integer.parseInt(condition.getxValue());
			int lastValue = this.getPacketLost();
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

	private boolean checkForRttTrigger(Trigger trigger) {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			long x = Long.parseLong(condition.getxValue());
			double lastValue = this.getRtt();
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

//	@Override
//	public void insertExistingRollups(DataPointsRollup[][] existing) {
//		this.addRollupsFromExistingMemoryDump(this.getPacketLostRollups(), existing[0]);
//		this.addRollupsFromExistingMemoryDump(this.getRttRollups(), existing[1]);
//	}

//	@Override
//	public DataPointsRollup[][] retrieveExistingRollups() {
//		DataPointsRollup[][] existing = new DataPointsRollup[2][6];
//		existing[0] = this.getPacketLostRollups();
//		existing[1] = this.getRttRollups();
//		return existing;
//	}

//	@Override
//	public HashMap<String, String> getResults() throws Throwable {
//		
//		String rpStr = this.getRp().getRPString();
//		if (rpStr.contains(
//				"fc46cf87-0872-4e5d-9b83-c44a3d1f3ea6@icmp_1f1aed08-7331-4126-97ef-225e90b4a969"))
//			System.out.println("BREAKPOINT");
//		
//		HashMap<String, String> results = super.getResults();
//		JSONArray rawResults = new JSONArray();
//		rawResults.add(1);
//		rawResults.add(this.getPacketLost());
//		this.setPacketLost(null);
//		rawResults.add(this.getRtt());
//		this.setRtt(null);
//		rawResults.add(this.getTtl());
//		this.setTtl(null);
//		results.put("RAW@packetLost_rtt_ttl@" + this.getLastTimestamp(), rawResults.toJSONString());
//		int rollupsNumber = this.getNumberOfRollupTables();
//		for (int i = 0; i < rollupsNumber; i++) {
//
//			DataPointsRollup currentPacketLostRollup = this.getPacketLostRollups()[i];
//			DataPointsRollup currentRttRollup = this.getRttRollups()[i];
//			DataPointsRollup finishedPacketLostRollup = currentPacketLostRollup.getLastFinishedRollup();
//			DataPointsRollup finishedRttRollup = currentRttRollup.getLastFinishedRollup();
//			if (currentPacketLostRollup == null || currentRttRollup == null) {
//				SysLogger
//						.Record(new Log("Wrong Rollup Tables Number Of: " + this.getRp().getRPString(), LogType.Debug));
//				continue;
//			}
//			if (finishedPacketLostRollup != null && finishedRttRollup != null) {
//				JSONArray packetLostRollupResults = new JSONArray();
//				packetLostRollupResults.add(finishedPacketLostRollup.getMin());
//				packetLostRollupResults.add(finishedPacketLostRollup.getMax());
//				packetLostRollupResults.add(finishedPacketLostRollup.getAvg());
//				packetLostRollupResults.add(finishedPacketLostRollup.getResultsCounter());
//
//				JSONArray rttRollupResults = new JSONArray();
//				rttRollupResults.add(finishedRttRollup.getMin());
//				rttRollupResults.add(finishedRttRollup.getMax());
//				rttRollupResults.add(finishedRttRollup.getAvg());
//				rttRollupResults.add(finishedRttRollup.getResultsCounter());
//
//				JSONArray fullRollupResults = new JSONArray();
//				fullRollupResults.add(packetLostRollupResults);
//				fullRollupResults.add(rttRollupResults);
//
//				results.put("ROLLUP" + finishedPacketLostRollup.getTimePeriod().getName() + "@packetLost_rtt@"
//						+ finishedPacketLostRollup.getEndTime(), fullRollupResults.toJSONString());
//
//				currentPacketLostRollup.setLastFinishedRollup(null);
//				currentRttRollup.setLastFinishedRollup(null);
//			} else {
//				if ((finishedPacketLostRollup == null && finishedRttRollup != null)
//						|| (finishedPacketLostRollup != null && finishedRttRollup == null)) {
//					Logit.LogError("PingerResults - getResults", "Bad RunnableProbeResults: " + this.getRp().getRPString()
//							+ ", DataPointsRollup values are not synced!");
//				}
//			}
//		}
//		this.setLastTimestamp((long) 0);
//		return results;
//	}
}
