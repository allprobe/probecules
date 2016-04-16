package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Elements.BaseElement;
import Elements.NicElement;
import Results.BaseResult;
import Results.DiscoveryResult;

public class ElementsContainer {
	private ConcurrentHashMap<String, ConcurrentHashMap<String, NicElement>> nicElements;// ConcurrentHashMap<runnableProbeId,ConcurrentHashMap<elementName,NicElement>>
	// private ConcurrentHashMap<String,NicElement> DiskElements;
	// private ConcurrentHashMap<String,NicElement> ProcessElements;
	private static ElementsContainer instance;

	private ElementsContainer() {
		nicElements = new ConcurrentHashMap<String, ConcurrentHashMap<String, NicElement>>();
	}

	public static ElementsContainer getInstance() {
		if (instance == null)
			instance = new ElementsContainer();
		return instance;
	}

	public void addResult(DiscoveryResult discoveryResult)
	{
		switch (discoveryResult.getElementsType()) {
		case nics:
			if(isNicElementsChanged(discoveryResult))
			{
				applyNicStatusesOnNewResult(discoveryResult);
				ResultsContainer.getInstance().addResult(discoveryResult);
			}
		case disks:
			break;
		}
	}
	
	private void applyNicStatusesOnNewResult(DiscoveryResult discoveryResult) {
		ConcurrentHashMap<String, NicElement> existing=nicElements.get(discoveryResult.getRunnableProbeId());
		HashMap<String, BaseElement> lastResultElements=discoveryResult.getElements();
		for(BaseElement baseElement:lastResultElements.values())
		{
			baseElement.setActive(existing.get(baseElement.getName()).isActive());
		}
	}


	private boolean isNicElementsChanged(DiscoveryResult discoveryResult) {
		Map<String, NicElement> currentElements = nicElements.get(discoveryResult.getRunnableProbeId());

		if (currentElements == null && discoveryResult != null && discoveryResult.getElements().size() != 0) {
			ConcurrentHashMap<String, NicElement> map = new ConcurrentHashMap<String, NicElement>(
					(Map) discoveryResult.getElements());
			nicElements.put(discoveryResult.getRunnableProbeId(), map);
			return true;
		}
		Map<String, BaseElement> newElements = discoveryResult.getElements();

		if (currentElements.size() != newElements.size()) {
			ConcurrentHashMap<String, NicElement> map = new ConcurrentHashMap<String, NicElement>(
					(Map) discoveryResult.getElements());
			nicElements.put(discoveryResult.getRunnableProbeId(), map);
			return true;
		}
		for (BaseElement newElement : newElements.values()) {
			if (currentElements.get(newElement.getName()) == null
					|| !currentElements.get(newElement.getName()).isIdentical(newElement)) {
				ConcurrentHashMap<String, NicElement> map = new ConcurrentHashMap<String, NicElement>(
						(Map) discoveryResult.getElements());
				nicElements.put(discoveryResult.getRunnableProbeId(), map);
				return true;
			}
		}
		return false;
	}

	public void addElement(String runnableProbeId,BaseElement element)
	{
		if(element instanceof NicElement)
		{
			ConcurrentHashMap<String, NicElement> elementMap=nicElements.get(runnableProbeId);
			if(elementMap==null)
				elementMap=new ConcurrentHashMap<String, NicElement>();
			elementMap.put(element.getName(), (NicElement)element);
			nicElements.put(runnableProbeId, elementMap);
		}
		
	}
	
}
