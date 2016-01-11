package lycus;

import java.util.HashMap;

public class DiscoveryElement {

	DiscoveryProbe container;
	private int index;
	private String name;
	private HashMap<String,String> values;
	public DiscoveryElement(DiscoveryProbe dp,String parentOidString,int index,String name) {
//		this.parentOid=parentOidString;
		this.index=index;
		this.name=name;
	}

	public DiscoveryProbe getContainer() {
		return container;
	}

	public void setContainer(DiscoveryProbe container) {
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

	public HashMap<String, String> getValues() {
		return values;
	}

	public void setValues(HashMap<String, String> values) {
		this.values = values;
	}

	public boolean identical(DiscoveryElement de)
	{
		return (this.getIndex()==de.getIndex()&&this.getName().equals(de.getName()))?true:false;
	}
}
