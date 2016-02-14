package lycus.Updates;

import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.JsonObject;

import Model.UpdateModel;
import lycus.ApiInterface;
import lycus.DAL;
import lycus.Enums.ApiAction;
import lycus.Host;
import lycus.RunInnerProbesChecks;
import lycus.RunnableProbe;
import lycus.User;
import lycus.UsersManager;
import lycus.Interfaces.IDAL;
import lycus.Probes.Probe;
import lycus.Probes.SnmpProbe;

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

		if (!user.isHostExist(UUID.fromString(getUpdate().host_id))) {
			// Get host from Ran for host_id
			IDAL dal = DAL.getInstanece();
			JSONObject hosts = new JSONObject();
			JSONArray hostsArray = new JSONArray();
			hostsArray.add(getUpdate().host_id);
			hosts.put("hosts", hostsArray);

			JSONObject jsonObject = dal.put(ApiAction.GetHosts, hosts);

			// host = new Host(update.host_id, String name, String host_ip,
			// boolean hostStatus, boolean snmpStatus,String bucket,UUID
			// notifGroups)
			// user.addHost(host);
		} else {

			host = user.getHost(UUID.fromString(getUpdate().host_id));
		}

		if (!user.isProbeExist(getUpdate().probe_id)) {
			// Get probe from Ran for probe_id
			// host = new Host(update.host_id, String name, String host_ip,
			// boolean hostStatus, boolean snmpStatus,String bucket,UUID
			// notifGroups)
			// user.addProbe(probe);
		} else {
			probe = user.getProbeFor(getUpdate().probe_id);
		}

		probe.updateKeyValues(update.update_value.key);

		RunnableProbe runnableProbe = new RunnableProbe(host, probe);
		user.addRunnableProbe(runnableProbe);

		return true;
	}

	@Override
	public Boolean Update() {
		super.Update();

		User user = UsersManager.getUser(UUID.fromString(getUpdate().user_id));
		List<RunnableProbe> runnableProbes = user.getRunnableProbesFor(update.probe_id);
		for (RunnableProbe runnableProbe : runnableProbes) {
			runnableProbe.getProbe().setName(update.update_value.name);
			runnableProbe.getProbe().setMultiplier(update.update_value.multiplier);
			runnableProbe.getProbe().setActive(update.update_value.status);
			runnableProbe.getProbe().updateKeyValues(update.update_value.key);

			if (runnableProbe.getProbe().getInterval() != update.update_value.interval) {
				runnableProbe.changeRunnableProbeInterval(update.update_value.interval);
			}

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
