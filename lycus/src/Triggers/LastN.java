package Triggers;

import java.util.Date;
import Results.BaseResult;
import Results.SnmpResult;
import Utils.Logit;

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
		try {
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
		} catch (Exception e) {
			Logit.LogError("EventTrigger - LastN()", "Error, Creating N vector", e);
			e.printStackTrace();
		}
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
		if (this.current >= getSize())
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
	
	public boolean isEnoughElements() {
		if (tail > head)
			return elenemtCount <= tail - head + 1;

		return true;
	}
}
