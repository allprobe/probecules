/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus.Probes;

import java.util.ArrayList;
import java.util.UUID;

import Model.KeyUpdateModel;
import lycus.Host;
import lycus.Log;
import lycus.LogType;
import lycus.Net;
import lycus.SysLogger;
import lycus.User;

/**
 *
 * @author Roi
 */
public class PingerProbe extends Probe {

    private int count;
    private int bytes;
    private int timeout;

    PingerProbe() {
    }

    public PingerProbe(User user,String probe_id,UUID template_id,String name,long interval,float multiplier,boolean status,int timeout, int count, int bytes) {
        super(user,probe_id,template_id,name,interval,multiplier,status);
        this.count = count;
        this.bytes = bytes;
        this.timeout=timeout;
    }
  // Getters/Setters
    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }

    public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
     * @return the bytes
     */
    public int getBytes() {
        return bytes;
    }

    /**
     * @param bytes the bytes to set
     */
    public void setBytes(int bytes) {
        this.bytes = bytes;
    }
    
    public void updateProbeAttributes(String probeNewName, long probeNewInterval, float probeNewMultiplier,
			boolean probeNewStatus,int newCount,int newBytes,int newTimeout)
    {
    	super.updateProbe(probeNewName, probeNewInterval, probeNewMultiplier, probeNewStatus);
    	this.setCount(newCount);
    	this.setBytes(newBytes);
    	this.setTimeout(newTimeout);
    }
    
    @Override
    public ArrayList<Object> Check(Host h)
    {
	
		ArrayList<Object> results=null;
		try{
    	results=Net.Pinger(h.getHostIp(), this.getCount(), this.getBytes(), this.getTimeout());
		}
		catch(Throwable th)
		{
			SysLogger.Record(new Log("Faild to run runnable probe check for: "+h.getHostId().toString()+"@"+this.getProbe_id(),LogType.Error));
		}
		
				
    	return results;
    }
    
    @Override
    public String toString()
    {
    StringBuilder s=new StringBuilder(super.toString());
    s.append("Num Of Pings:").append(this.getCount()).append("; ");
    s.append("Num Of Bytes:").append(this.getBytes()).append("; ");
    s.append("Timeout:").append(this.getTimeout()).append("; ");
    return s.toString();
    }
    
    public boolean updateKeyValues(KeyUpdateModel key)
	{
		super.updateKeyValues(key);
		this.setCount(key.npings);
		this.setBytes(key.bytes);
		this.setTimeout(key.timeout);
		return true;
	}
}