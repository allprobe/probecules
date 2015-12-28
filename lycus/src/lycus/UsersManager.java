/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.snmp4j.smi.OID;

import com.google.gson.Gson;

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

	private static void addHosts(JSONArray allHostsJson) {
		for (int i = 0; i < allHostsJson.size(); i++) {
			JSONObject hostJson = (JSONObject) allHostsJson.get(i);
			try {
				UUID user_id = UUID.fromString((String) hostJson.get("user_id"));
				User user = getUsers().get(user_id);
				UUID host_id = UUID.fromString((String) hostJson.get("host_id"));
				String name = (String) hostJson.get("host_name");
				String ip = (String) hostJson.get("ip");
				boolean status = ((String) hostJson.get("status")).equals("1") ? true : false;
				String bucket = ((String) hostJson.get("bucket"));

				UUID notif_groups;
				try{
				notif_groups=UUID.fromString((String) hostJson.get("notifications_group"));
				}
				catch(Exception e)
				{
					SysLogger.Record(new Log("Unable to parse notifications group for host: "+hostJson.toString(), LogType.Warn, e));
					notif_groups=null;
				}

				UUID snmp_template;
				try{
				snmp_template = UUID.fromString((String) hostJson.get("snmp_template"));
				}
				catch(Exception e)
				{
					SysLogger.Record(new Log("Unable to parse snmp template for host: "+hostJson.toString(), LogType.Warn, e));
					snmp_template=null;
				}
				
				Host host;

				if (snmp_template==null) {
					host = new Host(host_id, name, ip, status, true, bucket, notif_groups);
				} else {
					SnmpTemplate snmpTemp = user.getSnmpTemplates().get(snmp_template);
					host = new Host(host_id, name, ip, snmpTemp, status, true, bucket, notif_groups);
				}
				user.getHosts().put(host_id, host);
			} catch (Exception e) {
				SysLogger.Record(new Log("Creation of Host Failed: " + hostJson.toJSONString() + " , not added!",
						LogType.Warn, e));
				continue;
			}
		}
	}

