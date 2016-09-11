/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.json.simple.JSONObject;
import org.apache.log4j.BasicConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import com.google.gson.Gson;
import DAL.DAL;
import Elements.BaseElement;
import Elements.DiskElement;
import Elements.NicElement;
import GlobalConstants.Constants;
import GlobalConstants.Enums;
import GlobalConstants.SnmpDataType;
import GlobalConstants.TriggerSeverity;
import GlobalConstants.Enums.ApiAction;
import Interfaces.IDAL;
import Interfaces.IFunction;
import Model.ConditionUpdateModel;
import Model.DiscoveryElementParams;
import Model.HostParams;
import Model.ProbeParams;
import Model.SnmpTemplateParams;
import Probes.BaseProbe;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.DataPointsRollup;

/**
 * 
 * @author Roi
 */
public class UsersManager {

	private static HashMap<UUID, User> users;
	private static boolean initialized;
	// static Logger log = Logger.getLogger(UsersManager.class);

	public static void Initialize() {
		BasicConfigurator.configure();

		setUsers(new HashMap<UUID, User>());
		if (!UsersManager.Build()) {
			setInitialized(false);
			return;
		}
		Logit.LogInfo("Server successfully initialized with all user's data.");
		setInitialized(true);
	}

	public static HashMap<UUID, User> getUsers() {
		return users;
	}

	public static User getUser(UUID uid) {
		return getUsers().get(uid);
	}

	public static User getUser(String userId) {
		return getUsers().get(UUID.fromString(userId));
	}

	public static void setUsers(HashMap<UUID, User> users) {
		UsersManager.users = users;
	}

	public static boolean removeHost(UUID hostId, UUID userId) {
		if (!getUser(userId).removeHost(hostId) && getUsers().containsKey(userId)) {
			getUsers().remove(userId);
		}
		return true;
	}

	public static boolean addUser(UUID userId) {
		User user = new User(userId);
		if (!getUsers().containsKey(userId)) {

			getUsers().put(userId, user);

		}
		User u = getUsers().get(userId);
		if (u == null)
			;
		return true;
	}

