package Triggers;

import Probes.BaseProbe;
import Results.BaseResult;
import lycus.Event;
import lycus.ResultsContainer;
import lycus.Trigger;

public class EventTrigger {
	private CheckTrigger lastResults;
	private BaseProbe probe;
	private String runnableProbeId;

	public EventTrigger(BaseProbe probe, String runnableProbeId) {
		lastResults = new CheckTrigger(probe.getInterval());
		this.probe = probe;
		this.runnableProbeId = runnableProbeId;
	}

	public boolean addResult(BaseResult result) {
		lastResults.enqueue(result);
		for (Trigger trigger : probe.getTriggers().values()) {
			if (isConditionMet(trigger)) {
				triggerEvent(trigger);
			}
		}

		return true;
	}

	private boolean isConditionMet(Trigger trigger) {
		return lastResults.isConditionMet(trigger);
	}

	private boolean triggerEvent(Trigger trigger) {
		// if (lastEvent == null) {
		Event event = new Event(trigger, false);
		ResultsContainer.getInstance().addEvent(runnableProbeId, trigger.getTriggerId(), event);
		// }
		return true;
	}
}

