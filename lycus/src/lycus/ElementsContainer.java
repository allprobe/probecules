package lycus;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import Elements.BaseElement;
import Elements.DiskElement;
import Elements.NicElement;
import GlobalConstants.Enums.DiscoveryElementType;
import Model.ElementModel;
import Model.UpdateModel;
import Probes.DiscoveryProbe;
import Probes.NicProbe;
import Probes.DiskProbe;
import Results.DiscoveryResult;
import Utils.GeneralFunctions;
import Utils.Logit;
import GlobalConstants.Enums.HostType;

public class ElementsContainer {
	private ConcurrentHashMap<String, ConcurrentHashMap<String, BaseElement>> nicElements;// ConcurrentHashMap<runnableProbeId,ConcurrentHashMap<elementName,NicElement>>
	private ConcurrentHashMap<String, ConcurrentHashMap<String, BaseElement>> diskElements;// ConcurrentHashMap<runnableProbeId,ConcurrentHashMap<elementName,DiskElement>>
	// private ConcurrentHashMap<String,NicElement> ProcessElements;
	private static ElementsContainer instance;

	private ElementsContainer() {
		nicElements = new ConcurrentHashMap<String, ConcurrentHashMap<String, BaseElement>>();
		diskElements = new ConcurrentHashMap<String, ConcurrentHashMap<String, BaseElement>>();
	}

	public static ElementsContainer getInstance() {
		if (instance == null)
			instance = new ElementsContainer();
		return instance;
	}

	public boolean isDiskElementsChanged(DiscoveryResult discoveryResult) {
		Map<String, BaseElement> currentElements = diskElements.get(discoveryResult.getRunnableProbeId());
		// discoveryResult.getElements().size() != 0
		if (discoveryResult == null)
			return false;
		if (currentElements == null) {
			ConcurrentHashMap<String, BaseElement> map = new ConcurrentHashMap<String, BaseElement>(
					(Map) discoveryResult.getElements());
			diskElements.put(discoveryResult.getRunnableProbeId(), map);
			return true;
		}
		Map<String, BaseElement> newElements = discoveryResult.getElements();

		if (currentElements.size() != newElements.size()) {
			ConcurrentHashMap<String, BaseElement> newMap = new ConcurrentHashMap<String, BaseElement>(
					(Map) discoveryResult.getElements());
			updateStatuses(currentElements, newMap);
			diskElements.put(discoveryResult.getRunnableProbeId(), newMap);
			return true;
		}
		for (BaseElement newElement : newElements.values()) {
			if (currentElements.get(newElement.getName()) == null
					|| !currentElements.get(newElement.getName()).isIdentical(newElement)) {
				ConcurrentHashMap<String, BaseElement> newMap = new ConcurrentHashMap<String, BaseElement>(
						(Map) discoveryResult.getElements());
				updateStatuses(currentElements, newMap);
				diskElements.put(discoveryResult.getRunnableProbeId(), newMap);
				return true;
			}
		}
		return false;
	}

	// private void applyNicStatusesOnNewResult(DiscoveryResult discoveryResult)
	// {
	// ConcurrentHashMap<String, NicElement> existing =
	// nicElements.get(discoveryResult.getRunnableProbeId());
	// HashMap<String, BaseElement> lastResultElements =
	// discoveryResult.getElements();
	// for (BaseElement baseElement : lastResultElements.values()) {
	// baseElement.setActive(existing.get(baseElement.getName()).isActive());
	// }
	// }

	public boolean isNicElementsChanged(DiscoveryResult discoveryResult) {
		Map<String, BaseElement> currentElements = nicElements.get(discoveryResult.getRunnableProbeId());
		// discoveryResult.getElements().size() != 0
		if (discoveryResult == null)
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
			updateStatuses(currentElements, newMap);
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
		for (Map.Entry<String, BaseElement> element : newMap.entrySet()) {
			boolean oldStatus = currentElements.get(element.getKey()) == null ? false
					: currentElements.get(element.getKey()).isActive();
			element.getValue().setActive(oldStatus);
		}
	}

	public void addElement(String userId, String runnableProbeId, BaseElement element) {
		if (element instanceof NicElement) {
			if (runnableProbeId
					.contains("74cda666-3d85-4e56-a804-9d53c4e16259@discovery_777938b0-e4b0-4ec6-b0f2-ea880a0c09ef"))
				Logit.LogDebug("BREAKPOINT");
			addNicElement(userId, runnableProbeId, element);
			if (element.isActive())
				runNicElement(userId, runnableProbeId, element);
		}
		if (element instanceof DiskElement) {
			addDiskElement(userId, runnableProbeId, element);
			if (element.isActive())
				runDiskElement(userId, runnableProbeId, element);
		}
	}

	public void removeElement(String userId, String runnableProbeId, BaseElement element) {
		if (element instanceof NicElement) {
			if (runnableProbeId
					.contains("74cda666-3d85-4e56-a804-9d53c4e16259@discovery_777938b0-e4b0-4ec6-b0f2-ea880a0c09ef"))
				Logit.LogDebug("BREAKPOINT");
			stopElement(userId, runnableProbeId, element);
		}
		if (element instanceof DiskElement) {
			stopElement(userId, runnableProbeId, element);
		}
	}

	private void removeDiskElement(String userId, String runnableProbeId, BaseElement element) {
		ConcurrentHashMap<String, BaseElement> elementMap = diskElements.get(runnableProbeId);
		if (elementMap == null)
			return;
		elementMap.remove(element.getName());
	}

