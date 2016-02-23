/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus.Probes;

import java.util.ArrayList;
import java.util.UUID;

import Model.KeyUpdateModel;
import lycus.Host;
import lycus.Net;
import lycus.User;

/**
 * 
 * @author Roi
 */
public class PorterProbe extends Probe {

	private String proto;
	private int port;
	private int sendType;
	private String sendString;
	private String receiveString;
	private int timeout;

	PorterProbe() {
	}

	public PorterProbe(User user,String probe_id,UUID template_id,String name,long interval,float multiplier,boolean status,int timeout, String type, int port, String sendString, String acceptString) {
		super(user, probe_id,template_id,name,interval,multiplier,status);
		this.proto = type;
		this.port = port;
		this.timeout=timeout;
		this.sendString=sendString;
		this.receiveString=acceptString;
	}
	
	public PorterProbe(User user,String probe_id,UUID template_id,String name,long interval,float multiplier,boolean status,int timeout, String type, int port) {
		super(user, probe_id,template_id,name,interval,multiplier,status);
		this.proto = type;
		this.port = port;
		this.timeout=timeout;
		this.sendString = "";
		this.receiveString = "";
	}
	

	// Getters/Setters

	/**
	 * @return the type
	 */
	public String getProto() {
		return proto;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setProto(String type) {
		this.proto = type;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	public String getReceiveString() {
		return receiveString;
	}

	public void setReceiveString(String acceptString) {
		this.receiveString = acceptString;
	}

	public String getSendString() {
		return sendString;
	}

	public void setSendString(String sendString) {
		this.sendString = sendString;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getSendType() {
		return sendType;
	}

	
	public void updateProbeAttributes(String probeNewName, long probeNewInterval, float probeNewMultiplier,
			boolean probeNewStatus,String newType,int newPort,int newTimeout,String newSendString,String newReceiveString)
    {
    	super.updateProbe(probeNewName, probeNewInterval, probeNewMultiplier, probeNewStatus);
    	this.setProto(newType);
    	this.setPort(newPort);
    	this.setSendString(newSendString);
    	this.setReceiveString(newReceiveString);
    	this.setTimeout(newTimeout);
    }
	
	@Override
    public ArrayList<Object> Check(Host h)
    {

		String rpStr = h.getHostId().toString();
		if (rpStr.contains(
				"b631bd96-e2e6-4163-940b-ff376d7d2138"))
			System.out.println("BREAKPOINT - PorterProbe");
		ArrayList<Object> results=null;
		switch(this.getProto())
		{
		case "TCP":results=Net.TcpPorter(h.getHostIp(), this.getPort(), this.getTimeout());
    			   break;
		case "UDP":results=Net.UdpPorter(h.getHostIp(), this.getPort(), this.getTimeout(), this.getSendString(), this.getReceiveString());
				   break;
		}
		return results;
    }

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder(super.toString());
		s.append("Protocol:").append(this.getProto()).append("; ");
		s.append("Port Number:").append(this.getPort()).append("; ");
		s.append("Sending Type:").append(this.getSendType()).append("; ");
		s.append("Send String:").append(this.getSendString()).append("; ");
		s.append("Receive String:").append(this.getReceiveString())
				.append("; ");
        s.append("Auth Password:").append(this.getTimeout()).append("; ");
		return s.toString();
	}
	
	 public boolean updateKeyValues(KeyUpdateModel key)
		{
			super.updateKeyValues(key);
			this.setPort(key.port);
			this.setProto(key.proto);
			this.setTimeout(key.timeout);
			return true;
		}
}
