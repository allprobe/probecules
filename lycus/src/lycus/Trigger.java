package lycus;

import java.util.ArrayList;

import GlobalConstants.SnmpUnit;
import GlobalConstants.TriggerSeverity;
import Probes.BaseProbe;

public class Trigger implements Cloneable {
	private String triggerId;
	private String name;
	private BaseProbe probe;
	private TriggerSeverity svrty;
	private boolean status;
	private String elementType;
	private SnmpUnit unit;
	private ArrayList<TriggerCondition> condtions;
	private boolean isTriggered;

	public Trigger(String triggerId, String name, BaseProbe probe, TriggerSeverity svrty, boolean status,
			String elementType, SnmpUnit unit, ArrayList<TriggerCondition> condtions) {
		this.triggerId = triggerId;
		this.name = name;
		this.probe = probe;
		this.svrty = svrty;
		this.status = status;
		this.elementType = elementType;
		this.condtions = condtions;
		this.unit = unit;

	}

	public boolean isTriggered() {
		return isTriggered;
	}

	public void setTriggered(boolean isTriggered) {
		this.isTriggered = isTriggered;
	}

	public String getTriggerId() {
		return triggerId;
	}

	public void setTriggerId(String triggerId) {
		this.triggerId = triggerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BaseProbe getProbe() {
		return probe;
	}

	public void setProbe(BaseProbe probe) {
		this.probe = probe;
	}

	public TriggerSeverity getSvrty() {
		return svrty;
	}

	public void setSvrty(TriggerSeverity svrty) {
		this.svrty = svrty;
	}

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getElementType() {
		return elementType;
	}

	public void setElementType(String elementType) {
		this.elementType = elementType;
	}

	public SnmpUnit getUnit() {
		return unit;
	}

	public void setUnit(SnmpUnit unit) {
		this.unit = unit;
	}

	public ArrayList<TriggerCondition> getCondtions() {
		return condtions;
	}

	public void setCondtions(ArrayList<TriggerCondition> condtions) {
		this.condtions = condtions;
	}

	public boolean triggered() {
		return true;
	}

	protected Trigger clone() throws CloneNotSupportedException {
		Trigger trigger = (Trigger) super.clone();
		return trigger;
	}

}
