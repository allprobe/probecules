package Functions;

import Results.BaseResult;

import java.util.UUID;

import GlobalConstants.Enums.ResultValueType;

/**
 * Created by roi on 8/17/16.
 */
public abstract class BaseFunction {
	protected ResultValueType valueType;
	protected String triggerId;
	protected Object[] lastResults;

	public abstract Object[] get();

	public abstract void add(BaseResult result);

	public BaseFunction(ResultValueType valueType, String triggerId) {
		this.valueType = valueType;
		this.triggerId=triggerId;
	}

	public String getTriggerId() {
		return triggerId;
	}
}
