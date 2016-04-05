package Updates;

import java.util.UUID;

import Model.UpdateModel;
import Utils.Logit;
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
		Logit.LogCheck("Template " + getUpdate().object_id +  " was removed");
		return true;
	}
}
