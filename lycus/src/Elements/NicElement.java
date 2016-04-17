package Elements;

import GlobalConstants.Enums.HostType;
import Probes.DiscoveryProbe;

public class NicElement extends BaseElement {


	private HostType hostType;
	private long ifSpeed;
	
	public NicElement(DiscoveryProbe discoveryProbe,int index,String name, HostType hostType,long ifSpeed) {
		super(index,name,discoveryProbe);
		this.hostType=hostType;
		this.ifSpeed=ifSpeed;
	}
	public NicElement(int index, String name, DiscoveryProbe discoveryProbe, boolean active, HostType hostType,long ifSpeed) {
		super(index, name, discoveryProbe, active);
		this.hostType=hostType;
		this.ifSpeed=ifSpeed;
	}
	

}
