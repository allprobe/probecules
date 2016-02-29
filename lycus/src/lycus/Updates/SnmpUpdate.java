package lycus.Updates;

import java.util.UUID;

import Model.UpdateModel;
import lycus.SnmpTemplate;

public class SnmpUpdate  extends BaseUpdate{
	public SnmpUpdate(UpdateModel update) {
		super(update);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Boolean New()
	{
//		super.New();
	
		return true;
	}
	
	@Override
	public Boolean Update()
	{
		super.Update();
		
		SnmpTemplate snmpTemplate = getUser().getSnmpTemplates().get(getUpdate().template_id);
		snmpTemplate.setSnmpTemplateName(getUpdate().update_value.name);
		snmpTemplate.setVersion(getUpdate().update_value.snmp_version);
		snmpTemplate.setCommunityName(getUpdate().update_value.snmp_community);
		snmpTemplate.setUserName(getUpdate().update_value.snmp_user); 		
		snmpTemplate.setSec(getUpdate().update_value.snmp_sec);
		snmpTemplate.setAlgo(getUpdate().update_value.snmp_auth_method);           
		snmpTemplate.setAuthPass(getUpdate().update_value.snmp_auth_password); 
		snmpTemplate.setCryptType(getUpdate().update_value.snmp_crypt_method); 
		snmpTemplate.setCryptPass(getUpdate().update_value.snmp_crypt_password);
		snmpTemplate.setTimeout(getUpdate().update_value.timeout);
		snmpTemplate.setPort(getUpdate().update_value.snmp_port);
		
		return true;
	}
	
	@Override
	public Boolean Delete()
	{
		super.Delete();
		getUser().getSnmpTemplates().remove(UUID.fromString(getUpdate().object_id));
		
		return true;
	}
}
