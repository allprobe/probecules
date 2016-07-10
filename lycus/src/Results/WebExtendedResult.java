package Results;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Probes.HttpProbe;
import Utils.JsonUtil;
import lycus.RunnableProbeContainer;

public class WebExtendedResult extends WebResult {
	private ArrayList<DOMElement> allElementsResults;

	public WebExtendedResult(String runnableProbeId, long timestamp, long responseTime, int responseCode,
			long responseSize) {
		super(runnableProbeId, timestamp, responseCode, responseTime, responseSize);
	}

	public ArrayList<DOMElement> getAllElementsResults() {
		return allElementsResults;
	}

	public void setAllElementsResults(ArrayList<DOMElement> allElementsResults) {
		this.allElementsResults = allElementsResults;
	}

	@Override
	public Object getResultObject() {
		JSONArray result = new JSONArray();
		if (((HttpProbe) RunnableProbeContainer.getInstanece().get(this.getRunnableProbeId()).getProbe()).isDeepCheck())
			result.add(3.5);
		else
			result.add(3);

		result.add(this.getStatusCode());
		result.add(this.getResponseTime());
		result.add(this.getPageSize());
		
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		
		result.add(gson.toJson(this.getAllElementsResults()));
		// System.out.println(result);
		return result;
	}
}
