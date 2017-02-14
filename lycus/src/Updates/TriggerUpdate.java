package Updates;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import GlobalConstants.Constants;
import Model.UpdateModel;
import Probes.BaseProbe;
import Utils.GeneralFunctions;
import Utils.Logit;
import Triggers.Trigger;
import Triggers.TriggerCondition;
import lycus.ResultsContainer;
import lycus.RunnableProbe;
import lycus.RunnableProbeContainer;
import lycus.UsersManager;

public class TriggerUpdate extends BaseUpdate {

	public TriggerUpdate(UpdateModel update) {
		super(update);
		// TODO TriggerUpdate()
	}

	@Override
	public Boolean New() {
		super.New();
		BaseProbe probe = getUser().getProbeFor(getUpdate().probe_id);
		ArrayList<TriggerCondition> conditions = UsersManager
				.getTriggerConds(getUpdate().update_value.triggers[0].conditions);

		// From SsmpUnit swap integer and string to none - Roi
		Trigger trigger = new Trigger(getUpdate().update_value.triggers[0].id,
				getUpdate().update_value.triggers[0].name, probe,
				UsersManager.getTriggerSev(getUpdate().update_value.triggers[0].severity),
				getUpdate().update_value.triggers[0].status.equals(Constants._true), conditions);

		probe.addTrigger(trigger);
		Logit.LogCheck("Trigger: " + getUpdate().object_id + " was added");
		return true;
	}

	@Override
	public Boolean Update() {
		super.Update();
		BaseProbe probe = getUser().getProbeFor(getUpdate().probe_id);
		Trigger trigger = probe.getTriggers().get(getUpdate().update_value.triggers[0].id);
		ArrayList<TriggerCondition> conditions = UsersManager
				.getTriggerConds(getUpdate().update_value.triggers[0].conditions);

		if (conditions != null && !conditions.isEmpty()) {
			trigger.setCondtions(conditions);
			Logit.LogCheck("Conditions for trigger : " + getUpdate().update_value.triggers[0].id);
		}

		if (GeneralFunctions.isChanged(trigger.getName(), getUpdate().update_value.triggers[0].name)) {
			trigger.setName(getUpdate().update_value.triggers[0].name);
			ResultsContainer.getInstance().resendEvents(trigger.getTriggerId(), Constants.object_changed,
					trigger.getName(), null);
			Logit.LogCheck("Name for trigger " + getUpdate().update_value.triggers[0].id + " has changed to "
					+ getUpdate().update_value.triggers[0].name);
		}

		trigger.setProbe(probe);
		if (getUpdate().update_value.status != null
				&& trigger.getStatus() != getUpdate().update_value.triggers[0].status.equals(Constants._true)) {
			trigger.setStatus(getUpdate().update_value.triggers[0].status.equals(Constants._true));
			Logit.LogCheck("Status for trigger " + getUpdate().update_value.triggers[0].id + " has changed to "
					+ getUpdate().update_value.triggers[0].status);
		}

		if (GeneralFunctions.isChanged(trigger.getSvrty().toString().toLowerCase(),
				getUpdate().update_value.triggers[0].severity)) {
			trigger.setSvrty(UsersManager.getTriggerSev(getUpdate().update_value.triggers[0].severity));
			ResultsContainer.getInstance().resendEvents(trigger.getTriggerId(), Constants.object_changed, null,
					trigger.getSvrty().toString());
			Logit.LogCheck("Severity for trigger " + getUpdate().update_value.triggers[0].id + " has changed to "
					+ getUpdate().update_value.triggers[0].severity);
		}

		Logit.LogCheck("Trigger: " + getUpdate().object_id + " was updated");
		return true;
	}

	@Override
	public Boolean Delete() {
		super.Delete();
		BaseProbe probe = null;
		if (!GeneralFunctions.isNullOrEmpty(getUpdate().probe_id)
				&& !GeneralFunctions.isNullOrEmpty(getUpdate().object_id)) {
			probe = getUser().getProbeFor(getUpdate().probe_id);
			ConcurrentHashMap<String, RunnableProbe> runnbaleProbes = RunnableProbeContainer.getInstanece()
					.getByProbe(getUpdate().probe_id);
			ResultsContainer.getInstance().resendEvents(getUpdate().object_id, Constants.object_removed, null, null);
			for (RunnableProbe runnbleProbe : runnbaleProbes.values())
				runnbleProbe.removeEvents(getUpdate().object_id, false);

			probe.removeTrigger(getUpdate().object_id);

			Logit.LogCheck("Trigger: " + getUpdate().object_id + " was removed");
			return true;
		}
		return false;
	}
}
