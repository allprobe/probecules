package lycus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import Collectors.BaseCollector;
import Collectors.SnmpTemplate;
import Elements.NicElement;

public class Host {
	private UUID hostId;
	private UUID userId;
	private String name;
	private String hostIp;
	private BaseCollector snmpCollector;
	private boolean hostStatus;
	private String bucket;
	private UUID notificationGroups;
	private List<NicElement> nicElements;

	public Host(UUID host_id, String name, String host_ip, SnmpTemplate snmpTemplate, boolean hostStatus, String bucket,
			UUID notificationGroups, String userId) {
		this.setName(name);
		this.setHostId(host_id);
		this.setUserId(UUID.fromString(userId));
		this.setHostIp(host_ip);
		this.setHostStatus(hostStatus);
		this.setBucket(bucket);
		this.setNotificationGroups(notificationGroups);
		this.snmpCollector = snmpTemplate;
	}

	public UUID getHostId() {
		return hostId;
	}

	public void setHostId(UUID hostId) {
		this.hostId = hostId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public boolean isHostStatus() {
		return hostStatus;
	}

	public synchronized void setHostStatus(boolean hostStatus) {
		this.hostStatus = hostStatus;
	}

	public boolean getHostStatus() {
		return hostStatus;
	}

	public SnmpTemplate getSnmpCollector() {
		if(this.snmpCollector==null)
		return null;
		return (SnmpTemplate)this.snmpCollector;
	}
	public void setSnmpCollector(SnmpTemplate snmpTemplate) {
		this.snmpCollector=snmpTemplate;
	}
	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public UUID getNotificationGroups() {
		return notificationGroups;
	}

	public void setNotificationGroups(UUID notificationGroups) {
		this.notificationGroups = notificationGroups;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Host ID:").append(this.getHostId().toString()).append("; ");
		s.append("Host IP:").append(this.getHostIp().toString()).append("; ");
		return s.toString();
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}
}
