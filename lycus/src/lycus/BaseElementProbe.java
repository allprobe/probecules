package lycus;

import GlobalConstants.LogType;
import lycus.Probes.Probe;

public class BaseElementProbe extends Probe {

	DiscoveryResults container;
	private int index;
	public BaseElementProbe(DiscoveryResults dp,int index,String name) {
		this.container=dp;
		this.index=index;
	}

	public DiscoveryResults getContainer() {
		return container;
	}

	public void setContainer(DiscoveryResults container) {
		this.container = container;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}


	public boolean start() {
		SysLogger.Record(new Log("Starting elements for runnable discovery probe: "+this.getContainer().getRp().getRPString(),LogType.Debug));
		return true;
	}

	public boolean stop() {
		SysLogger.Record(new Log("Stopping elements for runnable discovery probe: "+this.getContainer().getRp().getRPString(),LogType.Debug));
		return true;
	}

}
