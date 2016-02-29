package lycus.Updates;

import java.util.ArrayList;
import java.util.UUID;

import GlobalConstants.TriggerSeverity;
import Model.UpdateModel;
import lycus.GeneralFunctions;
import lycus.RunnableProbe;
import lycus.SnmpUnit;
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
		Probe probe = getUser().getProbeFor(getUpdate().probe_id);
		ArrayList<TriggerCondition>  conditions = UsersManager.getTriggerConds(getUpdate().update_value.conditions);
		
		// From SsmpUnit swap integer and string to none - Roi
		Trigger trigger = new Trigger(getUpdate().update_value.key.trigger_id, getUpdate().update_value.name, 
				probe, UsersManager.getTriggerSev(getUpdate().update_value.severity), getUpdate().update_value.status,
				getUpdate().update_value.type, SnmpUnit.valueOf(getUpdate().update_value.xvalue_unit), conditions); 
		
		probe.addTrigger(trigger);
		return true;
	}
	
	@Override
	public Boolean Update()
	{
		super.Update();
		Probe probe = getUser().getProbeFor(getUpdate().probe_id);
		Trigger trigger = probe.getTriggers().get(getUpdate().object_id);
		ArrayList<TriggerCondition>  conditions = UsersManager.getTriggerConds(getUpdate().update_value.conditions);
		
		if (conditions != null && !conditions.isEmpty())
			trigger.setCondtions(conditions);
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.type))
			trigger.setElementType(getUpdate().update_value.type);
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.name))
			trigger.setName(getUpdate().update_value.name);
		
		trigger.setProbe(probe);      // Ran - Is it possible to change probe?
		
		if (getUpdate().update_value.status != null)
			trigger.setStatus(getUpdate().update_value.status);
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.severity))
			trigger.setSvrty(UsersManager.getTriggerSev(getUpdate().update_value.severity));
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().update_value.xvalue_unit))
			trigger.setUnit(SnmpUnit.valueOf(getUpdate().update_value.xvalue_unit));
		
//		trigger.setTriggered(isTriggered);    // What is it - Roi
		
//		for (RunnableProbe runnableProbe : getUser().getHost(UUID.fromString(getUpdate().host_id)).getRunnableProbes().values())
//		{
//			runnableProbe.getProbe().gett
//			
//		}
		
		return true;
	}
	
	@Override
	public Boolean Delete()
	{
		super.Delete();
		Probe probe = null;
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().probe_id) && 
			!GeneralFunctions.isNullOrEmpty(getUpdate().object_id))
		{
			probe = getUser().getProbeFor(getUpdate().probe_id);
			probe.removeTrigger(UUID.fromString(getUpdate().object_id));
		
//			Trigger trigger = probe.getTriggers().get(getUpdate().object_id);
		
			return true;
		}
		return false;
	}
}
