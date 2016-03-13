package lycus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.snmp4j.smi.OID;

import GlobalConstants.Constants;
import GlobalConstants.Enums;
import GlobalConstants.LogType;
import GlobalConstants.SnmpDataType;
import Utils.Logit;
import GlobalConstants.Enums.HostType;
import GlobalConstants.Enums.SnmpStoreAs;
import lycus.Probes.SnmpProbe;

public class NicElement extends BaseElement {

	private static final String ifOutOctetsOID = "1.3.6.1.2.1.2.2.1.16.";
	private static final String ifInOctetsOID = "1.3.6.1.2.1.2.2.1.10.";
	private SnmpProbe ifOutOctets;
	private SnmpProbe ifInOctets;

	private long ifSpeed;
	HostType hostType;

	public NicElement(String probe_id, UUID template_id, String name, long interval, float multiplier,
			boolean status, int index, long ifSpeed, Enums.HostType hostType) {
		super(null, probe_id, template_id, name, interval, multiplier, status, index);
		this.setIfSpeed(ifSpeed);
		this.hostType = hostType;

		this.ifInOctets = new SnmpProbe(probe_id+"@"+Constants.inBW, template_id, name, interval, multiplier, status,
				new OID(this.getIfinoctetsOID()), SnmpDataType.Numeric, SnmpUnit.bytes,
				SnmpStoreAs.deltaBytesPerSecond);
		this.ifOutOctets = new SnmpProbe(probe_id+"@"+Constants.outBW, template_id, name, interval, multiplier, status,
				new OID(this.getIfoutoctetsOID()), SnmpDataType.Numeric, SnmpUnit.bytes,
				SnmpStoreAs.deltaBytesPerSecond);

	}

	@Override
	public ArrayList<Object> Check(Host h) {
		super.Check(h);
		try {
			Host host = h;
			List<String> listOids = new ArrayList<String>();
			;
			if (h.isHostStatus() && h.isSnmpStatus()) {
				listOids.add(this.getIfinoctetsOID());
				listOids.add(this.getIfoutoctetsOID());

			}
			Map<String, String> response = null;
			if (host.getSnmpTemp().getVersion() == 2) {
				response = Net.Snmp2GETBULK(host.getHostIp(), host.getSnmpTemp().getPort(),
						host.getSnmpTemp().getTimeout(), host.getSnmpTemp().getCommunityName(), listOids);
			} else if (host.getSnmpTemp().getVersion() == 3) {
				response = Net.Snmp3GETBULK(host.getHostIp(), host.getSnmpTemp().getPort(),
						host.getSnmpTemp().getTimeout(), host.getSnmpTemp().getUserName(),
						host.getSnmpTemp().getAuthPass(), host.getSnmpTemp().getAlgo(),
						host.getSnmpTemp().getCryptPass(), host.getSnmpTemp().getCryptType(), listOids);
			}
			if (response == null) {
				Logit.LogInfo("no response for nic element probe" + this.getTemplate_id().toString() + "@"
						+ h.getHostId().toString() + "@" + this.getProbe_id());
				return null;
			} else {
				long resultsTimestamp = System.currentTimeMillis();

				String ifInResults = response.get(this.getIfinoctetsOID());
				String ifOutResults = response.get(this.getIfoutoctetsOID());
				if (ifInResults == null || ifOutResults == null) {
					Logit.LogInfo("no response for nic element probe" + this.getTemplate_id().toString() + "@"
							+ h.getHostId().toString() + "@" + this.getProbe_id());
					return null;
				}

				long ifTotalTraffic = Long.parseLong(ifInResults) + Long.parseLong(ifOutResults);
				ArrayList<Object> results = new ArrayList<Object>();
				results.add(resultsTimestamp);
				results.add(this.getIfSpeed());
				results.add(ifInResults);
				results.add(ifOutResults);
				results.add(ifTotalTraffic);
				return results;
			}
		} catch (Throwable th) {
			SysLogger.Record(new Log("Error running discovery element probe:" + this.getTemplate_id() + "@"
					+ h.getHostId().toString() + "@" + this.getProbe_id(), LogType.Error));
		}
		return null;
	}

	public String getIfoutoctetsOID() {
		return NicElement.ifOutOctetsOID + this.getIndex();
	}

	public String getIfinoctetsOID() {
		return NicElement.ifInOctetsOID + this.getIndex();
	}

	@Override
	public void setIndex(int index) {
		super.setIndex(index);
		this.getIfOutOctets().setOid(new OID(this.getIfoutoctetsOID()));
		this.getIfInOctets().setOid(new OID(this.getIfinoctetsOID()));
	}

	public long getIfSpeed() {
		return ifSpeed;
	}

	public void setIfSpeed(long ifSpeed) {
		this.ifSpeed = ifSpeed;
	}

	public SnmpProbe getIfOutOctets() {
		return ifOutOctets;
	}

	public SnmpProbe getIfInOctets() {
		return ifInOctets;
	}

}
