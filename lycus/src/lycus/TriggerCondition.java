package lycus;

import Interfaces.IFunction;

public class TriggerCondition {
	private int code;// 0 no trigger, 1 bigger, 2 tinier, 3 equal, 4 is not
	private String xValue;
	private IFunction function;

	public TriggerCondition(int code, String xValue, IFunction function) {
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

	public IFunction getFunction() {
		return function;
	}

	public void setFunction(IFunction function) {
		this.function = function;
	}

}
