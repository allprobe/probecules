package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lycus.Probes.Probe;
import lycus.Probes.SnmpProbe;

public class User {
	private UUID userId;
	private String email;
	private Map<UUID, Host> hosts;
	private Map<String, Probe> templateProbes;
	private Map<UUID, SnmpTemplate> snmpTemplates;
	private SnmpManager snmpManager;

	// public User(UUID userId, Map<UUID, Host> hosts,
	// Map<UUID, Template> templates, Map<UUID, SnmpTemplate> snmpTemps,
	// Set<String> runnableProbes) {
	// this.setUserId(userId);
	// this.setHosts(hosts);
	// this.setTemplates(templates);
	// this.setSnmpTemplates(snmpTemps);
	// this.setRunnableProbes(new HashMap<String, RunnableProbe>());
	// this.setRps(runnableProbes);
	// this.createRunningProbes();
	// }
	public User(UUID userId) {
		this.setUserId(userId);
		this.setHosts(new HashMap<UUID, Host>());
		this.setTemplateProbes(new HashMap<String, Probe>());
		this.setSnmpTemplates(new HashMap<UUID, SnmpTemplate>());
		this.setSnmpManager(new SnmpManager(this));
	}

	// Getters/Setters
	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Host getHost(UUID uid)
	{
		return getHosts().get(uid);
	}
	
	public Map<UUID, SnmpTemplate> getSnmpTemplates() {
		return snmpTemplates;
	}

	public void setSnmpTemplates(Map<UUID, SnmpTemplate> snmpTemplates) {
		this.snmpTemplates = snmpTemplates;
	}


	public SnmpManager getSnmpManager() {
		return snmpManager;
	}

	public void setSnmpManager(SnmpManager snmpManager) {
		this.snmpManager = snmpManager;
	}

	public Map<UUID, Host> getHosts() {
		return hosts;
	}

	public void setHosts(Map<UUID, Host> host) {
		this.hosts = host;
	}

	public Boolean isHostExist(UUID host_id)
	{
		return getHost(host_id) != null;
	}
	
	public Boolean addHost(Host host)
	{
		Host newHost = getHost(host.getHostId());
		if (newHost == null)
			hosts.put(host.getHostId(), host);
	
		return true;
	}
	
	public Map<String, Probe> getTemplateProbes() {
		return templateProbes;
	}

	public void setTemplateProbes(Map<String, Probe> templateProbes) {
		this.templateProbes = templateProbes;
	}

	public HashMap<String, RunnableProbe> getAllRunnableProbes() {
		HashMap<String,RunnableProbe> allHostsRunnableProbes=new HashMap<String,RunnableProbe>();
		Set<Host> hosts=new HashSet<Host>(this.getHosts().values());
		for(Host host:hosts)
		{
			allHostsRunnableProbes.putAll(host.getRunnableProbes());
		}
		return allHostsRunnableProbes;
	}


	public  List<RunnableProbe> getRunnableProbesFor(String probe_id) {
		List<RunnableProbe> runnableProbes = new ArrayList<RunnableProbe>();
		Set<Host> hosts=new HashSet<Host>(this.getHosts().values());
		for(Host host:hosts)
		{
			runnableProbes.addAll(host.getRunnableProbes(probe_id));
		}
		return runnableProbes;
	}
	
//	public Probe getProbe(UUID templateId, String probeId) {
//		for (Map.Entry<String, RunnableProbe> entry : this.getAllRunnableProbes().entrySet()) {
//			if (entry.getKey().contains(templateId.toString() + "@" + probeId))
//				return entry.getValue().getProbe();
//		}
//		HashMap<Host, HashMap<String, RunnableProbe>> snmpRunnableProbes = this.getSnmpManager().getProbesByHosts();
//		if (snmpRunnableProbes == null)
//			return null;
//		for (Map.Entry<Host, HashMap<String, RunnableProbe>> entry : snmpRunnableProbes.entrySet()) {
//			if (entry.getValue() != null) {
//				for (Map.Entry<String, RunnableProbe> entry2 : entry.getValue().entrySet()) {
//					if (entry2.getKey().contains(templateId.toString() + "@" + probeId))
//						return entry2.getValue().getProbe();
//				}
//			}
//		}
//		return null;
//	}
	
	public void addRunnableProbe(String rpID)
	{
		String probeId=rpID.split("@")[2];
		UUID hostId=UUID.fromString(rpID.split("@")[1]);
		Host host=this.getHosts().get(hostId);
		Probe probe=this.getTemplateProbes().get(probeId);
		RunnableProbe newRunnableProbe;

		if(probe==null||host==null)
		{
			SysLogger.Record(new Log("Unable to create Runnable Probe: "+rpID+", one of its elements is missing!",LogType.Error));
			return;
		}
		try {
			newRunnableProbe=new RunnableProbe(host,probe);
		} catch (Exception e) {
			SysLogger.Record(new Log("Unable to create Runnable Probe: "+rpID+", check probe type!",LogType.Error,e));
			return;
		}
		host.getRunnableProbes().put(rpID, newRunnableProbe);
	}

