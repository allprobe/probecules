/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.snmp4j.smi.OID;

import com.google.gson.Gson;

import GlobalConstants.Constants;
import GlobalConstants.Enums;
import GlobalConstants.LogType;
import GlobalConstants.SnmpDataType;
import GlobalConstants.TriggerSeverity;
import Model.HostParams;
import Model.ProbeParams;
import lycus.Probes.DiscoveryProbe;
import lycus.Probes.PingerProbe;
import lycus.Probes.PorterProbe;
import lycus.Probes.Probe;
import lycus.Probes.RBLProbe;
import lycus.Probes.SnmpProbe;
import lycus.Probes.WeberProbe;


/**
 * 
 * @author Roi
 */
public class UsersManager {

	private static HashMap<UUID, User> users;
	private static boolean initialized;

	public static void Initialize() {
		setUsers(new HashMap<UUID, User>());
		if (!UsersManager.Build()) {
			setInitialized(false);
			return;
		}
		SysLogger.Record(new Log("Server successfully initialized with all user's data.", LogType.Info));
		setInitialized(true);
	}

	public static HashMap<UUID, User> getUsers() {
		return users;
	}

	public static User getUser(UUID uid) {
		return getUsers().get(uid);
	}

	public static void setUsers(HashMap<UUID, User> users) {
		UsersManager.users = users;
	}

	public static boolean isInitialized() {
		return initialized;
	}

	public static void setInitialized(boolean initialized) {
		UsersManager.initialized = initialized;
	}

	public static boolean Build() {
		HashMap<UUID, User> user_s = UsersManager.getUsers();
		JSONObject initServer = UsersManager.getServerInfoFromApi();

		HashMap<String, UUID> runnableProbesIds = ApiInterface.getInitRPs(initServer.get("long_ids"));
		HashMap<String, UUID> probeByUser = getProbeByUser(runnableProbesIds);

		if (runnableProbesIds == null) {
			SysLogger.Record(new Log("no probes found for this server!", LogType.Info));
			return false;
		}

		Set<UUID> usersIds = new HashSet<UUID>(runnableProbesIds.values());
		initUsers(usersIds);

		JSONArray allSnmpTemplatesJson = (JSONArray) initServer.get("snmp_templates");
		addSnmpTemplates(allSnmpTemplatesJson);

		JSONArray allHostsJson = (JSONArray) initServer.get("hosts");
		addHosts(allHostsJson);

		JSONArray allTemplateProbesJson = (JSONArray) initServer.get("probes");
		addTemplates(allTemplateProbesJson, probeByUser);

		JSONArray allTemplateTriggersJson = (JSONArray) initServer.get("triggers");
		addTriggers(allTemplateTriggersJson, probeByUser);

		addRPs(runnableProbesIds);

		return true;
	}

	public static void runAtStart() {
		Set<User> allUsers = new HashSet<User>(getUsers().values());
		for (User user : allUsers) {
			user.runProbesAtStart();
		}
	}

	private static HashMap<String, UUID> getProbeByUser(HashMap<String, UUID> runnableProbesIds) {
		HashMap<String, UUID> probeByUser = new HashMap<String, UUID>();
		for (Map.Entry<String, UUID> rp : runnableProbesIds.entrySet()) {
			probeByUser.put(rp.getKey().split("@")[2], rp.getValue());
		}
		return probeByUser;
	}

	private static HashMap<UUID, Set<String>> probeByUser(Map<String, UUID> rps) {
		HashMap<UUID, Set<String>> probeByUser = new HashMap<UUID, Set<String>>();
		for (Map.Entry<String, UUID> rp : rps.entrySet()) {
			UUID userId = rp.getValue();
			String _rp = rp.getKey();
			if (probeByUser.containsKey(userId)) {
				probeByUser.get(userId).add(_rp.split("@")[2]);
			} else {
				probeByUser.put(userId, new HashSet<String>());
				probeByUser.get(userId).add(_rp.split("@")[2]);
			}
		}
		return probeByUser;
	}

