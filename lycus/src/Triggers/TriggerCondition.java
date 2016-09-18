package Triggers;

import GlobalConstants.Enums;
import GlobalConstants.Enums.Condition;
import GlobalConstants.Enums.LastType;
import GlobalConstants.Enums.ResultValueType;
import GlobalConstants.Enums;
import GlobalConstants.XvalueUnit;
import GlobalConstants.Enums.Function;

public class TriggerCondition {
	private Condition condition; 
	private String xValue;
	private ResultValueType elementType;
	private XvalueUnit xvalueUnit;
	private Function function;
	private Integer nValue;
	private LastType last_type;
	
	public TriggerCondition(String condition, String xValue, String function, String elementType, String xvalueUnit, String nVlaue, String lastType) {
		setCondition(Enums.parseCondition(Integer.parseInt(condition)));
		setElementType(ResultValueType.valueOf(elementType));
		setXvalueUnit(XvalueUnit.valueOf(xvalueUnit));
		if (function != null)
			setFunction(Function.valueOf(function));
		else
			setFunction(null);
		if (nVlaue != null)
			setnValue(Integer.parseInt(nVlaue));
		else
			setnValue(null);
		setLast_type(LastType.valueOf(lastType));
		
		setxValue(xValue);
	}	

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public String getxValue() {
		return xValue;
	}

	public void setxValue(String xValue) {
		this.xValue = xValue;
	}

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public ResultValueType getElementType() {
		return elementType;
	}

	public void setElementType(ResultValueType elementType) {
		this.elementType = elementType;
	}

	public XvalueUnit getXvalueUnit() {
		return xvalueUnit;
	}

	public void setXvalueUnit(XvalueUnit xvalueUnit) {
		this.xvalueUnit = xvalueUnit;
	}

	public Integer getnValue() {
		return nValue;
	}

	public void setnValue(Integer nValue) {
		this.nValue = nValue;
	}

	public LastType getLast_type() {
		return last_type;
	}

	public void setLast_type(LastType last_type) {
		this.last_type = last_type;
	}

}
