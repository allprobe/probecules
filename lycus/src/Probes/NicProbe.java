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
import Triggers.Trigger;
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
