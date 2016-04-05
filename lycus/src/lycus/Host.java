package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import Elements.NicElement;

public class Host {
	private UUID hostId;
	private String name;
	private String hostIp;
	private SnmpTemplate snmpTemp;
	private boolean snmpStatus;
	private boolean hostStatus;
	private String bucket;
	private UUID notificationGroups;
	private List<NicElement> nicElements;
	
	public Host(UUID host_id, String name, String host_ip,
			SnmpTemplate snmpTemp,boolean hostStatus, boolean snmpStatus,String bucket,UUID notifGroups, String userId) {
		this.setName(name);
		this.setHostId(host_id);
		this.setHostIp(host_ip);
		this.setSnmpTemp(snmpTemp);
		this.setSnmpStatus(snmpStatus);
		this.setHostStatus(hostStatus);
		this.setBucket(bucket);
		this.setNotificationGroups(notifGroups);
	}
	public Host(UUID host_id, String name, String host_ip,
			boolean hostStatus, boolean snmpStatus,String bucket,UUID notifGroups, String userId) {
		this.setName(name);
		this.setHostId(host_id);
		this.setHostIp(host_ip);
		this.setSnmpStatus(snmpStatus);
		this.setHostStatus(hostStatus);
		this.setBucket(bucket);
		this.setNotificationGroups(notifGroups);
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

	public boolean getHostStatus()
	{
		return hostStatus;
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
	
//	public HashMap<String,RunnableProbe> getRunnableProbes() {
//		return runnableProbes;
//	}

//	public RunnableProbe getRunnableProbe(UUID uid)
//	{
//		return getRunnableProbes().get(uid);
//	}
	
//	public List<RunnableProbe> getRunnableProbes(String probe_id)
//	{
//		List<RunnableProbe> runnableProbes = new ArrayList<RunnableProbe>();
//		for (RunnableProbe runnableProbe : getRunnableProbes().values())
//		{
//			if (runnableProbe.getProbe().getProbe_id().equals(probe_id))
//				runnableProbes.add(runnableProbe);
//		}
//		return runnableProbes;
//	}
	
	
//	private void setRunnableProbes(HashMap<String,RunnableProbe> runnableProbes) {
//		this.runnableProbes = runnableProbes;
//	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Host ID:").append(this.getHostId().toString()).append("; ");
		s.append("Host IP:").append(this.getHostIp().toString()).append("; ");
		return s.toString();
	}

//	public boolean removeRunnableProbes(UUID teplate_id)
//	{
//		List<String> keys = new ArrayList();
//		for (String key : this.runnableProbes.keySet())
//		{
//			if (runnableProbes.get(key).getProbe().getTemplate_id().equals(teplate_id))
//				keys.add(key);
//		}
//		
//		for (String key : keys)
//		{
//			try
//			{
//				RunnableProbe runnableProbe = this.runnableProbes.get(key);
//				runnableProbe.stop();
//				runnableProbe.getProbe().getTriggers().clear();
//				this.runnableProbes.remove(key);
//				
//			}
//			catch (Exception ex)
//			{
//				
//			}
//		}
//		return true;
//	}
	
//	public RunnableProbe getRunnableProbe(String runnableProbeId)
//	{
//		return getRunnableProbes().get(runnableProbeId);
//	}
	
//	public List<RunnableProbe> getRunnableProbes(UUID teplate_id)
//	{
//		List<RunnableProbe> runnableProbes =  new  ArrayList();
//		for (RunnableProbe runnableProbe : this.runnableProbes.values())
//		{
//			if (runnableProbe.getProbe().getTemplate_id().equals(teplate_id))
//				runnableProbes.add(runnableProbe);
//		}
//		return runnableProbes;
//	}
}
