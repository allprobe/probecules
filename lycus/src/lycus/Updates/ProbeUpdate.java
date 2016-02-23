package lycus.Updates;

import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import Model.UpdateModel;
import lycus.Constants;
import lycus.DAL;
import lycus.Enums.ApiAction;
import lycus.Host;
import lycus.Log;
import lycus.LogType;
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

		UUID userId=UUID.fromString(getUpdate().user_id);
		
		User user = UsersManager.getUser(userId);
		Host host = null;
		Probe probe = null;

		if(user==null)
		UsersManager.getUsers().put(userId, new User(userId));
		
		if (!user.isHostExist(UUID.fromString(getUpdate().host_id))) {
			// Get host from Ran for host_id
			IDAL dal = DAL.getInstanece();
			JSONObject hosts = new JSONObject();
			JSONArray hostsArray = new JSONArray();
			hostsArray.add(getUpdate().host_id);
			hosts.put(Constants.hosts, hostsArray);

			JSONObject jsonObject = dal.put(ApiAction.GetHosts, hosts);

			JSONArray jsonArray = (JSONArray) jsonObject.get(Constants.hosts);

			UsersManager.addHosts(jsonArray);

			// host = new Host(update.host_id, String name, String host_ip,
			// boolean hostStatus, boolean snmpStatus,String bucket,UUID
			// notifGroups)
			// user.addHost(host);
		}
		host = user.getHost(UUID.fromString(getUpdate().host_id));
		if (host == null) {
			SysLogger.Record(new Log("Failed update from type NEW PROBE - unknown host", LogType.Warn));
			return false;
		}

		if (!user.isProbeExist(getUpdate().probe_id)) {
			// Roi: Create probe from json
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
