package lycus;

import lycus.Elements.BaseElement;
import lycus.GlobalConstants.Enums.HostType;

public class NicElement extends BaseElement {

	private String name;
	private HostType hostType;
	
	public NicElement(int index,String name, HostType hostType) {
		super(index);
		this.name=name;
		this.hostType=hostType;
	}

}
