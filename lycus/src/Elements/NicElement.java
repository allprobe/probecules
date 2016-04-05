package Elements;

import GlobalConstants.Enums.HostType;

public class NicElement extends BaseElement {

	private HostType hostType;
	private long ifSpeed;
	
	public NicElement(int index,String name, HostType hostType,long ifSpeed) {
		super(index,name);
		this.hostType=hostType;
		this.ifSpeed=ifSpeed;
	}

}
