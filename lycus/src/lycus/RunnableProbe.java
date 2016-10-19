package lycus;

import java.util.HashMap;

import GlobalConstants.ProbeTypes;
import Probes.BaseProbe;
import Probes.DiscoveryProbe;
import Probes.HttpProbe;
import Probes.IcmpProbe;
import Probes.NicProbe;
import Probes.PortProbe;
import Probes.RBLProbe;
import Probes.SnmpProbe;
import Probes.TracerouteProbe;
import Probes.DiskProbe;
import Results.BaseResult;
import Rollups.RollupsContainer;
import SLA.SLAContainer;
import Triggers.EventTrigger;
import Triggers.Trigger;
import Utils.Logit;

public class RunnableProbe implements Runnable {
	private Host host;
	private BaseProbe probe;
	private boolean isActive;
	private boolean isRunning;
	private EventTrigger eventTrigger;

	public RunnableProbe(Host host, BaseProbe probe) {
		this.setHost(host);
		this.setProbe(probe);
		this.setRunning(true);
		this.setActive(true);
		if (this.getProbeType() == null) {
			// Must be handled by Roi
			return;
		}

		eventTrigger = new EventTrigger(probe, this.getId());

		if (probe.getProbe_id().contains("icmp_cc9a931c-6232-4b17-b2f9-be00b40ce02b"))
			Logit.LogDebug("BREAKPOINT - RunnableProbe");
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public BaseProbe getProbe() {
		return probe;
	}

	public void setProbe(BaseProbe probe) {
		this.probe = probe;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	private boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		String rpStr3 = this.getId();
		if (rpStr3.contains(
				"8b0104e7-5902-4419-933f-668582fc3acd@6975cb58-8aa4-4ecd-b9fc-47b78c0d7af8@snmp_5d937636-eb75-4165-b339-38a729aa2b7d"))
			Logit.LogDebug("BREAKPOINT - RunnableProbe");

		this.isRunning = isRunning;
	}

	public String getId() {
		return this.getProbe().getTemplate_id().toString() + "@" + this.getHost().getHostId().toString() + "@"
				+ this.getProbe().getProbe_id();
	}

	public ProbeTypes getProbeType() {
		if (getProbe() instanceof IcmpProbe)
			return ProbeTypes.ICMP;
		if (getProbe() instanceof PortProbe)
			return ProbeTypes.PORT;
		if (getProbe() instanceof HttpProbe)
			return ProbeTypes.HTTP;
		if ((getProbe() instanceof SnmpProbe))
			return ProbeTypes.SNMP;
		if (getProbe() instanceof RBLProbe)
			return ProbeTypes.RBL;
		if (getProbe() instanceof DiscoveryProbe)
			return ProbeTypes.DISCOVERY;
		if (getProbe() instanceof NicProbe)
			return ProbeTypes.BANDWIDTH_ELEMENT;
		if (getProbe() instanceof DiskProbe)
			return ProbeTypes.DISK_ELEMENT;
		if (getProbe() instanceof TracerouteProbe)
			return ProbeTypes.TRACEROUTE;
		return null;
	}

	public void run() {
		// isRunning = false will stop the thread
		while (isRunning()) {
			BaseResult result = null;
			try {
				String rpStr = this.getId();
				if (rpStr.contains("traceroute_0556f87f-6015-498d-8a27-a02086ff521a"))
					Logit.LogDebug("BREAKPOINT - RunnableProbe");

				// isActive = false will pause the thread
				if (!isActive() || !getProbe().isActive())
					continue;

				long timeStamp = System.currentTimeMillis();

				result = getResult();
				result = buildErrorResultWhenEmpty(result);

				addResult(result, timeStamp);
				addResultToTrigger(result);
				addResultToRollups(result);
				addResultToSLA(result);

			} finally {
				try {
					Logit.LogInfo("Running Probe: " + this.getId() + " at Host: " + this.getHost().getHostIp() + "("
							+ this.getHost().getName() + ")" + ", Results: " + result + " ...");

					synchronized (this) {
						wait(this.getProbe().getInterval() * 1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private BaseResult getResult() {
		BaseResult result;
		try {
			result = getProbe().getResult(this.getHost());
		} catch (Exception e) {
			result = new BaseResult(this.getId());
			result.setErrorMessage("RESULT_EXCEPTION");
			Logit.LogError("RunnableProbe - getResult()",
					"Error, getting runnable probe results from probe! getResult() throws exception "
							+ this.getProbeType() + " " + this.getProbe().getName() + ", \nRunnabelProbeId: "
							+ this.getId(),
					e);
		}
		return result;
	}

	private BaseResult buildErrorResultWhenEmpty(BaseResult result) {
		if (result == null) {
			result = new BaseResult(this.getId());
			result.setErrorMessage("RESULT_OBJECT_NULL");
			Logit.LogError("RunnableProbe - buildErrorResultWhenEmpty()",
					"Error, getting runnable probe results from probe! Returned object is null " + this.getProbeType()
							+ " " + this.getProbe().getName() + ", \nRunnabelProbeId: " + this.getId());
		}
		return result;
	}

	private void addResultToSLA(BaseResult result) {
		try {
			SLAContainer.getInstance().addToSLA(result);
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - addResultToSLA()",
					"Error processing runnable probe results to SLA container!" + this.getId(), e);
		}
	}

	private void addResultToRollups(BaseResult result) {
		try {
			RollupsContainer.getInstance().addResult(result);
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - addResultToRollups()",
					"Error processing runnable probe results to rollups container!" + this.getId(), e);
		}
	}

	private void addResult(BaseResult result, long timeStamp) {
		try {
			result.setLastTimestamp(timeStamp);
			ResultsContainer.getInstance().addResult(result);
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - addResult()",
					"Error processing runnable probe results to results container! " + this.getId());
		}
	}

	public boolean addResultToTrigger(BaseResult result) {
		try {
			if (probe.getTriggers().size() > 0)
				eventTrigger.addResult(result);
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - addResultToTrigger()",
					"Error Adding result to eventTrigger! " + this.getId());
			return false;
		}
		return true;
	}

	public boolean removeEvents(String triggerId) {
		try {
			eventTrigger.removeEvent(triggerId);
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - removeEvents()", "Error Removing event of trigger! " + triggerId);
			return false;
		}
		return true;
	}

	public boolean removeAllEvents() {
		HashMap<String, Trigger> triggers = getProbe().getTriggers();
		for (Trigger trigger : triggers.values())
			removeEvents(trigger.getTriggerId());
		return true;
	}
}