	public static boolean removeUser(UUID userId) {
		if (getUsers().containsKey(userId))
			getUsers().remove(userId);
		return true;
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

		HashMap<String, UUID> runnableProbesIds = getInitRPs(initServer.get("runnable_ids"));
		HashMap<String, UUID> probeByUser = getProbeByUser(runnableProbesIds);

		if (runnableProbesIds == null) {
			Logit.LogFatal("UsersManager", "no probes found for this server!", null);
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

		JSONArray allDiscoveryElementsJson = (JSONArray) initServer.get("discovery_elements");
		addDiscoveryElements(allDiscoveryElementsJson);

		JSONArray allTemplateTriggersJson = (JSONArray) initServer.get("triggers");
		addTriggers(allTemplateTriggersJson, probeByUser);

		addRunnableProbes(runnableProbesIds);

		return true;
	}

	// public static void runAtStart() {
	// Set<User> allUsers = new HashSet<User>(getUsers().values());
	// for (User user : allUsers) {
	// user.runProbesAtStart();
	// }
	// }

	public static HashMap<String, UUID> getInitRPs(Object longIds) {
		// key:templateId@hostId@probeId , value:userId
		HashMap<String, UUID> ExtendedProbeID_UserID = new HashMap<String, UUID>();
		JSONArray allRps = (JSONArray) longIds;

		for (int i = 0; i < allRps.size(); i++) {
			JSONObject rp = (JSONObject) allRps.get(i);
			ExtendedProbeID_UserID.put((String) rp.get("long_id"), UUID.fromString((String) rp.get("user_id")));
		}

		return ExtendedProbeID_UserID;

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

	public static void addSnmpTemplates(JSONArray allSnmpTemplatesJson) {
		for (int i = 0; i < allSnmpTemplatesJson.size(); i++) {
			JSONObject snmpTempJson = (JSONObject) allSnmpTemplatesJson.get(i);
			try {
				SnmpTemplateParams snmpTemplateParams = new SnmpTemplateParams();
				snmpTemplateParams.user_id = (String) snmpTempJson.get("snmp_user_id");
				snmpTemplateParams.snmp_template_id = (String) snmpTempJson.get("snmp_template_id");
				snmpTemplateParams.template_name = (String) snmpTempJson.get("snmp_template_name");
				snmpTemplateParams.version = Integer.parseInt((String) snmpTempJson.get("snmp_version"));
				snmpTemplateParams.community = (String) snmpTempJson.get("snmp_community");
				snmpTemplateParams.sec = (String) snmpTempJson.get("snmp_sec");
				snmpTemplateParams.auth_method = (String) snmpTempJson.get("snmp_auth_method");
				snmpTemplateParams.username = (String) snmpTempJson.get("snmp_user");
				snmpTemplateParams.password = (String) snmpTempJson.get("snmp_auth_password");
				snmpTemplateParams.crypt_method = (String) snmpTempJson.get("snmp_crypt_method");
				snmpTemplateParams.crypt_password = GeneralFunctions
						.Base64Decode((String) snmpTempJson.get("snmp_crypt_password"));
				snmpTemplateParams.timeout = Integer.parseInt((String) snmpTempJson.get("snmp_timeout"));
				snmpTemplateParams.port = Integer.parseInt((String) snmpTempJson.get("snmp_port"));

				User user = getUsers().get(UUID.fromString(snmpTemplateParams.user_id));
				if (user == null)
					continue;
				user.addSnmpTemplate(snmpTemplateParams);
			} catch (Exception e) {
				Logit.LogWarn("Creation of Snmp Template Failed: " + snmpTempJson.toJSONString() + " , not added! E: "
						+ e.getMessage());
				continue;
			}
		}
	}

	public static void addDiscoveryElements(JSONArray allElementsJson) {
		for (int i = 0; i < allElementsJson.size(); i++) {
			JSONObject hostElementsJson = (JSONObject) allElementsJson.get(i);
			try {
				JSONArray elementsArray = (JSONArray) hostElementsJson.get("elements");
				for (Object element : elementsArray) {
					JSONObject elementJson = (JSONObject) element;
					DiscoveryElementParams elementParams = new DiscoveryElementParams();
					elementParams.user_id = (String) hostElementsJson.get("user_id");
					elementParams.template_id = (String) hostElementsJson.get("template_id");
					elementParams.element_interval = Integer
							.parseInt((String) hostElementsJson.get("element_interval"));
					elementParams.elements_type = (String) hostElementsJson.get("elements_type");

					elementParams.host_id = (String) hostElementsJson.get("host_id");
					elementParams.discovery_id = (String) hostElementsJson.get("discovery_id");

					String runnableProbeId = elementParams.template_id + "@" + elementParams.host_id + "@"
							+ elementParams.discovery_id;

					String rpStr = runnableProbeId;
					if (rpStr.contains(
							"0122dc0b-2de1-4d9c-abe1-1c65371775f2@7352a46f-5189-428c-b4c0-fb98dedd10b1@discovery_d3c95875-4947-4388-989f-64ffd863c704"))
						Logit.LogDebug("BREAKPOINT");

					// JSONObject elementN=(JSONObject)elements.get();
					// JSONObject
					// elementValues=(JSONObject)elements.get(elementParams.name);

					elementParams.name = (String) elementJson.get("name");
					elementParams.index = Integer.parseInt(elementJson.get("index").toString());
					elementParams.status = (boolean) elementJson.get("active");

					if (((String) hostElementsJson.get("elements_type")).contains("bw")) {
						elementParams.hostType = (String) elementJson.get("hostType");
						elementParams.ifSpeed = (long) elementJson.get("ifSpeed");
					} else if (((String) hostElementsJson.get("elements_type")).contains("ds"))
						elementParams.hrStorageAllocationUnits = (long) elementJson.get("hrStorageAllocationUnits");
					User user = getUsers().get(UUID.fromString(elementParams.user_id));
					if (user == null)
						continue;
					Host host = user.getHost(UUID.fromString(elementParams.host_id));
					// TODO check for element type and add new elements
					BaseElement baseElement = null;
					switch (elementParams.elements_type) {
					case Constants.bw:
						// DiscoveryProbe
						// probe=(DiscoveryProbe)user.getTemplateProbes().get(elementParams.discovery_id);
						baseElement = new NicElement(elementParams.index, elementParams.name, elementParams.status,
								Utils.GeneralFunctions.getHostType(elementParams.hostType), elementParams.ifSpeed);
						break;
					case Constants.ds:
						baseElement = new DiskElement(elementParams.index, elementParams.name, elementParams.status);
						break;
					}
					ElementsContainer.getInstance().addElement(elementParams.user_id, runnableProbeId, baseElement);
					// TODO add exisitng elements
					// user.addNewDiscoveryElement(baseElement,host);
				}

			} catch (Exception e) {
				Logit.LogWarn("Creation of Discovery Element Failed: " + allElementsJson.toJSONString()
						+ " , not added! E: " + e.getMessage());
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
			hostParams.host_id = (String) hostJson.get("host_id");
			hostParams.name = (String) hostJson.get("host_name");
			hostParams.hostIp = (String) hostJson.get("ip");
			hostParams.hostStatus = (String) hostJson.get("status");
			hostParams.bucket = (String) hostJson.get("bucket");
			hostParams.notificationGroups = (String) hostJson.get("notifications_group");
			hostParams.snmpTemp = (String) hostJson.get("snmp_template");

			user.addHost(hostParams);
		}
	}

	public static void addHostsForUpdate(JSONArray allHostsJson) {
		for (int i = 0; i < allHostsJson.size(); i++) {
			JSONObject hostJson = (JSONObject) allHostsJson.get(i);
			UUID user_id = UUID.fromString((String) hostJson.get("user_id"));
			User user = getUsers().get(user_id);
			if (user == null) {
				addUser(user_id);
				user = getUser(user_id);
				// getUsers().put(user_id, user);
			}

			String snmpTemplateId = ((JSONObject) allHostsJson.get(0)).get("snmp_template").toString();
			if (snmpTemplateId != null && !user.isSnmpTemplateExist(snmpTemplateId)) {
				JSONObject templateId = new JSONObject();
				JSONObject userIdSnmpTemplate = new JSONObject();
				userIdSnmpTemplate.put(user_id, snmpTemplateId);

				JSONArray snmpTemplateIds = new JSONArray();
				snmpTemplateIds.add(userIdSnmpTemplate);
				templateId.put(Constants.snmpTemplates, snmpTemplateIds);

				IDAL dal = DAL.getInstanece();
				JSONObject jsonObject = dal.put(ApiAction.GetSnmpTemplates, templateId);

				UsersManager.addSnmpTemplates((JSONArray) jsonObject.get("snmp_templates"));
				// UUID id = jsonObject.get("snmp_user_id");
				//
				//
				// (UUID id,String name, int version,int port, String sec,
				// String userName,
				// String authPass, String algo,String cryptPass,String
				// cryptType,int timeout,boolean status
				// SnmpTemplate snmpTemplate = new SnmpTemplate(id, name,
				// version, port, sec, userName, authPass, algo, cryptPass,
				// cryptType, timeout, status)
			}

			HostParams hostParams = new HostParams();
			hostParams.host_id = (String) hostJson.get("host_id");
			hostParams.name = (String) hostJson.get("host_name");
			hostParams.hostIp = (String) hostJson.get("ip");
			hostParams.hostStatus = (String) hostJson.get("status");
			hostParams.bucket = (String) hostJson.get("bucket");
			hostParams.notificationGroups = (String) hostJson.get("notifications_group");
			hostParams.snmpTemp = (String) hostJson.get("snmp_template");

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
			try {
				UUID user_id = UUID.fromString((String) probeJson.get("user_id"));
				User user = getUsers().get(user_id);

				ProbeParams probeParams = new ProbeParams();

				probeParams.template_id = (String) probeJson.get("template_id");
				probeParams.probe_id = (String) probeJson.get("probe_id");
				String rpStr = probeParams.probe_id;
				if (rpStr.contains("snmp_1ea93ba7-8078-4880-8bc6-66c6b9ad9bdb"))
					Logit.LogDebug("BREAKPOINT");
				probeParams.name = (String) probeJson.get("probe_name");
				probeParams.interval = Integer.parseInt(probeJson.get("probe_interval").toString());
				probeParams.multiplier = GeneralFunctions.isNullOrEmpty(probeJson.get("probe_multiplier").toString())
						? 1 : Float.parseFloat(probeJson.get("probe_multiplier").toString());

				probeParams.is_active = probeJson.get("probe_status").toString().equals("1") ? true : false;
				probeParams.type = (String) probeJson.get("probe_type");
				probeKeyJson = (JSONObject) probeJson.get("probe_key");

				switch (probeParams.type) {
				case Constants.icmp: {
					probeParams.npings = Integer.parseInt(probeKeyJson.get("npings").toString());
					probeParams.bytes = Integer.parseInt(probeKeyJson.get("bytes").toString());
					probeParams.timeout = Integer.parseInt(probeKeyJson.get("timeout").toString());
					break;
				}
				case Constants.port: {
					probeParams.protocol = (String) probeKeyJson.get("proto");
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
					probeParams.http_deep = Integer.parseInt(probeKeyJson.get("http_deep").toString());
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

					// Roi - Please take a look here especially discovery_type
					probeParams.element_interval = Integer.parseInt(probeKeyJson.get("element_interval").toString());
					JSONParser jsonParser = new JSONParser();
					JSONArray triggers = (JSONArray) jsonParser.parse(probeKeyJson.get("triggers").toString());
					TriggerCondition[] discovery_triggers = new TriggerCondition[triggers.size()];
					probeParams.discovery_type = probeKeyJson.get("discovery_type").toString();
					probeParams.severity = probeKeyJson.get("severity").toString();
					probeParams.tuple = probeKeyJson.get("tuple").toString();
					probeParams.xvalue_unit = probeKeyJson.get("xvalue_unit").toString();
					probeParams.triggerName = probeKeyJson.get("triggerName").toString();
					probeParams.triggerId = probeKeyJson.get("triggerId").toString();

					ConditionUpdateModel[] discovertyTiggers = new ConditionUpdateModel[triggers.size()];
					for (int tIndex = 0; tIndex < triggers.size(); tIndex++) {
						ConditionUpdateModel discovertyTigger = new ConditionUpdateModel();

						JSONObject trigger = (JSONObject) triggers.get(tIndex);
						discovertyTigger.andor = trigger.get("andor").toString();
						discovertyTigger.condition = trigger.get("condition").toString();
						discovertyTigger.function = trigger.get("function").toString();
						discovertyTigger.index = trigger.get("index").toString();
						discovertyTigger.xvalue = trigger.get("xvalue").toString();
						discovertyTigger.results_vector_type = trigger.get("results_vector_type").toString();
						discovertyTigger.nvalue = trigger.get("nvalue").toString();

						discovertyTiggers[tIndex] = discovertyTigger;
					}

					probeParams.triggers = discovertyTiggers;
					break;
				}
				case Constants.rbl: {
					probeParams.rbl = (String) probeKeyJson.get("rbl");
					break;
				}
				}
				user.addTemplateProbe(probeParams);
			} catch (Exception e) {
				Logit.LogWarn("Unable to parse probe params for:" + probeJson + ", E:" + e.getMessage());
				continue;
			}
		}

	}

	public static SnmpDataType getSnmpDataType(String valueType) {
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

				String rpStr = triggerId;
				if (rpStr.contains("icmp_cc9a931c-6232-4b17-b2f9-be00b40ce02b"))
					Logit.LogDebug("BREAKPOINT");

				String name = (String) triggerJson.get("name");
				TriggerSeverity severity = getTriggerSev((String) triggerJson.get("severity"));
				if (severity == null)
					Logit.LogWarn("Unable to get trigger severity for: " + triggerId);
				boolean status = ((String) triggerJson.get("status")).equals("1") ? true : false;
				String elementType = (String) triggerJson.get("results_vector_type");

				ArrayList<TriggerCondition> conditions = getTriggerConds((JSONArray) triggerJson.get("conditions"));

				User user = getUsers().get(probeByUser.get(probeId));
				if (user == null) {
					Logit.LogWarn("No user exists for trigger: " + triggerJson.toJSONString());
					continue;
				}
				BaseProbe probe = user.getTemplateProbes().get(probeId);
				if (probe == null) {
					Logit.LogWarn("No probe exists for trigger: " + triggerJson.toJSONString());
					continue;
				}

				Trigger trigger = new Trigger(triggerId, name, probe, severity, status, conditions);

				probe.addTrigger(trigger);

			} catch (Exception e) {
				Logit.LogWarn("Creation of Trigger Failed: " + triggerJson.toJSONString() + " , not added! E: "
						+ e.getMessage());
				continue;
			}
		}
	}

	private static ArrayList<TriggerCondition> getTriggerConds(JSONArray jsonArray) {
		ArrayList<TriggerCondition> conditions = new ArrayList<TriggerCondition>();

		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject conditionJson = (JSONObject) jsonArray.get(i);
			String condition = conditionJson.get("condition").toString();
			String xValue = conditionJson.get("xvalue").toString();
			String xValueUnit = conditionJson.get("xvalue_unit").toString();
			String nValue = conditionJson.get("nvalue").toString();
			String elementType = conditionJson.get("results_vector_type").toString();
			String function = conditionJson.get("function").toString();
			String lastType = conditionJson.get("last_type").toString();

			TriggerCondition triggerCondition = new TriggerCondition(condition, xValue, function, elementType,
					xValueUnit, nValue, lastType);
			conditions.add(triggerCondition);
		}
		return conditions;
	}

