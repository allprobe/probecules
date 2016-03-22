package lycus.Probes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lycus.GlobalConstants.Enums;
import lycus.Results.BaseResult;
import lycus.Results.DiscoveryResult;
import lycus.Results.SnmpResult;
import lycus.Utils.GeneralFunctions;
import lycus.Utils.Logit;
import lycus.GlobalConstants.Enums.ElementChange;
import lycus.GlobalConstants.Enums.HostType;
import lycus.Host;
import lycus.User;
import lycus.Elements.BaseElement;
import lycus.Elements.NicElement;

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

		BaseResult results = null;
		try {
			switch (this.getType()) {
			case nics:
				results = this.checkForBandwidthElements(h);
				break;
			case disks:
				results = this.checkForDisksElements(h);
				break;
			}
		} catch (Throwable th) {
			Logit.LogError("DiscoveryProbe - Check",
					"Faild to run runnable probe check for: " + h.getHostId().toString() + "@" + this.getProbe_id());
		}

		return results;
	}

	private BaseResult checkForDisksElements(Host h) {
		// TODO Auto-generated method stub
		return null;
	}

	private BaseResult checkForBandwidthElements(Host h) {
		long checkTime;
		String ifAll = "1.3.6.1.2.1.2.2.1";
		String sysDescr = "1.3.6.1.2.1.1.1.0";
		Map<String, String> ifDescrResults = null;
		Map<String, SnmpResult> sysDescrResults = null;
		int snmpVersion = h.getSnmpTemp().getVersion();
		checkTime = System.currentTimeMillis();
//		if (snmpVersion == 2) {
//			ifDescrResults = Net.Snmp2Walk(h.getHostIp(), h.getSnmpTemp().getPort(), h.getSnmpTemp().getTimeout(),
//					h.getSnmpTemp().getCommunityName(), ifAll);
//
//			ArrayList<String> oids = new ArrayList<String>();
//			oids.add(sysDescr);
//			sysDescrResults = Net.Snmp2GETBULK(h.getHostIp(), h.getSnmpTemp().getPort(), h.getSnmpTemp().getTimeout(),
//					h.getSnmpTemp().getCommunityName(), oids);
//		} else if (snmpVersion == 3) {
//			ifDescrResults = Net.Snmp3Walk(h.getHostIp(), h.getSnmpTemp().getPort(), h.getSnmpTemp().getTimeout(),
//					h.getSnmpTemp().getUserName(), h.getSnmpTemp().getAuthPass(), h.getSnmpTemp().getAlgo(),
//					h.getSnmpTemp().getCryptPass(), h.getSnmpTemp().getCryptType(), ifAll);
//			ArrayList<String> oids = new ArrayList<String>();
//			oids.add(sysDescr);
//			sysDescrResults = Net.Snmp3GETBULK(h.getHostIp(), h.getSnmpTemp().getPort(), h.getSnmpTemp().getTimeout(),
//					h.getSnmpTemp().getUserName(), h.getSnmpTemp().getAuthPass(), h.getSnmpTemp().getAlgo(),
//					h.getSnmpTemp().getCryptPass(), h.getSnmpTemp().getCryptType(), oids, 
//					GeneralFunctions.getRunnableProbeId(getTemplate_id(), h.getHostId(), getProbe_id()));
//		}
		if (ifDescrResults == null)
			return null;

		
		//TODO: What to do with this information?
		Enums.HostType hostType = this.getHostType(sysDescrResults.get(sysDescr).getStringData());
		ArrayList<Object> results = new ArrayList<Object>();
		results.add(checkTime);
		results.add(ifDescrResults);
		results.add(hostType);

		String runnableProbeId = GeneralFunctions.getRunnableProbeId(getTemplate_id(), h.getHostId(), getProbe_id());
		DiscoveryResult discoveryResult = new DiscoveryResult(runnableProbeId);

		HashMap<String, BaseElement> lastScanElements = null;
		
		switch (this.getType()) {
		case nics:
			lastScanElements = this.convertNicsWalkToIndexes((HashMap<String, String>) results.get(1),
					(Enums.HostType) results.get(2), discoveryResult);
			break;
		case disks:
			lastScanElements = this.convertDisksWalkToIndexes((HashMap<String, String>) results.get(1),
					(Enums.HostType) results.get(2));
			break;
		}

		
//		discoveryResult.setCurrentElements(lastScanElements);
		
		
		
		HashMap<BaseElement, Enums.ElementChange> elementsChanges = new HashMap<BaseElement, Enums.ElementChange>();
		
		
		if (discoveryResult.getCurrentElements() == null) {
			for (Map.Entry<String, BaseElement> lastElement : lastScanElements.entrySet()) {
				elementsChanges.put(lastElement.getValue(), ElementChange.addedElement);
			}
			discoveryResult.setElementsChanges(elementsChanges);
			discoveryResult.setCurrentElements(lastScanElements);
			discoveryResult.setLastTimestamp(System.currentTimeMillis());
			return discoveryResult;
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

		return discoveryResult;
	}

	private HostType getHostType(String string) {
		if (string.contains("Linux"))
			return Enums.HostType.Linux;
		if (string.contains("Windows"))
			return Enums.HostType.Windows;
		return null;
	}

	private HashMap<String, BaseElement> convertDisksWalkToIndexes(HashMap<String, String> hashMap, HostType hostType) {
		// TODO DiscoveryResults.convertDisksWalkToIndexes
		return null;
	}

	private HashMap<String, BaseElement> convertNicsWalkToIndexes(HashMap<String, String> nicsWalk, HostType hostType, DiscoveryResult result) {
		HashMap<String, BaseElement> lastElements = new HashMap<String, BaseElement>();
		if (hostType == null)
			return null;
		for (Map.Entry<String, String> entry : nicsWalk.entrySet()) {
			if (!entry.getKey().toString().contains("1.3.6.1.2.1.2.2.1.1."))
				continue;
			int index = Integer.parseInt(entry.getValue());
			if (index == 0) {
				Logit.LogError("DiscoveryResults - convertNicsWalkToIndexes", "snmp OID index cannot be zero! ---" + result.getRunnableProbeId());
				continue;
			}

			String name;
			long ifSpeed;

			ifSpeed = Long.parseLong(nicsWalk.get("1.3.6.1.2.1.2.2.1.5." + index));
			switch (hostType) {
			case Windows:
				name = GeneralFunctions.convertHexToString(nicsWalk.get("1.3.6.1.2.1.2.2.1.2." + index));
				break;
			case Linux:
				name = nicsWalk.get("1.3.6.1.2.1.2.2.1.2." + index);
				break;
			default:
				return null;
			}
			
			//TODO: get the probe - is it currentprobe?
//			NicElement nicElement = new NicElement(result.getRp().getProbe().getUser(),
//					result.getRp().getProbe().getProbe_id(), result.getRp().getProbe().getTemplate_id(), name,
//					result.getRp().getProbe().getInterval(), result.getRp().getProbe().getMultiplier(),
//					result.getRp().getProbe().isActive(), index, ifSpeed, hostType);
			
			//TODO: this lines are not currect has to be the probe of the result - check with Roi
			NicElement nicElement = new NicElement(getUser(), getProbe_id(), getTemplate_id(), name,
					 getInterval(), getMultiplier(),
					 isActive(), index, ifSpeed, hostType);

			lastElements.put(name, nicElement);
		}

		if (lastElements.size() == 0)
			return null;

		return lastElements;
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
