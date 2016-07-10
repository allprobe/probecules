package Results;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Results.BaseResult;
import Utils.JsonUtil;
import Utils.Logit;

public class TraceRouteResult extends BaseResult {

	private ArrayList<ArrayList<Object>> routes;

	public TraceRouteResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public TraceRouteResult(String runnableProbeId, long timestamp) {
		super(runnableProbeId, timestamp);
	}

	public ArrayList<ArrayList<Object>> getRoutes() {
		return routes;
	}

	public void setRoutes(ArrayList<ArrayList<Object>> routes) {
		this.routes = routes;
	}

	@Override
	public HashMap<String, String> getRaw() throws Throwable {
		// TODO Auto-generated method stub
		return super.getRaw();
	}

	@Override
	public Object getResultObject() {
		JSONArray result = new JSONArray();
		result.add(8);
		
		try {
			result.add((JSONArray) (new JSONParser()).parse(JsonUtil.ToJson(routes)));
		} catch (ParseException e) {
			Logit.LogError("TraceRouteResult - getResultObject()",
					"Unable to parse all routes of traceroute probe " + this.getRunnableProbeId() + " to json! ",
					e);
			return null;
		}

		return result;
	}

}
