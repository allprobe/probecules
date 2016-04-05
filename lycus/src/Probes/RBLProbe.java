package Probes;

import java.util.UUID;

import Model.UpdateValueModel;
import lycus.Host;
import NetConnection.NetResults;
import Results.RblResult;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.User;

public class RBLProbe extends BaseProbe {
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
    	this.setRBL(newRBL);
    }
	
	@Override
    public RblResult getResult(Host h)
    {
		if (!h.isHostStatus())
    		return null;
		
		RblResult results=NetResults.getInstanece().getRblResult(h, this);
		return results;
    }
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder(super.toString());
		s.append("RBL:").append(this.getRBL().toString()).append("; ");
		return s.toString();
	}
	
	 public boolean updateKeyValues(UpdateValueModel updateValue)
		{
			super.updateKeyValues(updateValue);
			if (!GeneralFunctions.isNullOrEmpty(updateValue.key.rbl) && !getRBL().equals(updateValue.key.rbl))
			{
				this.setRBL(updateValue.key.rbl);
				Logit.LogCheck("Rbl for " + getName() +  " has changed to " + updateValue.key.rbl);
			}
				
			return true;
		}
}
