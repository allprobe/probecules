package lycus;

import Triggers.Trigger;

public class Event {
	private String triggerId;
	private boolean isStatus;   // false -The trigger is triggered /  true - when trigger is no longer active.
	private long time;
	private boolean isSent;
	private String userId;
	private String bucketId;
	private boolean isDeleted;

	public Event(String triggerId, String userId, String bucketId) {
		this.triggerId = triggerId;
		this.setUserId(userId);
		this.setBucketId(bucketId);
		this.isStatus = false;
		this.time = System.currentTimeMillis();
		this.isSent = false;
		this.setDeleted(false);
	}

	public String getTrigger() {
		return triggerId;
	}

	public void setTrigger(String triggerId) {
		this.triggerId = triggerId;
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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getBucketId() {
		return bucketId;
	}

	public void setBucketId(String bucketId) {
		this.bucketId = bucketId;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}
