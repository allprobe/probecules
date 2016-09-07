package Functions;

import GlobalConstants.Enums.ResultValueType;
import Results.BaseResult;

public class LastThreeFunction extends BaseFunction {

	public LastThreeFunction(ResultValueType valueType) {
		super(valueType, 2);
	}

	@Override
	public Object[] get() {
		return this.lastResults;
	}

	@Override
	public void add(BaseResult result) {
		this.lastResults[2] = this.lastResults[1];
		this.lastResults[1] = this.lastResults[0];
		this.lastResults[0] = result.getResultElementValue(this.valueType);
	}

}
