package lycus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import Elements.BaseElement;
import Elements.NicElement;
import Probes.DiscoveryProbe;
import Probes.NicProbe;
import Results.DiscoveryResult;
import Utils.GeneralFunctions;
import Utils.Logit;

public class ElementsContainer {
	private ConcurrentHashMap<String, ConcurrentHashMap<String, BaseElement>> nicElements;// ConcurrentHashMap<runnableProbeId,ConcurrentHashMap<elementName,NicElement>>
	// private ConcurrentHashMap<String,NicElement> DiskElements;
	// private ConcurrentHashMap<String,NicElement> ProcessElements;
	private static ElementsContainer instance;

	private ElementsContainer() {
		nicElements = new ConcurrentHashMap<String, ConcurrentHashMap<String, BaseElement>>();
	}

	public static ElementsContainer getInstance() {
		if (instance == null)
			instance = new ElementsContainer();
		return instance;
	}

	public void addResult(DiscoveryResult discoveryResult) {
		switch (discoveryResult.getElementsType()) {
		case bw:
			if (isNicElementsChanged(discoveryResult)) {
				ResultsContainer.getInstance().addResult(discoveryResult);
			}
		case ds:
			break;
		}
	}

//	private void applyNicStatusesOnNewResult(DiscoveryResult discoveryResult) {
//		ConcurrentHashMap<String, NicElement> existing = nicElements.get(discoveryResult.getRunnableProbeId());
//		HashMap<String, BaseElement> lastResultElements = discoveryResult.getElements();
//		for (BaseElement baseElement : lastResultElements.values()) {
//			baseElement.setActive(existing.get(baseElement.getName()).isActive());
//		}
//	}

	private boolean isNicElementsChanged(DiscoveryResult discoveryResult) {
		Map<String, BaseElement> currentElements = nicElements.get(discoveryResult.getRunnableProbeId());
//	    discoveryResult.getElements().size() != 0
		if(discoveryResult==null)
			return false;
		if (currentElements == null) {
			ConcurrentHashMap<String, BaseElement> map = new ConcurrentHashMap<String, BaseElement>(
					(Map) discoveryResult.getElements());
			nicElements.put(discoveryResult.getRunnableProbeId(), map);
			return true;
		}
		Map<String, BaseElement> newElements = discoveryResult.getElements();

		if (currentElements.size() != newElements.size()) {
			ConcurrentHashMap<String, BaseElement> newMap = new ConcurrentHashMap<String, BaseElement>(
					(Map) discoveryResult.getElements());
			updateStatuses(currentElements,newMap);
			nicElements.put(discoveryResult.getRunnableProbeId(), newMap);
			return true;
		}
		for (BaseElement newElement : newElements.values()) {
			if (currentElements.get(newElement.getName()) == null
					|| !currentElements.get(newElement.getName()).isIdentical(newElement)) {
				ConcurrentHashMap<String, BaseElement> newMap = new ConcurrentHashMap<String, BaseElement>(
						(Map) discoveryResult.getElements());
				updateStatuses(currentElements, newMap);
				nicElements.put(discoveryResult.getRunnableProbeId(), newMap);
				return true;
			}
		}
		return false;
	}

	private void updateStatuses(Map<String, BaseElement> currentElements,
			ConcurrentHashMap<String, BaseElement> newMap) {
		for(Map.Entry<String, BaseElement> element:newMap.entrySet())
		{
			boolean oldStatus=currentElements.get(element.getKey())==null?false:currentElements.get(element.getKey()).isActive();
			element.getValue().setActive(oldStatus);
		}
	}

	public void addElement(String userId, String runnableProbeId, BaseElement element) {
		if (element instanceof NicElement) {
			if(runnableProbeId.contains("74cda666-3d85-4e56-a804-9d53c4e16259@discovery_777938b0-e4b0-4ec6-b0f2-ea880a0c09ef"))
				Logit.LogDebug("BREAKPOINT");
			addNicElement(userId,runnableProbeId, element);
			if (element.isActive())
				runNicElement(userId,runnableProbeId, element);
		}

	}

	private void runNicElement(String userId, String runnableProbeId, BaseElement element) {
		DiscoveryProbe probe = (DiscoveryProbe)UsersManager.getUser(userId).getTemplateProbes().get(runnableProbeId.split("@")[2]);
		User user = probe.getUser();
		Host host=user.getHost(UUID.fromString(runnableProbeId.split("@")[1]));
		NicProbe nicProbe = new NicProbe(probe, (NicElement) element);
		RunnableProbe nicRunnableProbe=new RunnableProbe(host, nicProbe);
		user.addRunnableProbe(nicRunnableProbe);
	}

	private void addNicElement(String userId, String runnableProbeId, BaseElement element) {
		ConcurrentHashMap<String, BaseElement> elementMap = nicElements.get(runnableProbeId);
		if (elementMap == null)
			elementMap = new ConcurrentHashMap<String, BaseElement>();
		elementMap.put(element.getName(), (NicElement) element);
		nicElements.put(runnableProbeId, elementMap);
	}

}
