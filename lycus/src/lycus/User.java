package lycus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.snmp4j.smi.OID;
import Collectors.BaseCollector;
import Collectors.SnmpCollector;
import Collectors.SqlCollector;
import GlobalConstants.Constants;
import GlobalConstants.Enums;
import GlobalConstants.Enums.SnmpDataType;
import GlobalConstants.XvalueUnit;
import Model.HostParams;
import Model.ProbeParams;
import Model.CollectorParams;
import Probes.BaseProbe;
import Probes.DiscoveryProbe;
import Probes.HttpProbe;
import Probes.IcmpProbe;
import Probes.PortProbe;
import Probes.RBLProbe;
import Probes.SnmpProbe;
import Probes.SqlProbe;
import Probes.TracerouteProbe;
import Utils.GeneralFunctions;
import Utils.Logit;

public class User {
	private UUID userId;
	private String email;
	private Map<UUID, Host> hosts;
	private Map<String, BaseProbe> templateProbes;
	private Map<String, BaseCollector> collectors;

	public User(UUID userId) {
		this.setUserId(userId);
		this.setHosts(new HashMap<UUID, Host>());
		this.setTemplateProbes(new HashMap<String, BaseProbe>());
		this.setCollectors(new HashMap<String, BaseCollector>());
	}

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

	public Map<String, BaseCollector> getCollectors() {
		return collectors;
	}

	public void setCollectors(Map<String, BaseCollector> collectors) {
		this.collectors = collectors;
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

		// Host h = getHosts().get(host.getHostId());
		return true;
	}

