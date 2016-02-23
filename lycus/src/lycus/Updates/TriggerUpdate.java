package lycus.Updates;

import java.util.ArrayList;
import java.util.UUID;

import Model.UpdateModel;
import lycus.Trigger;
import lycus.TriggerCondition;
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
		
//		ArrayList<TriggerCondition>  conditions = new ArrayList<TriggerCondition>();
//		UsersManager.getTriggerConds();
		
//		Trigger trigger = new Trigger(update.update_value.key.trigger_id, name, 
//				probe, update.update_value.key.trigger_severity, update.update_value.status,
//				update.update_value.type, update.update_value.key.value_unit, conditions);
		
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
