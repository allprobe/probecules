package Probes;

import java.util.HashMap;
import java.util.UUID;
import org.json.simple.JSONObject;
import org.snmp4j.smi.OID;
import Elements.NicElement;
import GlobalConstants.Constants;
import GlobalConstants.Enums.HostType;
import GlobalConstants.Enums.InterfaceSpeed;
import NetConnection.NetResults;
import Results.BaseResult;
import Results.NicResult;
import Utils.GeneralFunctions;
import lycus.Host;
import Triggers.Trigger;
import lycus.User;

public class NicProbe extends BaseProbe {

	// private int index;
	// private long ifSpeed;
	// private HostType hostType;
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

	// @Override
	// public boolean isActive() {
	// return discoveryProbe.isActive();
	// }

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
		if (nicResult.getInBW() == null || nicResult.getOutBW() == null)
			return null;
		return nicResult;
	}

	public int getIndex() {
		return getNicElement().getIndex();
	}

	public OID getIfoutoctetsOID() {
		switch (this.getNicElement().getNicSpeed()) {
		case low:
			return new OID(Constants.ifOutOctetsOID_low.toString() + "." + this.getIndex());
		case high:
			return new OID(Constants.ifOutOctetsOID_high.toString() + "." + this.getIndex());
		}
		return null;
	}

	public OID getIfinoctetsOID() {
		switch (this.getNicElement().getNicSpeed()) {
		case low:
			return new OID(Constants.ifInOctetsOID_low.toString() + "." + this.getIndex());
		case high:
			return new OID(Constants.ifInOctetsOID_high.toString() + "." + this.getIndex());
		}
		return null;
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
