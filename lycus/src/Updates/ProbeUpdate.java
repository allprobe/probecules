package Updates;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import DAL.DAL;
import GlobalConstants.Constants;
import GlobalConstants.Enums.ApiAction;
import Interfaces.IDAL;
import Model.ProbeParams;
import Model.UpdateModel;
import Probes.BaseProbe;
import Utils.Logit;
import lycus.Host;
import lycus.RunnableProbe;
import lycus.RunnableProbeContainer;
import lycus.UsersManager;

public class ProbeUpdate extends BaseUpdate {

	public ProbeUpdate(UpdateModel update) {
		super(update);
	}

	@Override
	public Boolean New() {
		super.New();

		Host host = null;
		BaseProbe probe = null;

		try {
			if (!getUser().isHostExist(UUID.fromString(getUpdate().host_id))) {
				// Get host from Ran for host_id
				IDAL dal = DAL.getInstanece();
				JSONObject hosts = new JSONObject();
				JSONArray hostsArray = new JSONArray();
				hostsArray.add(getUpdate().host_id);
				hosts.put(Constants.hosts, hostsArray);

				JSONObject jsonObject = dal.put(ApiAction.GetHosts, hosts);
				JSONArray jsonArray = (JSONArray) jsonObject.get("hosts");
				UsersManager.addHostsForUpdate(jsonArray);
				Logit.LogCheck("New host was created from server");
			}

			if (!UsersManager.getUser(getUpdate().user_id).equals(getUser()))
				setUser(UsersManager.getUser(getUpdate().user_id));

			host = getUser().getHost(UUID.fromString(getUpdate().host_id));
			if (host == null) {
				Logit.LogError("ProbeUpdate - New()", "Failed update from type NEW PROBE - unknown host");
				return false;
			}

			if (!getUser().isProbeExist(getUpdate().probe_id)) {

				// host =
				// getUser().getHost(UUID.fromString(getUpdate().host_id));
				ProbeParams probeParams = new ProbeParams();
				probeParams.template_id = getUpdate().template_id;
				probeParams.bytes = getUpdate().update_value.key.bytes;
				probeParams.npings = getUpdate().update_value.key.npings;
				probeParams.element_interval = getUpdate().update_value.key.element_interval;
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
				probeParams.probe_id = getUpdate().probe_id;
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
				// todo: take care, also update
				probeParams.trigger_id = getUpdate().update_value.key.trigger_id;

				// Discovery
				probeParams.discovery_type = getUpdate().update_value.key.discovery_type;
				probeParams.triggers = getUpdate().update_value.conditions;
				 probeParams.severity = getUpdate().update_value.severity;
 				 probeParams.triggerName = getUpdate().update_value.name;
				 probeParams.tuple = getUpdate().update_value.tuple;
				 probeParams.severity = getUpdate().update_value.severity;
				 probeParams.severity = getUpdate().update_value.severity;
				 probeParams.triggerId = getUpdate().update_value.id;

				probe = getUser().addTemplateProbe(probeParams);
				Logit.LogCheck("New probe was created");

			} else {
				probe = getUser().getProbeFor(getUpdate().probe_id);
				// ChangeInterval(probe.getInterval());
				probe.updateKeyValues(getUpdate());
				Logit.LogCheck("New probe was updated,  probe was already exist");
			}

			RunnableProbe runnableProbe = new RunnableProbe(host, probe);
			RunnableProbeContainer.getInstanece().add(runnableProbe);
			// getUser().addRunnableProbe(runnableProbe);
			Logit.LogCheck("New Runnable probe was created: " + runnableProbe.getId());

		} catch (Exception e) {
			Logit.LogError("ProbeUpdate - New()", "New probe Could not be created\n" + getUpdate().template_id + "@"
					+ getUpdate().host_id + "@" + getUpdate().probe_id);
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public Boolean Update() {
		super.Update();

		ConcurrentHashMap<String, RunnableProbe> runnableProbes = RunnableProbeContainer.getInstanece()
				.getByProbe(getUpdate().probe_id);
		for (RunnableProbe runnableProbe : runnableProbes.values()) {
			// TODO: What to do with them
			// probeParams.template_id = update.template_id;
			// probeParams.type = update.update_value.type;

			if (getUpdate().update_value != null)
				runnableProbe.getProbe().updateKeyValues(getUpdate());

			if (getUpdate().update_value.interval != null
					&& runnableProbe.getProbe().getInterval() != getUpdate().update_value.interval) {
				RunnableProbeContainer.getInstanece().changeInterval(runnableProbe, getUpdate().update_value.interval);
			}
		}

		return true;
	}

	@Override
	public Boolean Delete() {
		super.Delete();
		if (!getUpdate().probe_id.contains("@")) {
			RunnableProbeContainer.getInstanece().removeByProbeId(getUpdate().probe_id);
			Logit.LogCheck("All runnable probes with ProbeID: " + getUpdate().probe_id + " was removed");
		} else /// probeId == runnableProbeId
		{
			RunnableProbeContainer.getInstanece().removeByRunnableProbeId(getUpdate().probe_id);
			Logit.LogCheck("Runnable probes with RunnableProbeID: " + getUpdate().probe_id + " was removed");
		}

		return true;
	}
}
