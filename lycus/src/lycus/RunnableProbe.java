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
import Probes.SqlProbe;
import Probes.TracerouteProbe;
import Probes.DiskProbe;
import Results.BaseResult;
import Rollups.RollupsContainer;
import SLA.SLAContainer;
import Triggers.EventTrigger;
import Triggers.Trigger;
import Utils.GeneralFunctions;
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
				"ffe47feb-4948-4d5f-bace-c7670d0e4fd1@ffe47feb-4948-4d5f-bace-c7670d0e4fd1@http_ee12a9e8-9a24-4e43-8388-d767a8c6b611"))
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
		if (getProbe() instanceof SqlProbe)
			return ProbeTypes.SQL;
		return null;
	}

	public void run() {
		try {
			// isRunning = false will stop the thread
			while (isRunning()) {
				BaseResult result = null;
				try {
					String rpStr = this.getId();
					if (rpStr.contains(
							"0eb888bc-ba24-49f0-8468-da89ca830c77@discovery_35667a76-cb01-4108-9429-cac2dbcf933e"))
						Logit.LogDebug("BREAKPOINT - RunnableProbe");


					if (rpStr.contains(
							"15a29f39-5baf-4672-8853-c08b4b247be0@discovery_3ee653fc-adaa-468e-9430-b1793b1d1c7d"))
						Logit.LogDebug("BREAKPOINT - RunnableProbe");

					// isActive = false will pause the thread
					if (!isActive() || !getProbe().isActive())
						continue;

					long timeStamp = System.currentTimeMillis();
//					if(getProbe() instanceof RBLProbe)
//					Logit.LogCheck("Checking RBL: "+GeneralFunctions.invertIPAddress(host.getHostIp()) + "." + ((RBLProbe)getProbe()).getRBL());

					result = getResult();
					result = buildErrorResultWhenEmpty(result);

					addResult(result, timeStamp);
					addResultToTrigger(result);
					addResultToRollups(result);
					addResultToSLA(result);

				} catch (Exception e) {
					Logit.LogError("RunnableProbe - run()",
							"Running Probe: " + this.getId() + " at Host: " + this.getHost().getHostIp() + "("
									+ this.getHost().getName() + ")" + ", Results: " + result
									+ "was thrown an exception: " + e.getMessage());
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
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - run()",
					"Error, The RunnableProbe Thread was Interrupted, Probe type: " + this.getProbeType()
							+ ", ProbeName: " + this.getProbe().getName() + ", \nRunnabelProbeId: " + this.getId()
							+ "\nException: " + e.getMessage());
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
					"Error processing runnable probe results to results container! RPID=" + this.getId()+" , RESULT: "+result.toString(),e);
		}
	}

	public boolean addResultToTrigger(BaseResult result) {

		if (!UsersManager.eventsPulled())
			return true;

		try {
			if ((this.getProbe() instanceof DiscoveryProbe))
				return true;
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - run()", "Error triggering runnable probe results!  " + this.getProbeType()
					+ " " + this.getProbe().getName() + ", \nRunnabelProbeId: " + this.getId());
			return false;
		}

		try {
			if (probe.getTriggers().size() > 0)
				getEventTrigger().addResult(result);
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - addResultToTrigger()",
					"Error Adding result to eventTrigger! " + this.getId() + " Error Message: " + e);
			return false;
		}
		return true;
	}

	public boolean removeEvents(String triggerId) {
		try {
			getEventTrigger().removeEvent(triggerId);
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - removeEvents()", "Error Removing event of trigger! " + triggerId);
			return false;
		}
		return true;
	}

	public boolean removeAllEvents() {
		HashMap<String, Trigger> triggers = getProbe().getTriggers();
		if (triggers == null)
			return true;
		for (Trigger trigger : triggers.values())
			removeEvents(trigger.getTriggerId());
		return true;
	}

	public EventTrigger getEventTrigger() {
		return eventTrigger;
	}
}
