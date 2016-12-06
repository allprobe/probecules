package Triggers;

import Elements.NicElement;
import Events.EvenetsQueue;
import Events.Event;
import Elements.DiskElement;
import Probes.BaseProbe;
import Probes.DiskProbe;
import Probes.NicProbe;
import Results.BaseResult;
import Utils.GeneralFunctions;
import Utils.Logit;
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
		try {
			lastResults.enqueue(result);
			for (Trigger trigger : probe.getTriggers().values()) {
				try {
					if (trigger.getStatus()) {
						if (isConditionMet(result, trigger))
							triggerEvent(trigger);
						else
							cancelEvent(trigger);
					}
				} catch (Exception e) {
					Logit.LogError("EventTrigger - addResult()", "Error, checking trigger: name = " + trigger.getName()
							+ " , TriggerId: " + trigger.getTriggerId(), e);
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			Logit.LogError("EventTrigger - addResult()", "Error, adding results to triggers container", e);
			e.printStackTrace();
		}

		return true;
	}

	private boolean isConditionMet(BaseResult result, Trigger trigger) {
		return lastResults.isConditionMet(result, trigger);
	}

	private boolean triggerEvent(Trigger trigger) {
		Event eventExist = ResultsContainer.getInstance().getEvent(runnableProbeId, trigger.getTriggerId());
		try {
			if (eventExist == null) {
				RunnableProbe runnableProbe = RunnableProbeContainer.getInstanece().get(runnableProbeId);

				String hostNotificationGroup = "";
				if (runnableProbe.getHost().getNotificationGroups() != null)
					hostNotificationGroup = runnableProbe.getHost().getNotificationGroups().toString();

				Event event = new Event(trigger.getTriggerId(),
						runnableProbe.getProbe().getUser().getUserId().toString(), runnableProbe.getHost().getBucket(),
						runnableProbe.getHost().getName(), hostNotificationGroup, trigger.getName(),
						trigger.getSvrty().toString(), runnableProbeId);
				if ((runnableProbe.getProbe() instanceof NicProbe)) {
					NicElement element = ((NicProbe) runnableProbe.getProbe()).getNicElement();
					event.setSubType("nic-element@" + GeneralFunctions.Base64Encode(element.getName()));
				} else if ((runnableProbe.getProbe() instanceof DiskProbe)) {
					DiskElement element = ((DiskProbe) runnableProbe.getProbe()).getDiskElement();
					event.setSubType("disk-element@" + GeneralFunctions.Base64Encode(element.getName()));
				}
				ResultsContainer.getInstance().addEvent(runnableProbeId, trigger.getTriggerId(), event);
				EvenetsQueue.getInstance().add(event);
			}
		} catch (Exception e) {
			Logit.LogError("EventTrigger - triggerEvent()",
					"Error, triggering event, triggerName: " + trigger.getName() + " , TriggerId: "
							+ trigger.getTriggerId() + " RunnableProbeId: " + eventExist.getRunnableProbeId(),
					e);
			e.printStackTrace();
		}
		return true;
	}

	private boolean cancelEvent(Trigger trigger) {
		Event eventExist = ResultsContainer.getInstance().getEvent(runnableProbeId, trigger.getTriggerId());
		try {
			if (eventExist != null) {
				eventExist.setIsStatus(true);
				EvenetsQueue.getInstance().add(eventExist);
				ResultsContainer.getInstance().removeEvent(runnableProbeId, trigger.getTriggerId());
				// eventExist.setTime(System.currentTimeMillis());
			}
		} catch (Exception e) {
			Logit.LogError("EventTrigger - triggerEvent()",
					"Error, cancelling event, triggerName: " + trigger.getName() + " , TriggerId: "
							+ trigger.getTriggerId() + " RunnableProbeId: " + eventExist.getRunnableProbeId(),
					e);
			e.printStackTrace();
		}
		return true;
	}

	public boolean removeEvent(String triggerId) {
		Trigger trigger = probe.getTrigger(triggerId);
		cancelEvent(trigger);
		return true;
	}
}
