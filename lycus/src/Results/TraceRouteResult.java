package Results;

import java.util.ArrayList;
import java.util.HashMap;

import Results.BaseResult;
import Utils.JsonUtil;
import lycus.RunnableProbe;

public class TraceRouteResult extends BaseResult {

	private ArrayList<String> routes;

	public TraceRouteResult(String runnableProbeId) {
		super(runnableProbeId);
	}
	public TraceRouteResult(String runnableProbeId, long timestamp) {
		super(runnableProbeId, timestamp);
	}
	
	public ArrayList<String> getRoutes() {
		return routes;
	}

	public void setRoutes(ArrayList<String> routes) {
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
