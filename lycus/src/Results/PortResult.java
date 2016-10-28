package Results;

import org.json.simple.JSONArray;
import GlobalConstants.ProbeTypes;

public class PortResult extends BaseResult {
	private Integer portStatus;
	private Long responseTime;

	public PortResult(String runnableProbeId, long timestamp, int portState, long responseTime2) {
		super(runnableProbeId, timestamp);
		this.probeType = ProbeTypes.PORT;
		this.portStatus = portState;
		this.responseTime = responseTime2;
	}

	public PortResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public boolean isActive() {
		return portStatus > 0;
	}
	
	public Integer getPortStatus()
	{
		return this.portStatus;
	}

	public void setIsActive(int portStatus) {
		this.portStatus = portStatus;
	}

	public Long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(Long responseTime) {
		this.responseTime = responseTime;
	}

	@Override
	public Object getResultObject() {
		JSONArray result = new JSONArray();
		result.add(2);
		if (this.getErrorMessage().equals("")) {
			result.add(portStatus);
			result.add(responseTime);
		} else
			result.add(this.getErrorMessage());

		return result;
	}
}
