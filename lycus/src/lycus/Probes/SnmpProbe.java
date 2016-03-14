package lycus.Probes;

import java.util.ArrayList;
import java.util.UUID;
import org.snmp4j.smi.OID;

import lycus.GlobalConstants.SnmpDataType;
import lycus.GlobalConstants.SnmpUnit;
import lycus.GlobalConstants.Enums.SnmpStoreAs;
import lycus.Model.KeyUpdateModel;
import lycus.Model.UpdateValueModel;
import lycus.Utils.GeneralFunctions;
import lycus.Host;
import lycus.Net;
import lycus.User;
import lycus.UsersManager;

public class SnmpProbe extends BaseProbe {
	private OID oid;
	private SnmpDataType dataType;
	private SnmpUnit unit;
	private SnmpStoreAs storeAs; 

	public SnmpProbe(User user,String probe_id, UUID template_id, String name, long interval, float multiplier,
			boolean status, OID oid, SnmpDataType dataType, SnmpUnit unit, SnmpStoreAs storeAs) {
		super(user,probe_id, template_id, name, interval, multiplier, status);
		this.setOid(oid);
		this.setDataType(dataType);
		this.setStoreAs(storeAs);
		this.setUnit(unit);

	}

	// Getters/Setters
	public OID getOid() {
		return oid;
	}

	public void setOid(OID oid) {
		this.oid = oid;
	}

	public SnmpDataType getDataType() {
		return dataType;
	}

	public void setDataType(SnmpDataType dataType) {
		this.dataType = dataType;
	}

	public SnmpStoreAs getStoreAs() {
		return storeAs;
	}

	public void setStoreAs(SnmpStoreAs storeAs) {
		this.storeAs = storeAs;
	}

	public SnmpUnit getUnit() {
		return unit;
	}

	public void setUnit(SnmpUnit unit) {
		this.unit = unit;
	}

	@Override
	public ArrayList<Object> Check(Host h) // Only V1
	{
		if (!h.isHostStatus())
    		return null;
		
		ArrayList<Object> results = Net.runSnmpCheckVer1(h.getHostIp(), h.getSnmpTemp().getPort(),
				h.getSnmpTemp().getCommunityName(), this.getOid().toString(), h.getSnmpTemp().getTimeout());
		return results;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder(super.toString());
		s.append("OID:").append(this.getOid().toString()).append("; ");
		return s.toString();
	}
	
	public boolean updateKeyValues(UpdateValueModel updateValue)
	{
		super.updateKeyValues(updateValue);
		if (!GeneralFunctions.isNullOrEmpty(updateValue.key.snmp_oid))
			oid = new OID(updateValue.key.snmp_oid);
		if (!GeneralFunctions.isNullOrEmpty(updateValue.key.value_unit))
			unit = UsersManager.getSnmpUnit(updateValue.key.value_unit);
		if (!GeneralFunctions.isNullOrEmpty(updateValue.key.value_type))
			dataType =  UsersManager.getSnmpDataType(updateValue.key.value_type);
		if (updateValue.key.store_value_as != null)
			storeAs = SnmpStoreAs.values()[updateValue.key.store_value_as];
		return true;
	}
}
