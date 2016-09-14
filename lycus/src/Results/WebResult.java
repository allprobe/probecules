package Results;

import java.util.Arrays;
import java.util.HashMap;

import Probes.HttpProbe;
import lycus.RunnableProbeContainer;
import org.json.simple.JSONArray;
import GlobalConstants.Constants;
import GlobalConstants.ProbeTypes;
import lycus.Trigger;
import lycus.TriggerCondition;

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
		this.stateCode=stateCode;
	}

	public WebResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public WebResult(String runnableProbeId, long timestamp,int stateCode) {
		super(runnableProbeId, timestamp);
		this.stateCode=stateCode;
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

//	@Override
//	public void checkIfTriggerd(HashMap<String, Trigger> triggers) throws Exception {
//		super.checkIfTriggerd(triggers);
//		for (Trigger trigger : triggers.values()) {
//			boolean triggered = false;
//			switch (trigger.getElementType()) {
//			case "RC":
//				triggered = checkForResponseCodeTrigger(trigger);
//				break;
//			case "RT":
//				triggered = checkForResponseTimeTrigger(trigger);
//				break;
//			case "PS":
//				triggered = checkForPageSizeTrigger(trigger);
//				break;
//			}
//
//			super.processTriggerResult(trigger, triggered);
//		}
//	}
//
//	private boolean checkForResponseCodeTrigger(Trigger trigger) {
//		boolean flag = false;
//		for (TriggerCondition condition : trigger.getCondtions()) {
//			int x = Integer.parseInt(condition.getxValue());
//			int lastValue = this.getStatusCode();
//			switch (condition.getCode()) {
//			case 1:
//				if (lastValue > x)
//					flag = true;
//				break;
//			case 2:
//				if (lastValue < x)
//					flag = true;
//				break;
//			case 3:
//				if (lastValue == x)
//					flag = true;
//				break;
//			case 4:
//				if (lastValue != x)
//					flag = true;
//				break;
//			}
//			if (!flag)
//				return false;
//		}
//		return flag;
//	}
//
//	private boolean checkForResponseTimeTrigger(Trigger trigger) {
//		boolean flag = false;
//		for (TriggerCondition condition : trigger.getCondtions()) {
//			long x = Long.parseLong(condition.getxValue());
//			long lastValue = this.getResponseTime();
//			switch (condition.getCode()) {
//			case 1:
//				if (lastValue > x)
//					flag = true;
//				break;
//			case 2:
//				if (lastValue < x)
//					flag = true;
//				break;
//			case 3:
//				if (lastValue == x)
//					flag = true;
//				break;
//			case 4:
//				if (lastValue != x)
//					flag = true;
//				break;
//			}
//			if (!flag)
//				return false;
//		}
//		return flag;
//	}
//
//	private boolean checkForPageSizeTrigger(Trigger trigger) {
//		boolean flag = false;
//		for (TriggerCondition condition : trigger.getCondtions()) {
//			long x = Long.parseLong(condition.getxValue());
//			Object[] lastValue = (RunnableProbeContainer.getInstanece().get(this.getRunnableProbeId()))
//					.getTriggerFunction(trigger).get();
//			switch (condition.getCode()) {
//			/*
//			 * case 1: if (lastValue > x) flag = true; break; case 2: if
//			 * (lastValue < x) flag = true; break; case 3: if (lastValue == x)
//			 * flag = true; break; case 4: if (lastValue != x) flag = true;
//			 * break;
//			 */
//			}
//			if (!flag)
//				return false;
//		}
//		return flag;
//	}

	@Override
	public Object getResultObject() {
		JSONArray result = new JSONArray();
		result.add(3);
		if (this.getErrorMessage().equals("")) {
			result.add(statusCode);
			result.add(responseTime);
			result.add(pageSize);
		} else
			result.add(this.getErrorMessage());

		return result;
	}

	public Boolean isActive() {
		if (statusCode == null)
			return false;
		return statusCode < 400;
	}
}
