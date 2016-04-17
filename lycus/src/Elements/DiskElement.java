package Elements;

import java.util.UUID;

import Probes.DiscoveryProbe;
import lycus.User;

public class DiskElement extends BaseElement {

	private long hrStorageAllocationUnits;// (in bytes) total size and used size depend on that
	
	public DiskElement(int index,String name, DiscoveryProbe probe) {
		super(index, name, probe);
		// TODO DiskElement()
	}


}
