package Updates;

import java.util.HashMap;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import DAL.DAL;
import GlobalConstants.Constants;
import GlobalConstants.Enums.ApiAction;
import Interfaces.IDAL;
import Model.UpdateModel;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.Host;
import lycus.RunnableProbe;
import lycus.RunnableProbeContainer;
import lycus.SnmpTemplate;
import lycus.UsersManager;

public class HostUpdate extends BaseUpdate {

	public HostUpdate(UpdateModel update) {
		super(update);
	}

	@Override
	public Boolean New() {
		super.New();

		return true;
	}

	@Override
	public Boolean Update() {
		super.Update();

		Host host = getUser().getHost(UUID.fromString(getUpdate().host_id));
		if (host == null)
			return false;

		Logit.LogCheck("Updating Host: " + getUpdate().host_id);

		if (getUpdate().update_value.name != null && GeneralFunctions.isChanged(host.getName(), getUpdate().update_value.name)) {
			host.setName(getUpdate().update_value.name);
			Logit.LogCheck("Host name for " + host.getName() + " has changed to " + getUpdate().update_value.name);
		}
		if (getUpdate().update_value.ip != null && GeneralFunctions.isChanged(host.getHostIp(), getUpdate().update_value.ip)) {
			host.setHostIp(getUpdate().update_value.ip);
			Logit.LogCheck("Ip for host " + host.getName() + " has changed to " + getUpdate().update_value.ip);
		}
		
		String  snmpTemplateId = host.getSnmpTemp() != null ? host.getSnmpTemp().getSnmpTemplateId().toString() : null;
		
		if (getUpdate().update_value.snmp_template != null && GeneralFunctions.isChanged(snmpTemplateId, getUpdate().update_value.snmp_template)) {
			SnmpTemplate snmpTemplate = getUser().getSnmpTemplates().get(UUID.fromString(getUpdate().update_value.snmp_template));
			if (snmpTemplate == null) {
				snmpTemplate = fetchSnmpTemplate();
			}
			host.setSnmpTemp(snmpTemplate);
			if (snmpTemplate != null)
				Logit.LogCheck("Snmp Template for host " + host.getName() + " has changed");
		}
		String notificationGroup = host.getNotificationGroups() != null ? host.getNotificationGroups().toString() : null;
		
		if (GeneralFunctions.isChanged(notificationGroup, getUpdate().update_value.notifications_group)) {
			if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.notifications_group))
				host.setNotificationGroups(UUID.fromString(getUpdate().update_value.notifications_group));
			else 
				host.setNotificationGroups(null);
			Logit.LogCheck("Notifications group for host " + host.getName() + " has changed to "
					+ getUpdate().update_value.notifications_group);
		}
		if (getUpdate().update_value.bucket != null && GeneralFunctions.isChanged(host.getBucket(), getUpdate().update_value.bucket)) {
			host.setBucket(getUpdate().update_value.bucket);
			Logit.LogCheck("Bucket for host " + host.getName() + " has changed to " + getUpdate().update_value.bucket);
		}

		if (getUpdate().update_value.status != null
				&& host.getHostStatus() != getUpdate().update_value.status.equals(Constants._true)) {
			host.setHostStatus(getUpdate().update_value.status.equals(Constants._true));
			Logit.LogCheck("Status for host " + host.getName() + " has changed to "
					+ getUpdate().update_value.status.equals(Constants._true));
		}
		return true;
	}

	private SnmpTemplate fetchSnmpTemplate() {
		SnmpTemplate snmpTemplate;
		// Get snmp template from Ran for snmp_template_id
		IDAL dal = DAL.getInstanece();
		JSONObject snmpTemplates = new JSONObject();
		JSONArray snmpTemplatesArray = new JSONArray();
		JSONObject userSnmpTemplate = new JSONObject();
		userSnmpTemplate.put(getUser().getUserId(), getUpdate().update_value.snmp_template);
		snmpTemplatesArray.add(userSnmpTemplate);

		snmpTemplates.put(Constants.snmpTemplates, snmpTemplatesArray);

		JSONObject jsonObject = dal.put(ApiAction.GetSnmpTemplates, snmpTemplates);

		JSONArray jsonArray = (JSONArray) jsonObject.get("snmp_templates");
		UsersManager.addSnmpTemplates(jsonArray);
		snmpTemplate = getUser().getSnmpTemplates().get(UUID.fromString(getUpdate().update_value.snmp_template));
		return snmpTemplate;
	}

	@Override
	public Boolean Delete() {
		super.Delete();

		Host host = getUser().getHost(UUID.fromString(getUpdate().host_id));
		if (host == null)
			return false;

		HashMap<String, RunnableProbe> runnableProbes = RunnableProbeContainer.getInstanece()
				.getByHost(host.getHostId().toString());
		for (RunnableProbe runnableProbe : runnableProbes.values()) {
			try {
//				runnableProbe.stop();
				RunnableProbeContainer.getInstanece().remove(runnableProbe);
				Logit.LogCheck("Runnable probe " + runnableProbe.getId() +  " was removed");

			} catch (Exception e) {
				Logit.LogError("HostUpdate - Delete()", "Runnable probe " + runnableProbe.getId() +  " Could did not remove");
			}
		}

		getUser().getHosts().remove(UUID.fromString(getUpdate().host_id));
		Logit.LogCheck("Host " + getUpdate().host_id +  " has removed");
		return true;
	}
}
