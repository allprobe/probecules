package lycus;

import Triggers.Trigger;

public class Event {
	private Trigger trigger;
	private boolean status;   // The trigger is triggered / false - when trigger is no longet active.
	private long time;
	private boolean isSent;

	public Event(Trigger trigger) {
		this.trigger = trigger;
		this.status = false;
		this.time = System.currentTimeMillis();
		this.isSent = false;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

	public synchronized boolean isStatus() {
		return status;
	}

	public synchronized void setStatus(boolean status) {
		this.status = status;
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
