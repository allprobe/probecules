package Results;

import org.json.simple.JSONArray;
import GlobalConstants.Enums.SnmpError;
import GlobalConstants.ProbeTypes;
import GlobalConstants.XvalueUnit;
import Probes.SnmpProbe;
import Triggers.Trigger;
import lycus.RunnableProbeContainer;
import Triggers.TriggerCondition;

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

//	private boolean checkForNumberTrigger(Trigger trigger) {
//		boolean flag = false;
//		for (TriggerCondition condition : trigger.getCondtions()) {
//			int x = Integer.parseInt(condition.getxValue());
//			Double lastValue = this.getNumData();
//			if (lastValue == null)
//				continue;
//			XvalueUnit resultUnit = ((SnmpProbe) RunnableProbeContainer.getInstanece().get(this.getRunnableProbeId())
//					.getProbe()).getUnit();
//			if (resultUnit.equals(XvalueUnit.as_is)) {
//				switch (condition.getCondition()) {
//				case bigger:
//					if (lastValue > x)
//						flag = true;
//					break;
//				case tinier:
//					if (lastValue < x)
//						flag = true;
//					break;
//				case equal:
//					if (lastValue == x)
//						flag = true;
//					break;
//				case not_equal:
//					if (lastValue != x)
//						flag = true;
//					break;
//				}
//			} else {
//
//				long resultInBits = XvalueUnit.getBasic(Math.round(lastValue), resultUnit);
//				long triggerInBits = XvalueUnit.getBasic(Long.parseLong(condition.getxValue()), trigger.getUnit());
//				switch (condition.getCondition()) {
//				case bigger:
//					if (resultInBits > triggerInBits)
//						flag = true;
//					break;
//				case tinier:
//					if (resultInBits < triggerInBits)
//						flag = true;
//					break;
//				case equal:
//					if (resultInBits == triggerInBits)
//						flag = true;
//					break;
//				case not_equal:
//					if (resultInBits != triggerInBits)
//						flag = true;
//					break;
//				}
//			}
//			if (!flag)
//				return false;
//		}
//		return flag;
//	}

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
