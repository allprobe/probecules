package lycus;

public class DiscoveryElement {

	private String parentOid;
	private int index;
	private String name;
	private Object value;
	public DiscoveryElement(String parentOidString,int index,String name) {
		this.parentOid=parentOidString;
		this.index=index;
		this.name=name;
	}

}
