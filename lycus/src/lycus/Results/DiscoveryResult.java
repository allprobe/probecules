package lycus.Results;

import java.util.HashMap;
import java.util.List;

import lycus.Trigger;
import lycus.Elements.BaseElement;
import lycus.Elements.NicElement;
import lycus.GlobalConstants.Enums;
import lycus.Utils.JsonUtil;

public class DiscoveryResult extends BaseResult {

	private List<BaseElement> elements;

	public DiscoveryResult(String runnableProbeId,long timestamp,List<BaseElement> elements) {
		super(runnableProbeId,timestamp);
		this.elements=elements;
	}
	
	public DiscoveryResult(String runnableProbeId) {
		super(runnableProbeId);
	}

//	@Override
//	public synchronized void acceptResults(ArrayList<Object> results) throws Exception {
//		super.acceptResults(results);
//
//		String rpStr = this.getRp().getRPString();
//		if (rpStr.contains("discovery_6b54463e-fe1c-4e2c-a090-452dbbf2d510"))
//			System.out.println("BREAKPOINT");
//
//		HashMap<String, BaseElement> lastScanElements = null;
//
//		switch (((DiscoveryProbe) this.getRp().getProbe()).getType()) {
//		case nics:
//			lastScanElements = this.convertNicsWalkToIndexes((HashMap<String, String>) results.get(1),
//					(Enums.HostType) results.get(2));
//			break;
//		case disks:
//			lastScanElements = this.convertDisksWalkToIndexes((HashMap<String, String>) results.get(1),
//					(Enums.HostType) results.get(2));
//			break;
//		}
//
//		long timestamp = (long) results.get(0);
//
//		boolean sameElements = checkForElementsChanges(lastScanElements, timestamp);
//	}


	

	@Override
	public void checkIfTriggerd(HashMap<String,Trigger> triggers) throws Exception {
		super.checkIfTriggerd(triggers);
	}
	@Override
	public String getResultString() {
		return JsonUtil.ToJson(this.elements).toString();
	}
	// returns true if there is any change made on the host elements
//	private boolean checkForElementsChanges(HashMap<String, BaseElement> lastScanElements, long timestamp) {
//		HashMap<BaseElement, Enums.ElementChange> elementsChanges = new HashMap<BaseElement, Enums.ElementChange>();
//
//		if (this.getCurrentElements() == null) {
//			for (Map.Entry<String, BaseElement> lastElement : lastScanElements.entrySet()) {
//				elementsChanges.put(lastElement.getValue(), ElementChange.addedElement);
//			}
//			this.setElementsChanges(elementsChanges);
//			this.setCurrentElements(lastScanElements);
//			this.setLastTimestamp(timestamp);
//			return true;
//		}
//
//		for (Map.Entry<String, BaseElement> lastElement : lastScanElements.entrySet()) {
//			if (this.getCurrentElements().get(lastElement.getKey()) == null) {
//				elementsChanges.put(lastElement.getValue(), ElementChange.addedElement);
//				continue;
//			}
//			if (this.getCurrentElements().get(lastElement.getKey()).isIdentical(lastElement.getValue()))
//				continue;
//			elementsChanges.put(lastElement.getValue(), ElementChange.indexElementChanged);
//			this.getCurrentElements().get(lastElement.getKey()).setIndex(lastElement.getValue().getIndex());
//		}
//
//		Set<String> newElements = lastScanElements.keySet();
//
//		// check for removed elements
//		for (Map.Entry<String, BaseElement> currentElement : this.getCurrentElements().entrySet()) {
//			if (!newElements.contains(currentElement.getKey())) {
//				elementsChanges.put(currentElement.getValue(), ElementChange.removedElement);
//				currentElement.getValue().getUser().removeDiscoveryElement(currentElement.getValue());
//			}
//		}
//
//		return true;
//	}

//	@Override
//	public HashMap<String, String> getResults() throws Throwable {
//		HashMap<String, String> results = super.getResults();
//		JSONArray rawResults = new JSONArray();
//		rawResults.add(6);
//
//		for (Map.Entry<BaseElement, ElementChange> elementChange : this.getElementsChanges().entrySet()) {
//			JSONObject changeJson = new JSONObject();
//			changeJson.put(elementChange.getValue(), elementChange.getKey());
//			rawResults.add(changeJson);
//		}
//
//		results.put("RAW@elements_map@" + this.getLastTimestamp(), rawResults.toJSONString());
//		this.setElementsChanges(null);
//		this.setLastTimestamp(null);
//		return results;
//	}
}
