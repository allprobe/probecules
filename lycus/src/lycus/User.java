	package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.snmp4j.smi.OID;

import Elements.BaseElement;
import Elements.DiskElement;
import GlobalConstants.Constants;
import GlobalConstants.Enums;
import GlobalConstants.LogType;
import GlobalConstants.SnmpDataType;
import GlobalConstants.SnmpUnit;
import GlobalConstants.TriggerSeverity;
import Model.DiscoveryElementParams;
import Model.HostParams;
import Model.ProbeParams;
import Model.SnmpTemplateParams;
import Probes.BaseProbe;
import Probes.DiscoveryProbe;
import Probes.HttpProbe;
import Probes.IcmpProbe;
import Probes.NicProbe;
import Probes.PortProbe;
import Probes.RBLProbe;
import Probes.SnmpProbe;
import Utils.GeneralFunctions;
import Utils.Logit;

public class User {
	private UUID userId;
	private String email;
	private Map<UUID, Host> hosts;
	private Map<String, BaseProbe> templateProbes;
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
		this.setTemplateProbes(new HashMap<String, BaseProbe>());
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

	public Host getHost(UUID uid) {
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

	public Boolean isHostExist(UUID host_id) {
		return getHost(host_id) != null;
	}

	public Boolean deleteBucket(String bucket) {
		for (Host host : getHosts().values()) {
			if (host.getBucket().equals(bucket))
				host.setBucket(Constants.default1);
		}
		return true;
	}

	public Boolean addHost(Host host) {
		Host newHost = getHost(host.getHostId());
		if (newHost == null)
			hosts.put(host.getHostId(), host);

		return true;
	}

	// return false when no more hosts
	public Boolean removeHost(UUID hostId) {
		if (hosts.containsKey(hostId))
		{
			getHosts().remove(hostId);
			return !getHosts().isEmpty();
		}

		return true;
	}
	
	public Map<String, BaseProbe> getTemplateProbes() {
		return templateProbes;
	}

	public void setTemplateProbes(Map<String, BaseProbe> templateProbes) {
		this.templateProbes = templateProbes;
	}

//	public HashMap<String, RunnableProbe> getAllRunnableProbes() {
//		HashMap<String, RunnableProbe> allHostsRunnableProbes = new HashMap<String, RunnableProbe>();
//		Set<Host> hosts = new HashSet<Host>(this.getHosts().values());
//		for (Host host : hosts) {
//			allHostsRunnableProbes.putAll(host.getRunnableProbes());
//		}
//		return allHostsRunnableProbes;
//	}

//	public List<RunnableProbe> getRunnableProbesFor(String probe_id) {
//		List<RunnableProbe> runnableProbes = new ArrayList<RunnableProbe>();
//		Set<Host> hosts = new HashSet<Host>(this.getHosts().values());
//		for (Host host : hosts) {
//			runnableProbes.addAll(host.getRunnableProbes(probe_id));
//		}
//		return runnableProbes;
//	}

	// public Probe getProbe(UUID templateId, String probeId) {
	// for (Map.Entry<String, RunnableProbe> entry :
	// this.getAllRunnableProbes().entrySet()) {
	// if (entry.getKey().contains(templateId.toString() + "@" + probeId))
	// return entry.getValue().getProbe();
	// }
	// HashMap<Host, HashMap<String, RunnableProbe>> snmpRunnableProbes =
	// this.getSnmpManager().getProbesByHosts();
	// if (snmpRunnableProbes == null)
	// return null;
	// for (Map.Entry<Host, HashMap<String, RunnableProbe>> entry :
	// snmpRunnableProbes.entrySet()) {
	// if (entry.getValue() != null) {
	// for (Map.Entry<String, RunnableProbe> entry2 :
	// entry.getValue().entrySet()) {
	// if (entry2.getKey().contains(templateId.toString() + "@" + probeId))
	// return entry2.getValue().getProbe();
	// }
	// }
	// }
	// return null;
	// }

	private void runProbes(List<RunnableProbe> rps) {
		for (RunnableProbe rp : rps) {
			if (rp.getId().contains(
					"0b05919c-6cc0-42cc-a74b-de3b0dcd4a2a@6aadf750-e887-43ee-b872-326c94fbab7c@discovery_6b54463e-fe1c-4e2c-a090-452dbbf2d510"))
				Logit.LogDebug("BREAKPOINT");
			this.startRunnableProbe(rp);
		}
	}

//	public void runProbesAtStart() {
////		this.runProbes(new ArrayList<RunnableProbe>(this.getAllRunnableProbes().values()));
//		this.runProbes(new ArrayList<RunnableProbe>(RunnableProbeContainer.getInstanece().getByUser(getUserId().toString()).values()));
//		;
//		// this.runSnmpVer2_3();
//	}

	// public void runSnmpVer2_3() {
	// this.getSnmpManager().runAllBatches();
	// }

	// public Probe getProbeByID(String probeId) {
	// for (Map.Entry<String, Probe> p : this.getTemplateProbes().entrySet()) {
	// if(p.getKey().split("@")[1].equals(probeId))
	// {
	// return p.getValue();
	// }
	// }
	// return null;
	// }

	// public Set<Host> getHostsByTemplate(Template t) {
	// if (t == null)
	// return null;
	// Set<Host> hosts = new HashSet<Host>();
	// for (String rpString : this.getAllRunnableProbes().keySet()) {
	// if (rpString.contains(t.getTemplateId().toString())) {
	// hosts.add(this.getHosts().get(UUID.fromString(rpString.split("@")[1])));
	// }
	// }
	// for (Map<String, RunnableProbe> rps :
	// this.getSnmpManager().getProbesByHosts().values()) {
	// for (String rpString : rps.keySet()) {
	// if (rpString.contains(t.getTemplateId().toString())) {
	// hosts.add(this.getHosts().get(UUID.fromString(rpString.split("@")[1])));
	// break;
	// }
	// }
	// }
	// return hosts;
	//
	// }

//	public List<RunnableProbe> getRPSbyProbeID(String probeId) {
//		List<RunnableProbe> matchedRps = new ArrayList<RunnableProbe>();
//		HashMap<String, RunnableProbe> allRps = this.getAllRunnableProbes();
//		for (Map.Entry<String, RunnableProbe> entry : allRps.entrySet()) {
//			if (entry.getKey().contains(probeId)) {
//				matchedRps.add(entry.getValue());
//			}
//		}
//		return matchedRps;
//	}

//	public List<RunnableProbe> getRPSbyTemplateIdHostId(UUID templateId, UUID hostId) {
//		List<RunnableProbe> matchedRps = new ArrayList<RunnableProbe>();
//		HashMap<String, RunnableProbe> allRps = this.getAllRunnableProbes();
//		for (Map.Entry<String, RunnableProbe> entry : allRps.entrySet()) {
//			if (entry.getKey().contains(templateId.toString() + "@" + hostId.toString())) {
//				matchedRps.add(entry.getValue());
//			}
//		}
//		return matchedRps;
//	}


	public boolean startRunnableProbe(RunnableProbe rp) {
//		if (rp.getProbe() instanceof SnmpProbe) {
//			this.getSnmpManager().startProbe(rp);
//			return true;
//		}

		return rp.start();
	}

	public boolean stopRunnableProbe(RunnableProbe rp) {
		if (rp == null)
			return false;
		if (rp.getProbe() instanceof SnmpProbe && rp.getHost().getSnmpTemp().getVersion() > 1) {
			this.getSnmpManager().stopProbe(rp);
			return true;
		}
		try {
			rp.stop();
			return true;
		} catch (Exception e) {
			Logit.LogError("User - stopRunnableProbe", "Unable To stop RunnableProbe: " + rp.getId());
			return false;
		}
	}

	public boolean addRunnableProbe(RunnableProbe runnableProbe) {
		String rpStr = runnableProbe.getId();
		if (rpStr.contains(
				"inner_996a80bf-913e-4ba4-ad46-a28c30f9fe36"))
			Logit.LogDebug("BREAKPOINT");

		RunnableProbeContainer.getInstanece().add(runnableProbe);
		return this.startRunnableProbe(runnableProbe);
	}

//	public boolean removeRunnableProbe(RunnableProbe runnableProbe) {
//		UUID hostId = runnableProbe.getHost().getHostId();
//		RunnableProbeContainer.getInstanece().remove(runnableProbe);
//		
//		if (this.getHosts().get(hostId).getRunnableProbes().size() == 0) {
//			this.getSnmpTemplates().remove(this.getHosts().get(hostId).getSnmpTemp().getSnmpTemplateId());
//			this.getHosts().remove(hostId);
//		}
//		return this.stopRunnableProbe(runnableProbe);
//	}

	public String toString() {
		return this.getUserId().toString();
	}

	public BaseProbe getProbeFor(String probe_id) {
		Map<String, BaseProbe> probes = getTemplateProbes();
		return probes.get(probe_id);
	}

	public boolean isProbeExist(String probe_id) {
		return getProbeFor(probe_id) != null;
	}

//	public boolean removeRunnableProbes(String probe_id) {
//		List<RunnableProbe> runnableProbes = getRunnableProbesFor(probe_id);
//		for (RunnableProbe runnableProbe : runnableProbes) {
//			removeRunnableProbe(runnableProbe);
//		}
//		return true;
//	}

	public void addHost(HostParams hostParams) {
		try {
			UUID host_id = UUID.fromString(hostParams.host_id);
			String name = hostParams.name;
			String ip = hostParams.hostIp;
			boolean status = (hostParams.hostStatus).equals("1") ? true : false;
			String bucket = hostParams.bucket;

			UUID notif_groups;
			try {
				notif_groups = UUID.fromString(hostParams.notificationGroups);
			} catch (Exception e) {
				Logit.LogWarn("Unable to parse notifications group: " + hostParams.notificationGroups+", E: "+e.getMessage());
				notif_groups = null;
			}

			UUID snmp_template;
			try {
				snmp_template = UUID.fromString(hostParams.snmpTemp);
			} catch (Exception e) {
				Logit.LogWarn("Unable to parse snmp template id for host: " + hostParams.host_id);
				snmp_template = null;
			}

			Host host;

			if (snmp_template == null) {
				host = new Host(host_id, name, ip, status, true, bucket, notif_groups, getUserId().toString());
			} else {
				SnmpTemplate snmpTemp = this.getSnmpTemplates().get(snmp_template);
				host = new Host(host_id, name, ip, snmpTemp, status, true, bucket, notif_groups, getUserId().toString());
			}
			this.getHosts().put(host_id, host);
		} catch (Exception e) {
			Logit.LogWarn("Creation of Host Failed: " + hostParams + " , not added! E: "+e.getMessage());
		}
	}

	public void addSnmpTemplate(SnmpTemplateParams snmpTemplateParams) {
		try {
			UUID userId = UUID.fromString(snmpTemplateParams.user_id);
			UUID templateId = UUID.fromString(snmpTemplateParams.snmp_template_id);
			String name = snmpTemplateParams.template_name;
			int version = snmpTemplateParams.version;
			String commName = snmpTemplateParams.community;
			String sec = snmpTemplateParams.sec;
			String authMethod = snmpTemplateParams.auth_method;
			String authUser = GeneralFunctions.Base64Decode(snmpTemplateParams.username);
			String authPass = GeneralFunctions.Base64Decode(snmpTemplateParams.password);
			String cryptMethod = snmpTemplateParams.crypt_method;
			String cryptPass = GeneralFunctions.Base64Decode(snmpTemplateParams.crypt_password);
			int timeout = snmpTemplateParams.timeout;
			int port = snmpTemplateParams.port;

			SnmpTemplate snmpTemp = null;
			if (version <= 2)
				snmpTemp = new SnmpTemplate(templateId, name, commName, version, port, timeout, true);
			else
				snmpTemp = new SnmpTemplate(templateId, name, version, port, sec, authUser, authPass, authMethod,
						cryptPass, cryptMethod, timeout, true);

			this.getSnmpTemplates().put(snmpTemp.getSnmpTemplateId(), snmpTemp);

		} catch (Exception e) {
			Logit.LogWarn("Unable to add SNMP Template: " + snmpTemplateParams.snmp_template_id.toString() + " , not added!");
		}

	}

	public BaseProbe addTemplateProbe(ProbeParams probeParams) {
		try {
			UUID templateId = UUID.fromString(probeParams.template_id);
			String probeId = probeParams.probe_id;

			String name = probeParams.name;
			long interval = probeParams.interval;
			float multiplier = probeParams.multiplier;
			boolean status = probeParams.is_active;
			String type = probeParams.type;

			BaseProbe probe = null;

			switch (type) {
			case Constants.icmp: {
				int npings = probeParams.npings;
				int bytes = probeParams.bytes;
				int timeout = probeParams.timeout;
				probe = new IcmpProbe(this, probeId, templateId, name, interval, multiplier, status, timeout, npings,
						bytes);
				break;
			}
			case Constants.port: {
				String proto = probeParams.protocol;
				int port = probeParams.port;
				int timeout = probeParams.timeout;
				probe = new PortProbe(this, probeId, templateId, name, interval, multiplier, status, timeout, proto,
						port);
				break;
			}
			case Constants.http: {
				String url = GeneralFunctions.Base64Decode(probeParams.url);
				String method = probeParams.http_request;
				String auth = probeParams.http_auth;
				String authUser = GeneralFunctions.Base64Decode(probeParams.http_auth_username);
				String authPass = GeneralFunctions.Base64Decode(probeParams.http_auth_password);
				int timeout = probeParams.timeout;

				if (auth.equals(Constants.no))
					probe = new HttpProbe(this, probeId, templateId, name, interval, multiplier, status, timeout,
							method, url);
				else
					probe = new HttpProbe(this, probeId, templateId, name, interval, multiplier, status, timeout,
							method, url, auth, authUser, authPass);
				break;
			}
			case Constants.snmp: {

				OID oid = new OID(probeParams.oid);
				Enums.SnmpStoreAs storeValue = probeParams.snmp_store_as == 0 ? Enums.SnmpStoreAs.asIs
						: Enums.SnmpStoreAs.delta;
				String valueType = probeParams.snmp_datatype;
				String valueUnit = probeParams.snmp_unit;
				SnmpDataType dataType = getSnmpDataType(valueType);
				if (dataType == null) {
					Logit.LogWarn("Probe: " + probeId + " Wrong Data Type, Doesn't Added!");
					return null;
				}

				SnmpUnit unit = getSnmpUnit(valueUnit);
				if (unit == null) {
					Logit.LogWarn("Probe: " + probeId + " Wrong Unit Type, Doesn't Added!");
					return null;
				}
				probe = new SnmpProbe(this,probeId, templateId, name, interval, multiplier, status, oid, dataType, unit,
						storeValue);
				break;
			}
			case Constants.discovery: {
				long elementsInterval = probeParams.discovery_elements_interval;
				int triggerCode = probeParams.discovery_trigger_code;
				String triggerXValue = probeParams.discovery_trigger_x;
				String unitType = probeParams.snmp_unit;
				String triggerUuid = probeParams.discovery_trigger_id;
				TriggerSeverity severity = getTriggerSev(probeParams.discovery_trigger_severity);
				SnmpUnit trigValueUnit = getSnmpUnit(unitType);
				if (trigValueUnit == null) {
					Logit.LogError("User = addTemplateProbe()", "Probe: " + probeId + " Wrong Unit Type, Doesn't Added!");
					return null;
				}
				Enums.DiscoveryElementType discoveryType;
				switch (probeParams.discovery_type) {
				case Constants.bw:
					discoveryType = Enums.DiscoveryElementType.nics;
					break;
				case Constants.ds:
					discoveryType = Enums.DiscoveryElementType.disks;
					break;
				default:
					throw new Exception("Unable to determine discovery type --- " + probeParams.probe_id);
				}

				probe = new DiscoveryProbe(this, probeId, templateId, name, interval, multiplier, status, discoveryType,
						elementsInterval);

				String triggerId = templateId.toString() + "@" + probeId + "@" + triggerUuid;
				ArrayList<TriggerCondition> conditions = new ArrayList<TriggerCondition>();
				TriggerCondition condition = new TriggerCondition(triggerCode, "and", triggerXValue, "");
				conditions.add(condition);
				Trigger discoveryTrigger = new Trigger(triggerId, name, probe, severity, status, "", trigValueUnit,
						conditions);
				probe.addTrigger(discoveryTrigger);
				break;
			}
			case Constants.rbl: {
				String rblName = probeParams.rbl;
				probe = new RBLProbe(this, probeId, templateId, name, interval, multiplier, status, rblName);
				break;
			}
			}
			if (probe == null) {
				Logit.LogWarn("Creation of Probe: " + probeParams.probe_id + " failed, skipping!");
				throw new Exception("Error parsing one of probe elements!");
			}
			this.getTemplateProbes().put(probeId, probe);
			return probe;
		} catch (Exception e) {
			Logit.LogWarn("Creation of Probe Failed: " + probeParams + " , not added!\n" + e.getMessage());
			return null;

		}
	}

	private SnmpDataType getSnmpDataType(String valueType) {
		SnmpDataType dataType;
		switch (valueType) {
		case Constants.integer:
			dataType = SnmpDataType.Numeric;
			break;
		case Constants.string:
			dataType = SnmpDataType.Text;
			break;
		case Constants._float:
			dataType = SnmpDataType.Numeric;
			break;
		case Constants._boolean:
			dataType = SnmpDataType.Text;
			break;
		default: {
			dataType = null;

		}
		}
		return dataType;
	}

	private SnmpUnit getSnmpUnit(String unitType) {
		SnmpUnit unit;
		switch (unitType) {
		case Constants.b:
			unit = SnmpUnit.bits;
			break;
		case Constants.B:
			unit = SnmpUnit.bytes;
			break;
		case Constants.Kb:
			unit = SnmpUnit.kbits;
			break;
		case Constants.KB:
			unit = SnmpUnit.kbytes;
			break;
		case Constants.Mb:
			unit = SnmpUnit.mbits;
			break;
		case Constants.MB:
			unit = SnmpUnit.mbytes;
			break;
		case Constants.Gb:
			unit = SnmpUnit.gbits;
			break;
		case Constants.GB:
			unit = SnmpUnit.gbytes;
			break;
		case Constants.none:
			unit = SnmpUnit.none;
			break;
		case "":
			unit = SnmpUnit.none;
			break;

		default: {
			unit = SnmpUnit.none;
		}
		}
		return unit;
	}

	private TriggerSeverity getTriggerSev(String sev) {
		switch (sev) {
		case Constants.notice:
			return TriggerSeverity.Notice;
		case Constants.warning:
			return TriggerSeverity.Warning;
		case Constants.high:
			return TriggerSeverity.High;
		case Constants.disaster:
			return TriggerSeverity.Disaster;
		}
		return null;
	}

//	public void addNewDiscoveryElement(BaseElement newElement, Host host) {
//		if(newElement==null || host==null)
//			return;
//			
//		if (newElement instanceof NicProbe) {
//			this.addNicRunnableProbes((NicProbe) newElement, host);
//		} else if (newElement instanceof DiskElement) {
//			this.addDiskRunnableProbes((NicProbe) newElement);
//		}
//	}

	private void addDiskRunnableProbes(NicProbe newElement) {
		// TODO User.addDiskRunnableProbes

	}

//	private void addNicRunnableProbes(NicProbe newElement, Host host) {
//		this.templateProbes.put(newElement.getProbe_id(), newElement);
//		if (newElement == null || host == null) {
//			Logit.LogError("User - addNicRunnableProbes()", "Unable to create Runnable Probe: " + newElement.getTemplate_id().toString() + "@"
//					+ host.getHostId().toString() + "@" + newElement.getProbe_id()
//					+ ", one of its elements is missing!");
//			return;
//		}
//		if (!newElement.isActive())
//			return;
//
//		RunnableProbe inOctetsRunnableProbe;
//		RunnableProbe outOctetsRunnableProbe;
//
//		try {
//			inOctetsRunnableProbe = new RunnableProbe(host, newElement.getIfInOctets());
//			outOctetsRunnableProbe = new RunnableProbe(host, newElement.getIfOutOctets());
//		} catch (Exception e) {
//			Logit.LogError("User - addNicRunnableProbes()", "Unable to create Runnable Probe: " + newElement.getTemplate_id().toString() + "@"
//					+ host.getHostId().toString() + "@" + newElement.getProbe_id() + ", check probe type!\n" + e.getMessage());
//			return;
//		}
//		
////		HashMap<String, RunnableProbe> runnableProbes = RunnableProbeContainer.getInstanece().getByHost(host.getHostId().toString());
////		if (runnableProbes != null)
////		{
//			RunnableProbeContainer.getInstanece().add(inOctetsRunnableProbe);
//			RunnableProbeContainer.getInstanece().add(inOctetsRunnableProbe);
////		}
////		this.getHost(host.getHostId()).getRunnableProbes().put(inOctetsRunnableProbe.getId(), inOctetsRunnableProbe);
////		this.getHost(host.getHostId()).getRunnableProbes().put(outOctetsRunnableProbe.getId(),outOctetsRunnableProbe);
//
//	}

//	public void removeDiscoveryElement(BaseElement baseElement) {
//		if (baseElement instanceof NicProbe) {
//			this.removeNicRunnableProbes((NicProbe) baseElement);
//		} else if (baseElement instanceof DiskElement) {
//			this.addDiskRunnableProbes((NicProbe) baseElement);
//		}
//	}

//	private void removeNicRunnableProbes(NicProbe baseElement) {
//		for (RunnableProbe runnableProbe : RunnableProbeContainer.getInstanece().getByProbe(baseElement.getProbe_id()).values()) {
//			RunnableProbeContainer.getInstanece().remove(runnableProbe);
////			this.removeRunnableProbe(runnableProbe);
//		}
//	}

}
