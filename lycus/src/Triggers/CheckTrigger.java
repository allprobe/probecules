package Triggers;

import java.util.Date;
import GlobalConstants.Enums.Condition;
import GlobalConstants.Enums.Function;
import GlobalConstants.XvalueUnit;
import Results.BaseResult;

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

	// private LastN getLastN(int n) {
	// int head = this.getTail() - n;
	// if (head < 0)
	// head += getSize();
	// LastN lastN = new LastN(this);
	// return lastN;
	// }

//	private double getDelta(String elementType) {
//		if (getTail() == 0)
//			return (double) getQueue()[getTail()].getResultElementValue(elementType).get(0) // null exception on tail=0 on start (no value inserted to array)
//					- (double) getQueue()[getSize() - 1].getResultElementValue(elementType).get(0);
//		else
//			return (double) getQueue()[getTail()].getResultElementValue(elementType).get(0)
//					- (double) getQueue()[getTail() - 1].getResultElementValue(elementType).get(0);
//	}

	// private LastN getLastNSeconds(int n) {
	// return null;
	// }
	//
	// private LastN getTimePeriod(Date time1, Date time2) {
	// return null;
	// }

//	private double getAvarage(LastN lastN, String elementType) {
//		double sum = 0;
//		int start = lastN.getHead();
//		int end = lastN.getTail();
//		if (lastN.getHead() > lastN.getTail()) {
//			start = lastN.getTail();
//			end = lastN.getHead();
//		}
//
//		while (start <= end) {
//			sum += (double) getQueue()[start++].getResultElementValue(elementType).get(0);
//		}
//		return true;
//	}
	
	public boolean isConditionMet(Trigger trigger) {
		for (TriggerCondition triggerCondition : trigger.getCondtions()) {
			Double xValue = getDouble(triggerCondition.getxValue());

			if (triggerCondition.getFunction() == Function.none) {
				if (!isNoFunctionConditionMet(triggerCondition, xValue))
					return false;
			} else if (triggerCondition.getFunction() == Function.delta) {
				Double delta = getDelta(triggerCondition.getElementType().toString());
				if (delta == null)
					return false;
				if (!isCondition(delta, triggerCondition.getCondition(), xValue, triggerCondition.getXvalueUnit()))
					return false;
			} else if (triggerCondition.getFunction() == Function.max) {
				if (!isMaxConditionMet(triggerCondition, xValue))
					return false;
			} else if (triggerCondition.getFunction() == Function.avg) {
				if (!isAvgConditionMet(triggerCondition, xValue))
					return false;
			} else if (triggerCondition.getFunction() == Function.delta_avg) {
				if (!isDeltaAvgConditionMet(triggerCondition, xValue))
					return false;
			}
		}
		return true;
	}
	private boolean isMaxConditionMet(TriggerCondition triggerCondition, Double xValue) {
		LastN lastN = getLast(triggerCondition);
		if (!lastN.isEnoughElements())
			return false;
		Object result = lastN.getNextResult(triggerCondition.getElementType().toString());
		double max = 0;

		while (result != null) {
			if (result == null || (!(result instanceof Double) && !(result instanceof Integer)) || xValue == null)
				return false;

			Double current = Double.parseDouble(result.toString());
			if (max < current)
				max = current;
			result = lastN.getNextResult(triggerCondition.getElementType().toString());
		}

		return isCondition(max, triggerCondition.getCondition(), xValue, triggerCondition.getXvalueUnit());
	}

	private boolean isAvgConditionMet(TriggerCondition triggerCondition, Double xValue) {
		LastN lastN = getLast(triggerCondition);
		if (!lastN.isEnoughElements())
			return false;
		Object result = lastN.getNextResult(triggerCondition.getElementType().toString());
		double sum = 0;
		while (result != null) {
			if (result == null || (!(result instanceof Double) && !(result instanceof Integer)) || xValue == null)
				return false;

			sum += Double.parseDouble(result.toString());
			result = lastN.getNextResult(triggerCondition.getElementType().toString());
		}

		return isCondition(sum / lastN.getElementCount(), triggerCondition.getCondition(), xValue,
				triggerCondition.getXvalueUnit());
	}

	private boolean isDeltaAvgConditionMet(TriggerCondition triggerCondition, Double xValue) {
		LastN lastN = getLast(triggerCondition);
		if (!lastN.isEnoughElements())
			return false;
		Object result = lastN.getNextResult(triggerCondition.getElementType().toString());
		double sum = 0;
		while (result != null) {
			if (result == null || (!(result instanceof Double) && !(result instanceof Integer)) || xValue == null)
				return false;

			sum += Double.parseDouble(result.toString());
			result = lastN.getNextResult(triggerCondition.getElementType().toString());
		}

		double delta_avg = sum / lastN.getElementCount() - (double) getQueue()[getTail()]
				.getResultElementValue(triggerCondition.getElementType().toString()).get(0);
		return isCondition(delta_avg, triggerCondition.getCondition(), xValue, triggerCondition.getXvalueUnit());
	}

	private boolean isNoFunctionConditionMet(TriggerCondition triggerCondition, Double xValue) {
		LastN lastN = getLast(triggerCondition);
		if (!lastN.isEnoughElements())
			return false;
		Object result = lastN.getNextResult(triggerCondition.getElementType().toString());
		int nValue = lastN.getElementCount();

		while (result != null && nValue > 0) {
			if (result == null || xValue == null)
				return false;

			if (!(result instanceof Double) && !(result instanceof Integer)) {// result always Object - need to convert to inherited object
				if (!isCondition(result.toString(), triggerCondition.getCondition(), triggerCondition.getxValue(),
						triggerCondition.getXvalueUnit()))
					return false;

			} else {
				if (!isCondition(Double.parseDouble(result.toString()), triggerCondition.getCondition(), xValue,
						triggerCondition.getXvalueUnit()))
					return false;
			}

			nValue--;
			result = lastN.getNextResult(triggerCondition.getElementType().toString());
		}

		return true;
	}

	private boolean isCondition(Double result, Condition condition, double xValue, XvalueUnit xvalueUnit) {
		switch (condition) {
		case bigger:
			return result > xvalueUnit.getBasic((long) xValue, xvalueUnit);
		case equal:
			return result == xvalueUnit.getBasic((long) xValue, xvalueUnit);
		case tinier:
			return result < xvalueUnit.getBasic((long) xValue, xvalueUnit);
		case not_equal:
			return result != xvalueUnit.getBasic((long) xValue, xvalueUnit);
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
//			return new LastN(this, triggerondition.getnValue());
		}
		return null;
	}

	private Double getDelta(String elementType) {
		if (getTail() > 1 && getQueue()[0] != null && getQueue()[1] != null) {
			if (getTail() == 0)
				return (double) getQueue()[getTail()].getResultElementValue(elementType).get(0)
						- (double) getQueue()[getSize() - 1].getResultElementValue(elementType).get(0);
			else
				return (double) getQueue()[getTail()].getResultElementValue(elementType).get(0)
						- (double) getQueue()[getTail() - 1].getResultElementValue(elementType).get(0);
		}
		return null;
	}

//	private double getAverage(LastN lastN, String elementType) {
//		double sum = 0;
//		int start = lastN.getHead();
//		int end = lastN.getTail();
//		if (lastN.getHead() > lastN.getTail()) {
//			start = lastN.getTail();
//			end = lastN.getHead();
//		}
//
//		while (start <= end) {
//			sum += (double) getQueue()[start++].getResultElementValue(elementType).get(0);
//		}
//
//		return sum / Math.abs(lastN.getTail() - lastN.getHead() + 1);
//	}

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
			this.setHead(checkTrigger.getTail() - nValue + 1);
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
		if (queue[cur] == null) // cur is the new pointer to the array index that havent been intialized yet
			return null;
		return queue[cur].getResultElementValue(elementType).get(0);
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