	private static void initUsers(Set<UUID> usersIds) {
		for (UUID userId : usersIds) {
			User user = new User(userId);
			getUsers().put(userId, user);
		}
	}

	private static void addSnmpTemplates(JSONArray allSnmpTemplatesJson) {
		for (int i = 0; i < allSnmpTemplatesJson.size(); i++) {
			JSONObject snmpTempJson = (JSONObject) allSnmpTemplatesJson.get(i);
			try {
				UUID userId = UUID.fromString((String) snmpTempJson.get("snmp_user_id"));
				UUID templateId = UUID.fromString((String) snmpTempJson.get("snmp_template_id"));
				String name = (String) snmpTempJson.get("snmp_template_name");
				int version = Integer.parseInt((String) snmpTempJson.get("snmp_version"));
				String commName = (String) snmpTempJson.get("snmp_community");
				String sec = (String) snmpTempJson.get("snmp_sec");
				String authMethod = (String) snmpTempJson.get("snmp_auth_method");
				String authUser = GeneralFunctions.Base64Decode((String) snmpTempJson.get("snmp_user"));
				String authPass = GeneralFunctions.Base64Decode((String) snmpTempJson.get("snmp_auth_password"));
				String cryptMethod = (String) snmpTempJson.get("snmp_crypt_method");
				String cryptPass = GeneralFunctions.Base64Decode((String) snmpTempJson.get("snmp_crypt_password"));
				int timeout = Integer.parseInt((String) snmpTempJson.get("snmp_timeout"));
				int port = Integer.parseInt((String) snmpTempJson.get("snmp_port"));
				SnmpTemplate snmpTemp = null;
				if (version <= 2)
					snmpTemp = new SnmpTemplate(templateId, name, commName, version, port, timeout, true);
				else
					snmpTemp = new SnmpTemplate(templateId, name, version, port, sec, authUser, authPass, authMethod,
							cryptPass, cryptMethod, timeout, true);

				User u = getUsers().get(userId);
				if (snmpTemp != null && u != null)
					u.getSnmpTemplates().put(templateId, snmpTemp);
				else
					SysLogger.Record(
							new Log("Unable to add snmp template: " + snmpTempJson.toJSONString(), LogType.Warn));
			} catch (Exception e) {
				SysLogger.Record(
						new Log("Creation of Snmp Template Failed: " + snmpTempJson.toJSONString() + " , not added!",
								LogType.Warn, e));
				continue;
			}
		}
	}

	public static void addHosts(JSONArray allHostsJson) {
		for (int i = 0; i < allHostsJson.size(); i++) {
			JSONObject hostJson = (JSONObject) allHostsJson.get(i);
				UUID user_id = UUID.fromString((String) hostJson.get("user_id"));
				User user = getUsers().get(user_id);

				HostParams hostParams = new HostParams();
				hostParams.host_id=(String) hostJson.get("host_id");
				hostParams.name=(String) hostJson.get("host_name");
				hostParams.hostIp=(String) hostJson.get("ip");
				hostParams.hostStatus=(String) hostJson.get("status");
				hostParams.bucket=(String) hostJson.get("bucket");
				hostParams.notificationGroups=(String) hostJson.get("notifications_group");
				hostParams.snmpTemp=(String) hostJson.get("snmp_template");

				user.addHost(hostParams);
		}
	}

	// private static ArrayList<UUID> convertNotificationGroupsArray(Object
	// notifs) {
	// if (notifs == null)
	// return null;
	// JSONArray notifGroups = (JSONArray) notifs;
	// ArrayList<UUID> groups = new ArrayList<UUID>();
	// for (int i = 0; i < notifGroups.size(); i++) {
	// groups.add(UUID.fromString((String) (notifGroups.get(i))));
	// }
	// return groups;
	// }

