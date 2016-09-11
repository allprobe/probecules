package Results;

import org.json.simple.JSONArray;
import GlobalConstants.Enums.SnmpError;
import GlobalConstants.ProbeTypes;
import GlobalConstants.XvalueUnit;
import Probes.SnmpProbe;
import lycus.Trigger;
import lycus.RunnableProbeContainer;
import lycus.TriggerCondition;

public class SnmpResult extends BaseResult {

	private String oid;
	private String data;
	private SnmpError error;

	public SnmpResult(String runnableProbeId, long timestamp) {
		super(runnableProbeId, timestamp);
		this.probeType = ProbeTypes.SNMP;
	}

	public SnmpResult(String runnableProbeId, long timestamp, String data) {
		super(runnableProbeId, timestamp);
		this.probeType = ProbeTypes.SNMP;
		this.data = data;
	}

	public SnmpResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public boolean isValidNumber() {
		return this.getNumData() != null;
	}

	public Double getNumData() {
		Double numData = null;

		try {
			if (data == null)
				return null;
			numData = Double.parseDouble(data);
		} catch (Exception e) {
		}

		return numData;
	}

//	@Override
//	public void checkIfTriggerd(HashMap<String, Trigger> triggers) throws Exception {
//
//		if (this.getRunnableProbeId()
//				.contains("15a29f39-5baf-4672-8853-c08b4b247be0@snmp_52caf27e-445b-4b8d-bfc6-0307fd4ef3eb"))
//			Logit.LogDebug("BREAKPOINT");
//
//		super.checkIfTriggerd(triggers);
//		for (Trigger trigger : triggers.values()) {
//
//			boolean triggered = false;
//			switch (((SnmpProbe) trigger.getProbe()).getDataType()) {
//			case Numeric:
//				triggered = checkForNumberTrigger(trigger);
//				break;
//			case Text:
//				triggered = checkForTextTrigger(trigger);
//				break;
//			}
//
//			super.processTriggerResult(trigger, triggered);
//
//		}
//	}

	private boolean checkForNumberTrigger(Trigger trigger) {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			int x = Integer.parseInt(condition.getxValue());
			Double lastValue = this.getNumData();
			if (lastValue == null)
				continue;
			XvalueUnit resultUnit = ((SnmpProbe) RunnableProbeContainer.getInstanece().get(this.getRunnableProbeId())
					.getProbe()).getUnit();
			if (resultUnit.equals(XvalueUnit.as_is)) {
				switch (condition.getCondition()) {
				case bigger:
					if (lastValue > x)
						flag = true;
					break;
				case tinier:
					if (lastValue < x)
						flag = true;
					break;
				case equal:
					if (lastValue == x)
						flag = true;
					break;
				case not_equal:
					if (lastValue != x)
						flag = true;
					break;
				}
			} else {

				long resultInBits = XvalueUnit.getBasic(Math.round(lastValue), resultUnit);
				long triggerInBits = XvalueUnit.getBasic(Long.parseLong(condition.getxValue()), trigger.getUnit());
				switch (condition.getCondition()) {
				case bigger:
					if (resultInBits > triggerInBits)
						flag = true;
					break;
				case tinier:
					if (resultInBits < triggerInBits)
						flag = true;
					break;
				case equal:
					if (resultInBits == triggerInBits)
						flag = true;
					break;
				case not_equal:
					if (resultInBits != triggerInBits)
						flag = true;
					break;
				}
			}
			if (!flag)
				return false;
		}
		return flag;
	}

	private boolean checkForTextTrigger(Trigger trigger) {
		boolean flag = false;
		for (TriggerCondition condition : trigger.getCondtions()) {
			String x = condition.getxValue();
			String lastValue = this.getData();
			switch (condition.getCondition()) {
			case equal:
				if (lastValue.equals(x))
					flag = true;
				break;
			case not_equal:
				if (!lastValue.equals(x))
					flag = true;
				break;
			}
			if (!flag)
				return false;
		}
		return flag;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	@Override
	public Object getResultObject() {
		JSONArray result = new JSONArray();
		result.add(4);
		if (this.getErrorMessage().equals("")) {
			result.add(data);
		} else
			result.add(this.getErrorMessage());

		return result;
	}

	public SnmpError getError() {
		return error;
	}

	public void setError(SnmpError error) {
		this.error = error;
	}
}
