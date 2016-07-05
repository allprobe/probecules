package SLA;

import java.util.concurrent.ConcurrentHashMap;
import org.json.simple.JSONArray;
import Interfaces.ISLAContainer;
import Results.BaseResult;
import Results.PingResult;
import Results.PortResult;
import Results.WebResult;
import Utils.GeneralFunctions;
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
			{
				slaObject = new SLAObject();
			}
			slaObject.addResult(((PortResult) result).isActive());
			portSLA.put(result.getRunnableProbeId(), slaObject);
			
		} else if (result instanceof PingResult) {
			SLAObject slaObject = pingSLA.get(result.getRunnableProbeId());
			if (slaObject == null)
			{
				slaObject = new SLAObject();
			}
			slaObject.addResult(((PingResult) result).isActive());
			pingSLA.put(result.getRunnableProbeId(), slaObject);
			
		} else if (result instanceof WebResult) {
			SLAObject slaObject = webSLA.get(result.getRunnableProbeId());
			if (slaObject == null)
			{
				slaObject = new SLAObject();
			}

			slaObject.addResult(((WebResult) result).isActive());
			webSLA.put(result.getRunnableProbeId(), slaObject);
		}

		return true;
	}

	public JSONObject getHourlySLA() {
		JSONObject returnJson = new JSONObject();
		try {
			JSONArray slaArray = new JSONArray();
			long timeStamp = System.currentTimeMillis();
			
			for (String runnableProbeId : webSLA.keySet()) {
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("RUNNABLE_PROBE_ID", runnableProbeId);
				jsonItem.put("TIMESTAMP", timeStamp);
				jsonItem.put("TYPE", Constants.hourly);
				jsonItem.put("SLA", webSLA.get(runnableProbeId).getResults() * 100);

				slaArray.add(jsonItem);
			}
			for (String runnableProbeId : pingSLA.keySet()) {
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("RUNNABLE_PROBE_ID", runnableProbeId);
				jsonItem.put("TIMESTAMP", timeStamp);
				jsonItem.put("TYPE", Constants.hourly);
				jsonItem.put("SLA", pingSLA.get(runnableProbeId).getResults() * 100);

				slaArray.add(jsonItem);
			}
			for (String runnableProbeId : portSLA.keySet()) {
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("RUNNABLE_PROBE_ID", runnableProbeId);
				jsonItem.put("TIMESTAMP", timeStamp);
				jsonItem.put("TYPE", Constants.hourly);
				jsonItem.put("SLA", portSLA.get(runnableProbeId).getResults() * 100);

				slaArray.add(jsonItem);
			}

			String slaResults = GeneralFunctions.Base64Encode(slaArray.toJSONString());
			returnJson.put("sla_results", slaResults);
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
				jsonItem.put("RUNNABLE_PROBE_ID", runnableProbeId);
				jsonItem.put("TIMESTAMP", webSLA.get(runnableProbeId));
				jsonItem.put("TYPE", Constants.daily);
				jsonItem.put("SLA", webSLA.get(runnableProbeId).getDailyResults() * 100);

				slaArray.add(jsonItem);
			}
			for (String runnableProbeId : pingSLA.keySet()) {
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("RUNNABLE_PROBE_ID", runnableProbeId);
				jsonItem.put("TIMESTAMP", webSLA.get(runnableProbeId));
				jsonItem.put("TYPE", Constants.daily);
				jsonItem.put("SLA", pingSLA.get(runnableProbeId).getDailyResults() * 100);

				slaArray.add(jsonItem);
			}
			for (String runnableProbeId : portSLA.keySet()) {
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("RUNNABLE_PROBE_ID", runnableProbeId);
				jsonItem.put("TIMESTAMP", webSLA.get(runnableProbeId));
				jsonItem.put("TYPE", Constants.daily);
				jsonItem.put("SLA", pingSLA.get(runnableProbeId).getDailyResults() * 100);

				slaArray.add(jsonItem);
			}
			
			String slaResults = GeneralFunctions.Base64Encode(slaArray.toJSONString());
			returnJson.put("sla_results", slaResults);
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
