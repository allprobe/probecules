package Results;

import java.util.HashMap;

import GlobalConstants.ProbeTypes;
import Interfaces.IResult;
import Utils.Logit;
import lycus.Trigger;
import lycus.Event;
import lycus.ResultsContainer;
import lycus.RunnableProbe;
import lycus.RunnableProbeContainer;

public class BaseResult implements IResult {
	private Long lastTimestamp;
	private boolean isSent;
	private String runnableProbeId;
	protected ProbeTypes probeType;

	public BaseResult(String runnableProbeId, long timestamp) {

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
		Logit.LogInfo("Triggering Runnable Probe: " + getRunnableProbeId());
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
	public String getResultString() {
		return null;
	}

}
