package Triggers;

import java.util.ArrayList;
import GlobalConstants.Enums.Condition;
import GlobalConstants.Enums.Function;
import GlobalConstants.XvalueUnit;
import Results.BaseResult;
import Utils.Logit;

public class CheckTrigger {
	private BaseResult[] queue;
	private int size;
	private int head = 0;
	private int tail = 0;
	private boolean empty = true;
	// private int actualSize = 0;
	private int interval;

	public CheckTrigger(int interval) {
		this.setInterval(interval);
		this.setSize(Math.round(86400 / interval));
		this.setQueue(new BaseResult[getSize()]);
	}

	public void enqueue(BaseResult result) {
		getQueue()[getTail()] = result;
		setTail((getTail() + 1) % getSize());
		if (getHead() == getTail()) {
			setHead(getHead() + 1);
			if (getHead() >= getSize())
				setHead(0);
		}
		// if (actualSize < size)
		// actualSize++;
		empty = false;
	}

	public boolean isConditionMet(BaseResult result, Trigger trigger) {
		for (TriggerCondition triggerCondition : trigger.getCondtions()) {
			Logit.LogDebug("Checking condition for RPID: " + result.getRunnableProbeId() + " ,condition is: "
					+ triggerCondition.getCondition().toString() + " ( " + triggerCondition.getxValue() + " )");

			try {
				// if (result instanceof SnmpResult && ((SnmpResult)
				// result).getNumData() == null
				// && ((SnmpResult) result).getData() != null) {
				// String xValue = ((SnmpResult) result).getData();
				// if (!isNoFunctionConditionMet(triggerCondition, xValue))
				// return false;
				// } else {
				// if (result instanceof SnmpResult)
				// Logit.LogDebug("xxx");

				Double xValue = getDouble(triggerCondition.getxValue());

				XvalueUnit resultUnit = result.getResultUnit(triggerCondition.getElementType().toString());

				if (result.getRunnableProbeId().contains(
						"6a10a32d-0d33-415b-a1f6-e9aeb2826d03@98437013-a93f-4b27-9963-a4800860b90f@snmp_1e189e8e-ec48-40bf-baba-88b61b18978a"))
					Logit.LogDebug("BREAKPOINT");

				if (triggerCondition.getFunction() == Function.none) {
					if (!isNoFunctionConditionMet(resultUnit, triggerCondition, xValue, result))
						return false;
				} else if (triggerCondition.getFunction() == Function.delta) {
					Double delta = getDelta(triggerCondition.getElementType().toString());
					if (delta == null)
						return false;
					if (!isCondition(delta, resultUnit, triggerCondition.getCondition(), xValue,
							triggerCondition.getXvalueUnit()))
						return false;
				} else if (triggerCondition.getFunction() == Function.max) {
					if (!isMaxConditionMet(resultUnit, triggerCondition, xValue))
						return false;
				} else if (triggerCondition.getFunction() == Function.avg) {
					if (!isAvgConditionMet(resultUnit, triggerCondition, xValue))
						return false;
				} else if (triggerCondition.getFunction() == Function.delta_avg) {
					if (!isDeltaAvgConditionMet(resultUnit, triggerCondition, xValue))
						return false;
				}
				// }
			} catch (Exception e) {
				Logit.LogError("EventTrigger - isConditionMet()", "Error, conditioning event, triggerName: "
						+ trigger.getName() + " , TriggerId: " + trigger.getTriggerId(), e);
				e.printStackTrace();
			}
		}
		return true;
	}

	// private boolean isNoFunctionConditionMet(TriggerCondition
	// triggerCondition, String xValue) {
	// try {
	// LastN lastN = getLast(triggerCondition);
	// if (!lastN.isEnoughElements())
	// return false;
	// Object result =
	// lastN.getNextResult(triggerCondition.getElementType().toString());
	// int nValue = lastN.getElementCount();
	//
	// while (nValue > 0) {
	// if (result == null || xValue == null)
	// return false;
	// for (Object oneResult : (ArrayList<Object>) result) {
	// if (oneResult == null)
	// continue;
	// if (isCondition(oneResult.toString(), triggerCondition.getCondition(),
	// triggerCondition.getxValue(),
	// triggerCondition.getXvalueUnit()))
	// return true;
	// }
	// nValue--;
	// result =
	// lastN.getNextResult(triggerCondition.getElementType().toString());
	// }
	// } catch (Exception e) {
	// Logit.LogError("EventTrigger - isNoFunctionConditionMet()", "Error, no
	// function conditioning", e);
	// e.printStackTrace();
	// }
	// return false;
	//
	// }

