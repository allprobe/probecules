package lycus.Results;

public class NicResult extends BaseResult {
	private long interfaceInOctets;
	private long interfaceOutOctets;

	public NicResult(String runnableProbeId,long timestamp,long interfaceInOctets,long interfaceOutOctets) {
		super(runnableProbeId,timestamp);
		this.interfaceInOctets=interfaceInOctets;
		this.interfaceOutOctets=interfaceOutOctets;
	}
	
	public long getInterfaceInOctets() {
		return interfaceInOctets;
	}

	public void setInterfaceInOctets(long interfaceInOctets) {
		this.interfaceInOctets = interfaceInOctets;
	}

	public long getInterfaceOutOctets() {
		return interfaceOutOctets;
	}

	public void setInterfaceOutOctets(long interfaceOutOctets) {
		this.interfaceOutOctets = interfaceOutOctets;
	}

}
