package NetConnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.snmp4j.smi.OID;

import lycus.Host;
import lycus.SnmpTemplate;
import lycus.Elements.BaseElement;
import lycus.Elements.NicElement;
import lycus.GlobalConstants.Constants;
import lycus.GlobalConstants.Enums;
import lycus.GlobalConstants.Enums.ElementChange;
import lycus.GlobalConstants.Enums.HostType;
import lycus.Interfaces.INetResults;
import lycus.Probes.BaseProbe;
import lycus.Probes.DiscoveryProbe;
import lycus.Probes.NicProbe;
import lycus.Probes.PingerProbe;
import lycus.Probes.PorterProbe;
import lycus.Probes.RBLProbe;
import lycus.Probes.SnmpProbe;
import lycus.Probes.WeberProbe;
import lycus.Results.BaseResult;
import lycus.Results.DiscoveryResult;
import lycus.Results.NicResult;
import lycus.Results.PingResult;
import lycus.Results.PortResult;
import lycus.Results.RblResult;
import lycus.Results.SnmpResult;
import lycus.Results.WebResult;
import lycus.Utils.GeneralFunctions;
import lycus.Utils.Logit;

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
	
	@Override
	public NicResult getNicResult(Host host,NicProbe probe) {
		
		SnmpTemplate snmpTemplate=host.getSnmpTemp();
		
		Set<OID> nicOids=new HashSet<OID>();
		nicOids.add(probe.getIfinoctetsOID());
		nicOids.add(probe.getIfoutoctetsOID());
		
		Map<String,String> rawResults = null;

		long timestamp=System.currentTimeMillis();
		switch(snmpTemplate.getVersion())
		{
		case 2:rawResults=Net.Snmp2GETBULK(host.getHostIp(), snmpTemplate.getPort(), snmpTemplate.getTimeout(), snmpTemplate.getCommunityName(), nicOids);
			break;
		case 3:rawResults=Net.Snmp3GETBULK(host.getHostIp(), snmpTemplate.getPort(),snmpTemplate.getTimeout(),snmpTemplate.getUserName(),snmpTemplate.getAuthPass(),snmpTemplate.getAlgo(),snmpTemplate.getCryptPass(),snmpTemplate.getCryptType(),nicOids);
		break;
		}
		
		if(rawResults==null ||rawResults.size()==0)
			return null;
		
		long interfaceInOctets=Long.parseLong(rawResults.get(probe.getIfinoctetsOID().toString()));
		long interfaceOutOctets=Long.parseLong(rawResults.get(probe.getIfoutoctetsOID().toString()));
		
		NicResult nicResut=new NicResult(getRunnableProbeId(probe, host),timestamp,interfaceInOctets,interfaceOutOctets);

		return nicResut;
	}
	private String getRunnableProbeId(BaseProbe probe,Host host)
	{
    	return probe.getTemplate_id().toString() + "@" + host.getHostId().toString() + "@" + probe.getProbe_id();
	}

	@Override
	public DiscoveryResult getDiscoveryResult(Host host, DiscoveryProbe probe) {

		DiscoveryResult discoveryResult = null;
		
		String walkOid;
		
		long timestamp=System.currentTimeMillis();
		List<BaseElement> elements=null;
		switch(probe.getType())
		{
		case nics:
			elements=this.getNicElements(host);
			break;
		case disks:
			elements=this.getDiskElements(host);
			break;
		}
		if(elements==null)
			return null;
		// TODO: handle elements.size()==0, might be a valid result ask Ran
		discoveryResult=new DiscoveryResult(getRunnableProbeId(probe, host),timestamp,elements);
		return discoveryResult;
	}
	private List<BaseElement> getDiskElements(Host host) {
		// TODO NetResults.getDiskElements()
		return null;
	}
	private HostType getHostType(String string) {
		if (string.contains("Linux"))
			return Enums.HostType.Linux;
		if (string.contains("Windows"))
			return Enums.HostType.Windows;
		return null;
	}
	private List<BaseElement> getNicElements(Host h) {
		
		long checkTime;
		List<BaseElement> nicElements=new ArrayList<BaseElement>(); // ArrayList<NicElement>
		
		Map<String, String> ifDescrResults = null;
		Map<String, String> sysDescrResults = null;
		
		int snmpVersion = h.getSnmpTemp().getVersion();
		if (snmpVersion == 2) {
			ifDescrResults = Net.Snmp2Walk(h.getHostIp(), h.getSnmpTemp().getPort(), h.getSnmpTemp().getTimeout(),
					h.getSnmpTemp().getCommunityName(), Constants.ifAll.toString());

			ArrayList<OID> oids=new ArrayList<OID>();
			oids.add(Constants.sysDescr);
			sysDescrResults = Net.Snmp2GETBULK(h.getHostIp(), h.getSnmpTemp().getPort(), h.getSnmpTemp().getTimeout(),
					h.getSnmpTemp().getCommunityName(), oids);
		} else if (snmpVersion == 3) {
			ifDescrResults = Net.Snmp3Walk(h.getHostIp(), h.getSnmpTemp().getPort(), h.getSnmpTemp().getTimeout(),
					h.getSnmpTemp().getUserName(), h.getSnmpTemp().getAuthPass(), h.getSnmpTemp().getAlgo(),
					h.getSnmpTemp().getCryptPass(), h.getSnmpTemp().getCryptType(), Constants.ifAll.toString());
			ArrayList<OID> oids = new ArrayList<OID>();
			oids.add(Constants.sysDescr);
			sysDescrResults = Net.Snmp3GETBULK(h.getHostIp(), h.getSnmpTemp().getPort(), h.getSnmpTemp().getTimeout(),
					h.getSnmpTemp().getUserName(), h.getSnmpTemp().getAuthPass(), h.getSnmpTemp().getAlgo(),
					h.getSnmpTemp().getCryptPass(), h.getSnmpTemp().getCryptType(), oids);
		}
		if (ifDescrResults == null)
			return null;

		Enums.HostType hostType = this.getHostType(sysDescrResults.get(Constants.sysDescr.toString()));

		List<BaseElement> lastScanElements=this.convertNicsWalkToElements(ifDescrResults,hostType);
		return lastScanElements;
		
		
//		HashMap<BaseElement, Enums.ElementChange> elementsChanges = new HashMap<BaseElement, Enums.ElementChange>();
//		
//		
//		if (discoveryResult.getCurrentElements() == null) {
//			for (Map.Entry<String, BaseElement> lastElement : lastScanElements.entrySet()) {
//				elementsChanges.put(lastElement.getValue(), ElementChange.addedElement);
//			}
//			discoveryResult.setElementsChanges(elementsChanges);
//			discoveryResult.setCurrentElements(lastScanElements);
//			discoveryResult.setLastTimestamp(System.currentTimeMillis());
//			return discoveryResult;
//		}
	}
	private List<BaseElement> convertNicsWalkToElements(Map<String, String> nicsWalk, HostType hostType) {
		List<BaseElement> lastElements = new ArrayList<BaseElement>();
		if (hostType == null)
			return null;
		for (Map.Entry<String, String> entry : nicsWalk.entrySet()) {
			if (!entry.getKey().toString().contains("1.3.6.1.2.1.2.2.1.1."))
				continue;
			int index = Integer.parseInt(entry.getValue());
			if (index == 0) {
				continue;
			}

			String name;
			long ifSpeed;

			ifSpeed = Long.parseLong(nicsWalk.get("1.3.6.1.2.1.2.2.1.5." + index));
			switch (hostType) {
			case Windows:
				name = GeneralFunctions.convertHexToString(nicsWalk.get("1.3.6.1.2.1.2.2.1.2." + index));
				break;
			case Linux:
				name = nicsWalk.get("1.3.6.1.2.1.2.2.1.2." + index);
				break;
			default:
				return null;
			}
			NicElement nicElement = new NicElement(index,name,hostType,ifSpeed);
			lastElements.add(nicElement);
		}

		if (lastElements.size() == 0)
			return null;

		return lastElements;
	}

	private HashMap<String, BaseElement> convertDisksWalkToElements(HashMap<String, String> hashMap, HostType hostType) {
		// TODO NetResults.convertDisksWalkToIndexes
		return null;
	}
}