	// return false when no more hosts
	public Boolean removeHost(UUID hostId) {
		if (hosts.containsKey(hostId)) {
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

	public boolean isSnmpTemplateExist(String snmp_template_id) {
		return collectors.containsKey(snmp_template_id);
	}

	public void addHost(HostParams hostParams) {
		try {
			UUID host_id = UUID.fromString(hostParams.host_id);
			String name = hostParams.name;
			String ip = hostParams.hostIp;
			boolean status = (hostParams.hostStatus).equals("1");
			String bucket = hostParams.bucket;

			String notif_groups = null;
			try {
				if (!hostParams.notificationGroups.equals("none"))
					notif_groups = hostParams.notificationGroups;
			} catch (Exception e) {
				Logit.LogWarn("Unable to parse notifications group: " + hostParams.notificationGroups + ", E: "
						+ e.getMessage());
				notif_groups = null;
			}

			String sql_template = hostParams.sqlTemplate;
			SnmpCollector snmpTemplate = (SnmpCollector) this.getCollectors().get(hostParams.snmpTemplate);
			SqlCollector sqlTemplate = (SqlCollector) this.getCollectors().get(hostParams.sqlTemplate);
			
			Host host = new Host(host_id, name, ip, snmpTemplate, sqlTemplate, status, bucket, notif_groups, getUserId().toString());
			addHost(host);
 
		} catch (Exception e) {
			Logit.LogWarn("Creation of Host Failed: " + hostParams + " , not added! E: " + e.getMessage());
		}
	}

	public void addSnmpTemplate(CollectorParams snmpTemplateParams) {
		try {
			// UUID userId = UUID.fromString(snmpTemplateParams.user_id);
			String templateId = snmpTemplateParams.id;
			String name = snmpTemplateParams.name;
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

			SnmpCollector snmpTemplate = null;
			if (version <= 2)
				snmpTemplate = new SnmpCollector(templateId, name, commName, version, port, timeout, true);
			else if (version == 3) {
				snmpTemplate = new SnmpCollector(templateId, name, version, port, sec, authUser, authPass, authMethod,
						cryptPass, cryptMethod, timeout, true);
			}
			else
			{
				Logit.LogWarn("Snmp template with bad version, will skip: templateID=" + templateId);

			}

			this.getCollectors().put(snmpTemplate.getId(), snmpTemplate);

		} catch (Exception e) {
			Logit.LogWarn("Unable to add SNMP Template: " + snmpTemplateParams.id.toString() + " , not added!");
		}
	}

	public void addSqlTemplate(CollectorParams snmpCollectorParams) {
		try {
			// UUID userId = UUID.fromString(snmpTemplateParams.user_id);

			// Complete add.
			String collectorId = snmpCollectorParams.id;
			String name = snmpCollectorParams.name;
			String user_id = snmpCollectorParams.user_id;
			Integer timeout = snmpCollectorParams.timeout;

			String sql_sec = snmpCollectorParams.sql_sec;
			String sql_user = snmpCollectorParams.sql_user;
			String sql_password = snmpCollectorParams.sql_password;
			Integer sql_port = snmpCollectorParams.sql_port;
			String sql_type = snmpCollectorParams.sql_type;

			SqlCollector sqlCollector = new SqlCollector(collectorId, name, user_id, timeout, sql_port, sql_sec,
					sql_user, sql_type, sql_password);

			this.getCollectors().put(sqlCollector.getId(), sqlCollector);

		} catch (Exception e) {
			Logit.LogWarn("Unable to add SNMP Template: " + snmpCollectorParams.id.toString() + " , not added!");
		}
	}

	public boolean removeTemplateProbe(String templateId) {
		templateProbes.remove(templateId);
		return true;
	}

	/**
	 * @param probeParams
	 * @return
	 */
	public BaseProbe addTemplateProbe(ProbeParams probeParams) {
		try {
			UUID templateId = UUID.fromString(probeParams.template_id);
			String probeId = probeParams.probe_id;

			String name = probeParams.name;
			int interval = probeParams.interval;
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

				boolean deepCheck = probeParams.http_deep != null && probeParams.http_deep == 1;

				int timeout = probeParams.timeout;

				if (auth.equals(Constants.no))
					probe = new HttpProbe(this, probeId, templateId, name, interval, multiplier, status, timeout,
							method, url, deepCheck);
				else
					probe = new HttpProbe(this, probeId, templateId, name, interval, multiplier, status, timeout,
							method, url, auth, authUser, authPass, deepCheck);
				break;
			}
			case Constants.traceroute: {
				int timeout = 20000;
				probe = new TracerouteProbe(this, probeId, templateId, name, interval, multiplier, status, timeout);
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

				XvalueUnit unit = XvalueUnit.valueOf(valueUnit);
				if (unit == null) {
					Logit.LogWarn("Probe: " + probeId + " Wrong Unit Type, Doesn't Added!");
					return null;
				}
				probe = new SnmpProbe(this, probeId, templateId, name, interval, multiplier, status, oid, dataType,
						unit, storeValue);
				break;
			}
			case Constants.discovery: {
				int elementsInterval = probeParams.element_interval;
				Enums.DiscoveryElementType discoveryType;
				switch (probeParams.discovery_type) {
				case Constants.bw:
					discoveryType = Enums.DiscoveryElementType.bw;
					break;
				case Constants.ds:
					discoveryType = Enums.DiscoveryElementType.ds;
					break;
				default:
					throw new Exception("Unable to determine discovery type --- " + probeParams.probe_id);
				}

				probe = new DiscoveryProbe(this, probeId, templateId, name, interval, multiplier, status, discoveryType,
						elementsInterval);
				break;
			}
			case Constants.rbl: {
				String rblName = probeParams.rbl;
				probe = new RBLProbe(this, probeId, templateId, name, interval, multiplier, status, rblName);
				break;
			}
			case Constants.sql: {
				probe = new SqlProbe(this, probeId, templateId, name, interval, multiplier, status, probeParams.timeout, probeParams.sql_db,
						probeParams.sql_query, probeParams.sql_fields);
				break;
			}
			}
			if (probe == null) {
				Logit.LogWarn("Creation of Probe: " + probeParams.probe_id + " failed, skipping!");
				throw new Exception("Error parsing one of probe elements!");
			}

			probe.addTriggers(probeParams.triggers);
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

	// private TriggerSeverity getTriggerSev(String sev) {
	// switch (sev) {
	// case Constants.notice:
	// return TriggerSeverity.Notice;
	// case Constants.warning:
	// return TriggerSeverity.Warning;
	// case Constants.high:
	// return TriggerSeverity.High;
	// case Constants.disaster:
	// return TriggerSeverity.Disaster;
	// }
	// return null;
	// }
}
