package lycus;

import java.util.ArrayList;
import java.util.UUID;

public class RBLProbe extends Probe {
	private String RBL;
	public RBLProbe(User user,String probe_id,UUID template_id,String name, long interval,float multiplier,boolean status,String rbl)
	{
		super(user,probe_id,template_id,name,interval,multiplier,status);
		this.RBL=rbl;
	}
	public String getRBL() {
		return RBL;
	}
	public void setRBL(String rBL) {
		RBL = rBL;
	}
	
	public void updateProbeAttributes(String probeNewName, long probeNewInterval, float probeNewMultiplier,
			boolean probeNewStatus,String newRBL)
    {
    	super.updateProbe(probeNewName, probeNewInterval, probeNewMultiplier, probeNewStatus);
    	this.setRBL(newRBL);
    }
	
	@Override
    public ArrayList<Object> Check(Host h)
    {
		ArrayList<Object> results=Net.RBLCheck(h.getHostIp(), this.getRBL());
    	return results;
    }
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder(super.toString());
		s.append("RBL:").append(this.getRBL().toString()).append("; ");
		return s.toString();
	}
}