	private static void addTemplates(JSONArray allTemplateProbesJson, HashMap<String, UUID> probeByUser) {
		for (int i = 0; i < allTemplateProbesJson.size(); i++) {
			JSONObject probeJson = (JSONObject) allTemplateProbesJson.get(i);
			JSONObject probeKeyJson;
			try{
			UUID user_id = UUID.fromString((String) probeJson.get("user_id"));
			User user = getUsers().get(user_id);

			ProbeParams probeParams=new ProbeParams();
			
			probeParams.template_id=(String) probeJson.get("template_id");
			probeParams.probe_id = (String) probeJson.get("probe_id");
				probeParams.name = (String) probeJson.get("probe_name");
				probeParams.interval = Long.parseLong(probeJson.get("probe_interval").toString());
				probeParams.multiplier = Float.parseFloat(probeJson.get("probe_multiplier").toString());
				probeParams.is_active = probeJson.get("probe_status").toString().equals("1") ? true : false;
				probeParams.type = (String) probeJson.get("probe_type");
				probeKeyJson= (JSONObject) probeJson.get("probe_key");

				Probe probe = null;

				switch (probeParams.type) {
				case Constants.icmp: {
					probeParams.npings = Integer.parseInt(probeKeyJson.get("npings").toString());
					probeParams.bytes = Integer.parseInt(probeKeyJson.get("bytes").toString());
					probeParams.timeout =Integer.parseInt(probeKeyJson.get("timeout").toString());
					break;
				}
				case Constants.port: {
					String proto = (String) probeKeyJson.get("proto");
					probeParams.port = Integer.parseInt(probeKeyJson.get("port").toString());
					probeParams.timeout = Integer.parseInt(probeKeyJson.get("timeout").toString());
					probeParams.port_extra = (String) probeKeyJson.get("port_extra");
					break;
				}
				case Constants.http: {
					
					probeParams.url = (String) probeKeyJson.get("urls");
					probeParams.http_request = (String) probeKeyJson.get("http_method");
					probeParams.http_auth = (String) probeKeyJson.get("http_auth");
					probeParams.http_auth_username = (String) probeKeyJson.get("http_auth_user");
					probeParams.http_auth_password = (String) probeKeyJson.get("http_auth_password");
					probeParams.timeout = Integer.parseInt(probeKeyJson.get("timeout").toString());

					break;
				}
				case Constants.snmp: {

					probeParams.oid = (String) probeKeyJson.get("snmp_oid");
					probeParams.snmp_store_as = Integer.parseInt(probeKeyJson.get("store_value_as").toString());
					probeParams.snmp_datatype = (String) probeKeyJson.get("value_type");
					probeParams.snmp_unit = (String) probeKeyJson.get("value_unit");
					break;
				}
				case Constants.discovery: {
					probeParams.discovery_elements_interval = Integer.parseInt(probeKeyJson.get("element_interval").toString());
					probeParams.discovery_trigger_code = Integer.parseInt(probeKeyJson.get("discovery_trigger").toString());
					probeParams.discovery_trigger_x = probeKeyJson.get("discovery_trigger_x_value").toString();
					probeParams.snmp_unit= (String) probeKeyJson.get("discovery_trigger_unit");
					probeParams.discovery_trigger_id = (String) probeKeyJson.get("trigger_id");
					probeParams.discovery_trigger_severity = (String) probeKeyJson.get("trigger_severity");
					break;
				}
				case Constants.rbl: {
					probeParams.rbl = (String) probeKeyJson.get("rbl");
					break;
				}
				}
				user.addTemplateProbe(probeParams);
			}
			catch(Exception e)
			{
				SysLogger.Record(new Log("Unable to parse probe params for:"+ probeJson,LogType.Warn));
				continue;
			}
		}

	}

	public  static SnmpDataType getSnmpDataType(String valueType) {
		SnmpDataType dataType;
		switch (valueType) {
			case "integer":
				dataType = SnmpDataType.Numeric;
				break;
			case "string":
				dataType = SnmpDataType.Text;
				break;
			case "float":
				dataType = SnmpDataType.Numeric;
				break;
			case "boolean":
				dataType = SnmpDataType.Text;
				break;
			default: {
				dataType = null;
	
			}
		}
		return dataType;
	}

