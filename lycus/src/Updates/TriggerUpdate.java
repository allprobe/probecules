package Updates;

import java.util.ArrayList;
import java.util.UUID;

import GlobalConstants.Constants;
import GlobalConstants.SnmpUnit;
import Model.UpdateModel;
import Probes.BaseProbe;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.Trigger;
import lycus.TriggerCondition;
import lycus.UsersManager;

public class TriggerUpdate extends BaseUpdate {

	public TriggerUpdate(UpdateModel update) {
		super(update);
		// TODO TriggerUpdate()
	}
	
	@Override
	public Boolean New()
	{
		super.New();
		BaseProbe probe = getUser().getProbeFor(getUpdate().probe_id);
		ArrayList<TriggerCondition>  conditions = UsersManager.getTriggerConds(getUpdate().update_value.conditions);
		
		// From SsmpUnit swap integer and string to none - Roi
		Trigger trigger = new Trigger(getUpdate().update_value.key.trigger_id, getUpdate().update_value.name, 
				probe, UsersManager.getTriggerSev(getUpdate().update_value.severity), getUpdate().update_value.status.equals(Constants._true),
				getUpdate().update_value.type, SnmpUnit.valueOf(getUpdate().update_value.xvalue_unit), conditions); 
		
		probe.addTrigger(trigger);
		return true;
	}
	
	@Override
	public Boolean Update()
	{
		super.Update();
		BaseProbe probe = getUser().getProbeFor(getUpdate().probe_id);
		Trigger trigger = probe.getTriggers().get(getUpdate().object_id);
		ArrayList<TriggerCondition>  conditions = UsersManager.getTriggerConds(getUpdate().update_value.conditions);
		
		if (conditions != null && !conditions.isEmpty())
		{
			trigger.setCondtions(conditions);
			Logit.LogCheck("Conditions for trigger : " + getUpdate().object_id + " has updated even when not changed");
		}
		if (GeneralFunctions.isChanged(trigger.getElementType(), getUpdate().update_value.type))
		{
			trigger.setElementType(getUpdate().update_value.type);
			Logit.LogCheck("Type for trigger " + getUpdate().object_id +  " has changed to " + getUpdate().update_value.type);
		}
		if (GeneralFunctions.isChanged(trigger.getName(), getUpdate().update_value.name))
		{
			trigger.setName(getUpdate().update_value.name);
			Logit.LogCheck("Name for trigger " + getUpdate().object_id +  " has changed to " + getUpdate().update_value.name);
		}
			
		trigger.setProbe(probe);      // Ran - Is it possible to change probe?
		if (getUpdate().update_value.status != null && trigger.getStatus() != getUpdate().update_value.status.equals(Constants._true))
		{
			trigger.setStatus(getUpdate().update_value.status.equals(Constants._true));
			Logit.LogCheck("Status for trigger " + getUpdate().object_id +  " has changed to " + getUpdate().update_value.status);
		}
		if (GeneralFunctions.isChanged(trigger.getName(), getUpdate().update_value.severity))
		{
			trigger.setSvrty(UsersManager.getTriggerSev(getUpdate().update_value.severity));
			Logit.LogCheck("Severity for trigger " + getUpdate().object_id +  " has changed to " + getUpdate().update_value.severity);
		}
		if (GeneralFunctions.isChanged(trigger.getUnit().toString(), getUpdate().update_value.xvalue_unit))
		{
			trigger.setUnit(SnmpUnit.valueOf(getUpdate().update_value.xvalue_unit));
			Logit.LogCheck("X value unit for trigger " + getUpdate().object_id +  " has changed to " + getUpdate().update_value.xvalue_unit);
		}
			
		
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
		BaseProbe probe = null;
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().probe_id) && 
			!GeneralFunctions.isNullOrEmpty(getUpdate().object_id))
		{
			probe = getUser().getProbeFor(getUpdate().probe_id);
			probe.removeTrigger(UUID.fromString(getUpdate().object_id));
		
//			Trigger trigger = probe.getTriggers().get(getUpdate().object_id);
			Logit.LogCheck("Trigger: " + getUpdate().object_id + " was removed");
			return true;
		}
		return false;
	}
}