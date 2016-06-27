package SLA;

import java.util.concurrent.ConcurrentHashMap;
import org.json.simple.JSONArray;
import Interfaces.ISLAContainer;
import Results.BaseResult;
import Results.PingResult;
import Results.PortResult;
import Results.WebResult;
import Utils.Logit;

import org.json.simple.JSONObject;

import GlobalConstants.Constants;

public class SLAContainer implements ISLAContainer {
	private static SLAContainer instance;

	private ConcurrentHashMap<String, SLAObject> webSLA; // ConcurrentHashMap<RunnableProbeId,
															// SLAObject>
	private ConcurrentHashMap<String, SLAObject> pingSLA; // ConcurrentHashMap<RunnableProbeId,
															// SLAObject>
	private ConcurrentHashMap<String, SLAObject> portSLA; // ConcurrentHashMap<RunnableProbeId,
															// SLAObject>

	// private JSONArray finishedSla = new JSONArray();

	public static SLAContainer getInstance() {
		if (instance == null) {
			instance = new SLAContainer();
		}
		return instance;
	}

	private SLAContainer() {
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

	public JSONObject getHourlySLA() {
		JSONObject returnJson = new JSONObject();
		try {
			JSONArray slaArray = new JSONArray();

			for (String runnableProbeId : webSLA.keySet()) {
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("runnable_probe_id", runnableProbeId);
				jsonItem.put("timestamp", webSLA.get(runnableProbeId));
				jsonItem.put("type", Constants.hourly);
				jsonItem.put("sla", webSLA.get(runnableProbeId).getResults());

				slaArray.add(jsonItem);
			}
			for (String runnableProbeId : pingSLA.keySet()) {
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("runnable_probe_id", runnableProbeId);
				jsonItem.put("timestamp", webSLA.get(runnableProbeId));
				jsonItem.put("type", Constants.hourly);
				jsonItem.put("sla", pingSLA.get(runnableProbeId).getResults());

				slaArray.add(jsonItem);
			}
			for (String runnableProbeId : portSLA.keySet()) {
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("runnable_probe_id", runnableProbeId);
				jsonItem.put("timestamp", webSLA.get(runnableProbeId));
				jsonItem.put("type", Constants.hourly);
				jsonItem.put("sla", pingSLA.get(runnableProbeId).getResults());

				slaArray.add(jsonItem);
			}

			returnJson.put("sla_results", slaArray);
			return returnJson;
		} catch (Exception ex) {
			Logit.LogError("SLAContainer - getHourlySLA()", "Building Json failed!");
			return null;
		}
	}

	public JSONObject getDailySLA() {
		JSONObject returnJson = new JSONObject();
		try {
			JSONArray slaArray = new JSONArray();

			for (String runnableProbeId : webSLA.keySet()) {
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("runnable_probe_id", runnableProbeId);
				jsonItem.put("timestamp", webSLA.get(runnableProbeId));
				jsonItem.put("type", Constants.daily);
				jsonItem.put("sla", webSLA.get(runnableProbeId).getDailyResults());

				slaArray.add(jsonItem);
			}
			for (String runnableProbeId : pingSLA.keySet()) {
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("runnable_probe_id", runnableProbeId);
				jsonItem.put("timestamp", webSLA.get(runnableProbeId));
				jsonItem.put("type", Constants.daily);
				jsonItem.put("sla", pingSLA.get(runnableProbeId).getDailyResults());

				slaArray.add(jsonItem);
			}
			for (String runnableProbeId : portSLA.keySet()) {
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("runnable_probe_id", runnableProbeId);
				jsonItem.put("timestamp", webSLA.get(runnableProbeId));
				jsonItem.put("type", Constants.daily);
				jsonItem.put("sla", pingSLA.get(runnableProbeId).getDailyResults());

				slaArray.add(jsonItem);
			}

			returnJson.put("sla_results", slaArray);
			return returnJson;
		} catch (Exception ex) {
			Logit.LogError("SLAContainer - getHourlySLA()", "Building Json failed!");
			return null;
		}
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
