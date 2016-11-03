package lycus;

public class Event {
	private String triggerId;
	private boolean isStatus;   // false -The trigger is triggered /  true - when trigger is no longer active.
	private long time;          // TimeStamp
	private boolean isSent;
	private String userId;
	private String bucketId;
	private boolean isDeleted;
	private long originalTimeStamp;     
	private String extraInfo;
	
	public Event(String triggerId, String userId, String bucketId) {
		this.triggerId = triggerId;
		this.setUserId(userId);
		this.setBucketId(bucketId);
		this.isStatus = false;
		this.time = System.currentTimeMillis();
		this.originalTimeStamp = this.time;
		this.isSent = false;
		this.setDeleted(false);
	}

	public String getTriggerId() {
		return triggerId;
	}

	public void setTriggerId(String triggerId) {
		this.triggerId = triggerId;
	}

	public synchronized boolean getIsStatus() {
		return isStatus;
	}

	public synchronized void setIsStatus(boolean status) {
		if (status == true)
		{
			this.time = System.currentTimeMillis();
		}
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

	public Long getOriginalTimeStamp() {
		return originalTimeStamp;
	}

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
}
