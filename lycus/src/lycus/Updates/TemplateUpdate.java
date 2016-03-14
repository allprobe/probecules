package lycus.Updates;

import java.util.UUID;

import lycus.Model.UpdateModel;
import lycus.Host;
import lycus.UsersManager;

public class TemplateUpdate  extends BaseUpdate{

	public TemplateUpdate(UpdateModel update) {
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
//		super.Update();
		
		return true;
	}
	
	@Override
	public Boolean Delete()
	{
		super.Delete();
		
		for (Host host : getUser().getHosts().values())
		{
			host.removeRunnableProbes(UUID.fromString(getUpdate().object_id));
			if(host.getRunnableProbes().size()==0)
				getUser().getHosts().remove(host.getHostId());
		}
		if(getUser().getHosts().size()==0)
			UsersManager.getUsers().remove(getUser().getUserId());
		return true;
	}
}
