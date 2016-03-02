package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;

import GlobalConstants.LogType;
import lycus.Probes.DiscoveryProbe;
import lycus.Probes.SnmpProbe;

public class DiscoveryResults extends BaseResults implements Runnable {

	private HashMap<Integer,BaseElementProbe> elements=null;
	private boolean newElements;
	
	public DiscoveryResults(RunnableProbe rp) {
		super(rp);
		this.elements=new HashMap<Integer,BaseElementProbe>();
		}
	
	public HashMap<Integer,BaseElementProbe> getElements() {
		return elements;
	}

	public void setElements(HashMap<Integer,BaseElementProbe> elements) {
		this.elements = elements;
	}

	private boolean isNewElements() {
		return newElements;
	}

	private void setNewElements(boolean newElements) {
		this.newElements = newElements;
	}


	@Override
	public synchronized void acceptResults(ArrayList<Object> results) throws Exception{
		super.acceptResults(results);
		
		HashMap<Integer,BaseElementProbe> lastScanElements=null;
		
		switch (((DiscoveryProbe)this.getRp().getProbe()).getType()) {
		case nics:
			lastScanElements = this.convertSnmpWalkToNicsElements(results);
			break;
		case disks:
			lastScanElements = this.acceptResultsForDisks(results);
			break;
		}
	
		if(lastScanElements==null)
			return;
		if(this.getElements()==null)
		{	
			this.setElements(lastScanElements);
			this.setLastTimestamp((long)results.get(0));
			this.startElementsThreads();
			this.setNewElements(true);
			return;
		}
		boolean theSame=this.isElementsIdentical(lastScanElements);
		if(theSame)
			return;
			
		this.stopElementsThreads();
		this.setElements(lastScanElements);
		this.setLastTimestamp((long)results.get(0));
		this.startElementsThreads();
		this.setNewElements(true);
	}

	private void startElementsThreads() {
		for(Map.Entry<Integer, BaseElementProbe> element:this.getElements().entrySet())
		{
			element.getValue().start();
		}
	}

	private void stopElementsThreads() {
		for(Map.Entry<Integer, BaseElementProbe> element:this.getElements().entrySet())
		{
			element.getValue().stop();
		}
	}

	@Override
	protected void checkIfTriggerd() throws Exception {
		super.checkIfTriggerd();
	}
	
	
	private HashMap<Integer,BaseElementProbe> acceptResultsForDisks(ArrayList<Object> results) {
		// TODO Auto-generated method stub
		return null;
	}

	private HashMap<Integer,BaseElementProbe> convertSnmpWalkToNicsElements(ArrayList<Object> results) {
		
		HashMap<Integer,BaseElementProbe> lastElements=new HashMap<Integer,BaseElementProbe>();
		
		Long ifInOctets=null;
		Long ifOutOctets=null;
		Map<String,String> walkResults=(Map<String,String>)results.get(1);
		for(Map.Entry<String, String> entry:walkResults.entrySet())
		{
			if(!entry.getKey().toString().contains("1.3.6.1.2.1.2.2.1.1"))
			continue;
			int index=Integer.parseInt(entry.getValue());
			if(index==0)
				SysLogger.Record(new Log("snmp OID index cannot be zero!",LogType.Warn));
			
			String name=walkResults.get("1.3.6.1.2.1.2.2.1.2."+index);
			
//			ifInOctets=Long.parseLong(walkResults.get("1.3.6.1.2.1.2.2.1.10."+index));
//			ifOutOctets=Long.parseLong(walkResults.get("1.3.6.1.2.1.2.2.1.16."+index));

			NicElementProbe element=new NicElementProbe(this,index,name);
			lastElements.put(index, element);
		}

		if(lastElements.size()==0)
			return null;
		
		return lastElements;
		
			
	}
	private boolean isElementsIdentical(HashMap<Integer,BaseElementProbe> lastElements) {
		
		if(this.getElements()==null&&lastElements==null)
			return true;
		if(lastElements!=null)
				if(this.getElements()==null)
					return false;
		if(this.getElements()!=null)
			if(lastElements==null)
				return false;
		if(this.getElements().size()!=lastElements.size())
			return false;
		for(Map.Entry<Integer, BaseElementProbe> newElement:lastElements.entrySet())
		{
			if(!this.getElements().get(newElement.getKey()).getName().equals(newElement.getValue().getName()))
				return false;
		}
		return true;
	}


	@Override
	public HashMap<String, String> getResults() throws Throwable {
		if(!this.isNewElements())
			return null;
		HashMap<String, String> results = super.getResults();
		JSONArray rawResults = new JSONArray();
		rawResults.add(6);
		rawResults.add(this.getGson().toJson(this.getElements()).toString());
		results.put("RAW@new_elements_map@" + this.getLastTimestamp(), rawResults.toJSONString());
		return results;
	}

	
	// run elements checks
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
