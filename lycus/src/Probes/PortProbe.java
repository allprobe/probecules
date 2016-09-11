/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Probes;

import java.util.UUID;

import Model.UpdateModel;
import Model.UpdateValueModel;
import lycus.Host;
import NetConnection.NetResults;
import Results.PortResult;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.User;

/**
 * 
 * @author Roi
 */
public class PortProbe extends BaseProbe {

	private String proto;
	private int port;
	private int sendType;
	private String sendString;
	private String receiveString;
	private int timeout;

	PortProbe() {
	}

	public PortProbe(User user, String probe_id, UUID template_id, String name, int interval, float multiplier,
			boolean status, int timeout, String type, int port, String sendString, String acceptString) {
		super(user, probe_id, template_id, name, interval, multiplier, status);
		this.proto = type;
		this.port = port;
		this.timeout = timeout;
		this.sendString = sendString;
		this.receiveString = acceptString;
	}

	public PortProbe(User user, String probe_id, UUID template_id, String name, int interval, float multiplier,
			boolean status, int timeout, String type, int port) {
		super(user, probe_id, template_id, name, interval, multiplier, status);
		this.proto = type;
		this.port = port;
		this.timeout = timeout;
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
			boolean probeNewStatus, String newType, int newPort, int newTimeout, String newSendString,
			String newReceiveString) {
		this.setProto(newType);
		this.setPort(newPort);
		this.setSendString(newSendString);
		this.setReceiveString(newReceiveString);
		this.setTimeout(newTimeout);
	}

	@Override
	public PortResult getResult(Host h) {
		if (!h.isHostStatus())
			return null;

		String rpStr = h.getHostId().toString()+"@"+this.getProbe_id();
		if (rpStr.contains("9dc99972-e28a-4e90-aabd-7e8bad61b232@inner_657259e4-b70b-47d2-9e4a-3db904a367e1"))
			Logit.LogDebug("BREAKPOINT - PorterProbe");
		
		PortResult results = NetResults.getInstanece().getPortResult(h,this);
		return results;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder(super.toString());
		s.append("Protocol:").append(this.getProto()).append("; ");
		s.append("Port Number:").append(this.getPort()).append("; ");
		s.append("Sending Type:").append(this.getSendType()).append("; ");
		s.append("Send String:").append(this.getSendString()).append("; ");
		s.append("Receive String:").append(this.getReceiveString()).append("; ");
		s.append("Auth Password:").append(this.getTimeout()).append("; ");
		return s.toString();
	}

	public boolean updateKeyValues(UpdateModel updateModel) {
		super.updateKeyValues(updateModel);
		UpdateValueModel updateValue = updateModel.update_value;
//		if (updateValue.key.port_extra != null && getExtraValue != updateValue.key.port_extra )
//		{
//			this.setPort(updateValue.key.port);
//			Logit.LogCheck("Port for " + getName() +  " has changed to " + updateValue.key.port);
//		}
		if (updateValue.key.port != null && getPort() != updateValue.key.port )
		{
			this.setPort(updateValue.key.port);
			Logit.LogCheck("Port for " + getName() +  " has changed to " + updateValue.key.port);
		}
			
		if (!GeneralFunctions.isNullOrEmpty(updateValue.key.proto) && !getProto().equals(updateValue.key.proto))
		{
			this.setProto(updateValue.key.proto);
			Logit.LogCheck("Proto for " + getName() +  " has changed to " + updateValue.key.proto);
		}
			
		if (updateValue.key.timeout != null && getTimeout() != updateValue.key.timeout)
		{
			this.setTimeout(updateValue.key.timeout);
			Logit.LogCheck("Timeout for " + getName() +  " has changed to " + updateValue.key.timeout);
		}
		
		return true;
	}
}
