package Results;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import Utils.JsonUtil;

public class WebExtendedResult extends WebResult {
	private ArrayList<DOMElement> allElementsResults;
	public WebExtendedResult(String runnableProbeId, long timestamp,long responseTime,int responseCode,long responseSize) {
		super(runnableProbeId,timestamp,responseCode,responseTime,responseSize);
	}
	public ArrayList<DOMElement> getAllElementsResults() {
		return allElementsResults;
	}
	public void setAllElementsResults(ArrayList<DOMElement> allElementsResults) {
		this.allElementsResults = allElementsResults;
	}
	@Override
	public String getResultString() {
		JSONArray result = new JSONArray();
		result.add(3);
		result.add(this.getStatusCode());
		result.add(this.getResponseTime());
		result.add(this.getPageSize());
		result.add(JsonUtil.ToJson(this.getAllElementsResults()));
		System.out.println(result);
		return result.toString();
	}
}
