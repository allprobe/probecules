package lycus;

import java.util.ArrayList;
import java.util.UUID;

import org.snmp4j.smi.OID;

import GlobalConstants.LogType;
import Utils.Logit;
import lycus.Probes.Probe;

public class BaseElement extends Probe {

	
	private int index;
	
	public BaseElement(User user,String probe_id,UUID template_id,String name,long interval,float multiplier,boolean status,int index) {
		super(null,probe_id,template_id,name,interval,multiplier,status);
		this.index=index;
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


	@Override
	public ArrayList<Object> Check(Host h) {
		Logit.LogInfo("Running Discovery Element Probe: "+this.getTemplate_id()+"@"+h.getHostId().toString()+"@"+this.getProbe_id());
		return null;
	}

}
