package Results;

import org.json.simple.JSONArray;

import GlobalConstants.ProbeTypes;

public class RblResult extends BaseResult {

	private Integer IsListed;

	public RblResult(String runnableProbeId, long timestamp, int isListed2) {
		super(runnableProbeId, timestamp);
		this.probeType = ProbeTypes.RBL;

		this.IsListed = isListed2;
	}

	public RblResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public int isIsListed() {
		return IsListed;
	}

	public void setIsListed(int isListed) {
		IsListed = isListed;
	}

	// @Override
	// public void checkIfTriggerd(HashMap<String, Trigger> triggers) throws
	// Exception {
	// super.checkIfTriggerd(triggers);
	// for (Trigger trigger : triggers.values()) {
	// boolean triggered = false;
	// triggered = checkForRblTrigger(trigger);
	//
	// super.processTriggerResult(trigger, triggered);
	//
	// }
	// }
	//
	// private boolean checkForRblTrigger(Trigger trigger) throws Exception {
	// boolean flag = false;
	// for (TriggerCondition condition : trigger.getCondtions()) {
	// boolean x = Boolean.parseBoolean(condition.getxValue());
	// boolean lastValue = this.isIsListed();
	//
	// switch (condition.getCode()) {
	// case 3:
	// if (lastValue == x)
	// flag = true;
	// break;
	// case 4:
	// if (lastValue != x)
	// flag = true;
	// break;
	// }
	//
	// if (!flag)
	// return false;
	// }
	// return flag;
	// }

	@Override
	public Object getResultObject() {
		JSONArray result = new JSONArray();
		result.add(5);
		if (this.getErrorMessage().equals("")) {
			result.add(IsListed);
		} else
			result.add(this.getErrorMessage());

		return result;
	}
}
