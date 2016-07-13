package lycus;

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
import Utils.Logit;

public class RunnableProbe implements Runnable {
	private Host host;
	private BaseProbe probe;
	private boolean isActive;
	private boolean isRunning;

	public RunnableProbe(Host host, BaseProbe probe) {
		this.setHost(host);
		this.setProbe(probe);
		this.setRunning(true);
		this.setActive(true);
		if (this.getProbeType() == null) {
			// Must be handled by Roi
			return;
		}
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
		while (isRunning()) {
			BaseResult result = null;
			try {
				String rpStr = this.getId();
				if (rpStr.contains("snmp_52caf27e-445b-4b8d-bfc6-0307fd4ef3eb"))
					Logit.LogDebug("BREAKPOINT - RunnableProbe");

				if (!isActive() || !getProbe().isActive())
					continue;

				// if (this.getProbeType().equals(ProbeTypes.BANDWIDTH_ELEMENT)
				// && (!((NicProbe)getProbe()).getNicElement().isActive() ||
				// !getProbe().isActive()))
				// continue;
				// if (this.getProbeType().equals(ProbeTypes.DISK_ELEMENT)
				// && (!((DiskProbe)getProbe()).getDiskElement().isActive() ||
				// !getProbe().isActive()))
				// continue;

				// Long timeStamp = result.getLastTimestamp();

				try {
					result = getProbe().getResult(this.getHost());
				} catch (Exception e) {
					Logit.LogError("RunnableProbe - run()", "Error getting runnable probe results! "
							+ this.getProbe().getName() + ", \nRunnabelProbeId: " + this.getId(), e);
					continue;
				}

				if (result == null) {
					Logit.LogError("RunnableProbe - run()", "Error getting runnable probe results! results are NULL! "
							+ this.getProbe().getName() + ", \nRunnabelProbeId: " + this.getId());
					continue;
				}

				try {
					result.checkIfTriggerd(getProbe().getTriggers());
				} catch (Exception e) {
					Logit.LogError("RunnableProbe - run()", "Error triggering runnable probe results!  "
							+ this.getProbe().getName() + ", \nRunnabelProbeId: " + this.getId());
					continue;
				}

				try {
					if (result.getLastTimestamp() == null) {
						Logit.LogError("RunnableProbe - run()",
								"Error getting runnable probe results! last timestamp is null! "
										+ this.getProbe().getName() + ", \nRunnabelProbeId: " + this.getId());
						continue;

					}

					ResultsContainer.getInstance().addResult(result);
				} catch (Exception e) {
					Logit.LogError("RunnableProbe - run()",
							"Error processing runnable probe results to results container! " + this.getId());
					continue;
				}

				try {
					RollupsContainer.getInstance().addResult(result);
				} catch (Exception e) {
					Logit.LogError("RunnableProbe - run()",
							"Error processing runnable probe results to rollups container!" + this.getId(), e);
					continue;
				}

				try {
					SLAContainer.getInstance().addToSLA(result);
				} catch (Exception e) {
					Logit.LogError("RunnableProbe - run()",
							"Error processing runnable probe results to SLA container!" + this.getId(), e);
					continue;
				}

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
}
