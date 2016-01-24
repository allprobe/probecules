package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiscoveryProbe extends Probe {
	private DiscoveryType type;
	private int elementInterval;

	public DiscoveryProbe() {
		// TODO Auto-generated constructor stub
	}

	public DiscoveryProbe(User user, String probe_id, UUID template_id, String name, long interval, float multiplier,
			boolean status) {
		super(user, probe_id, template_id, name, interval, multiplier, status);
		// TODO Auto-generated constructor stub
	}

	public DiscoveryType getType() {
		return type;
	}

	public void setType(DiscoveryType type) {
		this.type = type;
	}

	public int getElementInterval() {
		return elementInterval;
	}

	public void setElementInterval(int elementInterval) {
		this.elementInterval = elementInterval;
	}

	@Override
	public ArrayList<Object> Check(Host h) {

		ArrayList<Object> results = null;
		try {
			switch (this.getType()) {
			case BandWidth:
				results = this.checkForBandwidthElements(h);
				break;
			case Disk:
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
				
		String ifAll="1.3.6.1.2.1.2.2.1";

		Map<String,String> ifDescrResults=null;
		ifDescrResults=Net.Snmp2Walk(h.getHostIp(),h.getSnmpTemp().getPort(),h.getSnmpTemp().getTimeout(),h.getSnmpTemp().getCommunityName(), ifAll);
		
		if(ifDescrResults==null)
			return null;
		
		ArrayList<Object> results=new ArrayList<Object>();
		results.add(ifDescrResults);
		return results;
	}

}