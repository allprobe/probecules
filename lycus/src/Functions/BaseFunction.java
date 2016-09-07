package Functions;

import Results.BaseResult;
import GlobalConstants.Enums.ResultValueType;

/**
 * Created by roi on 8/17/16.
 */
public abstract class BaseFunction {
	protected ResultValueType valueType;
	protected int functionId;

	protected Object[] lastResults;

	public abstract Object[] get();

	public abstract void add(BaseResult result);

	public BaseFunction(ResultValueType valueType, int functionId) {
		this.valueType = valueType;
		this.functionId = functionId;
		this.lastResults = new Object[10];
	}

	public ResultValueType getValueType() {
		return valueType;
	}

	public int getFunctionId() {
		return functionId;
	}

	public boolean isEqual(BaseFunction function) {
		if (this.getValueType() != function.getValueType())
			return false;
		if (this.getFunctionId() != function.getFunctionId())
			return false;
		return true;
	}
}
