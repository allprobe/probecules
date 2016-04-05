package SLA;

import Results.BaseResult;
import Results.PingResult;
import Results.PortResult;
import Results.WebResult;

public class SLAContainer {
	private SLAObject webSLA;
	private SLAObject pingSLA;
	private SLAObject portSLA;

	public SLAContainer() {
		webSLA = new SLAObject();
		pingSLA = new SLAObject();
		portSLA = new SLAObject();
	}

	public boolean addToSLA(BaseResult result) {
		if (result instanceof PortResult) {
			portSLA.addResult(((PortResult) result).isActive());
		}
		else if (result instanceof PingResult) {
			pingSLA.addResult(((WebResult) result).isActive());
		}
		else if (result instanceof WebResult) {
			webSLA.addResult(((PingResult) result).isActive());
		}

		return true;
	}

	public double getWebSLA()
	{
		return webSLA.getPercentage();
	}
	
	public double getPingSLA()
	{
		return webSLA.getPercentage();
	}
	
	public double getPortSLA()
	{
		return webSLA.getPercentage();
	}
}
