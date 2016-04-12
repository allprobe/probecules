package Probes;

import java.util.UUID;

import org.snmp4j.smi.OID;

import NetConnection.NetResults;
import Results.BaseResult;
import Results.DiskResult;
import Results.NicResult;
import lycus.Host;
import lycus.User;

public class StorageProbe extends BaseProbe {
	private static final String hrStorageAllocationUnitsOID = "1.3.6.1.2.1.25.2.3.1.1.4.";
	private static final String hrStorageSizeOID = "1.3.6.1.2.1.25.2.3.1.1.5.";
	private static final String hrStorageUsedOID = "1.3.6.1.2.1.25.2.3.1.1.6.";
	private int index;

	public StorageProbe(User user, String probe_id, UUID template_id, String name, long interval, float multiplier,
			boolean status,int index) {
		super(user, probe_id, template_id, name, interval, multiplier, status);
		this.setIndex(index);
	}

	public OID getHrstorageallocationunitsoid() {
		return new OID(StorageProbe.hrStorageAllocationUnitsOID+this.getIndex());
	}

	public OID getHrstoragesizeoid() {
		return new OID(StorageProbe.hrStorageSizeOID+this.getIndex());
	}

	public OID getHrstorageusedoid() {
		return new OID(StorageProbe.hrStorageUsedOID+this.getIndex());
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public BaseResult getResult(Host h) {
		super.getResult(h);
		if (!h.isHostStatus())
			return null;

		DiskResult nicResult = NetResults.getInstanece().getDiskResult(h, this);
		

		return nicResult;
	}
}
