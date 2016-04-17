package Elements;

import Probes.DiscoveryProbe;

public class BaseElement {

	private int index;
	private String name;
	private boolean active;
	private DiscoveryProbe discoveryProbe;
	public BaseElement(int index,String name, DiscoveryProbe discoveryProbe) {
		this.index=index;
		this.setName(name);
		this.setDiscoveryProbe(discoveryProbe);
	}
	public BaseElement(int index,String name, DiscoveryProbe discoveryProbe,boolean active) {
		this.index=index;
		this.setName(name);
		this.setDiscoveryProbe(discoveryProbe);
		this.setActive(active);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isIdentical(BaseElement baseElement)
	{
		return this.getIndex()==baseElement.getIndex();
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


	public DiscoveryProbe getDiscoveryProbe() {
		return discoveryProbe;
	}


	public void setDiscoveryProbe(DiscoveryProbe discoveryProbe) {
		this.discoveryProbe = discoveryProbe;
	}


}
