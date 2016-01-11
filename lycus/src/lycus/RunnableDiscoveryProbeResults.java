package lycus;

import java.util.ArrayList;
import java.util.Map;

public class RunnableDiscoveryProbeResults extends RunnableProbeResults {

	private ArrayList<DiscoveryElement> elements;
	
	public RunnableDiscoveryProbeResults(RunnableProbe rp) {
		super(rp);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public synchronized void acceptResults(ArrayList<Object> results) throws Exception{
		super.acceptResults(results);
		
		switch (((DiscoveryProbe)this.getRp().getProbe()).getType()) {
		case BandWidth:
			results = this.acceptResultsForInterfaces(results);
			break;
		case Disk:
			results = this.acceptResultsForDisks(results);
			break;
		}

		
				try{
			checkIfTriggerd();
		}
		catch(Exception e)
		{
			SysLogger.Record(new Log("Error triggering RunnableProbe: "+this.getRp(),LogType.Warn,e));
		}
	}

	private ArrayList<Object> acceptResultsForDisks(ArrayList<Object> results) {
		// TODO Auto-generated method stub
		return null;
	}

	private ArrayList<Object> acceptResultsForInterfaces(ArrayList<Object> results) {
		Map<String,String> walkResults=(Map<String,String>)results.get(0);
		for(Map.Entry<String, String> entry:walkResults.entrySet())
		{
			
		}
		return null;
	}
}
