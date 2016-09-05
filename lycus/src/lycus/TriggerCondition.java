package lycus;

import GlobalConstants.Enums.ResultValueType;
import GlobalConstants.SnmpUnit;

public class TriggerCondition {
	private int code;// 0 no trigger, 1 bigger, 2 tinier, 3 equal, 4 is not
	private String xValue;
	private int function;
	private ResultValueType elementType;
	private SnmpUnit xvalueUnit;
	
	
	public TriggerCondition(int code, String xValue, int function, String elementType, String xvalueUnit) {
		this.code = code;
		this.xValue = xValue;
		this.setElementType(ResultValueType.valueOf(elementType));
		this.setXvalueUnit(SnmpUnit.valueOf(xvalueUnit));
		this.setFunction(function);
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getxValue() {
		return xValue;
	}

	public void setxValue(String xValue) {
		this.xValue = xValue;
	}

	public int getFunction() {
		return function;
	}

	public void setFunction(int function) {
		this.function = function;
	}

	public ResultValueType getElementType() {
		return elementType;
	}

	public void setElementType(ResultValueType elementType) {
		this.elementType = elementType;
	}

	public SnmpUnit getXvalueUnit() {
		return xvalueUnit;
	}

	public void setXvalueUnit(SnmpUnit xvalueUnit) {
		this.xvalueUnit = xvalueUnit;
	}

}
