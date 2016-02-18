package lycus;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import lycus.Probes.Probe;

public class DiscoveryProbe extends Probe {
	private Enums.DiscoveryElementType type;
	private long elementsInterval;

	public DiscoveryProbe() {
		// TODO Auto-generated constructor stub
	}

	public DiscoveryProbe(User user, String probe_id, UUID template_id, String name, long interval, float multiplier,
			boolean status,Enums.DiscoveryElementType discoveryType,long elementsInterval) {
		super(user, probe_id, template_id, name, interval, multiplier, status);
		this.type=discoveryType;
		this.elementsInterval=elementsInterval;
	}

	public Enums.DiscoveryElementType getType() {
		return type;
	}

	public void setType(Enums.DiscoveryElementType type) {
		this.type = type;
	}

	public long getElementInterval() {
		return elementsInterval;
	}

	public void setElementInterval(long  elementInterval) {
		this.elementsInterval = elementInterval;
	}

	@Override
	public ArrayList<Object> Check(Host h) {

		ArrayList<Object> results = null;
		try {
			switch (this.getType()) {
			case nics:
				results = this.checkForBandwidthElements(h);
				break;
			case disks:
				results = this.checkForDisksElements(h);
				break;
			}
		} catch (Throwable th) {
			SysLogger.Record(new Log(
					"Faild to run runnable probe check for: " + h.getHostId().toString() + "@" + this.getProbe_id(),
					LogType.Error));
		}

		return results;
	}

	private ArrayList<Object> checkForDisksElements(Host h) {
		// TODO Auto-generated method stub
		return null;
	}


	private ArrayList<Object> checkForBandwidthElements(Host h) {
		long checkTime;		
		String ifAll="1.3.6.1.2.1.2.2.1";
		Map<String,String> ifDescrResults=null;
		
		int snmpVersion=h.getSnmpTemp().getVersion();
		checkTime=System.currentTimeMillis();
		if(snmpVersion==2)
		ifDescrResults=Net.Snmp2Walk(h.getHostIp(),h.getSnmpTemp().getPort(),h.getSnmpTemp().getTimeout(),h.getSnmpTemp().getCommunityName(), ifAll);
		else if(snmpVersion==3)
			ifDescrResults=Net.Snmp3Walk(h.getHostIp(),h.getSnmpTemp().getPort(),h.getSnmpTemp().getTimeout(),h.getSnmpTemp().getUserName(),h.getSnmpTemp().getAuthPass(),h.getSnmpTemp().getAlgo(),h.getSnmpTemp().getCryptPass(),h.getSnmpTemp().getCryptType(),ifAll);

		if(ifDescrResults==null)
			return null;
		
		ArrayList<Object> results=new ArrayList<Object>();
		results.add(checkTime);
		results.add(ifDescrResults);
		return results;
	}

}
