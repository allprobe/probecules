package Elements;

import GlobalConstants.Enums.HostType;
import GlobalConstants.Enums.InterfaceSpeed;

public class NicElement extends BaseElement {
	private HostType hostType;
	private long nicSpeedPackets;
	private GlobalConstants.Enums.InterfaceSpeed nicSpeedType;

	public NicElement(int index, String name, HostType hostType, long ifSpeed,InterfaceSpeed nicSpeed) {
		super(index, name);
		this.setHostType(hostType);
		this.setIfSpeed(ifSpeed);
		this.setActive(false);
		this.setNicSpeed(nicSpeed);
	}

	public NicElement(int index, String name, boolean active, HostType hostType, long ifSpeed,InterfaceSpeed nicSpeed) {
		super(index, name, active);
		this.setHostType(hostType);
		this.setIfSpeed(ifSpeed);
		this.setNicSpeed(nicSpeed);
	}

	public long getIfSpeed() {
		return nicSpeedPackets;
	}

	public void setIfSpeed(long ifSpeed) {
		this.nicSpeedPackets = ifSpeed;
	}

	public HostType getHostType() {
		return hostType;
	}

	public void setHostType(HostType hostType) {
		this.hostType = hostType;
	}

	public GlobalConstants.Enums.InterfaceSpeed getNicSpeed() {
		return nicSpeedType;
	}

	public void setNicSpeed(GlobalConstants.Enums.InterfaceSpeed nicSpeed) {
		this.nicSpeedType = nicSpeed;
	}

}
