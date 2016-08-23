package Functions;

import Interfaces.IFunction;
import Results.*;

public class LastFunction extends BaseFunction {
	public LastFunction(String valueType) {
		super(valueType);
		this.lastResults = new Object[1];
	}

	// private Double lastValue;

	@Override
	public void add(BaseResult result) {
		this.lastResults[0] = result.getResultElementValue(this.valueType);

	}

	@Override
	public Object[] get() {
		return this.lastResults;
	}

}
