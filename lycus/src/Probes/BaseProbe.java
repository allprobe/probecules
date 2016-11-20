/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Probes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import GlobalConstants.Constants;
import Model.ConditionModel;
import Model.TriggerModel;
import Model.UpdateModel;
import Model.UpdateValueModel;
import Results.BaseResult;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.Host;
import Triggers.Trigger;
import Triggers.TriggerCondition;
import lycus.User;
import lycus.UsersManager;

/**
 *
 * @author Roi
 */
public class BaseProbe {
	private User user;
	private String probe_id;
	private UUID template_id;
	private String name;
	private int interval;
	private float multiplier;
	private boolean isActive;
	private HashMap<String, Trigger> triggers;

	public BaseProbe() {
	}

	public BaseProbe(User user, String probe_id, UUID template_id, String name, int interval, float multiplier,
			boolean status) {
		this.setUser(user);
		this.setProbe_id(probe_id);
		this.setName(name);
		this.setInterval(interval);
		this.setMultiplier(multiplier);
		this.setActive(status);
		this.setTemplate_id(template_id);
		this.setTriggers(new HashMap<String, Trigger>());
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	// Getters/Setters
	/**
	 * @return the probe_id
	 */

	public String getProbe_id() {
		return probe_id;
	}

	/**
	 * @param probe_id
	 *            the probe_id to set
	 */
	public void setProbe_id(String probe_id) {
		this.probe_id = probe_id;
	}

	public UUID getTemplate_id() {
		return template_id;
	}

	public void setTemplate_id(UUID template_id) {
		this.template_id = template_id;
	}

	public int getInterval() {
		return interval;
	}

	/**
	 * @param interval
	 *            the interval to set
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public float getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(float multiplier) {
		this.multiplier = multiplier;
	}

	public HashMap<String, Trigger> getTriggers() {
		return triggers;
	}

	public Trigger getTrigger(String triggerId) {
		return triggers.get(triggerId);
	}

	public void setTriggers(HashMap<String, Trigger> triggers) {
		this.triggers = triggers;
	}

	public void addTrigger(Trigger trigger) {
		this.getTriggers().put(trigger.getTriggerId(), trigger);
	}

	public void removeTrigger(String trigger_id) {
		// Todo: Oren, remove all live events as well
		this.getTriggers().remove(trigger_id);
	}

	public Boolean clearTriggers() {
		this.getTriggers().clear();
		return true;
	}

	public String getProbeKey() {
		return this.getTemplate_id().toString() + "@" + this.getProbe_id();
	}

	public BaseResult getResult(Host h) {
		return null;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("Probe " + this.getProbe_id() + ":");
		s.append("Name:").append(this.getName()).append("; ");
		s.append("Interval:").append(this.getInterval()).append("; ");
		s.append("Multiplier:").append(this.getMultiplier()).append("; ");
		s.append("Active:").append(this.isActive()).append("; ");

		return s.toString();
	}

	public boolean updateKeyValues(UpdateModel updateModel) {
		UpdateValueModel updateValue = updateModel.update_value;
		if (updateValue.status != null && isActive() != updateValue.status.equals(Constants._true)) {
			boolean isActive = updateValue.status.equals(Constants._true);
			setActive(isActive);
			Logit.LogCheck("Is active for " + getName() + " Is " + isActive + "Update_id: " + updateModel.update_id
					+ ", probe_id: " + updateModel.probe_id);
		}
		if (GeneralFunctions.isChanged(getMultiplier(), updateValue.multiplier)) {
			setMultiplier(updateValue.multiplier);
			Logit.LogCheck("Multiplier " + getName() + " has changed to " + multiplier);
		}
		if (GeneralFunctions.isChanged(getName(), updateValue.name)) {
			setName(updateValue.name);
			Logit.LogCheck("Name " + getProbe_id() + " has changed to " + name);
		}
		if (GeneralFunctions.isChanged(getInterval(), updateValue.interval)) {
			setInterval(updateValue.interval);
			Logit.LogCheck("Interval " + getProbe_id() + " has changed to " + interval);
		}

		return true;
	}

	public Boolean addTriggers(TriggerModel[] triggers) {
		if (triggers != null && triggers.length > 0) {
			for (TriggerModel triggerModel : triggers) {
				ArrayList<TriggerCondition> condtions = new ArrayList<>();
				for (ConditionModel ConditionModel : triggerModel.conditions) {
					TriggerCondition condition = new TriggerCondition(ConditionModel.condition, ConditionModel.xvalue,
							ConditionModel.function, ConditionModel.results_vector_type, ConditionModel.xvalue_unit,
							ConditionModel.nvalue, ConditionModel.last_type);
					condtions.add(condition);
				}

				Trigger trigger = new Trigger(triggerModel.id, triggerModel.name, this,
						UsersManager.getTriggerSev(triggerModel.severity), triggerModel.status.equals(Constants._true),
						condtions);

				addTrigger(trigger);
			}
		}

		return true;
	}
}
