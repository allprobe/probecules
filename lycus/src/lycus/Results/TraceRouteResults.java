package lycus.Results;

import java.util.ArrayList;

import lycus.RunnableProbe;
import lycus.Results.BaseResult;

public class TraceRouteResults extends BaseResult {
	
	private ArrayList<String> routes;
	
	public TraceRouteResults(RunnableProbe rp) {
		super(rp);
	}
	
	public ArrayList<String> getRoutes() {
		return routes;
	}

	public void setRoutes(ArrayList<String> routes) {
		this.routes = routes;
	}

	@Override
	public synchronized void acceptResults(ArrayList<Object> results) {
		long lastTimestamp = (long) results.get(0);
		ArrayList<String> routes = (ArrayList<String>) results.get(1);
		
		this.setLastTimestamp(lastTimestamp);
		this.setRoutes(routes);
		

	}
}
