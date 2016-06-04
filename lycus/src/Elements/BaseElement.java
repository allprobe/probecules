package Elements;

import Probes.DiscoveryProbe;

public class BaseElement {

	private int index;
	private String name;
	private boolean active;

	// private String discoveryProbeId;
	public BaseElement(int index, String name) {
		this.index = index;
		this.setName(name);
		// this.setDiscoveryProbeId(discoveryProbe.getProbe_id());
	}

	public BaseElement(int index, String name, boolean active) {
		this.index = index;
		this.setName(name);
		// this.setDiscoveryProbeId(discoveryProbe.getProbe_id());
		this.setActive(active);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isIdentical(BaseElement baseElement) {
		return this.getIndex() == baseElement.getIndex();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	//
	// public String getDiscoveryProbeId() {
	// return discoveryProbeId;
	// }
	//
	//
	// public void setDiscoveryProbeId(String string) {
	// this.discoveryProbeId = string;
	// }

}
