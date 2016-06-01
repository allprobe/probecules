package lycus;

import java.util.concurrent.ScheduledFuture;

import GlobalConstants.Enums.DiscoveryElementType;
import GlobalConstants.ProbeTypes;
import Probes.BaseProbe;
import Probes.DiscoveryProbe;
import Probes.HttpProbe;
import Probes.IcmpProbe;
import Probes.NicProbe;
import Probes.PortProbe;
import Probes.RBLProbe;
import Probes.SnmpProbe;
import Probes.DiskProbe;
import Results.BaseResult;
import Results.DiscoveryResult;
import Rollups.RollupsContainer;
import Utils.Logit;

public class RunnableProbe implements Runnable {
	private Host host;
	private BaseProbe probe;
	private boolean isActive;
	private boolean isRunning;

	public RunnableProbe(Host host, BaseProbe probe) {
		this.setHost(host);
		this.setProbe(probe);
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
			return ProbeTypes.DISCBANDWIDTH;
		if (getProbe() instanceof DiskProbe)
			return ProbeTypes.DISCDISK;
		// if (getProbe() instanceof BaseElement)
		// return ProbeTypes.DISCOVERYELEMENT;
		return null;
	}

	public void run() {

		if(!this.getProbe().isActive())
			return;

		// TODO: eliminate this from factory - check with Oren

		BaseResult result = null;

		String rpStr = this.getId();
		if (rpStr.contains(
				"eb62f236-4b53-4014-88fb-cbb72a77745d@7352a46f-5189-428c-b4c0-fb98dedd10b1@discovery_c7629ed7-d0ec-4eca-8742-06344954434e")&& ((DiscoveryProbe)this.getProbe()).getType()==DiscoveryElementType.bw)
			Logit.LogDebug("BREAKPOINT - RunnableProbe");

		String rpStr2 = this.getHost().getHostId().toString() + "@" + getProbe().getProbe_id();
		if (rpStr.contains("ff00ff2c-0f40-4616-9ac4-a71447b22431@http_83b9b614-b210-45ce-942b-cf45114afe01"))
			Logit.LogDebug("BREAKPOINT - RunnableProbe");

		if(this.getProbeType().equals(ProbeTypes.DISCBANDWIDTH))
			Logit.LogDebug("BREAKPOINT");
		
		try {
			result = getProbe().getResult(this.getHost());
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - run()", "Error getting runnable probe results! " + this.getId());
			return;
		}
		
		if (result == null) {
			Logit.LogError("RunnableProbe - run()", "Error getting runnable probe results! " + this.getId());
			return;
		}
		
		try {
			result.checkIfTriggerd(getProbe().getTriggers());
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - run()", "Error triggering runnable probe results!  " + this.getId());
			return;
		}
		
		try {
			if (result.getLastTimestamp() == null) {
				Logit.LogError("RunnableProbe - run()",
						"Error getting runnable probe results! last timestamp is null! " + this.getId());
				return;

			}
			if (this.getProbeType() == ProbeTypes.DISCOVERY)
				ElementsContainer.getInstance().addResult((DiscoveryResult) result);
			else
				ResultsContainer.getInstance().addResult(result);
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - run()",
					"Error processing runnable probe results to results container! " + this.getId());
			return;
		}
		
		try {
			RollupsContainer.getInstance().addResult(result);
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - run()",
					"Error processing runnable probe results to rollups container!" + this.getId(), e);
			return;
		}
		Logit.LogInfo("Running Probe: " + this.getId() + " at Host: " + this.getHost().getHostIp() + "("
				+ this.getHost().getName() + ")" + ", Results: " + result + " ...");
	}

	// returns this.isRunning();
	// public boolean start() {
	// if (this.getProbeType() == ProbeTypes.SNMP) {
	// this.setRunning(true);
	// return this.getProbe().getUser().getSnmpManager().startProbe(this);
	// }
	// // if (this.getProbeType() == ProbeTypes.DISCOVERYELEMENT) {
	// // this.setRunning(true);
	// // return this.getProbe().getUser().getSnmpManager().startProbe(this);
	// // }
	// boolean state = RunInnerProbesChecks.addRegularRP(this);
	// if (state)
	// this.setRunning(true);
	// return state;
	// }

	// returns false if any issue during stop process;
//	public boolean stop() {
//		try {
//			if (!this.isRunning)
//				return true;
//
//			ScheduledFuture<?> rpThread = null;
//			switch (this.getProbeType()) {
//			// case ICMP:
//			// rpThread =
//			// RunInnerProbesChecks.getPingerFutureMap().remove(this.getId());
//			// break;
//			// case PORT:
//			// rpThread =
//			// RunInnerProbesChecks.getPorterFutureMap().remove(this.getId());
//			// break;
//			// case HTTP:
//			// rpThread =
//			// RunInnerProbesChecks.getWeberFutureMap().remove(this.getId());
//			// break;
//			case SNMP:
//				this.setRunning(false);
//				return this.getProbe().getUser().getSnmpManager().stopProbe(this);
//			// case RBL:
//			// rpThread =
//			// RunInnerProbesChecks.getRblProbeFutureMap().remove(this.getId());
//			}
//			if (rpThread == null) {
//				Logit.LogError("RunnableProbe - stop()",
//						"RunnableProbe: " + this.getId() + " running, but doesn't exists in thread pool!");
//				return false;
//			} else {
//				rpThread.cancel(false);
//			}
//
//			return true;
//		} catch (Exception e) {
//			return false;
//		}
//	}

	// public String toString() {
	// return getId();
	// }

	// // Roi handle roleups
	// public boolean changeRunnableProbeInterval(Long interval) {
	// try {
	// this.stop();
	// this.getProbe().setInterval(interval);
	// } catch (Exception e) {
	// Logit.LogError("RunnableProbe - changeRunnableProbeInterval()",
	// "Could not change interval to " + interval + ", " + this.getId());
	// return false;
	// }
	// this.start();
	// Logit.LogCheck("Interval for " + this.getId() + " was changed to " +
	// interval);
	// return true;
	// }

}
