package Results;

import java.util.HashMap;

import com.google.common.base.Enums;

import GlobalConstants.Enums.ResultValueType;
import GlobalConstants.ProbeTypes;
import Interfaces.IResult;
import Utils.Logit;
import lycus.*;

public class BaseResult implements IResult {
	private Long lastTimestamp;
	private boolean isSent;
	private String runnableProbeId;
	protected ProbeTypes probeType;
	private String errorMessage;

	public BaseResult(String runnableProbeId, long timestamp) {

		this.runnableProbeId = runnableProbeId;
		this.lastTimestamp = timestamp;
		errorMessage = "";
		setSent(false);
	}

	public BaseResult(String runnableProbeId) {
		this.runnableProbeId = runnableProbeId;
		this.lastTimestamp = System.currentTimeMillis();
		errorMessage = "";
		setSent(false);
	}

	public Long getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(Long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public int getNumberOfRollupTables() {
		RunnableProbe runnableProbe = RunnableProbeContainer.getInstanece().get(runnableProbeId);
		if (runnableProbe == null) {
			Logit.LogError("BaseResult - getNumberOfRollupTables()",
					"Unable to determine number of rollups tables - " + runnableProbeId);
			return 0;
		}
		long interval = runnableProbe.getProbe().getInterval();
		if (interval < 240)
			return 6;
		if (interval >= 240 && interval < 1200)
			return 5;
		if (interval >= 1200 && interval < 3600)
			return 4;
		if (interval >= 3600 && interval < 21600)
			return 3;
		if (interval >= 21600)
			return 2;
		Logit.LogError("ProbeRollup - getNumberOfRollupTables", "Wrong interval at Runnable Probe:" + runnableProbeId);
		return 0;
	}

	public HashMap<String, String> getRaw() throws Throwable {
		return null;
	}

	public void checkIfTriggerd(HashMap<String, Trigger> triggers) throws Exception {
		for (Trigger trigger : triggers.values()) {
			boolean triggered = checkForTriggerActivated(trigger);
			processTriggerResult(trigger, triggered);

		}
	}

	public void processTriggerResult(Trigger trigger, boolean triggered) {
		Event lastEvent = ResultsContainer.getInstance().getEvent(getRunnableProbeId(), trigger.getTriggerId());
		if (lastEvent != null && !triggered) {
			// if trigger event became true and normal again send event to api
			lastEvent.setStatus(true);
			lastEvent.setSent(false);
			Logit.LogInfo("Trigger " + trigger.getTriggerId() + " of Runnable Probe: " + getRunnableProbeId()
					+ " deactivated, will send event to API...");
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
		return null;
	}

	@Override
	public Object getResultObject() {
		return null;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	private boolean checkForTriggerActivated(Trigger trigger) {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			String x = condition.getxValue();
			Double xNumber = Double.parseDouble(x);
			Object[] lastValues = (RunnableProbeContainer.getInstanece().get(this.getRunnableProbeId()))
					.getTriggerFunction(trigger).get();
			for (int i = 0; i < lastValues.length; i++) {
				flag = conditionByType(lastValues[i], x, condition.getCode());
				if (!flag)
					return false;
			}
		}
		return flag;
	}

	private boolean conditionByType(Object lastValue, String triggerValue, int code) {
		switch (code) {
		case 1:
			if (lastValue.getClass().equals(Integer.class))
				if ((Integer) lastValue > Integer.parseInt(triggerValue))
					return true;
			if (lastValue.getClass().equals(Double.class))
				if ((Double) lastValue > Double.parseDouble(triggerValue))
					return true;
			if (lastValue.getClass().equals(Long.class))
				if ((Integer) lastValue > Integer.parseInt(triggerValue))
					return true;

		case 2:
			if (lastValue.getClass().equals(Integer.class))
				if ((Integer) lastValue < Integer.parseInt(triggerValue))
					return true;
			if (lastValue.getClass().equals(Double.class))
				if ((Double) lastValue < Double.parseDouble(triggerValue))
					return true;
			if (lastValue.getClass().equals(Long.class))
				if ((Integer) lastValue < Integer.parseInt(triggerValue))
					return true;
		case 3:
			if (lastValue.getClass().equals(Integer.class))
				if ((Integer) lastValue == Integer.parseInt(triggerValue))
					return true;
			if (lastValue.getClass().equals(Double.class))
				if ((Double) lastValue == Double.parseDouble(triggerValue))
					return true;
			if (lastValue.getClass().equals(Long.class))
				if ((Integer) lastValue == Integer.parseInt(triggerValue))
					return true;
			if (lastValue.getClass().equals(Boolean.class))
				if ((Boolean) lastValue == Boolean.parseBoolean(triggerValue))
					return true;
			if (lastValue.getClass().equals(String.class))
				if ((String) lastValue == triggerValue)
					return true;
		case 4:
			if (lastValue.getClass().equals(Integer.class))
				if ((Integer) lastValue == Integer.parseInt(triggerValue))
					return true;
			if (lastValue.getClass().equals(Double.class))
				if ((Double) lastValue == Double.parseDouble(triggerValue))
					return true;
			if (lastValue.getClass().equals(Long.class))
				if ((Integer) lastValue == Integer.parseInt(triggerValue))
					return true;
			if (lastValue.getClass().equals(Boolean.class))
				if ((Boolean) lastValue == Boolean.parseBoolean(triggerValue))
					return true;
			if (lastValue.getClass().equals(String.class))
				if ((String) lastValue == triggerValue)
					return true;
		}
		return false;
	}

	public Object getResultElementValue(ResultValueType valueType) {
		switch (valueType) {
		case WRT:
			return ((WebResult) this).getResponseTime();
		case PRT:
			return ((PortResult) this).getResponseTime();

		case RC:
			return ((WebResult) this).getStatusCode();

		case PS:
			return ((WebResult) this).getPageSize();

		case ST:
			return ((PortResult) this).isActive();

		case RTA:
			return ((PingResult) this).getRtt();

		case PL:
			return ((PingResult) this).getPacketLost();

		// case "DFDS":
		// this.lastResults[0] = null;
		// break;
		// case "DUDS":
		// this.lastResults[0] = ((WebResult) result).getStatusCode();
		// break;
		// case "DBI":
		// this.lastResults[0] = ((WebResult) result).getStatusCode();
		// break;
		// case "DBO":
		// this.lastResults[0] = ((WebResult) result).getStatusCode();
		// break;
		// case "WSERT":
		// this.lastResults[0] = ((WebResult) result).getStatusCode();
		// break;
		// case "WAERC":
		// this.lastResults[0] = ((WebResult) result).getStatusCode();
		// break;
		// case "TRARHRT":
		// this.lastResults[0] = ((WebResult) result).getStatusCode();
		// break;
		// case "TRDHRT":
		// this.lastResults[0] = ((WebResult) result).getStatusCode();
		// break;
		// case "DTDS":
		// this.lastResults[0] = ((WebResult) result).getStatusCode();
		// break;
		}
		return null;
	}
}
