package Elements;

import GlobalConstants.Enums.HostType;
import GlobalConstants.Enums.InterfaceSpeed;

public class NicElement extends BaseElement {
	private HostType hostType;
	private long ifSpeed;
	private GlobalConstants.Enums.InterfaceSpeed nicSpeed;

	public NicElement(int index, String name, HostType hostType, long ifSpeed,InterfaceSpeed nicSpeed) {
		super(index, name);
		this.setHostType(hostType);
		this.setIfSpeed(ifSpeed);
		this.setActive(false);
	}

	public NicElement(int index, String name, boolean active, HostType hostType, long ifSpeed) {
		super(index, name, active);
		this.setHostType(hostType);
		this.setIfSpeed(ifSpeed);
	}

	public long getIfSpeed() {
		return ifSpeed;
	}

	public void setIfSpeed(long ifSpeed) {
		this.ifSpeed = ifSpeed;
	}

	public HostType getHostType() {
		return hostType;
	}

	public void setHostType(HostType hostType) {
		this.hostType = hostType;
	}

	public GlobalConstants.Enums.InterfaceSpeed getNicSpeed() {
		return nicSpeed;
	}

	public void setNicSpeed(GlobalConstants.Enums.InterfaceSpeed nicSpeed) {
		this.nicSpeed = nicSpeed;
	}

}
