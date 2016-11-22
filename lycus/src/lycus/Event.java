package lycus;

import Utils.Logit;

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
	private String subType;
	private String hostName;
	private String hostNotificationGroup;
	private String triggerName;
	private String triggerSeverity;
	
	public Event(String triggerId, String userId, String bucketId, String hostName, String hostNotificationGroup, String triggerName, String triggerSeverity) {
		this.triggerId = triggerId;
		this.setUserId(userId);
		this.setBucketId(bucketId);
		this.isStatus = false;
		this.time = System.currentTimeMillis();
		this.originalTimeStamp = this.time;
		this.isSent = false;
		this.setDeleted(false);
		this.setSubType("regular");
		this.setHostName(hostName);
		this.setHostNotificationGroup(hostNotificationGroup);
		this.setTriggerName(triggerName);
		this.setTriggerSeverity(triggerSeverity);
	}
	
//	public Event(String triggerId, String userId, String bucketId, boolean sent) {
//		this.triggerId = triggerId;
//		this.setUserId(userId);
//		this.setBucketId(bucketId);
//		this.isStatus = false;
//		this.time = System.currentTimeMillis();
//		this.originalTimeStamp = this.time;
//		this.isSent = sent;
//		this.setDeleted(false);
//		this.setSubType("regular");
//	}
	
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
		if(this.getTriggerId().contains("b631bd96-e2e6-4163-940b-ff376d7d2138@port_5da313b7-d802-4b95-80b3-34e1747cb60a@57593ba5-9d91-4686-ad2b-83e824a334b0"))
			Logit.LogDebug("BREAKPOINT");
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

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getHostNotificationGroup() {
		return hostNotificationGroup;
	}
	public void setHostNotificationGroup(String hostNotificationGroup) {
		this.hostNotificationGroup = hostNotificationGroup;
	}

	public String getTriggerName() {
		return triggerName;
	}

	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	public String getTriggerSeverity() {
		return triggerSeverity;
	}

	public void setTriggerSeverity(String triggerSeverity) {
		this.triggerSeverity = triggerSeverity;
	}
}