	public static ArrayList<TriggerCondition> getTriggerConds(ConditionUpdateModel[] conditionUpdateModels) {
		ArrayList<TriggerCondition> conditions = new ArrayList<TriggerCondition>();
		for (ConditionUpdateModel conditionUpdateModel : conditionUpdateModels) {
			// JSONObject conditionJson = (JSONObject) jsonArray.get(i);

			String condition = conditionUpdateModel.condition.toString();
			String xValue = conditionUpdateModel.xvalue.toString();
			String xValueUnit = conditionUpdateModel.xvalue_unit.toString();
			String elementType = conditionUpdateModel.results_vector_type.toString();
			String function = conditionUpdateModel.function.toString();
			String lastType = conditionUpdateModel.last_type.toString();
			String nValue = conditionUpdateModel.nvalue.toString();

			TriggerCondition triggerCondition = new TriggerCondition(condition, xValue, function, elementType,
					xValueUnit, nValue, lastType);
			conditions.add(triggerCondition);
		}
		return conditions;
	}

	public static TriggerSeverity getTriggerSev(String sev) {
		switch (sev) {
		case "notice":
			return TriggerSeverity.Notice;
		case "warning":
			return TriggerSeverity.Warning;
		case "high":
			return TriggerSeverity.High;
		case "disaster":
			return TriggerSeverity.Disaster;
		case "critical":
			return TriggerSeverity.Critical;
		}
		return null;
	}

