package NetConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snmp4j.smi.OID;

import lycus.Host;
import lycus.SnmpTemplate;
import lycus.GlobalConstants.Constants;
import lycus.Interfaces.INetResults;
import lycus.Probes.BaseProbe;
import lycus.Probes.PingerProbe;
import lycus.Probes.PorterProbe;
import lycus.Probes.RBLProbe;
import lycus.Probes.SnmpProbe;
import lycus.Probes.WeberProbe;
import lycus.Results.PingResult;
import lycus.Results.PortResult;
import lycus.Results.RblResult;
import lycus.Results.SnmpResult;
import lycus.Results.WebResult;

public class NetResults implements INetResults{
	private static NetResults netResults = null;

	protected NetResults() {
	}

	public static NetResults getInstanece() {
		if (netResults == null)
			netResults = new NetResults();
		return netResults;
	}

	@Override
	public PingResult getPingResult(Host host,PingerProbe probe) {
		ArrayList<Object> rawResults = Net.Pinger(host.getHostIp(), probe.getCount(),probe.getBytes(), probe.getTimeout());
		if(rawResults==null ||rawResults.size()==0)
			return null;
		
		long timestamp=(long)rawResults.get(0);
		int packetLoss=(int)rawResults.get(1);
		double rtt=(double)rawResults.get(2);
		int ttl=(int)rawResults.get(3);
		
		
		PingResult pingerResult = new PingResult(getRunnableProbeId(probe, host),timestamp,packetLoss,rtt,ttl);
		
		return pingerResult;
	}

	@Override
	public PortResult getPortResult(Host host,PorterProbe probe) {
		ArrayList<Object> rawResults=null;
		switch(probe.getProto())
		{
		case "TCP":rawResults = Net.TcpPorter(host.getHostIp(), probe.getPort(), probe.getTimeout());
		break;
		case "UDP":rawResults = Net.UdpPorter(host.getHostIp(), probe.getPort(), probe.getTimeout(), probe.getSendString(), probe.getReceiveString());
		break;
		}
		if(rawResults==null ||rawResults.size()==0)
			return null;
		
		long timestamp=(long)rawResults.get(0);
		boolean portState=(boolean)rawResults.get(1);
		long responseTime=(long)rawResults.get(2);
		
		PortResult porterResult = new PortResult(getRunnableProbeId(probe, host),timestamp,portState,responseTime);
		
		return porterResult;
	}

	@Override
	public WebResult getWebResult(Host host,WeberProbe probe) {
		ArrayList<Object> rawResults = Net.Weber(probe.getUrl(), probe.getHttpRequestType(),probe.getAuthUsername(),probe.getAuthPassword(), probe.getTimeout());
		if(rawResults==null ||rawResults.size()==0)
			return null;
		
		long timestamp=(long)rawResults.get(0);
		int responseCode=(int)rawResults.get(1);
		long responseTime=(long)rawResults.get(2);
		long responseSize=(long)rawResults.get(3);
		
		WebResult weberResult = new WebResult(getRunnableProbeId(probe, host),timestamp,responseCode,responseTime,responseSize);
		
		return weberResult;
	}

	@Override
	public RblResult getRblResult(Host host,RBLProbe probe) {
		ArrayList<Object> rawResults = Net.RBLCheck(host.getHostIp(), probe.getRBL());
		if(rawResults==null ||rawResults.size()==0)
			return null;
		
		long timestamp=(long)rawResults.get(0);
		boolean isListed=(boolean)rawResults.get(1);
		
		RblResult rblResult = new RblResult(getRunnableProbeId(probe, host),timestamp,isListed);
		
		return rblResult;
	}

	@Override
	public List<SnmpResult> getSnmpResults(Host host,List<SnmpProbe> snmpProbes) {
		
		List<SnmpResult> allResults=new ArrayList<SnmpResult>();
		SnmpTemplate snmpTemplate=host.getSnmpTemp();
		
		HashMap<String, OID> probesOids=new HashMap<String,OID>();
		for(SnmpProbe snmpProbe:snmpProbes)
			probesOids.put(getRunnableProbeId(snmpProbe, host), snmpProbe.getOid());
		
		Map<String,String> rawResults = null;

		long timestamp=System.currentTimeMillis();
		switch(snmpTemplate.getVersion())
		{
		case 2:rawResults=Net.Snmp2GETBULK(host.getHostIp(), snmpTemplate.getPort(), snmpTemplate.getTimeout(), snmpTemplate.getCommunityName(), probesOids.values());
			break;
		case 3:rawResults=Net.Snmp3GETBULK(host.getHostIp(), snmpTemplate.getPort(),snmpTemplate.getTimeout(),snmpTemplate.getUserName(),snmpTemplate.getAuthPass(),snmpTemplate.getAlgo(),snmpTemplate.getCryptPass(),snmpTemplate.getCryptType(),probesOids.values());
		break;
		}
		if(rawResults==null ||rawResults.size()==0)
			return null;
		
		for(SnmpProbe snmpProbe:snmpProbes)
		{
			String stringResult=rawResults.get((snmpProbe).getOid().toString());
			SnmpResult snmpResult;
			if(stringResult==null)
				snmpResult = new SnmpResult(getRunnableProbeId(snmpProbe, host),timestamp,Constants.WRONG_OID);
			else
				snmpResult = new SnmpResult(getRunnableProbeId(snmpProbe, host),timestamp,stringResult);
			allResults.add(snmpResult);
		}
		
		return allResults;
	}
	private String getRunnableProbeId(BaseProbe probe,Host host)
	{
    	return probe.getTemplate_id().toString() + "@" + host.getHostId().toString() + "@" + probe.getProbe_id();
	}
}
