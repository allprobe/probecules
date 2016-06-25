package Results;

import java.util.Arrays;
import java.util.HashMap;
import org.json.simple.JSONArray;
import GlobalConstants.Constants;
import GlobalConstants.ProbeTypes;
import lycus.Trigger;
import lycus.TriggerCondition;

public class WebResult extends BaseResult {
	private Integer statusCode;
	private Long responseTime;
	private Long pageSize;

	public WebResult(String runnableProbeId, long timestamp, int responseCode, long responseTime2, long responseSize) {
		super(runnableProbeId, timestamp);
		this.probeType = ProbeTypes.HTTP;
		this.statusCode = responseCode;
		this.responseTime = responseTime2;
		this.pageSize = responseSize;
	}

	public WebResult(String runnableProbeId) {
		super(runnableProbeId);
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
	public void checkIfTriggerd(HashMap<String, Trigger> triggers) throws Exception {
		super.checkIfTriggerd(triggers);
		for (Trigger trigger : triggers.values()) {
			boolean triggered = false;
			switch (trigger.getElementType()) {
			case "rc":
				triggered = checkForResponseCodeTrigger(trigger);
				break;
			case "rt":
				triggered = checkForResponseTimeTrigger(trigger);
				break;
			case "ps":
				triggered = checkForPageSizeTrigger(trigger);
				break;
			}

			super.processTriggerResult(trigger, triggered);
		}
	}

	private boolean checkForResponseCodeTrigger(Trigger trigger) {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			int x = Integer.parseInt(condition.getxValue());
			int lastValue = this.getStatusCode();
			switch (condition.getCode()) {
			case 1:
				if (lastValue > x)
					flag = true;
				break;
			case 2:
				if (lastValue < x)
					flag = true;
				break;
			case 3:
				if (lastValue == x)
					flag = true;
				break;
			case 4:
				if (lastValue != x)
					flag = true;
				break;
			}
			if (flag && condition.getAndOr().equals("or"))
				return true;
			else if (!flag && condition.getAndOr().equals("and"))
				return false;
		}
		return flag;
	}

	private boolean checkForResponseTimeTrigger(Trigger trigger) {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			long x = Long.parseLong(condition.getxValue());
			long lastValue = this.getResponseTime();
			switch (condition.getCode()) {
			case 1:
				if (lastValue > x)
					flag = true;
				break;
			case 2:
				if (lastValue < x)
					flag = true;
				break;
			case 3:
				if (lastValue == x)
					flag = true;
				break;
			case 4:
				if (lastValue != x)
					flag = true;
				break;
			}
			if (flag && condition.getAndOr().equals("or"))
				return true;
			else if (!flag && condition.getAndOr().equals("and"))
				return false;
		}
		return flag;
	}

	private boolean checkForPageSizeTrigger(Trigger trigger) {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			long x = Long.parseLong(condition.getxValue());
			long lastValue = this.getPageSize();
			switch (condition.getCode()) {
			case 1:
				if (lastValue > x)
					flag = true;
				break;
			case 2:
				if (lastValue < x)
					flag = true;
				break;
			case 3:
				if (lastValue == x)
					flag = true;
				break;
			case 4:
				if (lastValue != x)
					flag = true;
				break;
			}
			if (flag && condition.getAndOr().equals("or"))
				return true;
			else if (!flag && condition.getAndOr().equals("and"))
				return false;
		}
		return flag;
	}

	@Override
	public String getResultString() {
		JSONArray result = new JSONArray();
		result.add(3);
		result.add(statusCode);
		result.add(responseTime);
		result.add(pageSize);
		return result.toString();
	}

	// TODO: Oren ask ran what is true?
	public Boolean isActive() {
		return Arrays.asList(Constants.OkStatus).contains(statusCode);
	}
}
