package lycus.Results;

import lycus.RunnableProbe;
import lycus.GlobalConstants.ProbeTypes;

public class ResultFactory {
	public static BaseResult getResult(ProbeTypes type, String runnableProbeId) {
		switch (type) {
		case ICMP:
			return new PingResult(runnableProbeId);
		case PORT:
			return new PortResult(runnableProbeId);
		case HTTP:
			return new WebResult(runnableProbeId);
		case SNMP:
			return new SnmpResult(runnableProbeId);
		case SNMPv1:
			return new SnmpResult(runnableProbeId);
		case RBL:
			return new RblResult(runnableProbeId);
		case TRACEROUTE:
			return new TraceRouteResult(runnableProbeId);
		case DISCOVERY:
			return new DiscoveryResult(runnableProbeId);
		// case DISCOVERYELEMENT:
		// if (probe instanceof NicElement)
		// return new NicResults(runnableProbe);
		}
		return null;

	}

}
