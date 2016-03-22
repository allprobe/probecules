package lycus.Updates;

import java.util.UUID;

import lycus.Model.UpdateModel;
import lycus.Host;
import lycus.RunnableProbeContainer;
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
	
	// object_id = templateId
	@Override
	public Boolean Delete()
	{
		super.Delete();
		RunnableProbeContainer.getInstanece().removeByTemplateId(getUpdate().object_id);

		return true;
	}
}
