package lycus;

public class DiscoveryElement implements Runnable {

	RunnableDiscoveryProbeResults container;
	private int index;
	private String name;
	private boolean state;//Running or not
	public DiscoveryElement(RunnableDiscoveryProbeResults dp,int index,String name) {
		this.container=dp;
		this.index=index;
		this.name=name;
	}

	public RunnableDiscoveryProbeResults getContainer() {
		return container;
	}

	public void setContainer(RunnableDiscoveryProbeResults container) {
		this.container = container;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public boolean isState() {
		return state;
	}
	
	protected void setState(boolean state) {
		this.state = state;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
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
