package Probes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.snmp4j.smi.OID;

import GlobalConstants.Constants;
import GlobalConstants.Enums;
import GlobalConstants.SnmpDataType;
import GlobalConstants.SnmpUnit;
import GlobalConstants.Enums.HostType;
import GlobalConstants.Enums.SnmpStoreAs;
import NetConnection.NetResults;
import Results.BaseResult;
import Results.NicResult;
import Results.PingResult;
import Results.SnmpResult;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.Host;
import lycus.User;

public class NicProbe extends BaseProbe  {


	private static final String ifOutOctetsOID = "1.3.6.1.2.1.2.2.1.16.";
	private static final String ifInOctetsOID = "1.3.6.1.2.1.2.2.1.10.";
	private int index;
	private long ifSpeed;
	HostType hostType;

	public NicProbe(User user, String probe_id, UUID template_id, String name, long interval, float multiplier,
			boolean status,int index, long ifSpeed, HostType hostType) {
		super(user, probe_id, template_id, name, interval, multiplier, status);
		this.index=index;
		this.ifSpeed=ifSpeed;
		this.hostType=hostType;
	}

//	public NicProbe(User user,String probe_id, UUID template_id, String name, long interval, float multiplier,
//			boolean status, int index, long ifSpeed, Enums.HostType hostType) {
//		super(user, probe_id, template_id, name, interval, multiplier, status, index);
//		this.setIfSpeed(ifSpeed);
//		this.hostType = hostType;
//
//		this.ifInOctets = new SnmpProbe(user,probe_id+"@"+Constants.inBW, template_id, name, interval, multiplier, status,
//				new OID(this.getIfinoctetsOID()), SnmpDataType.Numeric, SnmpUnit.bytes,
//				SnmpStoreAs.deltaBytesPerSecond);
//		this.ifOutOctets = new SnmpProbe(user,probe_id+"@"+Constants.outBW, template_id, name, interval, multiplier, status,
//				new OID(this.getIfoutoctetsOID()), SnmpDataType.Numeric, SnmpUnit.bytes,
//				SnmpStoreAs.deltaBytesPerSecond);
//
//	}

	
	
//	@Override
//	public BaseResult getResult(Host host) {
//		super.getResult(host);
//		try {
//			List<String> listOids = new ArrayList<String>();
//			NicResult result = null;;
//			
//			if (host.isHostStatus() && host.isSnmpStatus()) {
//				listOids.add(this.getIfinoctetsOID());
//				listOids.add(this.getIfoutoctetsOID());
//			}
//			
//			Map<String, SnmpResult> response = null;
////			if (host.getSnmpTemp().getVersion() == 2) {
////				response = Net.Snmp2GETBULK(host.getHostIp(), host.getSnmpTemp().getPort(),
////						host.getSnmpTemp().getTimeout(), host.getSnmpTemp().getCommunityName(), listOids);
////			} 
////			else if (host.getSnmpTemp().getVersion() == 3) {
////				response = Net.Snmp3GETBULK(host.getHostIp(), host.getSnmpTemp().getPort(),
////						host.getSnmpTemp().getTimeout(), host.getSnmpTemp().getUserName(),
////						host.getSnmpTemp().getAuthPass(), host.getSnmpTemp().getAlgo(),
////						host.getSnmpTemp().getCryptPass(), host.getSnmpTemp().getCryptType(), listOids, 
////						GeneralFunctions.getRunnableProbeId(getTemplate_id(), host.getHostId(), getProbe_id()));
////			}
//			
//			if (response == null) {
//				
//				Logit.LogInfo("no response for nic element probe" + GeneralFunctions.getRunnableProbeId(getTemplate_id(), host.getHostId(), getProbe_id()));
//			} 
//			else {
//				long resultsTimestamp = System.currentTimeMillis();
//
//				SnmpResult ifInResults = response.get(this.getIfinoctetsOID());
//				SnmpResult ifOutResults = response.get(this.getIfoutoctetsOID());
//
//				if (ifInResults == null || ifOutResults == null) {
//					Logit.LogInfo("no response for nic element probe" + GeneralFunctions.getRunnableProbeId(getTemplate_id(), host.getHostId(), getProbe_id()));
//					return null;
//				}
//
//				
//				
////				long ifTotalTraffic = Long.parseLong(ifInResults) + Long.parseLong(ifOutResults);
////				results.add(ifTotalTraffic);
//				
//				
//				return result;
//			}
//		} catch (Throwable th) {
//			Logit.LogError("NicElement - Check","Error running discovery element probe:" +  GeneralFunctions.getRunnableProbeId(getTemplate_id(), host.getHostId(), getProbe_id()));
//		}
//		return null;
//	}

	@Override
	public BaseResult getResult(Host h) {
		super.getResult(h);
		if (!h.isHostStatus())
			return null;

		NicResult nicResult = NetResults.getInstanece().getNicResult(h, this);
		

		return nicResult;
	}

	public NicProbe() {
		super();
		this.setIndex(index);
		this.ifSpeed = ifSpeed;
		this.hostType = hostType;
	}
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}

	public OID getIfoutoctetsOID() {
		return new OID(NicProbe.ifOutOctetsOID + this.getIndex());
	}

	public OID getIfinoctetsOID() {
		return new OID(NicProbe.ifInOctetsOID + this.getIndex());
	}
	
	@Override
	public String getProbe_id() {
		return super.getProbe_id()+"@"+this.getName();
	}

	public long getIfSpeed() {
		return ifSpeed;
	}

	public void setIfSpeed(long ifSpeed) {
		this.ifSpeed = ifSpeed;
	}

	@Override
	public String toString() {
		JSONObject jsonObject=new JSONObject();
		jsonObject.put("probe_id", this.getProbe_id());
		jsonObject.put("if_name", this.getName());
		jsonObject.put("if_speed", this.getIfSpeed());
		jsonObject.put("if_index", this.getIndex());
		return jsonObject.toJSONString();
	}


}
