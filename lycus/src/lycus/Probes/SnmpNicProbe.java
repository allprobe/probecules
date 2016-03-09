package lycus.Probes;

import java.util.UUID;

import org.snmp4j.smi.OID;

import GlobalConstants.Enums.SnmpStoreAs;
import GlobalConstants.SnmpDataType;
import lycus.SnmpUnit;
import lycus.User;

public class SnmpNicProbe extends SnmpProbe {

	private long ifSpeed;
	
	public SnmpNicProbe(String probe_id, UUID template_id, String name, long interval, float multiplier,
			boolean status, OID oid, SnmpDataType dataType, SnmpUnit unit,long ifSpeed) {
		super(probe_id, template_id, name, interval, multiplier, status, oid, dataType, unit, SnmpStoreAs.deltaBytesPerSecond);
		this.setIfSpeed(ifSpeed);
	}

	public long getIfSpeed() {
		return ifSpeed;
	}

	public void setIfSpeed(long ifSpeed) {
		this.ifSpeed = ifSpeed;
	}

}
