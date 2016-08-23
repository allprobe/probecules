package Functions;

import Results.BaseResult;

import java.util.UUID;

/**
 * Created by roi on 8/17/16.
 */
public abstract class BaseFunction {
	protected String valueType;
	protected String triggerId;
	protected Object[] lastResults;

	public abstract Object[] get();

	public abstract void add(BaseResult result);

	public BaseFunction(String valueType) {
		this.valueType = valueType;
	}

	public String getTriggerId() {
		return triggerId;
	}
}
