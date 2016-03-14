package lycus;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;

import lycus.GlobalConstants.LogType;
import lycus.GlobalConstants.ProbeTypes;
import lycus.Elements.BaseElement;
import lycus.Elements.NicElement;
import lycus.Probes.DiscoveryProbe;
import lycus.Probes.PingerProbe;
import lycus.Probes.PorterProbe;
import lycus.Probes.BaseProbe;
import lycus.Probes.RBLProbe;
import lycus.Probes.SnmpProbe;
import lycus.Probes.WeberProbe;
import lycus.Results.BaseResults;
import lycus.Results.DiscoveryResults;
import lycus.Results.NicResults;
import lycus.Results.PingerResults;
import lycus.Results.PorterResults;
import lycus.Results.RblResults;
import lycus.Results.SnmpResults;
import lycus.Results.TraceRouteResults;
import lycus.Results.WeberResults;

public class RunnableProbe implements Runnable {
	private Host host;
	private BaseProbe probe;
	private boolean isActive;
	private boolean isRunning;
	private BaseResults results;

	public RunnableProbe(Host host, BaseProbe probe) {
		this.setHost(host);
		this.setProbe(probe);
		this.setActive(true);
		if (this.getProbeType() == null) {
			// Must be handled by Roi
			return;
		}

		switch (this.getProbeType()) {
		case PING:
			results = new PingerResults(this);
			break;
		case PORT:
			results = new PorterResults(this);
			break;
		case WEB:
			results = new WeberResults(this);
			break;
		case SNMP:
			results = new SnmpResults(this);
			break;
		case SNMPv1:
			results = new SnmpResults(this);
			break;
		case RBL:
			results = new RblResults(this);
			break;
		case TRACEROUTE:
			results = new TraceRouteResults(this);
			break;
		case DISCOVERY:
			results = new DiscoveryResults(this);
			break;
		case DISCOVERYELEMENT:
			if (probe instanceof NicElement)
				results = new NicResults(this);
			break;
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

	public BaseResults getResult() {
		return results;
	}

	public void setResult(BaseResults results) {
		this.results = results;
	}

	private boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public String getRPString() {
		return this.getProbe().getTemplate_id().toString() + "@" + this.getHost().getHostId().toString() + "@"
				+ this.getProbe().getProbe_id();
	}

	public ProbeTypes getProbeType() {
		if (getProbe() instanceof PingerProbe)
			return ProbeTypes.PING;
		if (getProbe() instanceof PorterProbe)
			return ProbeTypes.PORT;
		if (getProbe() instanceof WeberProbe)
			return ProbeTypes.WEB;
		if ((getProbe() instanceof SnmpProbe))
			return ProbeTypes.SNMP;
		if (getProbe() instanceof RBLProbe)
			return ProbeTypes.RBL;
		if (getProbe() instanceof DiscoveryProbe)
			return ProbeTypes.DISCOVERY;
		if (getProbe() instanceof BaseElement)
			return ProbeTypes.DISCOVERYELEMENT;
		return null;
	}

	public void run() {

		ArrayList<Object> result = null;

		String rpStr = this.getRPString();
		if (rpStr.contains(
				"e8b03d1e-48c8-4bd1-abeb-7e9a96a4cae4@ae1981c3-c157-4ce2-9086-11e869d4a344@icmp_a0285ff7-748f-4d79-96e5-72c8eaed384e"))
			System.out.println("BREAKPOINT");

		try {
			result = getProbe().Check(this.getHost());
		} catch (Exception e) {
			SysLogger.Record(new Log("Unable Probing Runnable Probe of: " + this.getRPString(), LogType.Error, e));
		}
		try {
			this.getResult().acceptResults(result);
		} catch (Exception e) {
			SysLogger.Record(
					new Log("Unable to set Runnable Probe results from Check " + this.getRPString(), LogType.Error, e));
		}
		SysLogger.Record(new Log("Running Probe: " + this.getRPString() + " at Host: " + this.getHost().getHostIp()
				+ "(" + this.getHost().getName() + ")" + ", Results: " + result + " ...", LogType.Debug));
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
			case PING:
				rpThread = RunInnerProbesChecks.getPingerFutureMap().remove(this.getRPString());
			case PORT:
				rpThread = RunInnerProbesChecks.getPorterFutureMap().remove(this.getRPString());
			case WEB:
				rpThread = RunInnerProbesChecks.getWeberFutureMap().remove(this.getRPString());
			case SNMP:
				this.setRunning(false);
				return this.getProbe().getUser().getSnmpManager().stopProbe(this);
			case RBL:
				rpThread = RunInnerProbesChecks.getRblProbeFutureMap().remove(this.getRPString());
			}
			if (rpThread == null) {
				SysLogger.Record(
						new Log("RunnableProbe: " + this.getRPString() + " running, but doesn't exists in thread pool!",
								LogType.Error));
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
			return false;
		}
		this.start();
		return true;
	}

}
