package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.GsonBuilder;

import GlobalConstants.Enums;
import GlobalConstants.Enums.ElementChange;
import GlobalConstants.Enums.HostType;
import GlobalConstants.LogType;
import lycus.Probes.DiscoveryProbe;
import lycus.Probes.SnmpProbe;

public class DiscoveryResults extends BaseResults {

	private HashMap<String,BaseElement> currentElements=null;
	private HashMap<BaseElement,Enums.ElementChange> elementsChanges=null;
	
	public DiscoveryResults(RunnableProbe rp) {
		super(rp);
		}
	

	public HashMap<String,BaseElement> getCurrentElements() {
		return currentElements;
	}


	public void setCurrentElements(HashMap<String,BaseElement> currentElements) {
		this.currentElements = currentElements;
	}


	public synchronized HashMap<BaseElement,Enums.ElementChange> getElementsChanges() {
		return elementsChanges;
	}


	public synchronized void setElementsChanges(HashMap<BaseElement,Enums.ElementChange> newElements) {
		this.elementsChanges = newElements;
	}


	@Override
	public synchronized void acceptResults(ArrayList<Object> results) throws Exception{
		super.acceptResults(results);
			
		String rpStr = this.getRp().getRPString();
		if (rpStr.contains(
				"discovery_6b54463e-fe1c-4e2c-a090-452dbbf2d510"))
			System.out.println("BREAKPOINT");
		
		
		HashMap<String,BaseElement> lastScanElements=null;
		
		switch (((DiscoveryProbe)this.getRp().getProbe()).getType()) {
		case nics:
			lastScanElements = this.convertNicsWalkToIndexes((HashMap<String,String>)results.get(1),(Enums.HostType)results.get(2));
			break;
		case disks:
			lastScanElements = this.convertDisksWalkToIndexes((HashMap<String,String>)results.get(1),(Enums.HostType)results.get(2));
			break;
		}
		
		long timestamp=(long)results.get(0);

		boolean sameElements=checkForElementsChanges(lastScanElements,timestamp);
	}

	private HashMap<String,BaseElement> convertDisksWalkToIndexes(HashMap<String, String> hashMap, HostType hostType) {
		// TODO Auto-generated method stub
		return null;
	}


	private HashMap<String,BaseElement> convertNicsWalkToIndexes(HashMap<String, String> nicsWalk, HostType hostType) {
		HashMap<String,BaseElement> lastElements=new HashMap<String,BaseElement>();
		if(hostType==null)
			return null;
		for(Map.Entry<String, String> entry:nicsWalk.entrySet())
		{
			if(!entry.getKey().toString().contains("1.3.6.1.2.1.2.2.1.1."))
				continue;
			int index=Integer.parseInt(entry.getValue());
			if(index==0)
			{
				SysLogger.Record(new Log("snmp OID index cannot be zero! ---"+this.getRp().getRPString(),LogType.Warn));
				continue;
			}
				
			String name;
			long ifSpeed;

			ifSpeed=Long.parseLong(nicsWalk.get("1.3.6.1.2.1.2.2.1.5."+index));
			switch(hostType)
			{
			case Windows:
				name=GeneralFunctions.convertHexToString(nicsWalk.get("1.3.6.1.2.1.2.2.1.2."+index));
				break;
			case Linux:
				name=nicsWalk.get("1.3.6.1.2.1.2.2.1.2."+index);
				break;
			default:return null;
			}
			NicElement nicElement=new NicElement(this.getRp().getProbe().getProbe_id(),this.getRp().getProbe().getTemplate_id(),name,this.getRp().getProbe().getInterval(),this.getRp().getProbe().getMultiplier(),this.getRp().getProbe().isActive(), index,ifSpeed,hostType);
			
			
			lastElements.put(name, nicElement);
		}
		
		if(lastElements.size()==0)
			return null;
		
		return lastElements;
	}

	@Override
	protected void checkIfTriggerd() throws Exception {
		super.checkIfTriggerd();
	}
	
	
	private HashMap<Integer,BaseElement> acceptResultsForDisks(ArrayList<Object> results) {
		// TODO Auto-generated method stub
		return null;
	}

	// returns true if there is any change made on the host elements
	private boolean checkForElementsChanges(HashMap<String, BaseElement> lastScanElements, long timestamp) {
		HashMap<BaseElement,Enums.ElementChange> elementsChanges=new HashMap<BaseElement,Enums.ElementChange>();
		
		if(this.getCurrentElements()==null)
		{   
			for(Map.Entry<String, BaseElement> lastElement:lastScanElements.entrySet())
			{
				elementsChanges.put(lastElement.getValue(), ElementChange.addedElement);
			}
			this.setElementsChanges(elementsChanges);
			this.setCurrentElements(lastScanElements);
			this.setLastTimestamp(timestamp);
			return true;
		}
		
		for(Map.Entry<String, BaseElement> lastElement:lastScanElements.entrySet())
		{
			if(this.getCurrentElements().get(lastElement.getKey())==null)
			{
				elementsChanges.put(lastElement.getValue(), ElementChange.addedElement);
				// TODO Add new element to system
				continue;
			}
			if(this.getCurrentElements().get(lastElement.getKey()).isIdentical(lastElement.getValue()))
			continue;
			elementsChanges.put(lastElement.getValue(), ElementChange.indexElementChanged);
			// TODO Change element index
		}

		Set<String> newElements=lastScanElements.keySet();
		
		//check for removed elements
		for(Map.Entry<String, BaseElement> currentElement:this.getCurrentElements().entrySet())
		{
			if(!newElements.contains(currentElement.getKey()))
				elementsChanges.put(currentElement.getValue(), ElementChange.removedElement);
			// TODO Remove nic element from the system
		}
		
		return true;
	}


	@Override
	public HashMap<String, String> getResults() throws Throwable {
		// TODO send discovery results to RAN
		return null;
	}

	
}
