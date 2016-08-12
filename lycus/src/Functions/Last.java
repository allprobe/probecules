package Functions;

import Interfaces.IFunction;

public class Last implements IFunction {

	private Double lastValue;
	
	@Override
	public void add(Double result) {
		result=lastValue;
	}

	@Override
	public Double[] get() {
		Double[] lastValues=new Double[1];
		lastValues[0]=lastValue;
		return lastValues;
	}

}
