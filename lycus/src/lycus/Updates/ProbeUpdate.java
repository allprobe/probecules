package lycus.Updates;

import java.util.UUID;

import Model.UpdateModel;
import lycus.Host;
import lycus.RunnableProbe;
import lycus.User;
import lycus.UsersManager;

public class ProbeUpdate extends BaseUpdate {
	
	public ProbeUpdate(UpdateModel update)
	{
		super(update);
	}
	
	@Override
	public Boolean New()
	{
		super.New();
		//getUpdate().host_id
		//getUpdate().user_id
		
//		User user = UsersManager.getUser(UUID.fromString(getUpdate().user_id));
//		user.getAllRunnableProbes();
//		Host host = user.getHost(UUID.fromString(getUpdate().host_id));
//		RunnableProbe runnableProbe = host.getRunnableProbe(UUID.fromString(getUpdate().probe_id));
		
	 
	    //UsersManager.getAllUsersRunnableProbes()
		return true;
	}
	
	@Override
	public Boolean Update()
	{
		super.Update();
		
		
		return true;
	}
	
	@Override
	public Boolean Delete()
	{
		super.Delete();
		
		
		return true;
	}
}
