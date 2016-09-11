package Probes;

import java.util.HashMap;
import java.util.UUID;
import org.json.simple.JSONObject;
import org.snmp4j.smi.OID;

import Elements.NicElement;
import GlobalConstants.Enums.HostType;
import NetConnection.NetResults;
import Results.BaseResult;
import Results.NicResult;
import Utils.GeneralFunctions;
import lycus.Host;
import lycus.Trigger;
import lycus.User;

public class NicProbe extends BaseProbe {

	private static final String ifOutOctetsOID = "1.3.6.1.2.1.2.2.1.16.";
	private static final String ifInOctetsOID = "1.3.6.1.2.1.2.2.1.10.";
	private int index;
	private long ifSpeed;
	HostType hostType;
	DiscoveryProbe discoveryProbe;
	private NicElement nicElement;

	public NicProbe(DiscoveryProbe probe, NicElement nicElement) {
		this.discoveryProbe = probe;
		this.setNicElement(nicElement);
		// this.index=index;
		// this.ifSpeed=ifSpeed;
		// this.hostType=hostType;
	}

	@Override
	public User getUser() {
		return discoveryProbe.getUser();
	}

	@Override
	public UUID getTemplate_id() {
		return discoveryProbe.getTemplate_id();
	}

	@Override
	public int getInterval() {
		return discoveryProbe.getElementInterval();
	}

	@Override
	public String getName() {
		return getNicElement().getName();
	}

//	@Override
//	public boolean isActive() {
//		return discoveryProbe.isActive();
//	}

	@Override
	public float getMultiplier() {
		return 1F;
	}

	@Override
	public HashMap<String, Trigger> getTriggers() {
		return discoveryProbe.getTriggers();
	}

	@Override
	public Trigger getTrigger(String triggerId) {
		return discoveryProbe.getTrigger(triggerId);
	}

	// public NicProbe(User user,String probe_id, UUID template_id, String name,
	// long interval, float multiplier,
	// boolean status, int index, long ifSpeed, Enums.HostType hostType) {
	// super(user, probe_id, template_id, name, interval, multiplier, status,
	// index);
	// this.setIfSpeed(ifSpeed);
	// this.hostType = hostType;
	//
	// this.ifInOctets = new SnmpProbe(user,probe_id+"@"+Constants.inBW,
	// template_id, name, interval, multiplier, status,
	// new OID(this.getIfinoctetsOID()), SnmpDataType.Numeric, SnmpUnit.bytes,
	// SnmpStoreAs.deltaBytesPerSecond);
	// this.ifOutOctets = new SnmpProbe(user,probe_id+"@"+Constants.outBW,
	// template_id, name, interval, multiplier, status,
	// new OID(this.getIfoutoctetsOID()), SnmpDataType.Numeric, SnmpUnit.bytes,
	// SnmpStoreAs.deltaBytesPerSecond);
	//
	// }

	// @Override
	// public BaseResult getResult(Host host) {
	// super.getResult(host);
	// try {
	// List<String> listOids = new ArrayList<String>();
	// NicResult result = null;;
	//
	// if (host.isHostStatus() && host.isSnmpStatus()) {
	// listOids.add(this.getIfinoctetsOID());
	// listOids.add(this.getIfoutoctetsOID());
	// }
	//
	// Map<String, SnmpResult> response = null;
	//// if (host.getSnmpTemp().getVersion() == 2) {
	//// response = Net.Snmp2GETBULK(host.getHostIp(),
	// host.getSnmpTemp().getPort(),
	//// host.getSnmpTemp().getTimeout(), host.getSnmpTemp().getCommunityName(),
	// listOids);
	//// }
	//// else if (host.getSnmpTemp().getVersion() == 3) {
	//// response = Net.Snmp3GETBULK(host.getHostIp(),
	// host.getSnmpTemp().getPort(),
	//// host.getSnmpTemp().getTimeout(), host.getSnmpTemp().getUserName(),
	//// host.getSnmpTemp().getAuthPass(), host.getSnmpTemp().getAlgo(),
	//// host.getSnmpTemp().getCryptPass(), host.getSnmpTemp().getCryptType(),
	// listOids,
	//// GeneralFunctions.getRunnableProbeId(getTemplate_id(), host.getHostId(),
	// getProbe_id()));
	//// }
	//
	// if (response == null) {
	//
	// Logit.LogInfo("no response for nic element probe" +
	// GeneralFunctions.getRunnableProbeId(getTemplate_id(), host.getHostId(),
	// getProbe_id()));
	// }
	// else {
	// long resultsTimestamp = System.currentTimeMillis();
	//
	// SnmpResult ifInResults = response.get(this.getIfinoctetsOID());
	// SnmpResult ifOutResults = response.get(this.getIfoutoctetsOID());
	//
	// if (ifInResults == null || ifOutResults == null) {
	// Logit.LogInfo("no response for nic element probe" +
	// GeneralFunctions.getRunnableProbeId(getTemplate_id(), host.getHostId(),
	// getProbe_id()));
	// return null;
	// }
	//
	//
	//
	//// long ifTotalTraffic = Long.parseLong(ifInResults) +
	// Long.parseLong(ifOutResults);
	//// results.add(ifTotalTraffic);
	//
	//
	// return result;
	// }
	// } catch (Throwable th) {
	// Logit.LogError("NicElement - Check","Error running discovery element
	// probe:" + GeneralFunctions.getRunnableProbeId(getTemplate_id(),
	// host.getHostId(), getProbe_id()));
	// }
	// return null;
	// }

	@Override
	public BaseResult getResult(Host h) {
		super.getResult(h);
		if (!h.isHostStatus())
			return null;

		NicResult nicResult = NetResults.getInstanece().getNicResult(h, this);

		return nicResult;
	}

	public int getIndex() {
		return getNicElement().getIndex();
	}

	public OID getIfoutoctetsOID() {
		return new OID(NicProbe.ifOutOctetsOID + this.getIndex());
	}

	public OID getIfinoctetsOID() {
		return new OID(NicProbe.ifInOctetsOID + this.getIndex());
	}

	@Override
	public String getProbe_id() {
		return discoveryProbe.getProbe_id() + "@" + GeneralFunctions.Base64Encode(this.getName());
	}

	public long getIfSpeed() {
		return getNicElement().getIfSpeed();
	}

	@Override
	public String toString() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("probe_id", this.getProbe_id());
		jsonObject.put("if_name", this.getName());
		jsonObject.put("if_speed", this.getIfSpeed());
		jsonObject.put("if_index", this.getIndex());
		return jsonObject.toJSONString();
	}

	public NicElement getNicElement() {
		return nicElement;
	}

	public void setNicElement(NicElement nicElement) {
		this.nicElement = nicElement;
	}

}
