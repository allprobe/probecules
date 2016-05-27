/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Probes;

import java.util.HashMap;
import java.util.UUID;
import GlobalConstants.Constants;
import Model.UpdateModel;
import Model.UpdateValueModel;
import Results.BaseResult;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.Host;
import lycus.RunnableProbeContainer;
import lycus.Trigger;
import lycus.User;

/**
 *
 * @author Roi
 */
public class BaseProbe  {
	private User user;				
    private String probe_id;
    private UUID template_id;
	private String name;
	private long interval;
	private float multiplier;
    private boolean isActive;
    private HashMap<String,Trigger> triggers;

    public BaseProbe() {
    }

    public BaseProbe(User user,String probe_id,UUID template_id,String name, long interval,float multiplier,boolean status) {
        this.setUser(user);
    	this.setProbe_id(probe_id);
        this.setName(name);
		this.setInterval(interval);
		this.setMultiplier(multiplier);
		this.setActive(status);
		this.setTemplate_id(template_id);
		this.setTriggers(new HashMap<String,Trigger>());
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
     * @param probe_id the probe_id to set
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

	public long getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(long interval) {
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
	
	public HashMap<String,Trigger> getTriggers() {
		return triggers;
	}

	public Trigger getTrigger(String triggerId) {
		return triggers.get(triggerId);
	}
	
	public void setTriggers(HashMap<String,Trigger> triggers) {
		this.triggers = triggers;
	}

	public void addTrigger(Trigger trigger)
	{
		this.getTriggers().put(trigger.getTriggerId(),trigger);
	}
	
	public void removeTrigger(UUID trigger_id)
	{
		this.getTriggers().remove(trigger_id);
	}
	
	public String getProbeKey()
	{
		return this.getTemplate_id().toString()+"@"+this.getProbe_id();
	}
	
	public BaseResult getResult(Host h)
    {
    	return null;
    }
	
	@Override
    public String toString() {
        StringBuilder s = new StringBuilder("Probe "+this.getProbe_id()+":");
        s.append("Name:").append(this.getName()).append("; ");
        s.append("Interval:").append(this.getInterval()).append("; ");
        s.append("Multiplier:").append(this.getMultiplier()).append("; ");
        s.append("Active:").append(this.isActive()).append("; ");

        return s.toString();
    }
	
	public boolean updateKeyValues(UpdateModel updateModel)
	{
		UpdateValueModel updateValue = updateModel.update_value;
		if (updateValue.status != null && isActive() != updateValue.status.equals(Constants._true))
		{
			boolean isActive = updateValue.status.equals(Constants._true);
			setActive(isActive);
			RunnableProbeContainer.getInstanece().pause(Utils.GeneralFunctions.getRunnableProbeId(updateModel.template_id, updateModel.host_id, updateModel.probe_id), isActive);
			Logit.LogCheck("Is active for " + getName() +  " Is " + isActive);
		}
		if (GeneralFunctions.isChanged(getMultiplier(), updateValue.multiplier))
		{
			setMultiplier(updateValue.multiplier);
			Logit.LogCheck("Multiplier " + getName() +  " has changed to " + multiplier);
		}
		if (GeneralFunctions.isChanged(getName(), updateValue.name))
		{
			setName(updateValue.name);
			Logit.LogCheck("Name " + getProbe_id() +  " has changed to " + name);
		}			
		
		return true;
	}
}
