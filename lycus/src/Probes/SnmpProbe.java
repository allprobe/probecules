package Probes;

import java.util.UUID;
import org.snmp4j.smi.OID;

import GlobalConstants.SnmpDataType;
import GlobalConstants.XvalueUnit;
import GlobalConstants.Enums.SnmpStoreAs;
import Model.UpdateModel;
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
	private XvalueUnit unit;
	private SnmpStoreAs storeAs;

	public SnmpProbe(User user, String probe_id, UUID template_id, String name, int interval, float multiplier,
			boolean status, OID oid, SnmpDataType dataType, XvalueUnit unit, SnmpStoreAs storeAs) {
		super(user, probe_id, template_id, name, interval, multiplier, status);
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

	public XvalueUnit getUnit() {
		return unit;
	}

	public void setUnit(XvalueUnit unit) {
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

	public boolean updateKeyValues(UpdateModel updateModel) {
		super.updateKeyValues(updateModel);
		UpdateValueModel updateValue = updateModel.update_value;
		if (!GeneralFunctions.isNullOrEmpty(updateValue.key.snmp_oid) && !getOid().equals(updateValue.key.snmp_oid)) {
			oid = new OID(updateValue.key.snmp_oid);
			Logit.LogCheck("OID for " + getName() + " has changed to " + updateValue.key.snmp_oid);
		}

		if ((unit == null && updateValue.key.value_unit != null)
				|| !GeneralFunctions.isNullOrEmpty(updateValue.key.value_unit)
						&& !updateValue.key.value_unit.equals(unit.toString())) {
			unit = XvalueUnit.valueOf(updateValue.key.value_unit);
			Logit.LogCheck("Snmp unit for " + getName() + " has changed to " + updateValue.key.value_unit);
		}

		if (!GeneralFunctions.isNullOrEmpty(updateValue.key.value_type)
				&& !updateValue.key.value_type.equals(dataType.toString())) {
			dataType = UsersManager.getSnmpDataType(updateValue.key.value_type);
			Logit.LogCheck("Snmp data for " + getName() + " has changed to " + updateValue.key.value_type);
		}

		if (updateValue.key.store_value_as != null && !updateValue.key.store_value_as.equals(storeAs.toString())) {
			storeAs = SnmpStoreAs.values()[updateValue.key.store_value_as];
			Logit.LogCheck("Snmp store as.. for " + getName() + " has changed to " + updateValue.key.store_value_as);
		}

		return true;
	}
}
