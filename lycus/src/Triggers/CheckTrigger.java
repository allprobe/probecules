package Triggers;

import java.util.ArrayList;
import java.util.Date;
import GlobalConstants.Enums.Condition;
import GlobalConstants.Enums.Function;
import GlobalConstants.XvalueUnit;
import Results.BaseResult;
import Results.SnmpResult;

public class CheckTrigger {
	private BaseResult[] queue;
	private int size;
	private int head = 0;
	private int tail = 0;
	private boolean empty = true;
	private int actualSize = 0;
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
			if (getHead() > getSize())
				setHead(0);
		}
		if (actualSize < size)
			actualSize++;
		empty = false;
	}

	public boolean isConditionMet(BaseResult result, Trigger trigger) {
		for (TriggerCondition triggerCondition : trigger.getCondtions()) {
			Double xValue = getDouble(triggerCondition.getxValue());

			XvalueUnit resultUnit = result.getResultUnit(triggerCondition.getElementType().toString());

			if (triggerCondition.getFunction() == Function.none) {
				if (!isNoFunctionConditionMet(resultUnit, triggerCondition, xValue))
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
		}
		return true;
	}

	private boolean isMaxConditionMet(XvalueUnit resultUnit, TriggerCondition triggerCondition, Double xValue) {
		LastN lastN = getLast(triggerCondition);
		if (!lastN.isEnoughElements())
			return false;
		Object result = lastN.getNextResult(triggerCondition.getElementType().toString());
		double max = 0;
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

		return isCondition(max, resultUnit, triggerCondition.getCondition(), xValue, triggerCondition.getXvalueUnit());
	}

	private boolean isAvgConditionMet(XvalueUnit resultUnit, TriggerCondition triggerCondition, Double xValue) {
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
	}

	private boolean isDeltaAvgConditionMet(XvalueUnit resultUnit, TriggerCondition triggerCondition, Double xValue) {
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
	}

	private boolean isNoFunctionConditionMet(XvalueUnit resultUnit, TriggerCondition triggerCondition, Double xValue) {
		LastN lastN = getLast(triggerCondition);
		if (!lastN.isEnoughElements())
			return false;
		Object result = lastN.getNextResult(triggerCondition.getElementType().toString());
		int nValue = lastN.getElementCount();

		while (nValue > 0) {
			if (result == null || xValue == null)
				return false;
			for (Object oneResult : (ArrayList<Object>) result) {
				if (!(oneResult instanceof Double) && !(oneResult instanceof Integer) && !(oneResult instanceof Long)) {
					if (isCondition(oneResult.toString(), triggerCondition.getCondition(), triggerCondition.getxValue(),
							triggerCondition.getXvalueUnit()))
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
		return false;

	}

	private boolean isCondition(Double result, XvalueUnit resultUnit, Condition condition, double xValue,
			XvalueUnit xvalueUnit) {
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
		return null;
	}

	// private double getAverage(LastN lastN, String elementType) {
	// double sum = 0;
	// int start = lastN.getHead();
	// int end = lastN.getTail();
	// if (lastN.getHead() > lastN.getTail()) {
	// start = lastN.getTail();
	// end = lastN.getHead();
	// }
	//
	// while (start <= end) {
	// sum += (double)
	// getQueue()[start++].getResultElementValue(elementType).get(0);
	// }
	//
	// return sum / Math.abs(lastN.getTail() - lastN.getHead() + 1);
	// }

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

class LastN {
	private int head; // Head of the selected group
	private int tail; // Tail of the selected group
	private int current;
	private int size;
	private BaseResult[] queue;
	private int elementsPopped;
	private int elenemtCount;

	// Gets the set of indexes for last N Items
	public LastN(int nValue, CheckTrigger checkTrigger) {
		this.setSize(checkTrigger.getSize());
		this.queue = checkTrigger.getQueue();
		this.setTail(checkTrigger.getTail());
		if (checkTrigger.getTail() > nValue - 1)
			this.setHead(checkTrigger.getTail() - nValue);
		if (checkTrigger.getHead() > checkTrigger.getTail()) {
			int start = checkTrigger.getTail() - nValue + 1;
			if (start < 0)
				start = checkTrigger.getTail() - nValue + 1 + checkTrigger.getSize();
			this.setHead(start);
		}
		elenemtCount = nValue;
		this.current = head;
		this.elementsPopped = nValue;
	}

	public boolean isEnoughElements() {
		if (tail > head)
			return elenemtCount <= tail - head + 1;

		return true;
	}

	// Implement
	// Gets the set of indexes for last N hours
	public LastN(CheckTrigger checkTrigger, int pValue) {
		this.setSize(pValue * 60 * 60 / checkTrigger.getInterval());
		this.setHead(checkTrigger.getHead());
		this.setTail(checkTrigger.getTail());
		this.queue = checkTrigger.getQueue();
		this.current = head;
		// elenemtCount = nValue;
	}

	// Implement
	// Gets the set of indexes for time interval
	public LastN(Date time1, Date time2, CheckTrigger checkTrigger) {
		this.setSize(checkTrigger.getSize());
		this.setHead(checkTrigger.getHead());
		this.setTail(checkTrigger.getTail());
		this.queue = checkTrigger.getQueue();
		this.current = head;
		// elenemtCount = nValue;
	}

	public Object getNextResult(String elementType) {
		if (this.current > this.size)
			this.current = 0;

		int cur = current++;
		if (this.elementsPopped < 0)
			return null;

		this.elementsPopped--;
		if (queue[cur] == null || (queue[cur] != null && queue[cur] instanceof SnmpResult
				&& ((SnmpResult) queue[cur]).getData() != null
				&& ((SnmpResult) queue[cur]).getData().startsWith("WRONG_"))) // cur
																				// is
																				// the
																				// new
																				// pointer
																				// to
																				// the
																				// array
																				// index
			// that havent been intialized yet
			return null;
		return queue[cur].getResultElementValue(elementType);
	}

	public int getElementCount() {
		return elenemtCount;
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

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
