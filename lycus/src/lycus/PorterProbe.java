/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.util.ArrayList;
import java.util.UUID;

/**
 * 
 * @author Roi
 */
public class PorterProbe extends Probe {

	private String type;
	private int port;
	private int sendType;
	private String sendString;
	private String receiveString;
	private int timeout;

	PorterProbe() {
	}

	PorterProbe(User user,String probe_id,UUID template_id,String name,long interval,float multiplier,boolean status,int timeout, String type, int port, String sendString, String acceptString) {
		super(user, probe_id,template_id,name,interval,multiplier,status);
		this.type = type;
		this.port = port;
		this.timeout=timeout;
		this.sendString=sendString;
		this.receiveString=acceptString;
	}
	

	// Getters/Setters

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
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
    	this.setType(newType);
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
		switch(this.getType())
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
		s.append("Protocol:").append(this.getType()).append("; ");
		s.append("Port Number:").append(this.getPort()).append("; ");
		s.append("Sending Type:").append(this.getSendType()).append("; ");
		s.append("Send String:").append(this.getSendString()).append("; ");
		s.append("Receive String:").append(this.getReceiveString())
				.append("; ");
        s.append("Auth Password:").append(this.getTimeout()).append("; ");
		return s.toString();
	}

}
