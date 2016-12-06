package Updates;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
		
		if (getUpdate().update_value.snmp_template != null && snmpTemplateId != getUpdate().update_value.snmp_template) {
			UUID uuid = null;
			try{
			    uuid = UUID.fromString(getUpdate().update_value.snmp_template);
			} catch (Exception exception){
			    //handle the case where string is not valid UUID 
			}
			
			if (uuid == null)
			{
				host.setSnmpTemp(null);
				Logit.LogCheck("Snmp Template for host " + host.getName() + " has changed");
			}
			else
			{
				SnmpTemplate snmpTemplate = getUser().getSnmpTemplates().get(uuid);
				if (snmpTemplate == null) {
					snmpTemplate = fetchSnmpTemplate();
				}
				host.setSnmpTemp(snmpTemplate);
				if (snmpTemplate != null)
					Logit.LogCheck("Snmp Template for host " + host.getName() + " has changed");
			}
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
			boolean isActive = getUpdate().update_value.status.equals(Constants._true);
			host.setHostStatus(isActive);
			ConcurrentHashMap<String, RunnableProbe>  runnableProbes = RunnableProbeContainer.getInstanece().getByHost(getUpdate().host_id);
			for (RunnableProbe runnableProbe : runnableProbes.values()) {
				runnableProbe.setActive(isActive);
				Logit.LogCheck("Status for " + runnableProbe.getProbe().getName() + " Is " + isActive);
			}
			
			Logit.LogCheck("Status for host " + host.getName() + " has changed to "
					+ isActive);
		}
		
		Logit.LogCheck("Host " + getUpdate().host_id +  " has updated");
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

//		Host host;
//		try {
//			host = getUser().getHost(UUID.fromString(getUpdate().host_id));
//			if (host == null)
//			{
//				Logit.LogError("HostUpdate - Delete()", "Error deleting HostId:  " + getUpdate().host_id);
//				return false;
//			}
//		} catch (Exception e1) {
//			Logit.LogError("HostUpdate - Delete()", "getting HostId:  " + getUpdate().host_id);
//			e1.printStackTrace();
//			return false;
//		}
		
		ConcurrentHashMap<String, RunnableProbe> runnableProbes = RunnableProbeContainer.getInstanece()
				.getByHost(getUpdate().host_id);
		
		List<RunnableProbe> rps = new ArrayList<RunnableProbe>();
		
		for (RunnableProbe runnableProbe : runnableProbes.values()) {
			try {
				rps.add(runnableProbe);

			} catch (Exception e) {
				Logit.LogError("HostUpdate - Delete()", "Runnable probe: " + runnableProbe.getId() +  " did not accumulate");
			}
		}

		for (RunnableProbe runnableProbe : rps) {
			try {
				RunnableProbeContainer.getInstanece().remove(runnableProbe);
				Logit.LogCheck("Runnable probe " + runnableProbe.getId() +  " was removed");

			} catch (Exception e) {
				Logit.LogError("HostUpdate - Delete()", "Runnable probe: " + runnableProbe.getId() +  " was not remove");
			}
		}
		
		getUser().getHosts().remove(UUID.fromString(getUpdate().host_id));
		Logit.LogCheck("Host: " + getUpdate().host_id +  " has removed");
		return true;
	}
}
