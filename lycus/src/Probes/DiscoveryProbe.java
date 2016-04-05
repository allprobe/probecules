package Probes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import Elements.BaseElement;
import GlobalConstants.Enums;
import GlobalConstants.Enums.ElementChange;
import GlobalConstants.Enums.HostType;
import NetConnection.NetResults;
import Results.BaseResult;
import Results.DiscoveryResult;
import Results.SnmpResult;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.Host;
import lycus.User;

public class DiscoveryProbe extends BaseProbe {
	private Enums.DiscoveryElementType type;
	private long elementsInterval;

	public DiscoveryProbe(User user, String probe_id, UUID template_id, String name, long interval, float multiplier,
			boolean status, Enums.DiscoveryElementType discoveryType, long elementsInterval) {
		super(user, probe_id, template_id, name, interval, multiplier, status);
		this.type = discoveryType;
		this.elementsInterval = elementsInterval;
	}

	public Enums.DiscoveryElementType getType() {
		return type;
	}

	public void setType(Enums.DiscoveryElementType type) {
		this.type = type;
	}

	public long getElementInterval() {
		return elementsInterval;
	}

	public void setElementInterval(long elementInterval) {
		this.elementsInterval = elementInterval;
	}

	@Override
	public BaseResult getResult(Host h) {
		if (!h.isHostStatus())
			return null;

		DiscoveryResult discoveryResult = NetResults.getInstanece().getDiscoveryResult(h, this);
		
		return discoveryResult;
	}

	private BaseResult checkForDisksElements(Host h) {
		// TODO DiscoveryProbe.checkForDisksElements
		return null;
	}

	

		//TODO: discoveryResult.getCurrentElements() != null
//		for (Map.Entry<String, BaseElement> lastElement : lastScanElements.entrySet()) {
//			if (discoveryResult.getCurrentElements().get(lastElement.getKey()) == null) {
//				elementsChanges.put(lastElement.getValue(), ElementChange.addedElement);
//				continue;
//			}
//			if (discoveryResult.getCurrentElements().get(lastElement.getKey()).isIdentical(lastElement.getValue()))
//				continue;
//			elementsChanges.put(lastElement.getValue(), ElementChange.indexElementChanged);
//			discoveryResult.getCurrentElements().get(lastElement.getKey()).setIndex(lastElement.getValue().getIndex());
//		}
//
//		Set<String> newElements = lastScanElements.keySet();
//
//		// check for removed elements
//		for (Map.Entry<String, BaseElement> currentElement : discoveryResult.getCurrentElements().entrySet()) {
//			if (!newElements.contains(currentElement.getKey())) {
//				elementsChanges.put(currentElement.getValue(), ElementChange.removedElement);
//				currentElement.getValue().getUser().removeDiscoveryElement(currentElement.getValue());
//			}
//		}
		
//		long timestamp = (long)results.get(0);

//		boolean sameElements = checkForElementsChanges(lastScanElements, timestamp);


	

	private HashMap<String, BaseElement> convertDisksWalkToIndexes(HashMap<String, String> hashMap, HostType hostType) {
		// TODO DiscoveryResults.convertDisksWalkToIndexes
		return null;
	}

	
	
//	private boolean checkForElementsChanges(HashMap<String, BaseElement> lastScanElements, long timestamp, DiscoveryResults result) {
//		HashMap<BaseElement, Enums.ElementChange> elementsChanges = new HashMap<BaseElement, Enums.ElementChange>();
//
//		if (result.getCurrentElements() == null) {
//			for (Map.Entry<String, BaseElement> lastElement : lastScanElements.entrySet()) {
//				elementsChanges.put(lastElement.getValue(), ElementChange.addedElement);
//			}
//			result.setElementsChanges(elementsChanges);
//			result.setCurrentElements(lastScanElements);
//			result.setLastTimestamp(timestamp);
//			return true;
//		}
//
//		for (Map.Entry<String, BaseElement> lastElement : lastScanElements.entrySet()) {
//			if (result.getCurrentElements().get(lastElement.getKey()) == null) {
//				elementsChanges.put(lastElement.getValue(), ElementChange.addedElement);
//				continue;
//			}
//			if (result.getCurrentElements().get(lastElement.getKey()).isIdentical(lastElement.getValue()))
//				continue;
//			elementsChanges.put(lastElement.getValue(), ElementChange.indexElementChanged);
//			result.getCurrentElements().get(lastElement.getKey()).setIndex(lastElement.getValue().getIndex());
//		}
//
//		Set<String> newElements = lastScanElements.keySet();
//
//		// check for removed elements
//		for (Map.Entry<String, BaseElement> currentElement : result.getCurrentElements().entrySet()) {
//			if (!newElements.contains(currentElement.getKey())) {
//				elementsChanges.put(currentElement.getValue(), ElementChange.removedElement);
//				currentElement.getValue().getUser().removeDiscoveryElement(currentElement.getValue());
//			}
//		}
//
//		return true;
//	}

}