	private static void addTriggers(JSONArray allTemplateTriggersJson, HashMap<String, UUID> probeByUser) {
		for (int i = 0; i < allTemplateTriggersJson.size(); i++) {
			JSONObject triggerJson = (JSONObject) allTemplateTriggersJson.get(i);
			try {
				UUID templateId = UUID.fromString((String) triggerJson.get("template_id"));
				String probeId = (String) triggerJson.get("probe_id");
				String triggerId = (String) triggerJson.get("trigger_id");

				String name = (String) triggerJson.get("trigger_name");
				TriggerSeverity severity = getTriggerSev((String) triggerJson.get("severity"));
				if (severity == null)
					SysLogger.Record(new Log("Unable to get trigger severity for: " + triggerId, LogType.Warn));
				boolean status = ((String) triggerJson.get("status")).equals("1") ? true : false;
				String elementType = (String) triggerJson.get("trigger_type");
				String unitType = (String) triggerJson.get("xvalue_unit");
				SnmpUnit trigValueUnit = getSnmpUnit(unitType);

				ArrayList<TriggerCondition> conditions = getTriggerConds((JSONArray) triggerJson.get("conditions"));

				User user = getUsers().get(probeByUser.get(probeId));
				if (user == null) {
					SysLogger
							.Record(new Log("No user exists for trigger: " + triggerJson.toJSONString(), LogType.Warn));
					continue;
				}
				Probe probe = user.getTemplateProbes().get(probeId);
				if (probe == null) {
					SysLogger.Record(
							new Log("No probe exists for trigger: " + triggerJson.toJSONString(), LogType.Warn));
					continue;
				}

				Trigger trigger = new Trigger(triggerId, name, probe, severity, status, elementType, trigValueUnit,
						conditions);

				probe.addTrigger(trigger);

			} catch (Exception e) {
				SysLogger.Record(new Log("Creation of Trigger Failed: " + triggerJson.toJSONString() + " , not added!",
						LogType.Warn, e));
				continue;
			}
		}
	}

	public static SnmpUnit getSnmpUnit(String unitType) {
		SnmpUnit unit;
		switch (unitType) {
		case "b":
			unit = SnmpUnit.bits;
			break;
		case "B":
			unit = SnmpUnit.bytes;
			break;
		case "Kb":
			unit = SnmpUnit.kbits;
			break;
		case "KB":
			unit = SnmpUnit.kbytes;
			break;
		case "Mb":
			unit = SnmpUnit.mbits;
			break;
		case "MB":
			unit = SnmpUnit.mbytes;
			break;
		case "Gb":
			unit = SnmpUnit.gbits;
			break;
		case "GB":
			unit = SnmpUnit.gbytes;
			break;
		case "int":
			unit = SnmpUnit.integer;
			break;
		case "str":
			unit = SnmpUnit.string;
			break;
		case "":
			unit = null;
			break;

		default: {
			unit = null;
//			throw new IOException("Unable to create SnmpUnit unreadble value: " + unitType);
		}
		}
		return unit;
	}

