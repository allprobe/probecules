package Triggers;

import java.util.Date;

import GlobalConstants.Enums.Condition;
import GlobalConstants.Enums.Function;
import GlobalConstants.Enums.LastType;
import GlobalConstants.XvalueUnit;
import Results.BaseResult;
import lycus.Trigger;
import lycus.TriggerCondition;

public class CheckTrigger {
	private BaseResult[] queue;
	private int size;
	private int head = 0;
	private int tail = 0;
	private boolean empty = true;

	public CheckTrigger(int interval) {
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

		empty = false;
	}

	// private LastN getLastN(int n) {
	// int head = this.getTail() - n;
	// if (head < 0)
	// head += getSize();
	// LastN lastN = new LastN(this);
	// return lastN;
	// }

	private double getDelta(String elementType) {
		if (getTail() == 0)
			return (double) getQueue()[getTail()].getResultElementValue(elementType).get(0)
					- (double) getQueue()[getSize() - 1].getResultElementValue(elementType).get(0);
		else
			return (double) getQueue()[getTail()].getResultElementValue(elementType).get(0)
					- (double) getQueue()[getTail() - 1].getResultElementValue(elementType).get(0);
	}

	// private LastN getLastNSeconds(int n) {
	// return null;
	// }
	//
	// private LastN getTimePeriod(Date time1, Date time2) {
	// return null;
	// }

	private double getAvarage(LastN lastN, String elementType) {
		double sum = 0;
		int start = lastN.getHead();
		int end = lastN.getTail();
		if (lastN.getHead() > lastN.getTail()) {
			start = lastN.getTail();
			end = lastN.getHead();
		}

		while (start <= end) {
			sum += (double) getQueue()[start++].getResultElementValue(elementType).get(0);
		}

		return sum / Math.abs(lastN.getTail() - lastN.getHead() + 1);
	}

	public boolean isConditionMet(Trigger trigger) {
		for (TriggerCondition triggerCondition : trigger.getCondtions()) {
			if (triggerCondition.getFunction() == Function.none) {
				LastN lastN = getLast(triggerCondition);
				Object result = null;
				do {
					result = lastN.getNextResult(triggerCondition.getElementType().toString());
					if (result == null)
						break;
					
					
					
				} while (result != null);
			} else if (triggerCondition.getFunction() == Function.delta) {
//				double  lastValue = 

				
			} else if (triggerCondition.getFunction() == Function.max) {

			} else if (triggerCondition.getFunction() == Function.avg) {
				LastN lastN = getLast(triggerCondition);
				Object result = lastN.getNextResult(triggerCondition.getElementType().toString());
				Double xValue = null;
				double sum = 0;
				while (result != null) {
					xValue = getDouble(triggerCondition.getxValue());
					if (result == null || (!(result instanceof Double) && !(result instanceof Integer) )|| xValue == null)
						return false;

					sum += Double.parseDouble(result.toString());
					result = lastN.getNextResult(triggerCondition.getElementType().toString());
				} 

				return isCondition(sum / lastN.getElementCount(), triggerCondition.getCondition(), xValue,
						triggerCondition.getXvalueUnit());
			}
		}
		return false;
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

	private Double getDouble(String value) {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private LastN getLast(TriggerCondition triggerondition) {
		switch (triggerondition.getLast_type()) {
		case N:
			return new LastN(triggerondition.getnValue(), this);
		case H:
			return new LastN(this, triggerondition.getnValue());
		case P:
			// return getTimePeriod((triggerondition.getnValue());
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

	// Implement
	// Gets the set of indexes for last N hours
	public LastN(CheckTrigger checkTrigger, int pValue) {
		this.setSize(checkTrigger.getSize());
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
		if (queue[cur] == null)
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
