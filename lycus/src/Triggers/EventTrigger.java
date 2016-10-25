package Triggers;

import GlobalConstants.XvalueUnit;
import Probes.BaseProbe;
import Results.BaseResult;
import lycus.Event;
import lycus.ResultsContainer;
import lycus.RunnableProbe;
import lycus.RunnableProbeContainer;

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
				if (isConditionMet(result, trigger))
					triggerEvent(trigger);
				else
					cancelEvent(trigger);
			}
		}

		return true;
	}

	private boolean isConditionMet(BaseResult result, Trigger trigger) {
		return lastResults.isConditionMet(result, trigger);
	}

	private boolean triggerEvent(Trigger trigger) {
		Event eventExist = ResultsContainer.getInstance().getEvent(runnableProbeId, trigger.getTriggerId());
		if (eventExist == null) {
			RunnableProbe runnableProbe = RunnableProbeContainer.getInstanece().get(runnableProbeId);
			Event event = new Event(trigger.getTriggerId(), runnableProbe.getProbe().getUser().getUserId().toString(), runnableProbe.getHost().getBucket());
			ResultsContainer.getInstance().addEvent(runnableProbeId, trigger.getTriggerId(), event);
		}
		return true;
	}

	private boolean cancelEvent(Trigger trigger) {

		Event eventExist = ResultsContainer.getInstance().getEvent(runnableProbeId, trigger.getTriggerId());
		if (eventExist != null) {
			eventExist.setIsStatus(true);
//			eventExist.setTime(System.currentTimeMillis());
		}
		return true;
	}

	public boolean removeEvent(String triggerId) {
		Trigger trigger = probe.getTrigger(triggerId);
		cancelEvent(trigger);
		return true;
	}
}
