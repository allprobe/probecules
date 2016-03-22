package lycus.Interfaces;

import java.util.List;

import lycus.Host;
import lycus.Probes.PingerProbe;
import lycus.Probes.PorterProbe;
import lycus.Probes.RBLProbe;
import lycus.Probes.SnmpProbe;
import lycus.Probes.WeberProbe;
import lycus.Results.PingResult;
import lycus.Results.PortResult;
import lycus.Results.RblResult;
import lycus.Results.SnmpResult;
import lycus.Results.WebResult;

public interface INetResults {
	PingResult getPingResult(Host host,PingerProbe probe);
	PortResult getPortResult(Host host,PorterProbe probe);
	WebResult  getWebResult(Host host,WeberProbe probe);
	RblResult    getRblResult(Host host,RBLProbe probe);
	List<SnmpResult>   getSnmpResults(Host host,List<SnmpProbe> snmpProbes);
}
