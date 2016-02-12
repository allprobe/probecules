package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.snmp4j.smi.OID;

import lycus.Enums.SnmpStoreAs;

public class RunnableDiscoveryProbeResults extends RunnableProbeResults {

	private HashMap<Integer,DiscoveryElement> elements;
	
	public RunnableDiscoveryProbeResults(RunnableProbe rp) {
		super(rp);
		setElements(new HashMap<Integer,DiscoveryElement>());
		}
	
	public HashMap<Integer,DiscoveryElement> getElements() {
		return elements;
	}

	public void setElements(HashMap<Integer,DiscoveryElement> elements) {
		this.elements = elements;
	}

	@Override
	public synchronized void acceptResults(ArrayList<Object> results) throws Exception{
		super.acceptResults(results);
		
		HashMap<Integer,DiscoveryElement> lastScanElements=null;
		
		switch (((DiscoveryProbe)this.getRp().getProbe()).getType()) {
		case nics:
			lastScanElements = this.convertSnmpWalkToNicsElements(results);
			break;
		case disks:
			lastScanElements = this.acceptResultsForDisks(results);
			break;
		}
	
		boolean theSame=this.isElementsIdentical(lastScanElements);
		if(theSame)
			return;
		this.stopElementsThreads();
		this.mergeNewElements();
		this.startElementsThreads();
			
	}

	private void startElementsThreads() {
		// TODO Auto-generated method stub
		
	}

	private void mergeNewElements() {
		// TODO Auto-generated method stub
		
	}

	private void stopElementsThreads() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void checkIfTriggerd() throws Exception {
		super.checkIfTriggerd();
	}
	
	
	private HashMap<Integer,DiscoveryElement> acceptResultsForDisks(ArrayList<Object> results) {
		// TODO Auto-generated method stub
		return null;
	}

	private HashMap<Integer,DiscoveryElement> convertSnmpWalkToNicsElements(ArrayList<Object> results) {
		
		HashMap<Integer,DiscoveryElement> lastElements=new HashMap<Integer,DiscoveryElement>();
		
		Long ifInOctets=null;
		Long ifOutOctets=null;
		Map<String,String> walkResults=(Map<String,String>)results.get(0);
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

			DiscoveryNicElement element=new DiscoveryNicElement(this,index,name);
			lastElements.put(index, element);
		}

		if(lastElements.size()==0)
			return null;
		
		return lastElements;
		
			
	}
	private boolean isElementsIdentical(HashMap<Integer,DiscoveryElement> lastElements) {
		
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
		for(Map.Entry<Integer, DiscoveryElement> newElement:lastElements.entrySet())
		{
			if(!this.getElements().get(newElement.getKey()).getName().equals(newElement.getValue().getName()))
				return false;
		}
		return true;
	}

	private boolean[] ifaceGetIndexes(Map<String,String> walkResults)
	{
//		for()
		return null;
	}
}
