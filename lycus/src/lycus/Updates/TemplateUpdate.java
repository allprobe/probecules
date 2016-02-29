package lycus.Updates;

import java.util.UUID;

import Model.UpdateModel;
import lycus.Host;

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
		
		// TODO: Check if remove for host or remove for user
		for (Host host : getUser().getHosts().values())
		{
			host.removeRunnableProbes(UUID.fromString(getUpdate().object_id));
		}
		
		return true;
	}
}
