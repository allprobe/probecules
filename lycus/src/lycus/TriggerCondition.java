package lycus;

import Interfaces.IFunction;

public class TriggerCondition {
	private int code;// 0 no trigger, 1 bigger, 2 tinier, 3 equal, 4 is not
	private String xValue;
	private int function;

	public TriggerCondition(int code, String xValue, int function) {
		this.code = code;
		this.xValue = xValue;
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

}
