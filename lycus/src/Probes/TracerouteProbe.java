package Probes;

import java.util.UUID;

import NetConnection.NetResults;
import Probes.BaseProbe;
import Results.BaseResult;
import Results.PingResult;
import Results.TraceRouteResult;
import lycus.Host;
import lycus.User;

public class TracerouteProbe extends BaseProbe {

	private int timeout;

	public TracerouteProbe(User user, String probe_id, UUID template_id, String name, int interval, float multiplier,
			boolean status, int timeout) {
		super(user, probe_id, template_id, name, interval, multiplier, status);
		this.setTimeout(timeout);
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public BaseResult getResult(Host h) {
		if (!h.isHostStatus())
			return null;

		TraceRouteResult tracerouteResult = NetResults.getInstanece().getTracerouteResult(h, this);

		return tracerouteResult;
	}
}
