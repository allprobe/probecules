package lycus;

public class Event {
//	private RunnableProbe rp;
	private Trigger trigger;
	private boolean status;
	private long time;
	private boolean sent;

	public Event(Trigger trigger, boolean status) {
		// this.rp = rp;
		this.trigger = trigger;
		this.status = status;
		this.time = System.currentTimeMillis();
		this.sent = false;
	}

	// public RunnableProbe getRp() {
	// return rp;
	// }
	//
	// public void setRp(RunnableProbe rp) {
	// this.rp = rp;
	// }

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
		return sent;
	}

	public synchronized void setSent(boolean sent) {
		this.sent = sent;
	}

}
