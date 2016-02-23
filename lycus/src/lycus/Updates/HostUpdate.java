package lycus.Updates;

import java.util.UUID;

import Model.UpdateModel;
import lycus.GeneralFunctions;
import lycus.Host;
import lycus.SnmpTemplate;
import lycus.User;
import lycus.UsersManager;

public class HostUpdate  extends BaseUpdate {

	public HostUpdate(UpdateModel update) {
		super(update);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Boolean New()
	{
		super.New();
	
		return true;
	}
	
	@Override
	public Boolean Update()
	{
		super.Update();
		User user = UsersManager.getUser(UUID.fromString(getUpdate().user_id));
		if (user == null)
			return false;
		
		Host host = user.getHost(UUID.fromString(getUpdate().host_id));
		if (host == null)
			return false;
		
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.name))
		{
			host.setName(getUpdate().update_value.name);
		}
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.ip))
		{
			host.setHostIp(getUpdate().update_value.ip);
		}
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.snmp_template))
		{
			SnmpTemplate snmpTemplate = user.getSnmpTemplates().get(UUID.fromString(getUpdate().update_value.snmp_template)); 
			if (snmpTemplate == null)
			{
				// TODO: Fetch from Ran and create new
				
			}
			host.setSnmpTemp(snmpTemplate);
		}
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.notifications_group))
		{
			host.setNotificationGroups(UUID.fromString(getUpdate().update_value.notifications_group));
		}
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.bucket))
		{
 
		}
		if (getUpdate().update_value.status != null)
		{	
			host.setHostStatus(getUpdate().update_value.status);
		}
		return true;
	}
	
	@Override
	public Boolean Delete()
	{
		super.Delete();
		
		User user = UsersManager.getUser(UUID.fromString(getUpdate().user_id));
		if (user == null)
			return false;
		
		Host host = user.getHost(UUID.fromString(getUpdate().host_id));
		if (host == null)
			return false;
		
		return true;
	}
}
