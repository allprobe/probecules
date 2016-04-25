package Results;

import org.json.simple.JSONArray;

import GlobalConstants.Enums;
import GlobalConstants.Enums.SnmpError;
import lycus.RunnableProbeContainer;

public class NicResult extends BaseResult {
	private long previousInterfaceInOctets;
	private long currrentInterfaceInOctets;
	
	private long previousInterfaceOutOctets;
	private long currentInterfaceOutOctets;
	
	private long previousTimestamp;
	private long currentTimestamp;

	private Enums.SnmpError error;
	
	public NicResult(String runnableProbeId,long currentTimestamp,long currentInterfaceInOctets,long currentInterfaceOutOctets,long previousTimestamp,long previousInterfaceInOctets,long previousInterfaceOutOctets) {
		super(runnableProbeId);
		this.previousInterfaceInOctets=previousInterfaceInOctets;
		this.currrentInterfaceInOctets=currentInterfaceInOctets;

		this.previousInterfaceOutOctets=previousInterfaceOutOctets;
		this.currentInterfaceOutOctets=currentInterfaceOutOctets;
	
		this.previousTimestamp=previousTimestamp;
		this.currentTimestamp=currentTimestamp;
	}
	
	@Override
	public Long getLastTimestamp() {
			return this.currentTimestamp;
		
	}

	public NicResult(String runnableProbeId) {
		super(runnableProbeId);
	}
//	public NicResult(String runnableProbeId,long timestamp) {
//		super(runnableProbeId);
//		super.setLastTimestamp(timestamp);
//	}

	public long getPreviousInterfaceInOctets() {
		return previousInterfaceInOctets;
	}

	public void setPreviousInterfaceInOctets(long previousInterfaceInOctets) {
		this.previousInterfaceInOctets = previousInterfaceInOctets;
	}

	public long getCurrrentInterfaceInOctets() {
		return currrentInterfaceInOctets;
	}

	public void setCurrrentInterfaceInOctets(long currrentInterfaceInOctets) {
		this.currrentInterfaceInOctets = currrentInterfaceInOctets;
	}

	public long getPreviousInterfaceOutOctets() {
		return previousInterfaceOutOctets;
	}

	public void setPreviousInterfaceOutOctets(long previousInterfaceOutOctets) {
		this.previousInterfaceOutOctets = previousInterfaceOutOctets;
	}

	public long getCurrentInterfaceOutOctets() {
		return currentInterfaceOutOctets;
	}

	public void setCurrentInterfaceOutOctets(long currentInterfaceOutOctets) {
		this.currentInterfaceOutOctets = currentInterfaceOutOctets;
	}

	public long getPreviousTimestamp() {
		return previousTimestamp;
	}

	public void setPreviousTimestamp(long previousTimestamp) {
		this.previousTimestamp = previousTimestamp;
	}

	public long getCurrentTimestamp() {
		return currentTimestamp;
	}

	public void setCurrentTimestamp(long currentTimestamp) {
		this.currentTimestamp = currentTimestamp;
		if(previousTimestamp!=0)
			super.setLastTimestamp(currentTimestamp);
	}
	
	public Long getInBW()
	{
		if(getPreviousTimestamp()==0)
			return null; 
		return calculateBW(previousInterfaceInOctets, currrentInterfaceInOctets, previousTimestamp, currentTimestamp);
	}
	public Long getOutBW()
	{
		if(getPreviousTimestamp()==0)
			return null;
		return calculateBW(previousInterfaceOutOctets, currentInterfaceOutOctets, previousTimestamp, currentTimestamp);

	}
	private long calculateBW(long d1,long d2, long t1,long t2)
	{
		long delta=d2-d1;
		long deltaTimeInSec=(t2-t1)/1000;
		long ifSpeed=((Probes.NicProbe)RunnableProbeContainer.getInstanece().get(getRunnableProbeId()).getProbe()).getIfSpeed();
//		long bwPrecentageUsage=(delta*8*100)/(deltaTimeInSec*ifSpeed);
		long bwInBits=(delta*8)/(deltaTimeInSec);
		return bwInBits;
	}
	public String getResultString() {
		if(this.getLastTimestamp()==null)
			return null;
			
		JSONArray result=new JSONArray();
		result.add(11);
		if(error==SnmpError.NO_COMUNICATION)
		{
			result.add("NO_ROUTE");
		}
		else
		{
		result.add(getInBW());
		result.add(getOutBW());
		}
		return result.toString();
	}

	public Enums.SnmpError getError() {
		return error;
	}

	public void setError(Enums.SnmpError error) {
		this.error = error;
	}
}
