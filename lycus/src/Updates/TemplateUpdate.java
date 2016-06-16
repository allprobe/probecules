package Updates;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import GlobalConstants.Constants;
import Model.UpdateModel;
import Utils.Logit;
import lycus.Host;
import lycus.RunnableProbe;
import lycus.RunnableProbeContainer;
import lycus.UsersManager;

public class TemplateUpdate  extends BaseUpdate{

	public TemplateUpdate(UpdateModel update) {
		super(update);
		// TODO TemplateUpdate()
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
		String templateId = getUpdate().template_id;
		Set<UUID> hosts = UsersManager.getUser(getUpdate().user_id).getHosts().keySet();
		
		for (UUID hostId : hosts){
			ConcurrentHashMap<String, RunnableProbe> runnableProbes = RunnableProbeContainer.getInstanece().getByHostTemplate(templateId, hostId.toString());
			if (runnableProbes == null)
					return true;
			for (RunnableProbe runnableProbe : runnableProbes.values()) {
				if (getUpdate().update_value.status != null && runnableProbe.getProbe().isActive() != getUpdate().update_value.status.equals(Constants._true))
				{
					boolean isActive = getUpdate().update_value.status.equals(Constants._true);
					runnableProbe.getProbe().setActive(isActive);
					Logit.LogCheck("Is active for " + runnableProbe.getProbe().getName() +  " Is " + isActive);
				}
			} 
		}
		
		return true;
	}
	
	// object_id = templateId
	@Override
	public Boolean Delete()
	{
		super.Delete();
		RunnableProbeContainer.getInstanece().removeByTemplateId(getUpdate().template_id);
		getUser().removeTemplateProbe(getUpdate().template_id);
		Logit.LogCheck("Template " + getUpdate().object_id +  " was removed");
		return true;
	}
}
