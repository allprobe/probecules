package Elements;

import GlobalConstants.Enums.HostType;

public class NicElement extends BaseElement {
	private HostType hostType;
	private long ifSpeed;

	public NicElement(int index, String name, HostType hostType, long ifSpeed) {
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

}
