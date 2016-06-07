package Probes;

import java.util.HashMap;
import java.util.UUID;
import org.snmp4j.smi.OID;
import Elements.DiskElement;
import NetConnection.NetResults;
import Results.BaseResult;
import Results.DiskResult;
import Utils.GeneralFunctions;
import lycus.Host;
import lycus.Trigger;
import lycus.User;

public class DiskProbe extends BaseProbe {
	private static final String hrStorageAllocationUnitsOID = "1.3.6.1.2.1.25.2.3.1.4.";
	private static final String hrStorageSizeOID = 			  "1.3.6.1.2.1.25.2.3.1.5.";
	private static final String hrStorageUsedOID = 			  "1.3.6.1.2.1.25.2.3.1.6.";

	DiscoveryProbe discoveryProbe;
	private DiskElement diskElement;
	
//	public DiskProbe(User user, String probe_id, UUID template_id, String name, long interval, float multiplier,
//			boolean status,int index) {
//		super(user, probe_id, template_id, name, interval, multiplier, status);
//	}

	public DiskProbe(DiscoveryProbe probe,DiskElement diskElement) {
		this.discoveryProbe=probe;
		this.setDiskElement(diskElement);
//		this.index=index;
//		this.ifSpeed=ifSpeed;
//		this.hostType=hostType;
	}
	@Override
	public User getUser() {
		return discoveryProbe.getUser();
	}
	
	@Override
	public UUID getTemplate_id() {
		return discoveryProbe.getTemplate_id();}
	
	@Override
	public long getInterval() {
		return discoveryProbe.getElementInterval();}
	
	@Override
	public String getName() {
		return getDiskElement().getName();
		}
	
	@Override
	public boolean isActive() {
		return discoveryProbe.isActive();
	}
	
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
	public int getIndex() {
		return getDiskElement().getIndex();
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
	public String getProbe_id() {
		return discoveryProbe.getProbe_id()+"@"+GeneralFunctions.Base64Encode(this.getName());
	}

	@Override
	public BaseResult getResult(Host h) {
		super.getResult(h);
		if (!h.isHostStatus())
			return null;

		DiskResult diskResult = NetResults.getInstanece().getDiskResult(h, this);

		return diskResult;
	}
	public DiskElement getDiskElement() {
		return diskElement;
	}
	public void setDiskElement(DiskElement diskElement) {
		this.diskElement = diskElement;
	}
}
