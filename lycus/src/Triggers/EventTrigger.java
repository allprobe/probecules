package Triggers;

import Probes.BaseProbe;
import Results.BaseResult;
import lycus.Event;
import lycus.ResultsContainer;

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
			if (trigger.getStatus()) {
				if (isConditionMet(trigger))
					triggerEvent(trigger);
				else
					cancelEvent(trigger);
			}
		}

		return true;
	}

	private boolean isConditionMet(Trigger trigger) {
		return lastResults.isConditionMet(trigger);
	}

	private boolean triggerEvent(Trigger trigger) {
		Event eventExist = ResultsContainer.getInstance().getEvent(runnableProbeId, trigger.getTriggerId());
		if (eventExist == null) {
			Event event = new Event(trigger);
			ResultsContainer.getInstance().addEvent(runnableProbeId, trigger.getTriggerId(), event);
		}
		return true;
	}

	private boolean cancelEvent(Trigger trigger) {
		Event eventExist = ResultsContainer.getInstance().getEvent(runnableProbeId, trigger.getTriggerId());
		if (eventExist != null) {
			eventExist.setIsStatus(true);
		}
		return true;
	}
}
