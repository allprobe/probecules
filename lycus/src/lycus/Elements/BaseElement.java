package lycus.Elements;

import java.util.ArrayList;
import java.util.UUID;

import lycus.Utils.Logit;
import lycus.Host;
import lycus.User;
import lycus.Probes.BaseProbe;
import lycus.Results.BaseResult;

public class BaseElement {

	private int index;
	private String name;
	
	public BaseElement(int index,String name) {
		this.index=index;
		this.name=name;
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


}
