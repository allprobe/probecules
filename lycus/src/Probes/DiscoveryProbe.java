package Probes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import GlobalConstants.Constants;
import GlobalConstants.Enums;
import GlobalConstants.Enums.DiscoveryElementType;
import GlobalConstants.SnmpUnit;
import Model.DiscoveryTrigger;
import Model.KeyUpdateModel;
import Model.UpdateModel;
import Model.UpdateValueModel;
import NetConnection.NetResults;
import Results.BaseResult;
import Results.DiscoveryResult;
import lycus.Host;
import lycus.Trigger;
import lycus.TriggerCondition;
import lycus.User;
import lycus.UsersManager;

public class DiscoveryProbe extends BaseProbe {
	private Enums.DiscoveryElementType type;
	private long elementsInterval;
	private List<Trigger> bandWidthTriggers;
	private List<Trigger> diskSpaceTriggers;

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

	public boolean updateKeyValues(UpdateModel updateModel) {
		UpdateValueModel updateValue = updateModel.update_value;
		super.updateKeyValues(updateModel);
		setType(DiscoveryElementType.valueOf(updateValue.key.discovery_type));
		setElementInterval(updateValue.key.element_interval);
		updateTriggers(updateValue.key);

		return true;
	}

	private void updateTriggers(KeyUpdateModel key) {
		if (key.discovery_type.equals("bw")) {
			bandWidthTriggers = new ArrayList<Trigger>();
		} else if (key.discovery_type.equals("ds")) {
			diskSpaceTriggers = new ArrayList<Trigger>();
		} else if (key.discovery_type.equals("pc")) {
			// Todo: implement
		}

		DiscoveryTrigger[] triggers = key.discovery_triggers;
		for (int index = 0; index < triggers.length; index++) {
			TriggerCondition condition = new TriggerCondition(triggers[index].discovery_trigger_condition,
					triggers[index].discovery_trigger_xvalue, triggers[index].discovery_trigger_function, triggers[index].discovery_trigger_results_vector_type);
			ArrayList<TriggerCondition> conditions = new ArrayList<TriggerCondition>();
			conditions.add(condition);

			// Trigger trigger = new
			// Trigger(triggers[index].discovery_trigger_id, "", this,
			// UsersManager.getTriggerSev(triggers[index].discovery_trigger_severity),
			// true, key.discovery_type,
			// SnmpUnit.valueOf(triggers[index].discovery_trigger_unit),
			// conditions);

			// if (key.discovery_type.equals("bw")) {
			// bandWidthTriggers.add(trigger);
			// } else if (key.discovery_type.equals("ds")) {
			// diskSpaceTriggers.add(trigger);
			// } else if (key.discovery_type.equals("pc")) {
			// // Todo: implement
			// }

		}
	}

	// for (Map.Entry<String, BaseElement> lastElement :
	// lastScanElements.entrySet()) {
	// if (discoveryResult.getCurrentElements().get(lastElement.getKey()) ==
	// null) {
	// elementsChanges.put(lastElement.getValue(), ElementChange.addedElement);
	// continue;
	// }
	// if
	// (discoveryResult.getCurrentElements().get(lastElement.getKey()).isIdentical(lastElement.getValue()))
	// continue;
	// elementsChanges.put(lastElement.getValue(),
	// ElementChange.indexElementChanged);
	// discoveryResult.getCurrentElements().get(lastElement.getKey()).setIndex(lastElement.getValue().getIndex());
	// }
	//
	// Set<String> newElements = lastScanElements.keySet();
	//
	// // check for removed elements
	// for (Map.Entry<String, BaseElement> currentElement :
	// discoveryResult.getCurrentElements().entrySet()) {
	// if (!newElements.contains(currentElement.getKey())) {
	// elementsChanges.put(currentElement.getValue(),
	// ElementChange.removedElement);
	// currentElement.getValue().getUser().removeDiscoveryElement(currentElement.getValue());
	// }
	// }

	// long timestamp = (long)results.get(0);

	// boolean sameElements = checkForElementsChanges(lastScanElements,
	// timestamp);

	// private boolean checkForElementsChanges(HashMap<String, BaseElement>
	// lastScanElements, long timestamp, DiscoveryResults result) {
	// HashMap<BaseElement, Enums.ElementChange> elementsChanges = new
	// HashMap<BaseElement, Enums.ElementChange>();
	//
	// if (result.getCurrentElements() == null) {
	// for (Map.Entry<String, BaseElement> lastElement :
	// lastScanElements.entrySet()) {
	// elementsChanges.put(lastElement.getValue(), ElementChange.addedElement);
	// }
	// result.setElementsChanges(elementsChanges);
	// result.setCurrentElements(lastScanElements);
	// result.setLastTimestamp(timestamp);
	// return true;
	// }
	//
	// for (Map.Entry<String, BaseElement> lastElement :
	// lastScanElements.entrySet()) {
	// if (result.getCurrentElements().get(lastElement.getKey()) == null) {
	// elementsChanges.put(lastElement.getValue(), ElementChange.addedElement);
	// continue;
	// }
	// if
	// (result.getCurrentElements().get(lastElement.getKey()).isIdentical(lastElement.getValue()))
	// continue;
	// elementsChanges.put(lastElement.getValue(),
	// ElementChange.indexElementChanged);
	// result.getCurrentElements().get(lastElement.getKey()).setIndex(lastElement.getValue().getIndex());
	// }
	//
	// Set<String> newElements = lastScanElements.keySet();
	//
	// // check for removed elements
	// for (Map.Entry<String, BaseElement> currentElement :
	// result.getCurrentElements().entrySet()) {
	// if (!newElements.contains(currentElement.getKey())) {
	// elementsChanges.put(currentElement.getValue(),
	// ElementChange.removedElement);
	// currentElement.getValue().getUser().removeDiscoveryElement(currentElement.getValue());
	// }
	// }
	//
	// return true;
	// }

}
