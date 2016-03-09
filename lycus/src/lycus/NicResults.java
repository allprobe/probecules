package lycus;

import java.util.ArrayList;
import java.util.HashMap;

public class NicResults extends BaseResults {


	private SnmpResults ifInResults;
	private SnmpResults ifOutResults;
	private SnmpResults ifTotal;

	public NicResults(RunnableProbe rp) {
		super(rp);
		ifInResults=new SnmpResults(rp);
		ifOutResults=new SnmpResults(rp);
		ifTotal=new SnmpResults(rp);
	}

	@Override
	public synchronized void acceptResults(ArrayList<Object> results) throws Exception {
		super.acceptResults(results);
		long timestamp=(long)results.get(0);
		long ifSpeed=(long)results.get(1);
		long ifInOctets=Long.parseLong((String)results.get(2));
		long ifOutOctets=Long.parseLong((String)results.get(3));
		long ifTotalOctets=(long)results.get(4);
		
		ArrayList<Object> inResults=new ArrayList<Object>();
		inResults.add(timestamp);
		inResults.add(ifInOctets);
		inResults.add(ifSpeed);
		
		ArrayList<Object> outResults=new ArrayList<Object>();
		outResults.add(timestamp);
		outResults.add(ifInOctets);
		outResults.add(ifSpeed);
		
		ArrayList<Object> totalResults=new ArrayList<Object>();
		totalResults.add(timestamp);
		totalResults.add(ifInOctets);
		totalResults.add(ifSpeed);
	}
	
	@Override
	public HashMap<String, String> getResults() throws Throwable {
		return super.getResults();
		// TODO finish send nic element results to RAN
	}
}
