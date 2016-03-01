package lycus.Updates;

import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import GlobalConstants.Constants;
import GlobalConstants.Enums.ApiAction;
import Model.UpdateModel;
import lycus.DAL;
import lycus.GeneralFunctions;
import lycus.Host;
import lycus.RunnableProbe;
import lycus.SnmpTemplate;
import lycus.User;
import lycus.UsersManager;
import lycus.Interfaces.IDAL;

public class HostUpdate extends BaseUpdate {

	public HostUpdate(UpdateModel update) {
		super(update);
		// TODO Auto-generated constructor stub
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

		if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.name)) {
			host.setName(getUpdate().update_value.name);
		}
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.ip)) {
			host.setHostIp(getUpdate().update_value.ip);
		}
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.snmp_template)) {
			SnmpTemplate snmpTemplate = getUser().getSnmpTemplates()
					.get(UUID.fromString(getUpdate().update_value.snmp_template));
			if (snmpTemplate == null) {
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
				snmpTemplate = getUser().getSnmpTemplates()
						.get(UUID.fromString(getUpdate().update_value.snmp_template));
			}
			host.setSnmpTemp(snmpTemplate);
		}
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.notifications_group)) {
			host.setNotificationGroups(UUID.fromString(getUpdate().update_value.notifications_group));
		}
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.bucket)) {

		}
		if (getUpdate().update_value.status != null) {
			host.setHostStatus(getUpdate().update_value.status);
		}
		return true;
	}

	@Override
	public Boolean Delete() {
		super.Delete();

		Host host = getUser().getHost(UUID.fromString(getUpdate().host_id));
		if (host == null)
			return false;

		for (RunnableProbe runnableProbe : host.getRunnableProbes().values()) {
			try {
				runnableProbe.stop();
			} catch (Exception e) {
			}
		}

		host.getRunnableProbes().clear();
		getUser().getHosts().remove(UUID.fromString(getUpdate().host_id));
		return true;
	}
}
