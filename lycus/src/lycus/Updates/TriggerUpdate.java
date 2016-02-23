package lycus.Updates;

import java.util.UUID;

import Model.UpdateModel;
import lycus.Trigger;
import lycus.User;
import lycus.UsersManager;
import lycus.Probes.Probe;

public class TriggerUpdate extends BaseUpdate {

	public TriggerUpdate(UpdateModel update) {
		super(update);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Boolean New()
	{
		super.New();
		User user = UsersManager.getUser(UUID.fromString(update.user_id));
		Probe probe = user.getProbeFor(update.probe_id);
//		Trigger trigger = new Trigger(update., update.update_value.key.name, probe, svrty, update.update_value.status, update.update_value.type, unit, condtions)
		
//		probe.addTrigger(trigger);
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
