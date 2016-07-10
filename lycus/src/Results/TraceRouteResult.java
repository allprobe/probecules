package Results;

import java.util.ArrayList;
import java.util.HashMap;

import Results.BaseResult;
import Utils.JsonUtil;

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
	public String getResultString() {
		return JsonUtil.ToJson(routes);
	}

}
