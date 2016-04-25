package Results;

import java.util.ArrayList;

import Results.BaseResult;
import lycus.RunnableProbe;

public class TraceRouteResult extends BaseResult {
	private ArrayList<String> routes;

	public TraceRouteResult(String runnableProbeId) {
		super(runnableProbeId);
	}
	
	public ArrayList<String> getRoutes() {
		return routes;
	}

	public void setRoutes(ArrayList<String> routes) {
		this.routes = routes;
	}

//	@Override
//	public synchronized void acceptResults(ArrayList<Object> results) throws Exception{
//		super.acceptResults(results);
//		long lastTimestamp = (long) results.get(0);
//		ArrayList<String> routes = (ArrayList<String>) results.get(1);
//		
//		this.setLastTimestamp(lastTimestamp);
//		this.setRoutes(routes);
//	}
}