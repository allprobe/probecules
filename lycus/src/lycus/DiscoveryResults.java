package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;

import GlobalConstants.LogType;
import lycus.Probes.DiscoveryProbe;
import lycus.Probes.SnmpProbe;

public class DiscoveryResults extends BaseResults {

	private HashMap<Integer,String> currentElements=null;
	private HashMap<Integer,String> previousElements=null;
	private boolean newElements;
	
	public DiscoveryResults(RunnableProbe rp) {
		super(rp);
		}
	

	@Override
	public synchronized void acceptResults(ArrayList<Object> results) throws Exception{
		super.acceptResults(results);
			
		HashMap<Integer,String> lastScanElements=null;
		
		switch (((DiscoveryProbe)this.getRp().getProbe()).getType()) {
		case nics:
			lastScanElements = this.convertNicsWalkToIndexes((HashMap<String,String>)results.get(1));
			break;
		case disks:
			lastScanElements = this.convertDisksWalkToIndexes((HashMap<String,String>)results.get(1));
			break;
		}
		
		// TODO finish checking if elements change and send results to Ran

		
	}

	private HashMap<Integer, String> convertDisksWalkToIndexes(HashMap<String, String> hashMap) {
		// TODO Auto-generated method stub
		return null;
	}


	private HashMap<Integer, String> convertNicsWalkToIndexes(HashMap<String, String> nicsWalk) {
		HashMap<Integer,String> lastElements=new HashMap<Integer,String>();
		
		for(Map.Entry<String, String> entry:nicsWalk.entrySet())
		{
			if(!entry.getKey().toString().contains("1.3.6.1.2.1.2.2.1.1"))
				continue;
			int index=Integer.parseInt(entry.getValue());
			if(index==0)
			{
				SysLogger.Record(new Log("snmp OID index cannot be zero! ---"+this.getRp().getRPString(),LogType.Warn));
				continue;
			}
				
			String name=nicsWalk.get("1.3.6.1.2.1.2.2.1.2."+index);
			lastElements.put(index, name);
		}
		
		if(lastElements.size()==0)
			return null;
		
		return lastElements;
	}

	@Override
	protected void checkIfTriggerd() throws Exception {
		super.checkIfTriggerd();
	}
	
	
	private HashMap<Integer,BaseElementProbe> acceptResultsForDisks(ArrayList<Object> results) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isElementsIdentical(HashMap<Integer,BaseElementProbe> lastElements) {
		return true;
	}


	@Override
	public HashMap<String, String> getResults() throws Throwable {
		// TODO send elements in case of change

		if(false)
			return null;
		HashMap<String, String> results = super.getResults();
		JSONArray rawResults = new JSONArray();
		rawResults.add(6);
		results.put("RAW@new_elements_map@" + this.getLastTimestamp(), rawResults.toJSONString());
		return results;
	}

	
}