	private void stopElement(String userId, String runnableProbeId, BaseElement element) {
		DiscoveryProbe probe = (DiscoveryProbe) UsersManager.getUser(userId).getTemplateProbes()
				.get(runnableProbeId.split("@")[2]);
		User user = probe.getUser();
		Host host = user.getHost(UUID.fromString(runnableProbeId.split("@")[1]));
		String elementRunnableProbeId = runnableProbeId + "@" + GeneralFunctions.Base64Encode(element.getName());
		RunnableProbe nicRunnableProbe = RunnableProbeContainer.getInstanece().get(elementRunnableProbeId);
		RunnableProbeContainer.getInstanece().remove(nicRunnableProbe);
	}

	private void removeNicElement(String userId, String runnableProbeId, BaseElement element) {
		ConcurrentHashMap<String, BaseElement> elementMap = nicElements.get(runnableProbeId);
		if (elementMap == null)
			return;
		elementMap.remove(element.getName());
	}

	private void runDiskElement(String userId, String runnableProbeId, BaseElement element) {
		if(UsersManager.getUser(userId)==null)
			return;
		DiscoveryProbe probe = (DiscoveryProbe) UsersManager.getUser(userId).getTemplateProbes()
				.get(runnableProbeId.split("@")[2]);
		if(probe==null)
			return;
		User user = probe.getUser();
		Host host = user.getHost(UUID.fromString(runnableProbeId.split("@")[1]));
		DiskProbe diskProbe = new DiskProbe(probe, (DiskElement) element);
		diskProbe.setActive(true);
		RunnableProbe diskRunnableProbe = new RunnableProbe(host, diskProbe);
		diskRunnableProbe.setActive(true);
		RunnableProbeContainer.getInstanece().add(diskRunnableProbe);
		// user.addRunnableProbe(nicRunnableProbe);
	}

	private void addDiskElement(String userId, String runnableProbeId, BaseElement element) {
		ConcurrentHashMap<String, BaseElement> elementMap = diskElements.get(runnableProbeId);
		if (elementMap == null)
			elementMap = new ConcurrentHashMap<String, BaseElement>();
		elementMap.put(element.getName(), (DiskElement) element);
		diskElements.put(runnableProbeId, elementMap);
	}

	private void runNicElement(String userId, String runnableProbeId, BaseElement element) {
		if(UsersManager.getUser(userId)==null)
			return;
		DiscoveryProbe probe = (DiscoveryProbe) UsersManager.getUser(userId).getTemplateProbes()
				.get(runnableProbeId.split("@")[2]);
		if(probe==null)
			return;
		User user = probe.getUser();
		Host host = user.getHost(UUID.fromString(runnableProbeId.split("@")[1]));
		NicProbe nicProbe = new NicProbe(probe, (NicElement) element);
		nicProbe.setActive(true);
		RunnableProbe nicRunnableProbe = new RunnableProbe(host, nicProbe);
		nicRunnableProbe.setActive(true);
		RunnableProbeContainer.getInstanece().add(nicRunnableProbe);
		// user.addRunnableProbe(nicRunnableProbe);
	}

	private void addNicElement(String userId, String runnableProbeId, BaseElement element) {
		ConcurrentHashMap<String, BaseElement> elementMap = nicElements.get(runnableProbeId);
		if (elementMap == null)
			elementMap = new ConcurrentHashMap<String, BaseElement>();
		elementMap.put(element.getName(), (NicElement) element);
		nicElements.put(runnableProbeId, elementMap);
	}

	public BaseElement getElement(String runnableProbeId, String elementName, DiscoveryElementType elementType) {
		ConcurrentHashMap<String, BaseElement> elementMap = null;
		if (elementType == elementType.ds) {
			elementMap = diskElements.get(runnableProbeId);
		} else if (elementType == elementType.bw) {
			elementMap = nicElements.get(runnableProbeId);
		}

		if (elementMap == null)
			return null;
		return elementMap.get(elementName);

	}

	public boolean updateElements(UpdateModel update) {
		String runnableProbeId = Utils.GeneralFunctions.getRunnableProbeId(update.template_id, update.host_id,
				update.probe_id);
		ConcurrentHashMap<String, BaseElement> elementMap = null;
		DiscoveryElementType elementType = null;
		BaseElement baseElement = null;

		if (update.elements == null)
			return true;
		for (ElementModel element : update.elements) {
			try {
				if (element.ifSpeed != null) {
					baseElement = getElement(runnableProbeId, element.name, elementType.bw);
					if (baseElement == null) {
						baseElement = new NicElement(element.index, element.name, HostType.valueOf(element.hostType),
								element.ifSpeed);
						addElement(update.user_id, runnableProbeId, baseElement);
					}
				} else if (element.hrStorageAllocationUnits != null) {
					baseElement = getElement(runnableProbeId, element.name, elementType.ds);
					if (baseElement == null) {
						baseElement = new DiskElement(element.index, element.name, element.active);
						addElement(update.user_id, runnableProbeId, baseElement);
					}
				}
				if (!baseElement.isActive() && element.active) {
					baseElement.setActive(element.active);
					addElement(update.user_id, runnableProbeId, baseElement);
				} else if (baseElement.isActive() && !element.active) {
					baseElement.setActive(element.active);
					removeElement(update.user_id, runnableProbeId, baseElement);
				}
			} catch (SecurityException se) {
				Logit.LogError("ElementsContainer - updateElements()",
						"Unable to create or retrieve element " + element.name);
			}
		}
		return true;
	}
}
