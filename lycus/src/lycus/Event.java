package lycus;

import Triggers.Trigger;

public class Event {
	private Trigger trigger;
	private boolean isStatus;   // false -The trigger is triggered /  true - when trigger is no longer active.
	private long time;
	private boolean isSent;

	public Event(Trigger trigger) {
		this.trigger = trigger;
		this.isStatus = false;
		this.time = System.currentTimeMillis();
		this.isSent = false;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

	public synchronized boolean getIsStatus() {
		return isStatus;
	}

	public synchronized void setIsStatus(boolean status) {
		this.isStatus = status;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public synchronized boolean isSent() {
		return isSent;
	}

	public synchronized void setSent(boolean sent) {
		this.isSent = sent;
	}
}