	private static ArrayList<TriggerCondition> getTriggerConds(JSONArray jsonArray) {
		ArrayList<TriggerCondition> conditions = new ArrayList<TriggerCondition>();

		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject conditionJson = (JSONObject) jsonArray.get(i);
			int code = Integer.parseInt((String) conditionJson.get("code"));
			String andOr = (String) conditionJson.get("and_or");
			String xValue = (String) conditionJson.get("xvalue");
			String tValue = (String) conditionJson.get("tvalue");
			TriggerCondition condition = new TriggerCondition(code, andOr, xValue, tValue);
			conditions.add(condition);

		}
		return conditions;
	}

	private static TriggerSeverity getTriggerSev(String sev) {
		switch (sev) {
		case "notice":
			return TriggerSeverity.Notice;
		case "warning":
			return TriggerSeverity.Warning;
		case "high":
			return TriggerSeverity.High;
		case "disaster":
			return TriggerSeverity.Disaster;
		}
		return null;
	}

	private static void addRPs(HashMap<String, UUID> runnableProbesIds) {
		for (Map.Entry<String, UUID> rp : runnableProbesIds.entrySet()) {
			UUID userID = rp.getValue();
			String rpID = rp.getKey();

			if (rpID.contains(
					"0b05919c-6cc0-42cc-a74b-de3b0dcd4a2a@6aadf750-e887-43ee-b872-326c94fbab7c@discovery_6b54463e-fe1c-4e2c-a090-452dbbf2d510"))
				System.out.println("BREAKPOINT");
			
			User u = getUsers().get(userID);
			u.addRunnableProbe(rpID);
		}
	}

	public static HashMap<String, RunnableProbe> getAllUsersRunnableProbes() {
		HashMap<String, RunnableProbe> allRps = new HashMap<String, RunnableProbe>();
		for (User user : getUsers().values()) {
			HashMap<String, RunnableProbe> allUserRps = user.getAllRunnableProbes();
			allRps.putAll(allUserRps);
		}
		return allRps;
	}

	public static void printUsers() {
		for (User user : getUsers().values()) {
			System.out.println("---User:" + user.getUserId() + "---");
			System.out.println(user.toString());
		}
	}

	// public static boolean updateRunnableProbe(UUID userId, UUID templateId,
	// String probeNewName, String probeId,
	// String probeType, long probeNewInterval, float probeNewMultiplier,
	// boolean probeNewStatus,
	// List<String> probeKey) {
	// User user = getUsers().get(userId);
	// if (user == null) {
	// SysLogger.Record(
	// new Log("User: " + userId.toString() + " Doesn't Exists! Probe Update
	// Failed!", LogType.Error));
	// return false;
	// }
	// switch (probeType) {
	// case "ICMP":
	// return user.updatePingerProbe(templateId, probeId, probeNewName,
	// probeNewInterval, probeNewMultiplier,
	// probeNewStatus, probeKey);
	//
	// case "PORT":
	// return user.updatePorterProbe(templateId, probeId, probeNewName,
	// probeNewInterval, probeNewMultiplier,
	// probeNewStatus, probeKey);
	// case "HTTP":
	// return user.updateWeberProbe(templateId, probeId, probeNewName,
	// probeNewInterval, probeNewMultiplier,
	// probeNewStatus, probeKey);
	// case "SNMP":
	// return user.updateSnmpProbe(templateId, probeId, probeNewName,
	// probeNewInterval, probeNewMultiplier,
	// probeNewStatus, probeKey);
	// case "RBL":
	// return user.updateRBLProbe(templateId, probeId, probeNewName,
	// probeNewInterval, probeNewMultiplier,
	// probeNewStatus, probeKey);
	// }
	// return false;
	// }

	public static boolean unMergeTemplateHost(UUID userId, UUID templateId, UUID hostId) {
		User user = getUsers().get(userId);
		boolean flag = true;
		List<RunnableProbe> rps = user.getRPSbyTemplateIdHostId(templateId, hostId);
		for (RunnableProbe rp : rps) {
			flag = user.removeRunnableProbe(rp);
		}
		return flag;
	}

	public static String serializeDataPoints(HashMap<String, DataPointsRollup[][]> rollups) {
		Gson gson = new Gson();
		return GeneralFunctions.Base64Encode(gson.toJson(rollups));
	}

	private static JSONObject getServerInfoFromApi() {
		Object initServer;
		while (true) {
			initServer = ApiInterface.executeRequest(Enums.ApiAction.InitServer, "GET", null);
			if (initServer == null) {
				SysLogger.Record(new Log("Error starting server, no API connectivity! trying again in 1 minutes...",
						LogType.Error));
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					SysLogger.Record(new Log("Main Thread Interrupted!", LogType.Error, e));
				}
			} else {
				JSONObject jsonInitServer = (JSONObject) (initServer);
				return jsonInitServer;
			}
		}
	}
}