	private void runProbes(List<RunnableProbe> rps) {
		for (RunnableProbe rp : rps) {
			this.startRunnableProbe(rp);
		}
	}

	public void runProbesAtStart() {
		this.runProbes(new ArrayList<RunnableProbe>(this.getAllRunnableProbes().values()));
		// this.runSnmpVer2_3();
	}

	// public void runSnmpVer2_3() {
	// this.getSnmpManager().runAllBatches();
	// }

//	public Probe getProbeByID(String probeId) {
//		for (Map.Entry<String, Probe> p : this.getTemplateProbes().entrySet()) {
//			if(p.getKey().split("@")[1].equals(probeId))
//			{
//				return p.getValue();
//			}
//			}
//		return null;
//		}

//	public Set<Host> getHostsByTemplate(Template t) {
//		if (t == null)
//			return null;
//		Set<Host> hosts = new HashSet<Host>();
//		for (String rpString : this.getAllRunnableProbes().keySet()) {
//			if (rpString.contains(t.getTemplateId().toString())) {
//				hosts.add(this.getHosts().get(UUID.fromString(rpString.split("@")[1])));
//			}
//		}
//		for (Map<String, RunnableProbe> rps : this.getSnmpManager().getProbesByHosts().values()) {
//			for (String rpString : rps.keySet()) {
//				if (rpString.contains(t.getTemplateId().toString())) {
//					hosts.add(this.getHosts().get(UUID.fromString(rpString.split("@")[1])));
//					break;
//				}
//			}
//		}
//		return hosts;
//
//	}
	
	public List<RunnableProbe> getRPSbyProbeID(String probeId) {
		List<RunnableProbe> matchedRps = new ArrayList<RunnableProbe>();
		HashMap<String, RunnableProbe> allRps = this.getAllRunnableProbes();
		for (Map.Entry<String, RunnableProbe> entry : allRps.entrySet()) {
			if (entry.getKey().contains(probeId)) {
				matchedRps.add(entry.getValue());
			}
		}
		return matchedRps;
	}
	
	public List<RunnableProbe> getRPSbyTemplateIdHostId(UUID templateId,UUID hostId) {
		List<RunnableProbe> matchedRps = new ArrayList<RunnableProbe>();
		HashMap<String, RunnableProbe> allRps = this.getAllRunnableProbes();
		for (Map.Entry<String, RunnableProbe> entry : allRps.entrySet()) {
			if (entry.getKey().contains(templateId.toString()+"@"+hostId.toString())) {
				matchedRps.add(entry.getValue());
			}
		}
		return matchedRps;
	}

	private boolean reInsertProbeRunningPool(String probeId) {
		List<RunnableProbe> rps = this.getRPSbyProbeID(probeId);
		if (rps == null) {
			SysLogger.Record(new Log("Unable to re enable probe: " + probeId + " no RPS found!", LogType.Error));
			return false;
		}
		for (RunnableProbe rp : rps) {
			if (this.stopRunnableProbe(rp))
				return this.startRunnableProbe(rp);
		}
		return false;
	}

	public boolean startRunnableProbe(RunnableProbe rp) {
		if (rp.getProbe() instanceof SnmpProbe) {
			this.getSnmpManager().startProbe(rp);
			return true;
		} 
			return rp.start();
	}

	public boolean stopRunnableProbe(RunnableProbe rp) {
		if(rp==null)
			return false;
		if (rp.getProbe() instanceof SnmpProbe && rp.getHost().getSnmpTemp().getVersion() > 1) {
			this.getSnmpManager().stopProbe(rp);
			return true;
		} 
		try {
			rp.stop();
			return true;
		} catch (Exception e) {
			SysLogger.Record(new Log("Unable To stop RunnableProbe: "+rp.getRPString(),LogType.Error));
			return false;
		}
	}

	public boolean addRunnableProbe(RunnableProbe rp)
	{
		this.getHost(rp.getHost().getHostId()).getRunnableProbes().put(rp.getRPString(), rp);
		return this.startRunnableProbe(rp);
	}
	
	public boolean removeRunnableProbe(RunnableProbe rp)
	{
		UUID hostId=rp.getHost().getHostId();
		this.getHosts().get(hostId).getRunnableProbes().remove(hostId);
		if(this.getHosts().get(hostId).getRunnableProbes().size()==0)
		{
			this.getSnmpTemplates().remove(this.getHosts().get(hostId).getSnmpTemp().getSnmpTemplateId());
			this.getHosts().remove(hostId);
		}
		return this.stopRunnableProbe(rp);
	}

	public String toString() {
		return this.getUserId().toString();
	}
	
	public Probe getProbeFor(String probe_id)
	{
		Map<String, Probe> probes = getTemplateProbes();
		return probes.get(probe_id);
	}
	
	public boolean isProbeExist(String probe_id)
	{
		return getProbeFor(probe_id) != null;
	}

	public boolean removeRunnableProbes(String probe_id)
	{
		List<RunnableProbe> runnableProbes = getRunnableProbesFor(probe_id);
		for (RunnableProbe runnableProbe : runnableProbes)
		{
			removeRunnableProbe(runnableProbe);
		}	
		return true;
	}
}
