package lycus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import lycus.Probes.PingerProbe;
import lycus.Probes.PorterProbe;
import lycus.Probes.Probe;
import lycus.Probes.RBLProbe;
import lycus.Probes.SnmpProbe;
import lycus.Probes.WeberProbe;

public class RunnableProbe implements Runnable {
	private Host host;
	private Probe probe;
	private boolean isActive;
	private boolean isRunning;
	private RunnableProbeResults results;

	public RunnableProbe(Host host, Probe probe) {
		this.setHost(host);
		this.setProbe(probe);
		this.setActive(true);
		if (this.getProbeType() == null)
		{
			// Must be handled by Roi
			return;
		}
		
		switch(this.getProbeType())
		{
			case PING: results=new RunnablePingerProbeResults(this);
			break;
			case PORT: results=new RunnablePorterProbeResults(this);
			break;
			case WEB: results=new RunnableWeberProbeResults(this);
			break;
			case SNMP: results=new RunnableSnmpProbeResults(this);
			break;
			case SNMPv1: results=new RunnableSnmpProbeResults(this);
			break;
			case RBL: results=new RunnableRblProbeResults(this);
			break;
			case TRACEROUTE:results=new RunnableTracerouteProbeResults(this);
			break;
			case DISCOVERY:results=new RunnableDiscoveryProbeResults(this);
			break;
		}
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public Probe getProbe() {
		return probe;
	}

	public void setProbe(Probe probe) {
		this.probe = probe;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public RunnableProbeResults getResult() {
		return results;
	}

	public void setResult(RunnableProbeResults results) {
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
		return null;
	}

	
	public void run() {
		
		ArrayList<Object> result = null;
		
		String rpStr = this.getRPString();
		if (rpStr.contains(
				"21fd2bec-bca4-4cf7-9812-6e78bd2a1809@d934aa3b-f703-4d4b-99c6-66b470c782f2@inner_eac09267-4050-4b11-b23d-5c0c05a397ed"))
			System.out.println("BREAKPOINT");
				
		try{
		result = getProbe().Check(this.getHost());
		}
		catch(Exception e)
		{
			SysLogger.Record(new Log("Unable Probing Runnable Probe of: "+this.getRPString(),LogType.Error,e));
		}
		try {
			this.getResult().acceptResults(result);
		} catch (Exception e) {
			SysLogger.Record(new Log("Unable to set Runnable Probe results from Check "+this.getRPString(),LogType.Error,e));
		}
		SysLogger.Record(new Log("Running Probe: "+this.getRPString()+" at Host: "+this.getHost().getHostIp()+"("+this.getHost().getName()+")"+", Results: "+result+" ...",LogType.Debug));
	}

	// returns this.isRunning();
	public boolean start() {
		boolean state= RunInnerProbesChecks.addRegularRP(this);
		if(state)
			this.setRunning(true);
		return state;
	}

	// returns this.isRunning();
	public boolean stop() throws Exception {
		if (!this.isRunning)
			return false;
		ScheduledFuture<?> rpThread;
		if (this.getProbe() instanceof PingerProbe) {
			rpThread = RunInnerProbesChecks.getPingerFutureMap().remove(this.getRPString());
			if (rpThread == null) {
				SysLogger.Record(
						new Log("RunnableProbe: " + this.getRPString() + " running, but doesn't exists in thread pool!",
								LogType.Error));
				throw new Exception("RunnableProbe.stop() at " + this.getRPString() + " failed!");
			} else
				rpThread.cancel(false);

			return true;
		} else if (this.getProbe() instanceof PorterProbe) {
			rpThread = RunInnerProbesChecks.getPorterFutureMap().remove(this.getRPString());
			if (rpThread == null) {
				SysLogger.Record(
						new Log("RunnableProbe: " + this.getRPString() + " running, but doesn't exists in thread pool!",
								LogType.Error));
				throw new Exception("RunnableProbe.stop() at " + this.getRPString() + " failed!");
			} else
				rpThread.cancel(false);
			return true;
		} else if (this.getProbe() instanceof WeberProbe) {
			rpThread = RunInnerProbesChecks.getWeberFutureMap().remove(this.getRPString());
			if (rpThread == null) {
				SysLogger.Record(
						new Log("RunnableProbe: " + this.getRPString() + " running, but doesn't exists in thread pool!",
								LogType.Error));
				throw new Exception("RunnableProbe.stop() at " + this.getRPString() + " failed!");
			} else
				rpThread.cancel(false);
			return true;
		} else if (this.getProbe() instanceof SnmpProbe && this.getHost().getSnmpTemp().getVersion() == 1) {
			rpThread = RunInnerProbesChecks.getSnmpProbeFutureMap().remove(this.getRPString());
			if (rpThread == null) {
				SysLogger.Record(
						new Log("RunnableProbe: " + this.getRPString() + " running, but doesn't exists in thread pool!",
								LogType.Error));
				throw new Exception("RunnableProbe.stop() at " + this.getRPString() + " failed!");
			} else
				rpThread.cancel(false);

			return true;
		} else if (this.getProbe() instanceof RBLProbe) {
			rpThread = RunInnerProbesChecks.getRblProbeFutureMap().remove(this.getRPString());
			if (rpThread == null) {
				SysLogger.Record(
						new Log("RunnableProbe: " + this.getRPString() + " running, but doesn't exists in thread pool!",
								LogType.Error));
				throw new Exception("RunnableProbe.stop() at " + this.getRPString() + " failed!");
			} else
				rpThread.cancel(false);
			return true;
		}
		return true;
	}

//	public void insertResult(ArrayList<Object> results) {
//		if (result == null) {
//			SysLogger.Record(new Log("Problem With Probe: " + this.getRPString() + " Results=null", LogType.Info));
//			return;
//		}
//		switch(this.getProbeType())
//		{
//		case PING:((PingerProbe)this.getProbe()).insertProbeResults(results);;
//		break;
//		case PORT:this.insertPorterPorbeResults();
//		break;
//		case WEB:this.insertPingerPorbeResults();
//		break;
//		case RBL:this.insertPingerPorbeResults();
//		break;
//		case SNMP:this.insertPingerPorbeResults();
//		break;
//		
//		}
//		this.getResult().add(result);
//		String stringResults;
//		stringResults = GeneralFunctions.valuesFormat(result);
//		SysLogger.Record(new Log("Probe: " + this.getRPString() + " Results: " + stringResults, LogType.Debug));
//		ApiInterface.InsertProbeHistory(this, stringResults);
//
//		// TODO Here Check Triggers of Probe
//
//	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(this.getProbe().getTemplate_id().toString()).append("@");
		s.append(this.getHost().getHostId().toString()).append("@");
		s.append(this.getProbe().getProbe_id());
		return s.toString();
	}
	
	// Roi handle roleups
	public boolean changeRunnableProbeInterval(Long interval){
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
