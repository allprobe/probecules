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
	private HashMap<Integer,String> newElements=null;
	
	public DiscoveryResults(RunnableProbe rp) {
		super(rp);
		}
	

	public HashMap<Integer,String> getCurrentElements() {
		return currentElements;
	}


	public void setCurrentElements(HashMap<Integer,String> currentElements) {
		this.currentElements = currentElements;
	}


	public HashMap<Integer,String> getNewElements() {
		return newElements;
	}


	public void setNewElements(HashMap<Integer,String> newElements) {
		this.newElements = newElements;
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
		
		if(this.getCurrentElements()==null)
		{
			this.setCurrentElements(lastScanElements);
			return;
		}
		
		// TODO finish checking if elements change and send results to Ran
		boolean changed=isElementsIdentical(lastScanElements);
		
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
			String ifSpeed=nicsWalk.get("1.3.6.1.2.1.2.2.1.5."+index);
			
			lastElements.put(index, name+"@"+ifSpeed);
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

	private boolean isElementsIdentical(HashMap<Integer, String> lastScanElements) {
		HashMap<Integer, String> currentScanElements=this.getCurrentElements();
		for(Map.Entry<Integer, String> element:lastScanElements.entrySet())
		{
			
			
		}
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
