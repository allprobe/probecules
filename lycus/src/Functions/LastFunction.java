package Functions;

import GlobalConstants.Enums.ResultValueType;
import Interfaces.IFunction;
import Results.*;

public class LastFunction extends BaseFunction {
	public LastFunction(ResultValueType valueType) {
		super(valueType,1);
	}

	@Override
	public void add(BaseResult result) {
		this.lastResults[0] = result.getResultElementValue(this.valueType);

	}

	@Override
	public Object[] get() {
		return this.lastResults;
	}

}
