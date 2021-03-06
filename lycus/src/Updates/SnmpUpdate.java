package Updates;

import java.util.UUID;

import Collectors.BaseCollector;
import Collectors.SnmpCollector;
import Collectors.SqlCollector;
import Model.UpdateModel;
import Utils.GeneralFunctions;
import Utils.Logit;

public class SnmpUpdate extends BaseUpdate {
	public SnmpUpdate(UpdateModel update) {
		super(update);
//		Logit.LogCheck("Updating snmp collector");
		// TODO SnmpUpdate()
	}

	@Override
	public Boolean New() {
		return true;
	}

	@Override
	public Boolean Update() {
//		Logit.LogCheck("Updating snmp collector");
		super.Update();
		Logit.LogCheck("Updating snmp collector");
		try {
			SnmpCollector snmpCollector = null;
			SqlCollector sqlCollector = null;
			Logit.LogCheck("Updating snmp collector: "+getUpdate().object_id);
			BaseCollector collector = getUser().getCollectors().get(getUpdate().object_id);
			if (collector instanceof SnmpCollector)
				snmpCollector = (SnmpCollector) getUser().getCollectors().get(getUpdate().object_id);
			else if (collector instanceof SqlCollector)
				sqlCollector = (SqlCollector) getUser().getCollectors().get(getUpdate().object_id);
			String failedObject = getUpdate().update_value.name;
			if (sqlCollector != null) {
				if (GeneralFunctions.isChanged(sqlCollector.getName(), getUpdate().update_value.name)) {
					sqlCollector.setName(getUpdate().update_value.name);
					Logit.LogCheck(
							"Sql name " + sqlCollector.getName() + " has changed to " + getUpdate().update_value.name);
				}
				
				if (GeneralFunctions.isChanged(sqlCollector.getSql_port(), getUpdate().update_value.sql_port)) {
					sqlCollector.setSql_port(getUpdate().update_value.sql_port);
					Logit.LogCheck("Snmp sql port for " + sqlCollector.getSql_port() + " has changed to "
							+ getUpdate().update_value.sql_port);
				}

				if (GeneralFunctions.isChanged(sqlCollector.getSql_password(), getUpdate().update_value.sql_password)) {
					sqlCollector.setSql_password(getUpdate().update_value.sql_password);
					Logit.LogCheck("Snmp sql password for " + sqlCollector.getSql_password() + " has changed to "
							+ getUpdate().update_value.sql_password);
				}
				
				if (GeneralFunctions.isChanged(sqlCollector.getSql_type(), getUpdate().update_value.sql_type)) {
					sqlCollector.setSql_type(getUpdate().update_value.sql_type);
					Logit.LogCheck("Snmp sql type for " + sqlCollector.getSql_type() + " has changed to "
							+ getUpdate().update_value.sql_type);
				}

				if (GeneralFunctions.isChanged(sqlCollector.getSql_user(), getUpdate().update_value.sql_user)) {
					sqlCollector.setSql_user(getUpdate().update_value.sql_user);
					Logit.LogCheck("Snmp sql user for " + sqlCollector.getSql_user() + " has changed to "
							+ getUpdate().update_value.sql_user);
				}

				if (GeneralFunctions.isChanged(sqlCollector.getSql_sec(), getUpdate().update_value.sql_sec)) {
					sqlCollector.setSql_sec(getUpdate().update_value.sql_sec);
					Logit.LogCheck("Snmp sql sqc for " + sqlCollector.getSql_sec() + " has changed to "
							+ getUpdate().update_value.sql_sec);
				}
			}
			else if (snmpCollector != null) {
				try {
					if (GeneralFunctions.isChanged(snmpCollector.getName(), getUpdate().update_value.name)) {
						snmpCollector.setName(getUpdate().update_value.name);
						Logit.LogCheck(
								"Snmp name " + snmpCollector.getName() + " has changed to " + getUpdate().update_value.name);
					}

					failedObject = getUpdate().update_value.snmp_version.toString();
					if (GeneralFunctions.isChanged(snmpCollector.getVersion(), getUpdate().update_value.snmp_version)) {
						snmpCollector.setVersion(getUpdate().update_value.snmp_version);
						Logit.LogCheck("Snmp version for  " + snmpCollector.getName() + " has changed to "
								+ getUpdate().update_value.snmp_version);
					}
					
					failedObject = getUpdate().update_value.snmp_community;
					if (GeneralFunctions.isChanged(snmpCollector.getCommunityName(), getUpdate().update_value.snmp_community)) {
						snmpCollector.setCommunityName(getUpdate().update_value.snmp_community);
						Logit.LogCheck("Snmp community name for  " + snmpCollector.getName() + " has changed to "
								+ getUpdate().update_value.snmp_community);
					}
					
					failedObject = getUpdate().update_value.snmp_user;
					if (GeneralFunctions.isChanged(snmpCollector.getUserName(), getUpdate().update_value.snmp_user)) {
						snmpCollector.setUserName(getUpdate().update_value.snmp_user);
						Logit.LogCheck("Snmp user name " + snmpCollector.getName() + " has changed to "
								+ getUpdate().update_value.snmp_user);
					}
					
					failedObject = getUpdate().update_value.snmp_sec;
					if (GeneralFunctions.isChanged(snmpCollector.getSec(), getUpdate().update_value.snmp_sec)) {
						snmpCollector.setSec(getUpdate().update_value.snmp_sec);
						Logit.LogCheck("Npings count for " + snmpCollector.getName() + " has changed to "
								+ getUpdate().update_value.snmp_sec);
					}
					
					failedObject =  getUpdate().update_value.snmp_auth_method;
					if (GeneralFunctions.isChanged(snmpCollector.getAlgo(), getUpdate().update_value.snmp_auth_method)) {
						snmpCollector.setAlgo(getUpdate().update_value.snmp_auth_method);
						Logit.LogCheck("Snmp algo for " + snmpCollector.getName() + " has changed to "
								+ getUpdate().update_value.snmp_auth_method);
					}
					
					failedObject = getUpdate().update_value.snmp_auth_password;
					if (GeneralFunctions.isChanged(snmpCollector.getAuthPass(), getUpdate().update_value.snmp_auth_password)) {
						snmpCollector.setAuthPass(getUpdate().update_value.snmp_auth_password);
						Logit.LogCheck("Snmp auto password for " + snmpCollector.getName() + " has changed");
					}
					
					failedObject = getUpdate().update_value.snmp_crypt_method;
					if (GeneralFunctions.isChanged(snmpCollector.getCryptType(), getUpdate().update_value.snmp_crypt_method)) {
						snmpCollector.setCryptType(getUpdate().update_value.snmp_crypt_method);
						Logit.LogCheck("Snmp crypt method for " + snmpCollector.getName() + " has changed to "
								+ getUpdate().update_value.snmp_crypt_method);
					}
					
					failedObject = getUpdate().update_value.snmp_crypt_password;
					if (GeneralFunctions.isChanged(snmpCollector.getCryptPass(),
							getUpdate().update_value.snmp_crypt_password)) {
						snmpCollector.setCryptPass(getUpdate().update_value.snmp_crypt_password);
						Logit.LogCheck("Snmp crypt password for " + snmpCollector.getName() + " has changed");
					}
					
					failedObject =  getUpdate().update_value.timeout.toString();
					if (GeneralFunctions.isChanged(snmpCollector.getTimeout(), getUpdate().update_value.timeout)) {
						snmpCollector.setTimeout(getUpdate().update_value.timeout);
						Logit.LogCheck("Snmp timeout for " + snmpCollector.getName() + " has changed to "
								+ getUpdate().update_value.timeout);
					}

					if (GeneralFunctions.isChanged(snmpCollector.getPort(), getUpdate().update_value.snmp_port)) {
						snmpCollector.setPort(getUpdate().update_value.snmp_port);
						Logit.LogCheck("Snmp port for " + snmpCollector.getName() + " has changed to "
								+ getUpdate().update_value.snmp_port);
					}
				} catch (Exception ex) {
					Logit.LogError("SnmpUpdate - Update()", "Failed to update " + "\n" + failedObject , ex);
					ex.printStackTrace();
				}

				Logit.LogCheck("Snmp template: " + getUpdate().object_id + " was updated");
				return true;
			}
		} catch (Exception e) {
			Logit.LogError("SnmpUpdate - Update()", "Snmp template: " + getUpdate().object_id.toString() + " was not updated, collector does not exist" , e);
			e.printStackTrace();
		}
		
		
		return true;
		
	}

	@Override
	public Boolean Delete() {
		super.Delete();
		getUser().getCollectors().remove(getUpdate().object_id);
		Logit.LogCheck("Snmp template: " + getUpdate().object_id + " was removed");
		return true;
	}
}
