package lycus.Results;

public class NicResult extends BaseResult {
	private long interfaceInOctets;
	private long interfaceOutOctets;
//	private long interfaceTotal;

	public NicResult(String runnableProbeId) {
		super(runnableProbeId);
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
