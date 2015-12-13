package lycus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;


public class SnmpProbe extends Probe {
private OID oid;
private SnmpDataType dataType;
private SnmpUnit unit;
private int storeAs; //0 - as is, 1 - bits, 2 - diff

public SnmpProbe(User user,String probe_id,UUID template_id,String name,long interval,float multiplier,boolean status,OID oid,SnmpDataType dataType,SnmpUnit unit,int storeAs) {
	super(user,probe_id,template_id,name,interval,multiplier,status);
	this.setOid(oid);
	this.setDataType(dataType);
	this.setStoreAs(storeAs);
	this.setUnit(unit);
	
}
//Getters/Setters
public OID getOid() {
	return oid;
}
public void setOid(OID oid) {
	this.oid = oid;
}
public SnmpDataType getDataType() {
	return dataType;
}
public void setDataType(SnmpDataType dataType) {
	this.dataType = dataType;
}
public int getStoreAs() {
	return storeAs;
}
public void setStoreAs(int storeAs) {
	this.storeAs = storeAs;
}
public SnmpUnit getUnit() {
	return unit;
}
public void setUnit(SnmpUnit unit) {
	this.unit = unit;
}

public void updateProbeAttributes(String probeNewName, long probeNewInterval, float probeNewMultiplier,
		boolean probeNewStatus,OID newOID,int newStoreAs,SnmpDataType newDataType)
{
	super.updateProbe(probeNewName, probeNewInterval, probeNewMultiplier, probeNewStatus);
	this.setOid(newOID);
	this.setStoreAs(newStoreAs);
	this.setDataType(newDataType);
}

@Override
public ArrayList<Object> Check(Host h) //Only V1
{
	ArrayList<Object> results=Net.runSnmpCheckVer1(h.getHostIp(), h.getSnmpTemp().getPort(), h.getSnmpTemp().getCommunityName(), this.getOid().toString(), h.getSnmpTemp().getTimeout());
	return results;
}

@Override
public String toString() {
	StringBuilder s = new StringBuilder(super.toString());
	s.append("OID:").append(this.getOid().toString()).append("; ");
	return s.toString();
}

}
