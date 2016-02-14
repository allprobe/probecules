/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus.Probes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.commons.collections4.queue.*;
import org.snmp4j.smi.OID;

import Model.KeyUpdateModel;
import lycus.Host;
import lycus.Trigger;
import lycus.User;
import lycus.UsersManager;
import lycus.Enums.SnmpStoreAs;

/**
 *
 * @author Roi
 */
public class Probe  {
	private User user;
    private String probe_id;
    private UUID template_id;
	private String name;
	private long interval;
	private float multiplier;
    private boolean isActive;
    private HashMap<String,Trigger> triggers;

    public Probe() {
    }

    public Probe(User user,String probe_id,UUID template_id,String name, long interval,float multiplier,boolean status) {
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

	public void setTriggers(HashMap<String,Trigger> triggers) {
		this.triggers = triggers;
	}

	public void addTrigger(Trigger trigger)
	{
		this.getTriggers().put(trigger.getTriggerId(),trigger);
	}
	public String getProbeKey()
	{
		return this.getTemplate_id().toString()+"@"+this.getProbe_id();
	}
    
	protected void updateProbe(String probeNewName, long probeNewInterval, float probeNewMultiplier,
			boolean probeNewStatus)
	{
		this.setName(probeNewName);
		this.setInterval(probeNewInterval);
		this.setMultiplier(probeNewMultiplier);
		this.setActive(probeNewStatus);
	}
	
	public ArrayList<Object> Check(Host h)
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
	public boolean updateKeyValues(KeyUpdateModel key)
	{
		return true;
	}
	
}
