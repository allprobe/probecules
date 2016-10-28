package Results;

import org.json.simple.JSONArray;
import GlobalConstants.Enums;
import GlobalConstants.Enums.SnmpError;

public class DiskResult extends BaseResult {

	private long hrStorageUnits;// in bytes
	private long hrStorageSize;// in hrStorageUnits
	private long hrStorageUsed;// in hrStorageUnits

	private Enums.SnmpError error;

	public DiskResult(String runnableProbeId, long timestamp, long hrStorageUsed, long hrStorageSize,
			long hrStorageAllocationUnits) {
		super(runnableProbeId, timestamp);
		this.setHrStorageSize(hrStorageSize);
		this.setHrStorageUsed(hrStorageUsed);
		this.setHrStorageUnits(hrStorageAllocationUnits);
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

	public Long getStorageSize() {
		return this.hrStorageSize * this.hrStorageUnits;
	}

	public Long getStorageUsed() {
		return this.hrStorageUsed * this.hrStorageUnits;
	}

	public Long getStorageFree() {
		return this.getStorageSize() - this.getStorageUsed();
	}

	public Object getResultObject() {
		if (this.getLastTimestamp() == null)
			return null;

		JSONArray result = new JSONArray();
		result.add(12);
		if (error == SnmpError.NO_COMUNICATION) {
			result.add("NO_ROUTE");
		} else if (this.getErrorMessage().equals("")) {
			result.add(getStorageSize());
			result.add(getStorageUsed());
			result.add(getStorageFree());
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

	public int getStorageFreePercentage() {
		return (100 - this.getStorageUsedPercentage());
	}

	public int getStorageUsedPercentage() {
		return ((int) ((100 * this.getStorageUsed()) / this.getStorageSize()));
	}
}
