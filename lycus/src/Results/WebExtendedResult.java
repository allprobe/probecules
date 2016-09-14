package Results;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Probes.HttpProbe;
import Utils.JsonUtil;
import Utils.Logit;
import lycus.RunnableProbeContainer;

public class WebExtendedResult extends WebResult {
	private ArrayList<DOMElement> allElementsResults;

	public WebExtendedResult(String runnableProbeId, long timestamp, long responseTime, int responseCode,
			long responseSize,int stateCode) {
		super(runnableProbeId, timestamp, responseCode, responseTime, responseSize,stateCode);
	}
	public WebExtendedResult(String runnableProbeId, long timestamp,int stateCode) {
		super(runnableProbeId, timestamp,stateCode);
	}
	public WebExtendedResult(String runnableProbeId) {
		super(runnableProbeId);
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

		try {
			result.add((JSONArray) (new JSONParser()).parse(gson.toJson(this.getAllElementsResults())));
		} catch (ParseException e) {
			Logit.LogError("WebExtendedResult - getResultObject()",
					"Unable to parse all elements of extended http probe " + this.getRunnableProbeId() + " to json! ",
					e);
			return null;
		}
		// System.out.println(result);
		return result;
	}
}
