package lycus;

import java.util.List;
import java.util.UUID;

import Collectors.BaseCollector;
import Collectors.SnmpCollector;
import Collectors.SqlCollector;
import Elements.NicElement;

public class Host {
	private UUID hostId;
	private String name;
	private String hostIp;
	private SnmpCollector snmpCollector;
	private SqlCollector sqlCollector;
	private boolean hostStatus;
	private String bucket;
	private UUID notificationGroups;
	private List<NicElement> nicElements;

	public Host(UUID host_id, String name, String host_ip, SnmpCollector snmpTemplate, boolean hostStatus, String bucket, UUID notificationGroups, String userId) {
		this.setName(name);
		this.setHostId(host_id);
		this.setHostIp(host_ip);
		this.setCollector(snmpTemplate);
		this.setHostStatus(hostStatus);
		this.setBucket(bucket);
		this.setNotificationGroups(notificationGroups);
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

	public SnmpCollector getSnmpCollector() {
		return snmpCollector;
	}

	public void setCollector(SnmpCollector snmpCollector) {
		this.snmpCollector = snmpCollector;
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

	public SqlCollector getSqlCollector() {
		return sqlCollector;
	}

	public void setSqlCollector(SqlCollector sqlCollector) {
		this.sqlCollector = sqlCollector;
	}
}
