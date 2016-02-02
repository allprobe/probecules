package lycus;

import java.util.HashMap;

public class DiscoveryElement implements Runnable {

	RunnableDiscoveryProbeResults container;
	private int index;
	private String name;
	public DiscoveryElement(RunnableDiscoveryProbeResults dp,String parentOidString,int index,String name) {
//		this.parentOid=parentOidString;
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


//	public boolean identical(DiscoveryElement de)
//	{
//		return (this.getIndex()==de.getIndex()&&this.getName().equals(de.getName()))?true:false;
//	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
