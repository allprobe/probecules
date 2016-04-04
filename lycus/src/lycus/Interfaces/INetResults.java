package lycus.Interfaces;

import java.util.List;

import lycus.Host;
import lycus.Probes.DiscoveryProbe;
import lycus.Probes.NicProbe;
import lycus.Probes.IcmpProbe;
import lycus.Probes.PortProbe;
import lycus.Probes.RBLProbe;
import lycus.Probes.SnmpProbe;
import lycus.Probes.HttpProbe;
import lycus.Results.DiscoveryResult;
import lycus.Results.NicResult;
import lycus.Results.PingResult;
import lycus.Results.PortResult;
import lycus.Results.RblResult;
import lycus.Results.SnmpResult;
import lycus.Results.WebResult;

public interface INetResults {
	PingResult getPingResult(Host host,IcmpProbe probe);
	PortResult getPortResult(Host host,PortProbe probe);
	WebResult  getWebResult(Host host,HttpProbe probe);
	RblResult    getRblResult(Host host,RBLProbe probe);
	List<SnmpResult>   getSnmpResults(Host host,List<SnmpProbe> snmpProbes);
	NicResult getNicResult(Host host, NicProbe nicProbe);
	DiscoveryResult getDiscoveryResult(Host h, DiscoveryProbe discoveryProbe);
}
