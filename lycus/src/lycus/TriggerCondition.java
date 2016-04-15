package lycus;

public class TriggerCondition {
	private int code;//0 no trigger, 1 bigger, 2 tinier, 3 equal, 4 is not
	private String andOr;
	private String xValue;
	private String tValue;
	
	public TriggerCondition(int code, String andOr, String xValue, String tValue) {
		this.code = code;
		this.andOr = andOr;
		this.xValue = xValue;
		this.tValue = tValue;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getAndOr() {
		return andOr;
	}

	public void setAndOr(String andOr) {
		this.andOr = andOr;
	}

	public String getxValue() {
		return xValue;
	}

	public void setxValue(String xValue) {
		this.xValue = xValue;
	}

	public String gettValue() {
		return tValue;
	}

	public void settValue(String tValue) {
		this.tValue = tValue;
	}
}
