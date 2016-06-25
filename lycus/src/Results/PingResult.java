package Results;

import java.util.HashMap;
import org.json.simple.JSONArray;

import GlobalConstants.Constants;
import GlobalConstants.ProbeTypes;
import lycus.Trigger;
import lycus.TriggerCondition;

public class PingResult extends BaseResult {

	private Integer packetLoss;
	private Double rtt;
	private Integer ttl;

	public PingResult(String runnableProbeId, long timestamp, int packetLoss, double rtt, int ttl) {
		super(runnableProbeId, timestamp);
		this.probeType = ProbeTypes.ICMP;
		this.packetLoss = packetLoss;
		this.rtt = rtt;
		this.ttl = ttl;
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

	public Double getRtt() {
		return rtt;
	}

	public void setRtt(Double rtt) {
		this.rtt = rtt;
	}

	public Integer getTtl() {
		return ttl;
	}

	public void setTtl(Integer ttl) {
		this.ttl = ttl;
	}

	@Override
	public synchronized void checkIfTriggerd(HashMap<String, Trigger> triggers) throws Exception {
		super.checkIfTriggerd(triggers);
		for (Trigger trigger : triggers.values()) {
			boolean triggered = false;
			if (trigger.getElementType() == null)
				return;
			switch (trigger.getElementType()) {
			case "pl":
				triggered = checkForPacketLostTrigger(trigger);
				break;
			case "rta":
				triggered = checkForRttTrigger(trigger);
				break;
			}
			super.processTriggerResult(trigger, triggered);

		}
	}

	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public String getResultString() {
		JSONArray result = new JSONArray();
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
			if (flag && condition.getAndOr().equals("or"))
				return true;
			else if (!flag && condition.getAndOr().equals("and"))
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
			if (flag && condition.getAndOr().equals("or"))
				return true;
			else if (!flag && condition.getAndOr().equals("and"))
				return false;
		}
		return flag;
	}

	// TODO: Oren ask ran what is true?
	public Boolean isActive() {
		return packetLoss > Constants.pingPacketLostMin;
	}
}
