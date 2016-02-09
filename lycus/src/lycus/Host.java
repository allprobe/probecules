package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Host {
	private UUID hostId;
	private String name;
	private String hostIp;
	private SnmpTemplate snmpTemp;
	private boolean snmpStatus;
	private boolean hostStatus;
	private String bucket;
	private UUID notificationGroups;
	private HashMap<String,RunnableProbe> runnableProbes;
	

	public Host(UUID host_id, String name, String host_ip,
			SnmpTemplate snmpTemp,boolean hostStatus, boolean snmpStatus,String bucket,UUID notifGroups) {
		this.setName(name);
		this.setHostId(host_id);
		this.setHostIp(host_ip);
		this.setSnmpTemp(snmpTemp);
		this.setSnmpStatus(snmpStatus);
		this.setHostStatus(hostStatus);
		this.setRunnableProbes(new HashMap<String,RunnableProbe>());
		this.setBucket(bucket);
		this.setNotificationGroups(notifGroups);
	}
	public Host(UUID host_id, String name, String host_ip,
			boolean hostStatus, boolean snmpStatus,String bucket,UUID notifGroups) {
		this.setName(name);
		this.setHostId(host_id);
		this.setHostIp(host_ip);
		this.setSnmpStatus(snmpStatus);
		this.setHostStatus(hostStatus);
		this.setBucket(bucket);
		this.setNotificationGroups(notifGroups);
		this.setRunnableProbes(new HashMap<String,RunnableProbe>());
	}

	// Getters/Setters
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

	public synchronized boolean isSnmpStatus() {
		return snmpStatus;
	}

	public void setSnmpStatus(boolean snmpStatus) {
		this.snmpStatus = snmpStatus;
	}

	public SnmpTemplate getSnmpTemp() {
		return snmpTemp;
	}

	public void setSnmpTemp(SnmpTemplate snmpTemp) {
		this.snmpTemp = snmpTemp;
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
	
	public HashMap<String,RunnableProbe> getRunnableProbes() {
		return runnableProbes;
	}

	public RunnableProbe getRunnableProbe(UUID uid)
	{
		return getRunnableProbes().get(uid);
	}
	
	private void setRunnableProbes(HashMap<String,RunnableProbe> runnableProbes) {
		this.runnableProbes = runnableProbes;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Host ID:").append(this.getHostId().toString()).append("; ");
		s.append("Host IP:").append(this.getHostIp().toString()).append("; ");
		return s.toString();
	}

}
