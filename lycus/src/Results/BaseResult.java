package Results;

import java.util.ArrayList;
import java.util.HashMap;

import GlobalConstants.XvalueUnit;
import Probes.SnmpProbe;
import com.google.common.base.Enums;

import GlobalConstants.Enums.ResultValueType;
import GlobalConstants.ProbeTypes;
import Interfaces.IResult;
import Utils.Logit;
import lycus.*;

public class BaseResult implements IResult {
	private Long lastTimestamp;
	private boolean isSent;
	private String runnableProbeId;
	protected ProbeTypes probeType;
	private String errorMessage;

	public BaseResult(String runnableProbeId, long timestamp) {
		this.runnableProbeId = runnableProbeId;
		this.lastTimestamp = timestamp;
		errorMessage = "";
		setSent(false);
	}

	public BaseResult(String runnableProbeId) {
		this.runnableProbeId = runnableProbeId;
		this.lastTimestamp = System.currentTimeMillis();
		errorMessage = "";
		setSent(false);
	}

	public Long getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(Long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public int getNumberOfRollupTables() {
		RunnableProbe runnableProbe = RunnableProbeContainer.getInstanece().get(runnableProbeId);
		if (runnableProbe == null) {
			Logit.LogError("BaseResult - getNumberOfRollupTables()",
					"Unable to determine number of rollups tables - " + runnableProbeId);
			return 0;
		}
		long interval = runnableProbe.getProbe().getInterval();
		if (interval < 240)
			return 6;
		if (interval >= 240 && interval < 1200)
			return 5;
		if (interval >= 1200 && interval < 3600)
			return 4;
		if (interval >= 3600 && interval < 21600)
			return 3;
		if (interval >= 21600)
			return 2;
		Logit.LogError("ProbeRollup - getNumberOfRollupTables", "Wrong interval at Runnable Probe:" + runnableProbeId);
		return 0;
	}

	public HashMap<String, String> getRaw() throws Throwable {
		return null;
	}

//	public void checkIfTriggerd(HashMap<String, Trigger> triggers) throws Exception {
//		for (Trigger trigger : triggers.values()) {
//			boolean triggered = checkForTriggerActivated(trigger);
//			processTriggerResult(trigger, triggered);
//
//		}
//	}

//	public void processTriggerResult(Trigger trigger, boolean triggered) {
//		Event lastEvent = ResultsContainer.getInstance().getEvent(getRunnableProbeId(), trigger.getTriggerId());
//		if (lastEvent != null && !triggered) {
//			// if trigger event became true and normal again send event to api
//			lastEvent.setStatus(true);
//			lastEvent.setSent(false);
//			Logit.LogInfo("Trigger " + trigger.getTriggerId() + " of Runnable Probe: " + getRunnableProbeId()
//					+ " deactivated, will send event to API...");
//		} else if (lastEvent == null && triggered) {
//			Event event = new Event(trigger, false);
//			ResultsContainer.getInstance().addEvent(runnableProbeId, trigger.getTriggerId(), event);
//		}
//	}

	public boolean isSent() {
		return isSent;
	}

	public void setSent(boolean isSentOK) {
		this.isSent = isSentOK;
	}

	public String getRunnableProbeId() {
		return runnableProbeId;
	}

	public void setRunnableProbeId(String runnableProbeId) {
		this.runnableProbeId = runnableProbeId;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Object getResultObject() {
		return null;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

//	private boolean checkForTriggerActivated(Trigger trigger) {
//		boolean flag = false;
//		for (TriggerCondition condition : trigger.getCondtions()) {
//			String x = condition.getxValue();
//			// Double xNumber = Double.parseDouble(x);
//			Object[] lastValues = (RunnableProbeContainer.getInstanece().get(this.getRunnableProbeId()))
//					.getConditionFunction(trigger, condition).get();
//			for (int i = 0; i < lastValues.length; i++) {
//				for (Object oneValue : (ArrayList<Object>) lastValues[i]) {
//					flag = conditionByType(oneValue, x, condition.getCode());
//					if (!flag)
//						return false;
//				}
//			}
//		}
//		return flag;
//	}

	private boolean conditionByType(Object lastValue, String triggerValue, int code) {
		if (lastValue == null || triggerValue == null)
			return false;
		switch (code) {
		case 1:
			if (lastValue.getClass().equals(Integer.class))
				if ((Integer) lastValue > Integer.parseInt(triggerValue))
					return true;
			if (lastValue.getClass().equals(Double.class))
				if ((Double) lastValue > Double.parseDouble(triggerValue))
					return true;
			if (lastValue.getClass().equals(Long.class))
				if ((Long) lastValue > Long.parseLong(triggerValue))
					return true;
			break;
		case 2:
			if (lastValue.getClass().equals(Integer.class))
				if ((Integer) lastValue < Integer.parseInt(triggerValue))
					return true;
			if (lastValue.getClass().equals(Double.class))
				if ((Double) lastValue < Double.parseDouble(triggerValue))
					return true;
			if (lastValue.getClass().equals(Long.class))
				if ((Long) lastValue < Long.parseLong(triggerValue))
					return true;
			break;
		case 3:
			if (lastValue.getClass().equals(Integer.class))
				if ((Integer) lastValue == Integer.parseInt(triggerValue))
					return true;
			if (lastValue.getClass().equals(Double.class))
				if ((Double) lastValue == Double.parseDouble(triggerValue))
					return true;
			if (lastValue.getClass().equals(Long.class))
				if ((Long) lastValue == Long.parseLong(triggerValue))
					return true;
			if (lastValue.getClass().equals(Boolean.class))
				if ((Boolean) lastValue == Boolean.parseBoolean(triggerValue))
					return true;
			if (lastValue.getClass().equals(String.class))
				if ((String) lastValue == triggerValue)
					return true;
			break;
		case 4:
			if (lastValue.getClass().equals(Integer.class))
				if ((Integer) lastValue == Integer.parseInt(triggerValue))
					return true;
			if (lastValue.getClass().equals(Double.class))
				if ((Double) lastValue == Double.parseDouble(triggerValue))
					return true;
			if (lastValue.getClass().equals(Long.class))
				if ((Long) lastValue == Long.parseLong(triggerValue))
					return true;
			if (lastValue.getClass().equals(Boolean.class))
				if ((Boolean) lastValue == Boolean.parseBoolean(triggerValue))
					return true;
			if (lastValue.getClass().equals(String.class))
				if ((String) lastValue == triggerValue)
					return true;
			break;
		}
		return false;
	}

	public ArrayList<Object> getResultElementValue(String elementType) {
		ResultValueType valueType = ResultValueType.valueOf(elementType);
		ArrayList<Object> values = new ArrayList<Object>();

		switch (valueType) {
		case WRT:
			values.add(((WebResult) this).getResponseTime());
			break;
		case PRT:
			values.add(((PortResult) this).getResponseTime());
			break;
		case RC:
			values.add(((WebResult) this).getStatusCode());
			break;
		case PS:
			values.add(((WebResult) this).getPageSize());
			break;
		case PST:
			values.add(((PortResult) this).isActive());
			break;
		case RTA:
			values.add(((PingResult) this).getRtt());
			break;
		case PL:
			values.add(((PingResult) this).getPacketLost());
			break;
		case DFDS:
			values.add(((DiskResult) this).getStorageFree());
			break;
		case DUDS:
			values.add(((DiskResult) this).getStorageUsed());
			break;
		case DTDS:
			values.add(((DiskResult) this).getStorageSize());
			break;
		case DBI:
			values.add(((NicResult) this).getInBW());
			break;
		case DBO:
			values.add(((NicResult) this).getOutBW());
			break;
		case WSERT:
			for (DOMElement dome : ((WebExtendedResult) this).getAllElementsResults()) {
				values.add(dome.getTime());
			}
			break;
		case WAERC:
			for (DOMElement dome : ((WebExtendedResult) this).getAllElementsResults()) {
				values.add(dome.getResponseStatusCode());
			}
			break;
		case TRARHRT:
			for (ArrayList<Object> route : ((TraceRouteResult) this).getRoutes()) {
				values.add((Double) route.get(1));
			}
			break;
		case TRDHRT:
			values.add((Double) ((TraceRouteResult) this).getRoutes()
					.get(((TraceRouteResult) this).getRoutes().size() - 1).get(1));
			break;
		case SNMP:
			values.add(((SnmpResult) this).getData());
			break;
		case RBL:
			values.add(((RblResult) this).isIsListed());
			break;
		}
	
		return values;
	}
	public XvalueUnit getResultUnit(String elementType)
	{
		ResultValueType valueType = ResultValueType.valueOf(elementType);
		switch (valueType) {
			case WRT:
				return XvalueUnit.ms;
			case PRT:
				return XvalueUnit.ms;
			case RC:
				return XvalueUnit.as_is;
			case PS:
				return XvalueUnit.B;
			case PST:
				return XvalueUnit.as_is;
			case RTA:
				return XvalueUnit.ms;
			case PL:
				return XvalueUnit.as_is;
			case DFDS:
				return XvalueUnit.B;
			case DUDS:
				return XvalueUnit.B;
			case DTDS:
				return XvalueUnit.B;
			case DBI:
				return XvalueUnit.b;
			case DBO:
				return XvalueUnit.b;
			case WSERT:
				return XvalueUnit.ms;
			case WAERC:
				return XvalueUnit.as_is;
			case TRARHRT:
				return XvalueUnit.ms;
			case TRDHRT:
				return XvalueUnit.ms;
			case SNMP:
				RunnableProbe rp=RunnableProbeContainer.getInstanece().get(this.getRunnableProbeId());
				return ((SnmpProbe)(rp.getProbe())).getUnit();
//				return XvalueUnit.as_is;

			case RBL:
				return XvalueUnit.as_is;
		}

		return null;
	}
}
