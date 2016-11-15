package Results;

import org.json.simple.JSONArray;
import GlobalConstants.ProbeTypes;

public class WebResult extends BaseResult {
	private Integer statusCode;
	private Long responseTime;
	private Long pageSize;
	private Integer stateCode;

	public WebResult(String runnableProbeId, long timestamp, int responseCode, long responseTime2, long responseSize,int stateCode) {
		super(runnableProbeId, timestamp);
		this.probeType = ProbeTypes.HTTP;
		this.statusCode = responseCode;
		this.responseTime = responseTime2;
		this.pageSize = responseSize;
		this.setStateCode(stateCode);
	}

	public WebResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public WebResult(String runnableProbeId, long timestamp,int stateCode) {
		super(runnableProbeId, timestamp);
		this.setStateCode(stateCode);
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	public Long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(Long responseTime) {
		this.responseTime = responseTime;
	}

	public Long getPageSize() {
		return pageSize;
	}

	public void setPageSize(Long pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	public Object getResultObject() {
		JSONArray result = new JSONArray();
		result.add(3);
		if (this.getErrorMessage().equals("")) {
			result.add(statusCode);
			result.add(responseTime);
			result.add(pageSize);
			result.add(getStateCode());
		} else
			result.add(this.getErrorMessage());

		return result;
	}

	public Boolean isActive() {
		if (stateCode == null)
			return false;
		return this.stateCode == 3;
	}

	public Integer getStateCode() {
		return stateCode;
	}

	public void setStateCode(Integer stateCode) {
		this.stateCode = stateCode;
	}
}
