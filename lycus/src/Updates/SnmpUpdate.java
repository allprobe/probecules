package Updates;

import java.util.UUID;

import Collectors.SnmpCollector;
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
		SnmpCollector snmpCollector = (SnmpCollector)getUser().getCollectors().get(getUpdate().object_id);
		
		if (GeneralFunctions.isChanged(snmpCollector.getName(), getUpdate().update_value.name)) 
		{
			snmpCollector.setName(getUpdate().update_value.name);
			Logit.LogCheck("Snmp name " + snmpCollector.getName() +  " has changed to " + getUpdate().update_value.name);
		}
		
		if (GeneralFunctions.isChanged(snmpCollector.getVersion(), getUpdate().update_value.snmp_version)) 
		{
			snmpCollector.setVersion(getUpdate().update_value.snmp_version);
			Logit.LogCheck("Snmp version for  " + snmpCollector.getName() +  " has changed to " + getUpdate().update_value.snmp_version);
		}
		
		if (GeneralFunctions.isChanged(snmpCollector.getCommunityName(), getUpdate().update_value.snmp_community)) 
		{
			snmpCollector.setCommunityName(getUpdate().update_value.snmp_community);
			Logit.LogCheck("Snmp community name for  " + snmpCollector.getName() +  " has changed to " + getUpdate().update_value.snmp_community);
		}
		
		if (GeneralFunctions.isChanged(snmpCollector.getUserName(), getUpdate().update_value.snmp_user)) 
		{
			snmpCollector.setUserName(getUpdate().update_value.snmp_user); 
			Logit.LogCheck("Snmp user name " + snmpCollector.getName() +  " has changed to " + getUpdate().update_value.snmp_user);
		}
		
		if (GeneralFunctions.isChanged(snmpCollector.getSec(), getUpdate().update_value.snmp_sec)) 
		{
			snmpCollector.setSec(getUpdate().update_value.snmp_sec);
			Logit.LogCheck("Npings count for " + snmpCollector.getName() +  " has changed to " + getUpdate().update_value.snmp_sec);
		}
		  
		if (GeneralFunctions.isChanged(snmpCollector.getAlgo(), getUpdate().update_value.snmp_auth_method)) 
		{
			snmpCollector.setAlgo(getUpdate().update_value.snmp_auth_method); 
			Logit.LogCheck("Snmp algo for " + snmpCollector.getName() +  " has changed to " + getUpdate().update_value.snmp_auth_method);
		}
	 
		if (GeneralFunctions.isChanged(snmpCollector.getAuthPass(), getUpdate().update_value.snmp_auth_password)) 
		{
			snmpCollector.setAuthPass(getUpdate().update_value.snmp_auth_password);
			Logit.LogCheck("Snmp auto password for " + snmpCollector.getName() +  " has changed");
		}
		
		if (GeneralFunctions.isChanged(snmpCollector.getCryptType(), getUpdate().update_value.snmp_crypt_method)) 
		{
			snmpCollector.setCryptType(getUpdate().update_value.snmp_crypt_method); 
			Logit.LogCheck("Snmp crypt method for " + snmpCollector.getName() +  " has changed to " + getUpdate().update_value.snmp_crypt_method);
		}
	
		if (GeneralFunctions.isChanged(snmpCollector.getCryptPass(), getUpdate().update_value.snmp_crypt_password)) 
		{
			snmpCollector.setCryptPass(getUpdate().update_value.snmp_crypt_password);
			Logit.LogCheck("Snmp crypt password for " + snmpCollector.getName() +  " has changed");
		}
		
		if (GeneralFunctions.isChanged(snmpCollector.getTimeout(), getUpdate().update_value.timeout)) 
		{
			snmpCollector.setTimeout(getUpdate().update_value.timeout);
			Logit.LogCheck("Snmp timeout for " + snmpCollector.getName() +  " has changed to " + getUpdate().update_value.timeout);
		}
		
		if (GeneralFunctions.isChanged(snmpCollector.getPort(), getUpdate().update_value.snmp_port)) 
		{
			snmpCollector.setPort(getUpdate().update_value.snmp_port);
			Logit.LogCheck("Snmp port for " + snmpCollector.getName() +  " has changed to " + getUpdate().update_value.snmp_port);
		}  	
		
		Logit.LogCheck("Snmp template: " + getUpdate().object_id + " was updated");
		return true;
	}
	
	@Override
	public Boolean Delete()
	{
		super.Delete();
		getUser().getCollectors().remove(getUpdate().object_id);
		Logit.LogCheck("Snmp template: " + getUpdate().object_id + " was removed");
		return true;
	}
}
