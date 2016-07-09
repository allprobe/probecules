package Interfaces;

import java.util.List;

import Probes.DiscoveryProbe;
import Probes.HttpProbe;
import Probes.IcmpProbe;
import Probes.NicProbe;
import Probes.PortProbe;
import Probes.RBLProbe;
import Probes.SnmpProbe;
import Probes.TracerouteProbe;
import Results.DiscoveryResult;
import Results.NicResult;
import Results.PingResult;
import Results.PortResult;
import Results.RblResult;
import Results.SnmpResult;
import Results.TraceRouteResult;
import Results.WebExtendedResult;
import Results.WebResult;
import lycus.Host;

public interface INetResults {
	PingResult getPingResult(Host host,IcmpProbe probe);
	PortResult getPortResult(Host host,PortProbe probe);
	WebResult  getWebResult(Host host,HttpProbe probe);
	RblResult    getRblResult(Host host,RBLProbe probe);
	List<SnmpResult>   getSnmpResults(Host host,List<SnmpProbe> snmpProbes);
	NicResult getNicResult(Host host, NicProbe nicProbe);
	DiscoveryResult getDiscoveryResult(Host h, DiscoveryProbe discoveryProbe);
	WebExtendedResult getWebExtendedResult(Host host, HttpProbe probe);
	TraceRouteResult getTracerouteResult(Host host, TracerouteProbe probe);
}
