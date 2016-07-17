package Results;

import java.util.HashMap;

import org.json.simple.JSONArray;

import GlobalConstants.Enums;
import GlobalConstants.SnmpUnit;
import GlobalConstants.Enums.SnmpError;
import Probes.SnmpProbe;
import lycus.RunnableProbeContainer;
import lycus.Trigger;
import lycus.TriggerCondition;

public class DiskResult extends BaseResult {

	 private long hrStorageUnits;// in bytes
	private long hrStorageSize;// in hrStorageUnits
	private long hrStorageUsed;// in hrStorageUnits

	private Enums.SnmpError error;

	public DiskResult(String runnableProbeId, long timestamp, long hrStorageUsed, long hrStorageSize,
			long hrStorageAllocationUnits) {
		super(runnableProbeId, timestamp);
		this.setHrStorageSize(hrStorageAllocationUnits * hrStorageSize);
		this.setHrStorageUsed(hrStorageAllocationUnits * hrStorageUsed);
	}

	public DiskResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public Long getHrStorageSize() {
		return new Long(hrStorageSize);
	}

	public void setHrStorageSize(long hrStorageSize) {
		this.hrStorageSize = hrStorageSize;
	}

	public Long getHrStorageUsed() {
		return new Long(hrStorageUsed);
	}

	public void setHrStorageUsed(long hrStorageUsed) {
		this.hrStorageUsed = hrStorageUsed;
	}

	public long getHrStorageUnits() {
		return hrStorageUnits;
	}

	public void setHrStorageUnits(long hrStorageUnits) {
		this.hrStorageUnits = hrStorageUnits;
	}

	// public long getHrStorageUnits() {
	// return hrStorageUnits;
	// }
	//
	// public void setHrStorageUnits(long hrStorageUnits) {
	// this.hrStorageUnits = hrStorageUnits;
	// }
	public Object getResultObject() {
		if (this.getLastTimestamp() == null)
			return null;

		JSONArray result = new JSONArray();
		result.add(12);
		if (error == SnmpError.NO_COMUNICATION) {
			result.add("NO_ROUTE");
		} else if (this.getErrorMessage().equals("")) {
			result.add(getHrStorageSize());
			result.add(getHrStorageUsed());
		} else
			result.add(this.getErrorMessage());
		return result;
	}

	public Enums.SnmpError getError() {
		return error;
	}

	public void setError(Enums.SnmpError error) {
		this.error = error;
	}

	@Override
	public void checkIfTriggerd(HashMap<String, Trigger> triggers) throws Exception {
		boolean flag = false;
		super.checkIfTriggerd(triggers);
		for (Trigger trigger : triggers.values()) {
			TriggerCondition condition = trigger.getCondtions().get(0);
			SnmpUnit resultUnit = SnmpUnit.B;
			SnmpUnit triggerUnit = trigger.getUnit();
			long usedInBits = SnmpUnit.getBasic(this.getHrStorageUsed(), resultUnit);
			long freeInBits = SnmpUnit.getBasic(this.getHrStorageSize()-this.getHrStorageUsed(),resultUnit);
			long triggerInBits = SnmpUnit.getBasic(Long.parseLong(condition.getxValue()), triggerUnit);
			switch (condition.getCode()) {
			case 11://free disk is less than
				if (freeInBits < triggerInBits)
					flag = true;
				break;
			case 12://free disk is larger than
				if (freeInBits > triggerInBits)
					flag = true;
				break;
			case 13://used disk is less than
				if (usedInBits < triggerInBits)
					flag = true;
				break;
			case 14://used disk is larger than
				if (usedInBits > triggerInBits)
					flag = true;
				break;
			}
			super.processTriggerResult(trigger, flag);
		}

	}
}