//	private static ArrayList<UUID> convertNotificationGroupsArray(Object notifs) {
//		if (notifs == null)
//			return null;
//		JSONArray notifGroups = (JSONArray) notifs;
//		ArrayList<UUID> groups = new ArrayList<UUID>();
//		for (int i = 0; i < notifGroups.size(); i++) {
//			groups.add(UUID.fromString((String) (notifGroups.get(i))));
//		}
//		return groups;
//	}

	private static void addTemplates(JSONArray allTemplateProbesJson, HashMap<String, UUID> probeByUser) {
		for (int i = 0; i < allTemplateProbesJson.size(); i++) {
			JSONObject probeJson = (JSONObject) allTemplateProbesJson.get(i);
			try {
				UUID templateId = UUID.fromString((String) probeJson.get("template_id"));
				String probeId = (String) probeJson.get("probe_id");		
				String name = (String) probeJson.get("probe_name");
				long interval = Long.parseLong((String) probeJson.get("probe_interval"));
				float multiplier = Float.parseFloat((String) probeJson.get("probe_multiplier"));
				boolean status = ((String) probeJson.get("probe_status")).equals("1") ? true : false;
				String type = (String) probeJson.get("probe_type");
				JSONObject key = (JSONObject) probeJson.get("probe_key");

				User user = getUsers().get(probeByUser.get(probeId));

				Probe probe = null;

				switch (type) {
				case "icmp": {
					int npings = Integer.parseInt((String) key.get("npings"));
					int bytes = Integer.parseInt((String) key.get("bytes"));
					int timeout = Integer.parseInt((String) key.get("timeout"));
					probe = new PingerProbe(user, probeId, templateId, name, interval, multiplier, status, timeout,
							npings, bytes);
					break;
				}
				case "port": {
					String proto = (String) key.get("proto");
					int port = Integer.parseInt((String) key.get("port"));
					int timeout = Integer.parseInt((String) key.get("timeout"));
					String sendString = "ALL";
					String receiveString = "PROBE";
					probe = new PorterProbe(user, probeId, templateId, name, interval, multiplier, status, timeout,
							proto, port, sendString, receiveString);
					break;
				}
				case "http": {
					String url = GeneralFunctions.Base64Decode((String) key.get("urls"));
					String method = (String) key.get("http_method");
					String auth = (String) key.get("http_auth");
					String authUser = GeneralFunctions.Base64Decode((String) key.get("http_auth_user"));
					String authPass = GeneralFunctions.Base64Decode((String) key.get("http_auth_password"));
					int timeout = Integer.parseInt((String) key.get("timeout"));
					probe = new WeberProbe(user, probeId, templateId, name, interval, multiplier, status, timeout,
							method, url, auth, authUser, authPass);
					break;
				}
				case "snmp": {
					
					OID oid = new OID((String) key.get("snmp_oid"));
					int storeValue = Integer.parseInt((String) key.get("store_value_as"));
					String valueType = (String) key.get("value_type");
					String valueUnit = (String) key.get("value_unit");
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
						SysLogger.Record(
								new Log("Probe: " + probeId + " Wrong Data Type, Doesn't Added!", LogType.Error));
						continue;
					}
					}

					SnmpUnit unit=getSnmpUnit(valueUnit);
					
					probe = new SnmpProbe(user, probeId, templateId, name, interval, multiplier, status, oid, dataType,
							unit, storeValue);
					break;
				}
				case "rbl": {
					String rblName = (String) key.get("rbl");
					probe = new RBLProbe(user, probeId, templateId, name, interval, multiplier, status, rblName);
					break;
				}
				}
				if (probe == null) {
					SysLogger.Record(new Log("Creation of Probe: " + probeJson + " failed, skipping!", LogType.Warn));
					continue;
				}
				user.getTemplateProbes().put(probeId, probe);
			} catch (Exception e) {
				SysLogger.Record(new Log("Creation of Probe Failed: " + probeJson.toJSONString() + " , not added!",
						LogType.Warn, e));
				continue;
			}
		}
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

	private static SnmpUnit getSnmpUnit(String unitType) throws Exception {
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
			throw new IOException("Unable to create SnmpUnit unreadble value: " + unitType);
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

//	public static boolean updateRunnableProbe(UUID userId, UUID templateId, String probeNewName, String probeId,
//			String probeType, long probeNewInterval, float probeNewMultiplier, boolean probeNewStatus,
//			List<String> probeKey) {
//		User user = getUsers().get(userId);
//		if (user == null) {
//			SysLogger.Record(
//					new Log("User: " + userId.toString() + " Doesn't Exists! Probe Update Failed!", LogType.Error));
//			return false;
//		}
//		switch (probeType) {
//		case "ICMP":
//			return user.updatePingerProbe(templateId, probeId, probeNewName, probeNewInterval, probeNewMultiplier,
//					probeNewStatus, probeKey);
//
//		case "PORT":
//			return user.updatePorterProbe(templateId, probeId, probeNewName, probeNewInterval, probeNewMultiplier,
//					probeNewStatus, probeKey);
//		case "HTTP":
//			return user.updateWeberProbe(templateId, probeId, probeNewName, probeNewInterval, probeNewMultiplier,
//					probeNewStatus, probeKey);
//		case "SNMP":
//			return user.updateSnmpProbe(templateId, probeId, probeNewName, probeNewInterval, probeNewMultiplier,
//					probeNewStatus, probeKey);
//		case "RBL":
//			return user.updateRBLProbe(templateId, probeId, probeNewName, probeNewInterval, probeNewMultiplier,
//					probeNewStatus, probeKey);
//		}
//		return false;
//	}

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
			initServer = ApiInterface.executeRequest(ApiStages.InitServer, "GET", null);
			if (initServer == null) {
				SysLogger.Record(new Log("Error starting server, no API connectivity! trying again in 1 minutes...",
						LogType.Error));
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					SysLogger.Record(new Log("Main Thread Interrupted!", LogType.Error, e));
				}
			} else {
				JSONObject jsonInitServer=(JSONObject)(initServer);
				return jsonInitServer;
			}
		}
	}
}
