package Elements;

public class BaseElement {

	private int index;
	private String name;
	private boolean active;
	
	public BaseElement(int index,String name) {
		this.index=index;
		this.setName(name);
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


}
