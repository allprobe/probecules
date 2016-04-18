package Results;

public class DiskResult extends BaseResult {

	private long hrStorageSize;// in bytes
	private long hrStorageUsed;// in bytes

	public DiskResult(String runnableProbeId, long timestamp, long hrStorageUsed, long hrStorageSize,
			long hrStorageAllocationUnits) {
		super(runnableProbeId, timestamp);
		this.setHrStorageSize(hrStorageAllocationUnits * hrStorageSize);
		this.setHrStorageUsed(hrStorageAllocationUnits * hrStorageUsed);
	}

	public DiskResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public long getHrStorageSize() {
		return hrStorageSize;
	}

	public void setHrStorageSize(long hrStorageSize) {
		this.hrStorageSize = hrStorageSize;
	}

	public long getHrStorageUsed() {
		return hrStorageUsed;
	}

	public void setHrStorageUsed(long hrStorageUsed) {
		this.hrStorageUsed = hrStorageUsed;
	}

}