	private static void addRunnableProbes(HashMap<String, UUID> runnableProbesIds) {
		for (Map.Entry<String, UUID> rp : runnableProbesIds.entrySet()) {
			UUID userID = rp.getValue();
			String rpID = rp.getKey();

			if (rpID.contains("bf4e7e1c-4c44-4e0f-bee5-871aadfe1174@rbl_619c9a8f-ceab-4e6f-8fe5-3b57da32fa52"))
				Logit.LogDebug("BREAKPOINT");

			User u = getUsers().get(userID);
			Host host = u.getHosts().get(UUID.fromString(rpID.split("@")[1]));
			BaseProbe probe = u.getTemplateProbes().get(rpID.split("@")[2]);

			if (host == null || probe == null) {
				Logit.LogWarn("Unable to initiate RunnableProbe, one of its elements is missing! ID: " + rpID);
				continue;
			}

			RunnableProbe runnableProbe = new RunnableProbe(host, probe);
			RunnableProbeContainer.getInstanece().add(runnableProbe);
			// u.addRunnableProbe(runnableProbe);
		}
	}

	// public static HashMap<String, RunnableProbe> getAllUsersRunnableProbes()
	// {
	// HashMap<String, RunnableProbe> allRps = new HashMap<String,
	// RunnableProbe>();
	// for (User user : getUsers().values()) {
	//// HashMap<String, RunnableProbe> allUserRps =
	// user.getAllRunnableProbes();
	// HashMap<String, RunnableProbe> allUserRps =
	// RunnableProbeContainer.getInstanece().getByUser(user.getUserId().toString());
	// allRps.putAll(allUserRps);
	// }
	// return allRps;
	// }

	public static void printUsers() {
		for (User user : getUsers().values()) {
			Logit.LogDebug("---User:" + user.getUserId() + "---" + user.toString());
		}
	}

	public static String serializeDataPoints(HashMap<String, DataPointsRollup[][]> rollups) {
		Gson gson = new Gson();
		return GeneralFunctions.Base64Encode(gson.toJson(rollups));
	}

	private static JSONObject getServerInfoFromApi() {
		Object initServer;

		while (true) {
			initServer = DAL.getInstanece().get(Enums.ApiAction.InitServer);
			if (initServer == null) {
				Logit.LogError("UsersManager - getServerInfoFromApi",
						"Error starting server, no API connectivity! trying again in 1 minutes...");
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					Logit.LogError("UsersManager - getServerInfoFromApi",
							"Main Thread Interrupted! E: " + e.getMessage());
				}
			} else {
				JSONObject jsonInitServer = (JSONObject) (initServer);
				return jsonInitServer;
			}
		}
	}
}
