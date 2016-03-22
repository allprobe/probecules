package lycus.Probes;

import java.util.UUID;
import lycus.Host;
import lycus.User;
import lycus.Probes.BaseProbe;
import lycus.Results.BaseResult;

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
	 public BaseResult getResult(Host h)
	 {
	  	return null;
	 }
}
