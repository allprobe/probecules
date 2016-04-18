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

import javax.persistence.GenerationType;

import org.json.simple.JSONObject;
import org.apache.log4j.BasicConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import com.google.gson.Gson;

import DAL.ApiInterface;
import Elements.BaseElement;
import Elements.NicElement;
import GlobalConstants.Constants;
import GlobalConstants.Enums;
import GlobalConstants.SnmpDataType;
import GlobalConstants.SnmpUnit;
import GlobalConstants.TriggerSeverity;
import Model.ConditionUpdateModel;
import Model.DiscoveryElementParams;
import Model.DiscoveryTrigger;
import Model.HostParams;
import Model.ProbeParams;
import Model.SnmpTemplateParams;
import Probes.BaseProbe;
import Probes.DiscoveryProbe;
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

		HashMap<String, UUID> runnableProbesIds = ApiInterface.getInitRPs(initServer.get("long_ids"));
		HashMap<String, UUID> probeByUser = getProbeByUser(runnableProbesIds);

		if (runnableProbesIds == null) {
			Logit.LogFatal("UsersManager", "no probes found for this server!");
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
				JSONArray elementsArray=(JSONArray)(new JSONParser()).parse((String)hostElementsJson.get("elements"));
				for(Object element : elementsArray)
				{
					JSONObject elementJson=(JSONObject)element;
					DiscoveryElementParams elementParams = new DiscoveryElementParams();
				elementParams.user_id=(String)hostElementsJson.get("user_id");
				elementParams.template_id=(String)hostElementsJson.get("template_id");
				elementParams.element_interval=Integer.parseInt((String)hostElementsJson.get("element_interval"));
				elementParams.elements_type=(String)hostElementsJson.get("elements_type");

				elementParams.host_id=(String)hostElementsJson.get("host_id");
				elementParams.discovery_id=(String)hostElementsJson.get("discovery_id");

				String runnableProbeId=elementParams.template_id+"@"+elementParams.host_id+"@"+elementParams.discovery_id;
				
				//				JSONObject elementN=(JSONObject)elements.get();
//				JSONObject elementValues=(JSONObject)elements.get(elementParams.name);

				elementParams.name=(String)elementJson.get("name");
				elementParams.index=Integer.parseInt(elementJson.get("index").toString());
				elementParams.status=(boolean)elementJson.get("active");
				elementParams.hostType=(String)elementJson.get("hostType");
			
				elementParams.ifSpeed=(long)elementJson.get("ifSpeed");

				
				
				User user = getUsers().get(UUID.fromString(elementParams.user_id));
				if (user == null)
					continue;
				Host host=user.getHost(UUID.fromString(elementParams.host_id));
				// TODO check for element type and add new elements
				BaseElement baseElement=null;
				switch(elementParams.elements_type)
				{
				case Constants.bw: 
//					DiscoveryProbe probe=(DiscoveryProbe)user.getTemplateProbes().get(elementParams.discovery_id);
					baseElement=new NicElement(elementParams.index, elementParams.name,elementParams.status,  Utils.GeneralFunctions.getHostType(elementParams.hostType),elementParams.ifSpeed);
					ElementsContainer.getInstance().addElement(elementParams.user_id,runnableProbeId, baseElement);
					break;
				case Constants.ds:
					break;
				}
				// TODO add exisitng elements
//				user.addNewDiscoveryElement(baseElement,host);
				}
				
			} catch (Exception e) {
				Logit.LogWarn("Creation of Discovery Element Failed: " + allElementsJson.toJSONString() + " , not added! E: "+e.getMessage());
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
				if (rpStr.contains("inner_657259e4-b70b-47d2-9e4a-3db904a367e1"))
					Logit.LogDebug("BREAKPOINT");
				probeParams.name = (String) probeJson.get("probe_name");
				probeParams.interval = Long.parseLong(probeJson.get("probe_interval").toString());
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
					probeParams.element_interval = Integer.parseInt(probeKeyJson.get("element_interval").toString());
					JSONParser jsonParser = new JSONParser();
					JSONArray triggers = (JSONArray) jsonParser
							.parse(probeKeyJson.get("discovery_triggers").toString());
					DiscoveryTrigger[] discovery_triggers = new DiscoveryTrigger[triggers.size()];
					probeParams.discovery_type = probeKeyJson.get("discovery_type").toString();

					for (int index = 0; index < triggers.size(); index++) {
						JSONObject trigger = (JSONObject) triggers.get(index);
						DiscoveryTrigger discoveryTrigger = new DiscoveryTrigger();

						discoveryTrigger.discovery_trigger_code = Integer
								.parseInt(trigger.get("discovery_trigger_code").toString());

						discoveryTrigger.discovery_trigger_x_value = trigger.get("discovery_trigger_x_value")
								.toString();
						discoveryTrigger.discovery_trigger_severity = trigger.get("discovery_trigger_severity")
								.toString();
						discoveryTrigger.discovery_trigger_unit = trigger.get("discovery_trigger_unit").toString();
						discoveryTrigger.discovery_trigger_id = trigger.get("discovery_trigger_id").toString();
						discovery_triggers[index] = discoveryTrigger;

					}

					probeParams.discovery_triggers = discovery_triggers;
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
				if (rpStr.contains(
						"e8b03d1e-48c8-4bd1-abeb-7e9a96a4cae4@icmp_41468c4c-c7d4-4dae-bd03-a5b2ca0b44d6@2b082834-7c37-4988-a12a-14947b064430"))
					Logit.LogDebug("BREAKPOINT");

				String name = (String) triggerJson.get("name");
				TriggerSeverity severity = getTriggerSev((String) triggerJson.get("severity"));
				if (severity == null)
					Logit.LogWarn("Unable to get trigger severity for: " + triggerId);
				boolean status = ((String) triggerJson.get("status")).equals("1") ? true : false;
				String elementType = (String) triggerJson.get("trigger_type");
				String unitType = (String) triggerJson.get("xvalue_unit");
				SnmpUnit trigValueUnit = getSnmpUnit(unitType);

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

				Trigger trigger = new Trigger(triggerId, name, probe, severity, status, elementType, trigValueUnit,
						conditions);

				probe.addTrigger(trigger);

			} catch (Exception e) {
				Logit.LogWarn("Creation of Trigger Failed: " + triggerJson.toJSONString() + " , not added! E: "
						+ e.getMessage());
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
		case "none":
			unit = SnmpUnit.none;
			break;
		case "":
			unit = null;
			break;

		default: {
			unit = null;
			// throw new IOException("Unable to create SnmpUnit unreadble value:
			// " + unitType);
		}
		}
		return unit;
	}

	private static ArrayList<TriggerCondition> getTriggerConds(JSONArray jsonArray) {
		ArrayList<TriggerCondition> conditions = new ArrayList<TriggerCondition>();

		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject conditionJson = (JSONObject) jsonArray.get(i);
			int code = Integer.parseInt((String) conditionJson.get("condition_id"));
			String andOr = (String) conditionJson.get("andor");
			String xValue = (String) conditionJson.get("xvalue");
			String tValue = (String) conditionJson.get("tvalue");
			TriggerCondition condition = new TriggerCondition(code, andOr, xValue, tValue);
			conditions.add(condition);

		}
		return conditions;
	}

	public static ArrayList<TriggerCondition> getTriggerConds(ConditionUpdateModel[] conditionUpdateModels) {
		ArrayList<TriggerCondition> conditions = new ArrayList<TriggerCondition>();

		for (ConditionUpdateModel conditionUpdateModel : conditionUpdateModels) {
			// JSONObject conditionJson = (JSONObject) jsonArray.get(i);
			int code = Integer.parseInt((String) conditionUpdateModel.condition_id);
			String andOr = (String) conditionUpdateModel.andor;
			String xValue = (String) conditionUpdateModel.xvalue;
			String tValue = (String) conditionUpdateModel.tvalue;
			TriggerCondition condition = new TriggerCondition(code, andOr, xValue, tValue);
			conditions.add(condition);

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

			if (rpID.contains("788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_036f81e0-4ec0-468a-8396-77c21dd9ae5a"))
				Logit.LogDebug("BREAKPOINT");

			User u = getUsers().get(userID);
			Host host = u.getHosts().get(UUID.fromString(rpID.split("@")[1]));
			BaseProbe probe = u.getTemplateProbes().get(rpID.split("@")[2]);

			if (host == null || probe == null) {
				Logit.LogWarn("Unable to initiate RunnableProbe, one of its elements is missing! ID: " + rpID);
				continue;
			}

			RunnableProbe runnableProbe = new RunnableProbe(host, probe);
			u.addRunnableProbe(runnableProbe);
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
			initServer = ApiInterface.executeRequest(Enums.ApiAction.InitServer, "GET", null);
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
