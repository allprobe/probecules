package lycus.Updates;

import java.util.HashMap;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import lycus.GlobalConstants.Constants;
import lycus.GlobalConstants.Enums.ApiAction;
import lycus.Model.ProbeParams;
import lycus.Model.UpdateModel;
import lycus.DAL.DAL;
import lycus.Utils.GeneralFunctions;
import lycus.Host;
import lycus.RunnableProbe;
import lycus.RunnableProbeContainer;
import lycus.UsersManager;
import lycus.Interfaces.IDAL;
import lycus.Probes.BaseProbe;
import lycus.Utils.Logit;

public class ProbeUpdate extends BaseUpdate {

	public ProbeUpdate(UpdateModel update) {
		super(update);
	}

	@Override
	public Boolean New() {
		super.New();

		Host host = null;
		BaseProbe probe = null;

		if (!getUser().isHostExist(UUID.fromString(getUpdate().host_id))) {
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
		host = getUser().getHost(UUID.fromString(getUpdate().host_id));
		if (host == null) {
			Logit.LogError("ProbeUpdate - New()", "Failed update from type NEW PROBE - unknown host");
			return false;
		}

		host = getUser().getHost(UUID.fromString(getUpdate().host_id));

		if (!getUser().isProbeExist(getUpdate().probe_id)) {

			ProbeParams probeParams = new ProbeParams();
			probeParams.bytes = getUpdate().update_value.key.bytes;
			probeParams.npings = getUpdate().update_value.key.npings;
			probeParams.discovery_elements_interval = getUpdate().update_value.key.element_interval;
			probeParams.discovery_trigger_x = getUpdate().update_value.key.discovery_trigger_x_value;
			// probeParams.discovery_type =
			// getUpdate().update_value.key.discovery_type;
			probeParams.http_auth = getUpdate().update_value.key.http_auth;
			probeParams.http_auth_password = getUpdate().update_value.key.http_auth_password;
			probeParams.http_auth_username = getUpdate().update_value.key.http_auth_user;
			probeParams.http_request = getUpdate().update_value.key.http_method;
			probeParams.interval = getUpdate().update_value.interval;
			probeParams.is_active = getUpdate().update_value.status.equals(Constants._true);
			probeParams.multiplier = getUpdate().update_value.multiplier;
			probeParams.name = getUpdate().update_value.name;
			probeParams.port = getUpdate().update_value.key.port;
			probeParams.port_extra = getUpdate().update_value.key.port_extra;
			// probeParams.probe_id = getUpdate().probe_id;
			probeParams.protocol = getUpdate().update_value.key.proto;
			probeParams.rbl = getUpdate().update_value.key.rbl;
			probeParams.oid = getUpdate().update_value.key.snmp_oid;
			// probeParams.template_id = getUpdate().template_id;
			probeParams.timeout = getUpdate().update_value.key.timeout;
			probeParams.type = getUpdate().update_value.type;
			probeParams.url = getUpdate().update_value.key.urls;
			// probeParams.user_id = getUpdate().user_id;
			probeParams.snmp_datatype = getUpdate().update_value.key.value_type;
			probeParams.snmp_unit = getUpdate().update_value.key.value_unit;
			probeParams.snmp_store_as = getUpdate().update_value.key.store_value_as;
			probeParams.discovery_trigger_id = getUpdate().update_value.key.trigger_id;
			probeParams.discovery_trigger_severity = getUpdate().update_value.key.trigger_severity;
			probeParams.discovery_trigger_code = getUpdate().update_value.key.discovery_trigger;

			getUser().addTemplateProbe(probeParams);
			RunnableProbe runnableProbe = new RunnableProbe(host, probe);
			runnableProbe.start();
			getUser().addRunnableProbe(runnableProbe);

		} else {
			probe = getUser().getProbeFor(getUpdate().probe_id);
			ChangeInterval(probe.getInterval());
			probe.updateKeyValues(getUpdate().update_value);
		}

		Logit.LogInfo("New probe was update");
		return true;
	}

	//TODO: check with ROI
	private boolean ChangeInterval(long currentInterval) {
		try {
			if (currentInterval != getUpdate().update_value.interval) {
				HashMap<String, RunnableProbe> runnableProbes = RunnableProbeContainer.getInstanece().getByProbe(getUpdate().probe_id);
				if (runnableProbes == null)
					return false;
				for (RunnableProbe runnableProbe : runnableProbes.values()) {
					if (runnableProbe.stop())
						runnableProbe.start();
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Boolean Update() {
		super.Update();

		HashMap<String, RunnableProbe> runnableProbes = RunnableProbeContainer.getInstanece().getByProbe(getUpdate().probe_id);
		for (RunnableProbe runnableProbe : runnableProbes.values()) {
			if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.name))
				runnableProbe.getProbe().setName(getUpdate().update_value.name);
			if (getUpdate().update_value.multiplier != null)
				runnableProbe.getProbe().setMultiplier(getUpdate().update_value.multiplier);
			if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.status))
				runnableProbe.getProbe().setActive(getUpdate().update_value.status.equals(Constants._true));
			if (getUpdate().update_value.interval != null
					&& runnableProbe.getProbe().getInterval() != getUpdate().update_value.interval) {
				runnableProbe.changeRunnableProbeInterval(getUpdate().update_value.interval);
			}

			// TODO: What to do with them
			// probeParams.template_id = update.template_id;
			// probeParams.type = update.update_value.type;

			if (getUpdate().update_value.key != null)
				runnableProbe.getProbe().updateKeyValues(getUpdate().update_value);
			// SnmpProbe Probe (SnmpProbe)runnableProbe.getProbe();
		}

		return true;
	}

	@Override
	public Boolean Delete() {
		super.Delete();
		RunnableProbeContainer.getInstanece().removeByProbeId(getUpdate().probe_id);
		return true;
	}
}
