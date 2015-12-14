package lycus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.google.common.collect.Lists;

public class SnmpProbesBatch implements Runnable {
	private String batchId;//hostId@templateId@interval@batchUUID
	private SnmpManager snmpManager;
	private Map<String, RunnableProbe> snmpProbes;
	private Host host;
	private long interval;
	private boolean snmpError;
	private boolean isRunning;
	
	//check vars
	private TransportMapping transport;
	private Snmp snmp;

	public SnmpProbesBatch(SnmpManager SM, RunnableProbe rp) {
		this.setHost(rp.getHost());
		this.setInterval(rp.getProbe().getInterval());
		this.setSnmpProbes(new HashMap<String, RunnableProbe>());
		this.getSnmpProbes().put(rp.getRPString(), rp);
		this.batchId = this.getHost().getHostId().toString()+"@"+rp.getProbe().getTemplate_id().toString()+"@"+rp.getProbe().getInterval()+"@"+UUID.randomUUID().toString();
		this.setRunning(false);
		try {
			this.setTransport(new DefaultUdpTransportMapping());
			this.getTransport().listen();
		} catch (IOException e) {
			SysLogger.Record(new Log("Socket binding for failed for Snmp Batch:"+this.getBatchId(), LogType.Error, e));
		}
		this.setSnmp(new Snmp(this.getTransport()));
	}

	// #region Getters/Setters

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public Map<String, RunnableProbe> getSnmpProbes() {
		return snmpProbes;
	}

