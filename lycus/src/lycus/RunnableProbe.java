package lycus;

import java.util.concurrent.ScheduledFuture;

import Elements.BaseElement;
import GlobalConstants.LogType;
import GlobalConstants.ProbeTypes;
import Probes.BaseProbe;
import Probes.DiscoveryProbe;
import Probes.HttpProbe;
import Probes.IcmpProbe;
import Probes.PortProbe;
import Probes.RBLProbe;
import Probes.SnmpProbe;
import Results.BaseResult;
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
		// if (getProbe() instanceof BaseElement)
		// return ProbeTypes.DISCOVERYELEMENT;
		return null;
	}

	public void run() {
		// TODO: eliminate this from factory - check with Oren

		BaseResult result = null;

		String rpStr = this.getHost().getHostId().toString() + "@" + getProbe().getProbe_id();
		if (rpStr.contains("d934aa3b-f703-4d4b-99c6-66b470c782f2@http_52c1b1ba-3842-4448-9c85-a1ae6dd52729"))
			Logit.LogDebug("BREAKPOINT - RunnableProbe");

		try {
			result = getProbe().getResult(this.getHost());
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - run()", "Error getting runnable probe results!");
			return;
		}
		if (result == null) {
			Logit.LogError("RunnableProbe - run()", "Error getting runnable probe results!");
			return;
		}
		try {
			result.checkIfTriggerd(getProbe().getTriggers());
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - run()", "Error triggering runnable probe results! "+this.getId());
			return;
		}
		try {
			ResultsContainer.getInstance().addResult(result);
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - run()", "Error processing runnable probe results to results container!");
			return;
		}
		try {
			RollupsContainer.getInstance().addResult(result);
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - run()", "Error processing runnable probe results to results container!");
			return;
//			if (this.getId().split("@")[2].equals("null"))
//				Logit.LogDebug("BREAKPOINT");
//			Logit.LogError("RunnableProbe - run()",
//					"Unable Probing Runnable Probe of: " + this.getId() + "\n" + e.getMessage());
		}
		Logit.LogInfo("Running Probe: " + this.getId() + " at Host: " + this.getHost().getHostIp() + "("
				+ this.getHost().getName() + ")" + ", Results: " + result + " ...");
	}

	// returns this.isRunning();
	public boolean start() {
		if (this.getProbeType() == ProbeTypes.SNMP) {
			this.setRunning(true);
			return this.getProbe().getUser().getSnmpManager().startProbe(this);
		}
		if (this.getProbeType() == ProbeTypes.DISCOVERYELEMENT) {
			this.setRunning(true);
			return this.getProbe().getUser().getSnmpManager().startProbe(this);
		}
		boolean state = RunInnerProbesChecks.addRegularRP(this);
		if (state)
			this.setRunning(true);
		return state;
	}

	// returns false if any issue during stop process;
	public boolean stop() {
		try {
			if (!this.isRunning)
				return true;

			ScheduledFuture<?> rpThread = null;
			switch (this.getProbeType()) {
			case ICMP:
				rpThread = RunInnerProbesChecks.getPingerFutureMap().remove(this.getId());
			case PORT:
				rpThread = RunInnerProbesChecks.getPorterFutureMap().remove(this.getId());
			case HTTP:
				rpThread = RunInnerProbesChecks.getWeberFutureMap().remove(this.getId());
			case SNMP:
				this.setRunning(false);
				return this.getProbe().getUser().getSnmpManager().stopProbe(this);
			case RBL:
				rpThread = RunInnerProbesChecks.getRblProbeFutureMap().remove(this.getId());
			}
			if (rpThread == null) {
				Logit.LogError("RunnableProbe - stop()",
						"RunnableProbe: " + this.getId() + " running, but doesn't exists in thread pool!");
				return false;
			} else
				rpThread.cancel(false);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(this.getProbe().getTemplate_id().toString()).append("@");
		s.append(this.getHost().getHostId().toString()).append("@");
		s.append(this.getProbe().getProbe_id());
		return s.toString();
	}

	// Roi handle roleups
	public boolean changeRunnableProbeInterval(Long interval) {
		try {
			this.stop();
			this.getProbe().setInterval(interval);
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - changeRunnableProbeInterval()",
					"Could not change interval to " + interval + ", " + this.getId());
			return false;
		}
		this.start();
		Logit.LogCheck("Interval for " + this.getId() + " was changed to " + interval);
		return true;
	}

}
