package lycus;

import Triggers.Trigger;

public class Event {
	private Trigger trigger;
	private boolean isTriggered;   // The trigger is triggered / false - when trigger is no longer active.
	private long time;
	private boolean isSent;

	public Event(Trigger trigger) {
		this.trigger = trigger;
		this.isTriggered = false;
		this.time = System.currentTimeMillis();
		this.isSent = false;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

	public synchronized boolean getIsTriggered() {
		return isTriggered;
	}

	public synchronized void setIsTriggered(boolean status) {
		this.isTriggered = status;
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
