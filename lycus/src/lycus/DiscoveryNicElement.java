package lycus;

import org.snmp4j.smi.OID;

import lycus.Enums.SnmpStoreAs;
import lycus.Probes.SnmpProbe;

public class DiscoveryNicElement extends DiscoveryElement {

	private RunnableProbe ifInOctets;
	private RunnableProbe ifOutOctets;
	public DiscoveryNicElement(RunnableDiscoveryProbeResults dp, int index, String name) {
		super(dp, index, name);
		SnmpProbe ifInOctetsProbe=new SnmpProbe(null, dp.getRp().getProbe().getProbe_id()+"@"+index, dp.getRp().getProbe().getTemplate_id(), "ifInOctets",dp.getRp().getProbe().getInterval(), dp.getRp().getProbe().getMultiplier(),true,new OID("1.3.6.1.2.1.2.2.1.10."+index),SnmpDataType.Numeric,SnmpUnit.bytes,SnmpStoreAs.delta);
		try {
			RunnableProbe inOctetsRP=new RunnableProbe(dp.getRp().getHost(), ifInOctetsProbe);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SnmpProbe ifOutOctetsProbe=new SnmpProbe(null, dp.getRp().getProbe().getProbe_id()+"@"+index, dp.getRp().getProbe().getTemplate_id(), "ifOutOctets",dp.getRp().getProbe().getInterval(), dp.getRp().getProbe().getMultiplier(),true,new OID("1.3.6.1.2.1.2.2.1.16."+index),SnmpDataType.Numeric,SnmpUnit.bytes,SnmpStoreAs.delta);
		try {
			RunnableProbe outOctetsRP=new RunnableProbe(dp.getRp().getHost(), ifOutOctetsProbe);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
