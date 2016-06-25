package Results;

import java.util.HashMap;
import org.json.simple.JSONArray;
import GlobalConstants.ProbeTypes;
import Utils.Logit;
import lycus.Trigger;
import lycus.TriggerCondition;

public class PortResult extends BaseResult {
	private Boolean portStatus;
	private Long responseTime;

	public PortResult(String runnableProbeId, long timestamp, boolean portState, long responseTime2) {
		super(runnableProbeId, timestamp);
		this.probeType = ProbeTypes.PORT;
		this.portStatus = portState;
		this.responseTime = responseTime2;
	}

	public PortResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public Boolean isActive() {
		return portStatus;
	}

	public void setIsActive(Boolean portStatus) {
		this.portStatus = portStatus;
	}

	public Long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(Long responseTime) {
		this.responseTime = responseTime;
	}

	@Override
	public void checkIfTriggerd(HashMap<String, Trigger> triggers) throws Exception {
		super.checkIfTriggerd(triggers);
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
			boolean lastValue = this.isActive();
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
			long x;
			try {
				x = Long.parseLong(condition.getxValue());
			} catch (Exception e) {
				Logit.LogInfo("Unable to parse trigger X value for triggerId: " + trigger.getTriggerId() + ", E: "
						+ e.getMessage());
				throw e;
			}
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
	public String getResultString() {
		JSONArray result = new JSONArray();
		result.add(2);
		result.add(portStatus);
		result.add(responseTime);
		return result.toString();
	}
}
