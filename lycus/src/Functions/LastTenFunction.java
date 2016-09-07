package Functions;

import GlobalConstants.Enums.ResultValueType;
import Results.BaseResult;

public class LastTenFunction extends BaseFunction {

	public LastTenFunction(ResultValueType valueType) {
		super(valueType, 3);
	}

	@Override
	public Object[] get() {
		return this.lastResults;
	}

	@Override
	public void add(BaseResult result) {
		this.lastResults[9] = this.lastResults[8];
		this.lastResults[8] = this.lastResults[7];
		this.lastResults[7] = this.lastResults[6];
		this.lastResults[6] = this.lastResults[5];
		this.lastResults[5] = this.lastResults[4];
		this.lastResults[4] = this.lastResults[3];
		this.lastResults[3] = this.lastResults[2];
		this.lastResults[2] = this.lastResults[1];
		this.lastResults[1] = this.lastResults[0];
		this.lastResults[0] = result.getResultElementValue(this.valueType);
	}

}