	private boolean isMaxConditionMet(XvalueUnit resultUnit, TriggerCondition triggerCondition, Double xValue) {
		try {
			double max;
			LastN lastN = getLast(triggerCondition);
			if (!lastN.isEnoughElements())
				return false;
			Object result = lastN.getNextResult(triggerCondition.getElementType().toString());
			max = 0;
			int nValue = lastN.getElementCount();

			while (nValue > 0) {
				if (result == null || (!(result instanceof Double) && !(result instanceof Integer)) || xValue == null)
					return false;

				Double current = Double.parseDouble(result.toString());
				if (max < current)
					max = current;
				result = lastN.getNextResult(triggerCondition.getElementType().toString());
				nValue--;
			}

			return isCondition(max, resultUnit, triggerCondition.getCondition(), xValue,
					triggerCondition.getXvalueUnit());
		} catch (NumberFormatException e) {
			Logit.LogError("EventTrigger - isMaxConditionMet()", "Error, no max function conditioning", e);
			e.printStackTrace();
		}

		return false;
	}

	private boolean isAvgConditionMet(XvalueUnit resultUnit, TriggerCondition triggerCondition, Double xValue) {
		try {
			LastN lastN = getLast(triggerCondition);
			if (!lastN.isEnoughElements())
				return false;
			Object result = lastN.getNextResult(triggerCondition.getElementType().toString());
			double sum = 0;
			int nValue = lastN.getElementCount();

			while (nValue > 0) {
				if (result == null || (!(result instanceof Double) && !(result instanceof Integer)) || xValue == null)
					return false;

				sum += Double.parseDouble(result.toString());
				result = lastN.getNextResult(triggerCondition.getElementType().toString());
				nValue--;
			}

			return isCondition(sum / lastN.getElementCount(), resultUnit, triggerCondition.getCondition(), xValue,
					triggerCondition.getXvalueUnit());
		} catch (NumberFormatException e) {
			Logit.LogError("EventTrigger - isAvgConditionMet()", "Error, average function conditioning", e);
			e.printStackTrace();
		}
		return false;
	}

	private boolean isDeltaAvgConditionMet(XvalueUnit resultUnit, TriggerCondition triggerCondition, Double xValue) {
		try {
			LastN lastN = getLast(triggerCondition);
			if (!lastN.isEnoughElements())
				return false;
			Object result = lastN.getNextResult(triggerCondition.getElementType().toString());
			double sum = 0;
			int nValue = lastN.getElementCount();

			while (nValue > 0) {
				if (result == null || (!(result instanceof Double) && !(result instanceof Integer)) || xValue == null)
					return false;

				sum += Double.parseDouble(result.toString());
				result = lastN.getNextResult(triggerCondition.getElementType().toString());
				nValue--;
			}

			double delta_avg = sum / lastN.getElementCount() - (double) getQueue()[getTail()]
					.getResultElementValue(triggerCondition.getElementType().toString()).get(0);
			return isCondition(delta_avg, resultUnit, triggerCondition.getCondition(), xValue,
					triggerCondition.getXvalueUnit());
		} catch (NumberFormatException e) {
			Logit.LogError("EventTrigger - isDeltaAvgConditionMet()", "Error, delta average function conditioning", e);
			e.printStackTrace();
		}
		return false;
	}

	private boolean isNoFunctionConditionMet(XvalueUnit resultUnit, TriggerCondition triggerCondition, Double xValue,
			BaseResult result1) {
		try {
			LastN lastN = getLast(triggerCondition);
			if (!lastN.isEnoughElements())
				return false;

			if (triggerCondition.getnValue() == 1) {
				ArrayList<Object> results = result1.getResultElementValue(triggerCondition.getElementType().toString());
				for (Object oneResult : (ArrayList<Object>) results) {
					if (oneResult == null)
						continue;
					if (!(oneResult instanceof Double) && !(oneResult instanceof Integer)
							&& !(oneResult instanceof Long)) {
						if (isCondition(oneResult.toString(), triggerCondition.getCondition(),
								triggerCondition.getxValue(), triggerCondition.getXvalueUnit()))
							return true;

					} else {
						if (isCondition(Double.parseDouble(oneResult.toString()), resultUnit,
								triggerCondition.getCondition(), xValue, triggerCondition.getXvalueUnit()))
							return true;
					}
				}
			}
			else {
				Object result = lastN.getNextResult(triggerCondition.getElementType().toString());
				int nValue = lastN.getElementCount();

				while (nValue > 0) {
					if (result == null)
						return false;
					for (Object oneResult : (ArrayList<Object>) result) {
						if (oneResult == null)
							continue;
						if (!(oneResult instanceof Double) && !(oneResult instanceof Integer)
								&& !(oneResult instanceof Long)) {
							if (isCondition(oneResult.toString(), triggerCondition.getCondition(),
									triggerCondition.getxValue(), triggerCondition.getXvalueUnit()))
								return true;

						} else {
							if (isCondition(Double.parseDouble(oneResult.toString()), resultUnit,
									triggerCondition.getCondition(), xValue, triggerCondition.getXvalueUnit()))
								return true;
						}
					}
					nValue--;
					result = lastN.getNextResult(triggerCondition.getElementType().toString());
				}
			}
		} catch (NumberFormatException e) {
			Logit.LogError("EventTrigger - isNoFunctionConditionMet()", "Error, no function conditioning", e);
			e.printStackTrace();
		}
		return false;
	}

