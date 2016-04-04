package lycus;

import java.util.concurrent.ScheduledFuture;
import lycus.GlobalConstants.LogType;
import lycus.GlobalConstants.ProbeTypes;
import lycus.Elements.BaseElement;
import lycus.Probes.DiscoveryProbe;
import lycus.Probes.PingerProbe;
import lycus.Probes.PorterProbe;
import lycus.Probes.BaseProbe;
import lycus.Probes.RBLProbe;
import lycus.Probes.SnmpProbe;
import lycus.Probes.WeberProbe;
import lycus.Results.BaseResult;
import lycus.Rollups.RollupsContainer;
import lycus.Utils.Logit;

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
		if (getProbe() instanceof PingerProbe)
			return ProbeTypes.ICMP;
		if (getProbe() instanceof PorterProbe)
			return ProbeTypes.PORT;
		if (getProbe() instanceof WeberProbe)
			return ProbeTypes.HTTP;
		if ((getProbe() instanceof SnmpProbe))
			return ProbeTypes.SNMP;
		if (getProbe() instanceof RBLProbe)
			return ProbeTypes.RBL;
		if (getProbe() instanceof DiscoveryProbe)
			return ProbeTypes.DISCOVERY;
//		if (getProbe() instanceof BaseElement)
//			return ProbeTypes.DISCOVERYELEMENT;
		return null;
	}

	public void run() {
		//TODO: eliminate this from factory
	
		BaseResult result = null;

		String rpStr = this.getHost().getHostId().toString()+"@"+getProbe().getProbe_id();
		if (rpStr.contains(
				"ff00ff2c-0f40-4616-9ac4-a71447b22431@inner_33695a83-654d-4177-b90d-0a89c5f0120d"))
			System.out.println("BREAKPOINT - RunnableProbe");
		
		
		try {
			result = getProbe().getResult(this.getHost());
			if(result==null)
			{
				Logit.LogError("RunnableProbe - run()", "Error getting runnable probe results!");
				return;
			}
			result.checkIfTriggerd(getProbe().getTriggers());
			ResultsContainer.getInstance().addResult(result);
			RollupsContainer.getInstance().addResult(result);
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - run()", "Unable Probing Runnable Probe of: " + this.getId() + "\n" + e.getMessage());
		System.err.println("test");
		}
		Logit.LogInfo("Running Probe: " + this.getId() + " at Host: " + this.getHost().getHostIp()
				+ "(" + this.getHost().getName() + ")" + ", Results: " + result + " ...");
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
				Logit.LogError("RunnableProbe - stop()", "RunnableProbe: " + this.getId() + " running, but doesn't exists in thread pool!");
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
			Logit.LogError("RunnableProbe - changeRunnableProbeInterval()", "Could not change interval to " + interval + ", " + this.getId());
			return false;
		}
		this.start();
		Logit.LogDebug("Interval for " + this.getId() + " was changed to " + interval);
		return true;
	}

}
