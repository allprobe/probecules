package lycus.Updates;

import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import GlobalConstants.Constants;
import GlobalConstants.LogType;
import GlobalConstants.Enums.ApiAction;
import Model.ProbeParams;
import Model.UpdateModel;
import lycus.DAL;
import lycus.GeneralFunctions;
import lycus.Host;
import lycus.Log;
import lycus.RunnableProbe;
import lycus.SysLogger;
import lycus.User;
import lycus.UsersManager;
import lycus.Interfaces.IDAL;
import lycus.Probes.Probe;

public class ProbeUpdate extends BaseUpdate {

	public ProbeUpdate(UpdateModel update) {
		super(update);
	}

	@Override
	public Boolean New() {
		super.New();

		User user = UsersManager.getUser(UUID.fromString(getUpdate().user_id));
		Host host = null;
		Probe probe = null;

		if (user == null)
		{
			user = new User(UUID.fromString(getUpdate().user_id));
		}
		
		if (!user.isHostExist(UUID.fromString(getUpdate().host_id))) {
			// Get host from Ran for host_id
			IDAL dal = DAL.getInstanece();
			JSONObject hosts = new JSONObject();
			JSONArray hostsArray = new JSONArray();
			hostsArray.add(getUpdate().host_id);
			hosts.put(Constants.hosts, hostsArray);

			JSONObject jsonObject = dal.put(ApiAction.GetHosts, hosts);

			JSONArray jsonArray = (JSONArray) jsonObject.get("hosts");
			UsersManager.addHosts(jsonArray);
		}
		host = user.getHost(UUID.fromString(getUpdate().host_id));
		if (host == null) {
			SysLogger.Record(new Log("Failed update from type NEW PROBE - unknown host", LogType.Warn));
			return false;
		}
		
		host = user.getHost(UUID.fromString(getUpdate().host_id));

		if (!user.isProbeExist(getUpdate().probe_id)) {
			ProbeParams probeParams = new ProbeParams();
			
			probeParams.bytes = update.update_value.key.bytes;
			probeParams.npings =  update.update_value.key.npings;
			probeParams.discovery_elements_interval = update.update_value.key.element_interval;
			probeParams.discovery_trigger_x =  update.update_value.key.discovery_trigger_x_value;
			probeParams.discovery_type =  update.update_value.key.discovery_type;
			probeParams.http_auth =  update.update_value.key.http_auth;
			probeParams.http_auth_password =  update.update_value.key.http_auth_password;
			probeParams.http_auth_username =  update.update_value.key.http_auth_user;
			probeParams.http_request =  update.update_value.key.http_method;
			probeParams.interval =  update.update_value.interval;
			probeParams.is_active =  update.update_value.status;
			probeParams.multiplier =  update.update_value.multiplier;
			probeParams.name =  update.update_value.name;
			probeParams.port =  update.update_value.key.port;
			probeParams.port_extra =  update.update_value.key.port_extra;
			probeParams.probe_id =  update.probe_id;
			probeParams.protocol =  update.update_value.key.proto;
			probeParams.rbl =  update.update_value.key.rbl;
			probeParams.oid =  update.update_value.key.snmp_oid;
			probeParams.template_id =  update.template_id;
			probeParams.timeout =  update.update_value.key.timeout;
			probeParams.type =  update.update_value.type;
			probeParams.url =  update.update_value.key.urls;
			probeParams.user_id  =  update.user_id;
			probeParams.snmp_datatype =  update.update_value.key.value_type;
			probeParams.snmp_unit =  update.update_value.key.value_unit;
			probeParams.snmp_store_as =  update.update_value.key.store_value_as; 
			probeParams.discovery_trigger_id =  update.update_value.key.trigger_id;
			probeParams.discovery_trigger_severity =  update.update_value.key.trigger_severity;
			probeParams.discovery_trigger_code =  update.update_value.key.discovery_trigger;
//			
			 user.addTemplateProbe(probeParams);
		} else {
			probe = user.getProbeFor(getUpdate().probe_id);
		}

		probe.updateKeyValues(update.update_value.key);

		RunnableProbe runnableProbe = new RunnableProbe(host, probe);
		runnableProbe.start();
		user.addRunnableProbe(runnableProbe);

		return true;
	}

	@Override
	public Boolean Update() {
		super.Update();

		User user = UsersManager.getUser(UUID.fromString(getUpdate().user_id));
		List<RunnableProbe> runnableProbes = user.getRunnableProbesFor(update.probe_id);
		for (RunnableProbe runnableProbe : runnableProbes) {
			if (!GeneralFunctions.isNullOrEmpty(update.update_value.name))
				runnableProbe.getProbe().setName(update.update_value.name);
			if (update.update_value.multiplier != null)
				runnableProbe.getProbe().setMultiplier(update.update_value.multiplier);
			if (update.update_value.status != null)
				runnableProbe.getProbe().setActive(update.update_value.status);
			if (runnableProbe.getProbe().getInterval() != update.update_value.interval && update.update_value.interval != null) {
				runnableProbe.changeRunnableProbeInterval(update.update_value.interval);
			}
			
			// TODO: What to do with them
//			probeParams.template_id =  update.template_id;
//			probeParams.type =  update.update_value.type;
			
			
			if (update.update_value.key != null)
				runnableProbe.getProbe().updateKeyValues(update.update_value.key);
			// SnmpProbe Probe (SnmpProbe)runnableProbe.getProbe();
		}

		return true;
	}

	@Override
	public Boolean Delete() {
		super.Delete();
		User user = UsersManager.getUser(UUID.fromString(getUpdate().user_id));
		user.removeRunnableProbes(getUpdate().probe_id);

		return true;
	}
}
