package Updates;

import java.util.UUID;

import Collectors.SnmpTemplate;
import Model.UpdateModel;
import Utils.GeneralFunctions;
import Utils.Logit;

public class SnmpUpdate  extends BaseUpdate{
	public SnmpUpdate(UpdateModel update) {
		super(update);
		// TODO SnmpUpdate()
	}

	@Override
	public Boolean New()
	{
		return true;
	}
	
	@Override
	public Boolean Update()
	{
		super.Update();
		SnmpTemplate snmpTemplate = getUser().getSnmpTemplates().get(UUID.fromString(getUpdate().object_id));
		
		if (GeneralFunctions.isChanged(snmpTemplate.getName(), getUpdate().update_value.name)) 
		{
			snmpTemplate.setName(getUpdate().update_value.name);
			Logit.LogCheck("Snmp name " + snmpTemplate.getName() +  " has changed to " + getUpdate().update_value.name);
		}
		
		if (GeneralFunctions.isChanged(snmpTemplate.getVersion(), getUpdate().update_value.snmp_version)) 
		{
			snmpTemplate.setVersion(getUpdate().update_value.snmp_version);
			Logit.LogCheck("Snmp version for  " + snmpTemplate.getName() +  " has changed to " + getUpdate().update_value.snmp_version);
		}
		
		if (GeneralFunctions.isChanged(snmpTemplate.getCommunityName(), getUpdate().update_value.snmp_community)) 
		{
			snmpTemplate.setCommunityName(getUpdate().update_value.snmp_community);
			Logit.LogCheck("Snmp community name for  " + snmpTemplate.getName() +  " has changed to " + getUpdate().update_value.snmp_community);
		}
		
		if (GeneralFunctions.isChanged(snmpTemplate.getUserName(), getUpdate().update_value.snmp_user)) 
		{
			snmpTemplate.setUserName(getUpdate().update_value.snmp_user); 
			Logit.LogCheck("Snmp user name " + snmpTemplate.getName() +  " has changed to " + getUpdate().update_value.snmp_user);
		}
		
		if (GeneralFunctions.isChanged(snmpTemplate.getSec(), getUpdate().update_value.snmp_sec)) 
		{
			snmpTemplate.setSec(getUpdate().update_value.snmp_sec);
			Logit.LogCheck("Npings count for " + snmpTemplate.getName() +  " has changed to " + getUpdate().update_value.snmp_sec);
		}
		  
		if (GeneralFunctions.isChanged(snmpTemplate.getAlgo(), getUpdate().update_value.snmp_auth_method)) 
		{
			snmpTemplate.setAlgo(getUpdate().update_value.snmp_auth_method); 
			Logit.LogCheck("Snmp algo for " + snmpTemplate.getName() +  " has changed to " + getUpdate().update_value.snmp_auth_method);
		}
	 
		if (GeneralFunctions.isChanged(snmpTemplate.getAuthPass(), getUpdate().update_value.snmp_auth_password)) 
		{
			snmpTemplate.setAuthPass(getUpdate().update_value.snmp_auth_password);
			Logit.LogCheck("Snmp auto password for " + snmpTemplate.getName() +  " has changed");
		}
		
		if (GeneralFunctions.isChanged(snmpTemplate.getCryptType(), getUpdate().update_value.snmp_crypt_method)) 
		{
			snmpTemplate.setCryptType(getUpdate().update_value.snmp_crypt_method); 
			Logit.LogCheck("Snmp crypt method for " + snmpTemplate.getName() +  " has changed to " + getUpdate().update_value.snmp_crypt_method);
		}
	
		if (GeneralFunctions.isChanged(snmpTemplate.getCryptPass(), getUpdate().update_value.snmp_crypt_password)) 
		{
			snmpTemplate.setCryptPass(getUpdate().update_value.snmp_crypt_password);
			Logit.LogCheck("Snmp crypt password for " + snmpTemplate.getName() +  " has changed");
		}
		
		if (GeneralFunctions.isChanged(snmpTemplate.getTimeout(), getUpdate().update_value.timeout)) 
		{
			snmpTemplate.setTimeout(getUpdate().update_value.timeout);
			Logit.LogCheck("Snmp timeout for " + snmpTemplate.getName() +  " has changed to " + getUpdate().update_value.timeout);
		}
		
		if (GeneralFunctions.isChanged(snmpTemplate.getPort(), getUpdate().update_value.snmp_port)) 
		{
			snmpTemplate.setPort(getUpdate().update_value.snmp_port);
			Logit.LogCheck("Snmp port for " + snmpTemplate.getName() +  " has changed to " + getUpdate().update_value.snmp_port);
		}  	
		
		Logit.LogCheck("Snmp template: " + getUpdate().object_id + " was updated");
		return true;
	}
	
	@Override
	public Boolean Delete()
	{
		super.Delete();
		getUser().getSnmpTemplates().remove(UUID.fromString(getUpdate().object_id));
		Logit.LogCheck("Snmp template: " + getUpdate().object_id + " was removed");
		return true;
	}
}
