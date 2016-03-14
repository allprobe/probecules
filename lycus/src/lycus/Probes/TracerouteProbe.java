package lycus.Probes;

import java.util.ArrayList;
import java.util.UUID;

import lycus.Host;
import lycus.Net;
import lycus.User;
import lycus.Probes.BaseProbe;

public class TracerouteProbe extends BaseProbe {

	private int timeout;
	
	TracerouteProbe(User user,String probe_id,UUID template_id,String name,long interval,float multiplier,boolean status,int timeout) {
        super(user,probe_id,template_id,name,interval,multiplier,status);
        this.setTimeout(timeout);
    }

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	 @Override
	 public ArrayList<Object> Check(Host h)
	 {
	  	ArrayList<Object> results=Net.Traceroute(h.getHostIp());
	    return results;
	 }
}