	public void setSnmpProbes(Map<String, RunnableProbe> snmpProbes) {
		this.snmpProbes = snmpProbes;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public SnmpManager getSnmpManager() {
		return snmpManager;
	}

	public void setSnmpManager(SnmpManager snmpManager) {
		this.snmpManager = snmpManager;
	}

	public TransportMapping getTransport() {
		return transport;
	}

	public void setTransport(TransportMapping transport) {
		this.transport = transport;
	}

	public Snmp getSnmp() {
		return snmp;
	}

	public void setSnmp(Snmp snmp) {
		this.snmp = snmp;
	}

	public boolean isSnmpError() {
		return snmpError;
	}

	public void setSnmpError(boolean snmpError) {
		this.snmpError = snmpError;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public String getBatchId() {
		return batchId;
	}

	// #endregion

	public void run() {

		try
		{
		
		if (this.getHost().isHostStatus() && this.getHost().isSnmpStatus()) {
			Host host = this.getHost();

			List<RunnableProbe> snmpProbes = new ArrayList<RunnableProbe>(this
					.getSnmpProbes().values());
			
			String rpStr = host.getHostIp().toString();
			if (rpStr.contains(
					"62.90.132.119"))
				System.out.println("TEST");
			
			if(host.getSnmpTemp()==null)
			{
				for(RunnableProbe rp:snmpProbes)
				{
				SysLogger.Record(new Log("Snmp Probe doesn't run: "+rp.getRPString()+", no SNMP template configured!",LogType.Info));
				}
			return;	
			}
			List<String> listOids = new ArrayList<String>();
			for (RunnableProbe rp : snmpProbes) {				
				SysLogger.Record(new Log("Running Probe: "+rp.getRPString()+" at Host: "+this.getHost().getHostIp()+"("+this.getHost().getName()+")...",LogType.Debug));

				if (rp.isActive()) {
					listOids.add(((SnmpProbe) rp.getProbe()).getOid()
							.toString());
				}
			}
			Map<String, String> response = null;
			if (host.getSnmpTemp().getVersion() == 2) {
				response = Net.Snmp2GETBULK(host.getHostIp(), host
						.getSnmpTemp().getPort(), host.getSnmpTemp()
						.getTimeout(), host.getSnmpTemp().getCommunityName(),
						listOids);
//				response = Net.Snmp2GETBULK(host.getHostIp(), host
//						.getSnmpTemp().getPort(), host.getSnmpTemp()
//						.getTimeout(), host.getSnmpTemp().getCommunityName(),
//						listOids,this.getTransport(),this.getSnmp());
			} else if (host.getSnmpTemp().getVersion() == 3) {
				response = Net.Snmp3GETBULK(host.getHostIp(), host
						.getSnmpTemp().getPort(), host.getSnmpTemp()
						.getTimeout(), host.getSnmpTemp().getUserName(), host
						.getSnmpTemp().getAuthPass(), host.getSnmpTemp()
						.getAlgo(), host.getSnmpTemp().getCryptPass(), host
						.getSnmpTemp().getCryptType(), listOids);
			
				
//				response = Net.Snmp3GETBULK(host.getHostIp(), host
//						.getSnmpTemp().getPort(), host.getSnmpTemp()
//						.getTimeout(), host.getSnmpTemp().getUserName(), host
//						.getSnmpTemp().getAuthPass(), host.getSnmpTemp()
//						.getAlgo(), host.getSnmpTemp().getCryptPass(), host
//						.getSnmpTemp().getCryptType(), listOids,this.getTransport(),this.getSnmp());
			}
			if (response == null) {
				for (RunnableProbe runnableProbe : snmpProbes) {
					SysLogger.Record(new Log("Unable Probing Runnable Probe of: "+runnableProbe.getRPString(),LogType.Warn));
				}
				SysLogger.Record(new Log("Failed running  snmp batch - host: "+this.getHost().getHostIp()+", snmp template:"+this.getHost().getSnmpTemp().toString(), LogType.Info));
				return;
//				switch (Net.checkHostSnmpActive(host)) {
//				case "host problem":
//					SysLogger.Record(new Log(
//							"Snmp Batch Failed - caused By Host: "
//									+ this.getHost().toString()
//									+ " didn't responsed! ", LogType.Debug));
//					break;
//				case "snmp problem":
//					SysLogger.Record(new Log(
//							"Snmp Batch Failed - caused By Snmp Template: "
//									+ this.getHost().getSnmpTemp().toString(),
//							LogType.Debug));
//					this.setSnmpError(true);
//					break;
//				case "no problem":
//					SysLogger.Record(new Log(
//							"Snmp Batch Failed - caused By Unknown, SNMP Batch:"
//									+ this.toString(),
//							LogType.Error));
//					break;
//				}
			} else {
				long resultsTimestamp=System.currentTimeMillis();
//				if(this.isSnmpError())
//					this.setSnmpError(false);
				for (RunnableProbe _rp : snmpProbes) {
					
					
					if (_rp.isActive()) {
//						String rpStr = this.getHost().getHostId().toString();
						if (rpStr.contains(
								"788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_10e61538-b4e1-44c6-aa12-b81ef6a5528d"))
							System.out.println("TEST");

						SnmpProbe snmpProbe=(SnmpProbe) _rp.getProbe();
						String _value = response.get(snmpProbe.getOid().toString());
						ArrayList<Object> results=new ArrayList<Object>();
						if (_value != null) {
							results.add(resultsTimestamp);
							results.add(_value);
							SysLogger.Record(new Log("Running Probe: "+_rp.getRPString()+" at Host: "+this.getHost().getHostIp()+"("+this.getHost().getName()+")"+", Results: "+results+" ...",LogType.Debug));
						} else {
							SysLogger.Record(new Log("Unable to get results for SNMP Probe: "+_rp.getRPString()+" oid issue ("+snmpProbe.getOid()+")",LogType.Info));
							// wrong oid insert probe issues
							results.add(resultsTimestamp);
							results.add("WRONG_OID");
						}
						try {
														_rp.getResult().acceptResults(results);
						} catch (Exception e) {
							SysLogger.Record(new Log("Unable to set Runnable Probe results from Check "+this.getSnmpProbes(),LogType.Error,e));
						}
					}
				}
			}
		}
		}
		catch(Throwable th)
		{
			SysLogger.Record(new Log("Error running snmp probes batch:"+this.getBatchId(),LogType.Error));
		}
	}

	public void deleteSnmpProbe(RunnableProbe rp) {
	this.getSnmpProbes().remove(rp.getRPString());
	}

	public void addSnmpProbe(RunnableProbe rp) {
		this.getSnmpProbes().put(rp.getRPString(), rp);
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("Snmp Probes Batch: "
				+ this.getBatchId() + ":\n");
		for (RunnableProbe p : this.getSnmpProbes().values()) {
			s.append(p.toString()).append("\n");
		}
		return s.toString();
	}
	 @Override
	    protected void finalize() throws Throwable {
	        try{
	        	if (this.getSnmp() != null) {
	        			this.getSnmp().close();
	        	}
	        	if (this.getTransport() != null) {
						this.getTransport().close();
				}
	        }catch(Throwable t){
	            SysLogger.Record(new Log("Memory leak, unable to close network connection!",LogType.Error));
	        	throw t;
	        }finally{
	            super.finalize();
	        }
	      
	    }
}
