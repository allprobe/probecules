package SLA;

import java.util.concurrent.ConcurrentHashMap;

import Results.BaseResult;
import Results.PingResult;
import Results.PortResult;
import Results.WebResult;

public class SLAContainer {
	private ConcurrentHashMap<String, SLAObject> webSLA; // ConcurrentHashMap<RunnableProbeId,
															// SLAObject>
	private ConcurrentHashMap<String, SLAObject> pingSLA; // ConcurrentHashMap<RunnableProbeId,
															// SLAObject>
	private ConcurrentHashMap<String, SLAObject> portSLA; // ConcurrentHashMap<RunnableProbeId,
															// SLAObject>

	public SLAContainer() {
		webSLA = new ConcurrentHashMap<String, SLAObject>();
		pingSLA = new ConcurrentHashMap<String, SLAObject>();
		portSLA = new ConcurrentHashMap<String, SLAObject>();
	}

	public boolean addToSLA(BaseResult result) {
		if (result instanceof PortResult) {
			SLAObject slaObject = portSLA.get(result.getRunnableProbeId());
			if (slaObject == null)
				slaObject = new SLAObject();

			slaObject.addResult(((PortResult) result).isActive());
		} else if (result instanceof PingResult) {
			SLAObject slaObject = pingSLA.get(result.getRunnableProbeId());
			if (slaObject == null)
				slaObject = new SLAObject();

			slaObject.addResult(((PingResult) result).isActive());
		} else if (result instanceof WebResult) {
			SLAObject slaObject = webSLA.get(result.getRunnableProbeId());
			if (slaObject == null)
				slaObject = new SLAObject();

			slaObject.addResult(((WebResult) result).isActive());
		}

		return true;
	}

	public ConcurrentHashMap<String, SLAObject> getWebSLA() {
		return webSLA;
	}

	public ConcurrentHashMap<String, SLAObject> getPingSLA() {
		return pingSLA;
	}

	public ConcurrentHashMap<String, SLAObject> getPortSLA() {
		return portSLA;
	}
}
