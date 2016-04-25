package Probes;

import java.util.UUID;
import org.snmp4j.smi.OID;

import GlobalConstants.SnmpDataType;
import GlobalConstants.SnmpUnit;
import GlobalConstants.Enums.SnmpStoreAs;
import Model.UpdateValueModel;
import Results.BaseResult;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.Host;
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
	public BaseResult getResult(Host h) // Only V1
	{
		return null;
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
		if (!GeneralFunctions.isNullOrEmpty(updateValue.key.snmp_oid) && !getOid().equals(updateValue.key.snmp_oid))
		{
			oid = new OID(updateValue.key.snmp_oid);
			Logit.LogCheck("OID for " + getName() +  " has changed to " + updateValue.key.snmp_oid);
		}
			
		if (!GeneralFunctions.isNullOrEmpty(updateValue.key.value_unit) && !updateValue.key.value_unit.equals(unit.toString()))
		{
			unit = UsersManager.getSnmpUnit(updateValue.key.value_unit);
			Logit.LogCheck("Snmp unit for " + getName() +  " has changed to " + updateValue.key.value_unit);
		}
		
		if (!GeneralFunctions.isNullOrEmpty(updateValue.key.value_type) && !updateValue.key.value_type.equals(dataType.toString()))
		{
			dataType =  UsersManager.getSnmpDataType(updateValue.key.value_type);
			Logit.LogCheck("Snmp data for " + getName() +  " has changed to " + updateValue.key.value_type);
		}
		
		if (updateValue.key.store_value_as != null && !updateValue.key.store_value_as.equals(storeAs.toString()))
		{
			storeAs = SnmpStoreAs.values()[updateValue.key.store_value_as];
			Logit.LogCheck("Snmp store as.. for " + getName() +  " has changed to " + updateValue.key.store_value_as);
		}
			
		return true;
	}
}