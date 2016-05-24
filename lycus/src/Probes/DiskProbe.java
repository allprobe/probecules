package Probes;

import java.util.UUID;

import org.snmp4j.smi.OID;

import Elements.DiskElement;
import Elements.NicElement;
import NetConnection.NetResults;
import Results.BaseResult;
import Results.DiskResult;
import lycus.Host;
import lycus.User;

public class DiskProbe extends BaseProbe {
	private static final String hrStorageAllocationUnitsOID = "1.3.6.1.2.1.25.2.3.1.1.4.";
	private static final String hrStorageSizeOID = "1.3.6.1.2.1.25.2.3.1.1.5.";
	private static final String hrStorageUsedOID = "1.3.6.1.2.1.25.2.3.1.1.6.";

	DiscoveryProbe discoveryProbe;
	DiskElement diskElement;
	
//	public DiskProbe(User user, String probe_id, UUID template_id, String name, long interval, float multiplier,
//			boolean status,int index) {
//		super(user, probe_id, template_id, name, interval, multiplier, status);
//	}

	public DiskProbe(DiscoveryProbe probe,DiskElement diskElement) {
		this.discoveryProbe=probe;
		this.diskElement=diskElement;
//		this.index=index;
//		this.ifSpeed=ifSpeed;
//		this.hostType=hostType;
	}
	
	public int getIndex() {
		return diskElement.getIndex();
	}
	
	public OID getHrstorageallocationunitsoid() {
		return new OID(DiskProbe.hrStorageAllocationUnitsOID+this.getIndex());
	}

	public OID getHrstoragesizeoid() {
		return new OID(DiskProbe.hrStorageSizeOID+this.getIndex());
	}

	public OID getHrstorageusedoid() {
		return new OID(DiskProbe.hrStorageUsedOID+this.getIndex());
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
