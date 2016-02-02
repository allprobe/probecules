package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
		
		switch (((DiscoveryProbe)this.getRp().getProbe()).getType()) {
		case nics:
			results = this.acceptResultsForNics(results);
			break;
		case disks:
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

	private ArrayList<Object> acceptResultsForNics(ArrayList<Object> results) {
		
		HashMap<Integer,DiscoveryElement> lastElements=new HashMap<Integer,DiscoveryElement>();
		
		Map<String,String> walkResults=(Map<String,String>)results.get(0);
		for(Map.Entry<String, String> entry:walkResults.entrySet())
		{
			if(!entry.getKey().toString().contains("1.3.6.1.2.1.2.2.1.1"))
			continue;
			int index=Integer.parseInt(entry.getValue());
			String name=walkResults.get("1.3.6.1.2.1.2.2.1.2."+index);
			long ifInOctets=Long.parseLong(walkResults.get("1.3.6.1.2.1.2.2.1.10."+index));
			long ifOutOctets=Long.parseLong(walkResults.get("1.3.6.1.2.1.2.2.1.16."+index));
			DiscoveryNicElement element=new DiscoveryNicElement(this,"1.3.6.1.2.1.2.2.1",index,name);
//			element.getValues().put("IN", ifInOctets);			
//			element.getValues().put("OUT",ifOutOctets);
			lastElements.put(index, element);
		}
		
		boolean theSame=this.isElementsIdentical(lastElements);
//		if(theSame)
			
		
		return null;
	}
	private boolean isElementsIdentical(HashMap<Integer,DiscoveryElement> lastElements) {
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