	private boolean isCondition(Double result, XvalueUnit resultUnit, Condition condition, double xValue,
			XvalueUnit xvalueUnit) {
		if (resultUnit == null || xvalueUnit == null) {
			Logit.LogError("EventTrigger - isCondition()",
					"Error while processing condition, one of valueUnits is null!");
			return false;
		}
		try {
			switch (condition) {
			case bigger:
				return xvalueUnit.getBasic(result, resultUnit) > xvalueUnit.getBasic(xValue, xvalueUnit);
			case equal:
				return xvalueUnit.getBasic(result, resultUnit) == xvalueUnit.getBasic(xValue, xvalueUnit);
			case tinier:
				return xvalueUnit.getBasic(result, resultUnit) < xvalueUnit.getBasic(xValue, xvalueUnit);
			case not_equal:
				return xvalueUnit.getBasic(result, resultUnit) != xvalueUnit.getBasic(xValue, xvalueUnit);
			}
		} catch (Exception e) {
			Logit.LogError("EventTrigger - isCondition()", "Error, is condition: " + xvalueUnit.toString(), e);
			Logit.LogError("EventTrigger - isCondition()", "The condition is: " + condition.name(), e);

			e.printStackTrace();
		}
		return false;
	}

	private boolean isCondition(String result, Condition condition, String xValue, XvalueUnit xvalueUnit) {
		switch (condition) {
		case bigger:
			return result.compareTo(xValue) > 0;
		case equal:
			return result.equals(xValue);
		case tinier:
			return result.compareTo(xValue) < 0;
		case not_equal:
			return !result.equals(xValue);
		}
		return false;
	}

	private Double getDouble(String value) {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private LastN getLast(TriggerCondition triggerondition) {
		switch (triggerondition.getLast_type()) {
		case K:
			return new LastN(triggerondition.getnValue(), this);
		case N:
			return new LastN(triggerondition.getnValue(), this);
		case H:
			return new LastN(this, triggerondition.getnValue());
		case P:
			// return new LastN(this, triggerondition.getnValue());
		}
		return null;
	}

	// retrun null is false;
	private Double getDelta(String elementType) {
		try {
			if (getTail() > 1 && getQueue()[0] != null && getQueue()[1] != null) {
				BaseResult lastResult = null;
				BaseResult previousResult = null;

				if (getTail() == 0) {
					lastResult = getQueue()[getSize() - 1];
					previousResult = getQueue()[getTail()];
					if (lastResult == null || previousResult == null)
						return null;

					return (double) lastResult.getResultElementValue(elementType).get(0)
							- (double) previousResult.getResultElementValue(elementType).get(0);
				} else {
					lastResult = getQueue()[getTail()];
					previousResult = getQueue()[getTail() - 1];
					if (lastResult == null || previousResult == null)
						return null;

					return (double) lastResult.getResultElementValue(elementType).get(0)
							- (double) previousResult.getResultElementValue(elementType).get(0);
				}

			}
		} catch (Exception e) {
			Logit.LogError("EventTrigger - getDelta()", "Error, get delta", e);
			e.printStackTrace();
		}
		return null;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getHead() {
		return head;
	}

	public void setHead(int head) {
		this.head = head;
	}

	public int getTail() {
		return tail;
	}

	public void setTail(int tail) {
		this.tail = tail;
	}

	public BaseResult[] getQueue() {
		return queue;
	}

	public void setQueue(BaseResult[] queue) {
		this.queue = queue;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
}
