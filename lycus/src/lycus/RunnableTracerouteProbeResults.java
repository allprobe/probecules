package lycus;

import java.util.ArrayList;

public class RunnableTracerouteProbeResults extends RunnableProbeResults {
	
	private ArrayList<String> routes;
	
	public RunnableTracerouteProbeResults(RunnableProbe rp) {
		super(rp);
	}
	
	public ArrayList<String> getRoutes() {
		return routes;
	}

	public void setRoutes(ArrayList<String> routes) {
		this.routes = routes;
	}

	@Override
	public void acceptResults(ArrayList<Object> results) {
		long lastTimestamp = (long) results.get(0);
		ArrayList<String> routes = (ArrayList<String>) results.get(1);
		
		this.setLastTimestamp(lastTimestamp);
		this.setRoutes(routes);
		

	}
}
