package Elements;

import java.util.UUID;

import Probes.DiscoveryProbe;
import lycus.User;

public class DiskElement extends BaseElement {

	private long hrStorageAllocationUnits;// (in bytes) total size and used size depend on that
	
	public DiskElement(int index,String name,boolean status) {
		super(index, name,status);
		// TODO DiskElement()
	}


}
