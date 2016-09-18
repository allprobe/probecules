package Probes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import GlobalConstants.Enums;
import GlobalConstants.Enums.DiscoveryElementType;
import Model.ConditionUpdateModel;
import Model.UpdateModel;
import Model.UpdateValueModel;
import NetConnection.NetResults;
import Results.BaseResult;
import Results.DiscoveryResult;
import lycus.Host;
import Triggers.Trigger;
import Triggers.TriggerCondition;
import lycus.User;

public class DiscoveryProbe extends BaseProbe {
	private Enums.DiscoveryElementType type;
	private int elementsInterval;
	private List<Trigger> bandWidthTriggers;
	private List<Trigger> diskSpaceTriggers;

	public DiscoveryProbe(User user, String probe_id, UUID template_id, String name, int interval, float multiplier,
			boolean status, Enums.DiscoveryElementType discoveryType, int elementsInterval) {
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

	public int getElementInterval() {
		return elementsInterval;
	}

	public void setElementInterval(int elementInterval) {
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
		updateTriggers(updateValue);

		return true;
	}

	private void updateTriggers(UpdateValueModel updateValue) {
		if (updateValue.key.discovery_type.equals("bw")) {
			bandWidthTriggers = new ArrayList<Trigger>();
		} else if (updateValue.key.discovery_type.equals("ds")) {
			diskSpaceTriggers = new ArrayList<Trigger>();
		} else if (updateValue.key.discovery_type.equals("pc")) {
			// Todo: implement
		}

		ConditionUpdateModel[] conditions = updateValue.conditions;
		for (int index = 0; index < conditions.length; index++) {
			TriggerCondition condition = new TriggerCondition(conditions[index].condition,
					conditions[index].xvalue, conditions[index].function, conditions[index].results_vector_type, conditions[index].xvalue_unit, conditions[index].nvalue, conditions[index].last_type);
			ArrayList<TriggerCondition> triggerConditions = new ArrayList<TriggerCondition>();
			triggerConditions.add(condition);

		}
	}
}
