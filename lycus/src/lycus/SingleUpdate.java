package lycus;

import java.util.List;
import java.util.UUID;

public class SingleUpdate {
	private UUID updateId;
	private String updateType;
	private String objectId;
	private UUID userId;
	private UUID templateId;
	private UUID hostId;
	private List<String> updateValue;
	private int executeAttempts;

	public SingleUpdate(UUID updateId, String updateType, String objectId, UUID userId, UUID templateId, UUID hostId,
			List<String> updateValue, int executeAttempts) {
		this.updateId = updateId;
		this.updateType = updateType;
		this.objectId = objectId;
		this.userId = userId;
		this.templateId = templateId;
		this.hostId = hostId;
		this.updateValue = updateValue;
		this.executeAttempts = executeAttempts;
	}

	public String getUpdateType() {
		return updateType;
	}

	public void setUpdateType(String updateType) {
		this.updateType = updateType;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public UUID getTemplateId() {
		return templateId;
	}

	public void setTemplateId(UUID templateId) {
		this.templateId = templateId;
	}

	public UUID getHostId() {
		return hostId;
	}

	public void setHostId(UUID hostId) {
		this.hostId = hostId;
	}

	public List<String> getUpdateValue() {
		return updateValue;
	}

	public void setUpdateValue(List<String> updateValue) {
		this.updateValue = updateValue;
	}

	public UUID getUpdateId() {
		return updateId;
	}

	private void setUpdateId(UUID updateId) {
		this.updateId = updateId;
	}

	public int getExecuteAttempts() {
		return executeAttempts;
	}

	private void setExecuteAttempts(int executeAttempts) {
		this.executeAttempts = executeAttempts;
	}

	public void decreaseExecuteAttempts() {
		this.setExecuteAttempts(this.getExecuteAttempts() - 1);
	}

	public List<String> getUpdateValuekey() {
		return GeneralFunctions.probeKeyFormatOrdered((String) this.getUpdateValue().get(6));
	}

	private boolean mergeTemplateHost() {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean unMergeTemplateHost() {
		// TODO Auto-generated method stub
		return false;
	}

//	private boolean deleteHost() {
//		return MainContainer.unMergeTemplateHost(this.getUserId(), this.getTemplateId(), this.getHostId());
//	}
//
//	private boolean updateProbe() {
//		String probeId = this.getObjectId();
//		UUID userId = this.getUserId();
//		UUID templateId = this.getTemplateId();
//		List<String> updateValueList = this.getUpdateValue();
//		String probeName = updateValueList.get(1);
//		String probeType = updateValueList.get(2);
//		long probeInterval = Long.parseLong(updateValueList.get(3));
//		double probeMultiplier = Double.parseDouble(updateValueList.get(4));
//		boolean probeStatus = (updateValueList.get(5)).equals("1") ? true : false;
//		List<String> probeKey = this.getUpdateValuekey();
//		return MainContainer.updateRunnableProbe(userId, templateId, probeId, probeName, probeType, probeInterval,
//				probeMultiplier, probeStatus, probeKey);
//	}

//	public boolean execute() {
//		switch (this.getUpdateType()) {
//		case "pud":
//			return this.updateProbe();
//		case "nht":
//			return this.mergeTemplateHost();
//		case "delht":
//			return this.unMergeTemplateHost();
//		case "delh":
//			return this.deleteHost();
//		default:
//			return false;
//		}
//	}
}
