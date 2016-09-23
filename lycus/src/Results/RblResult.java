package Results;

import org.json.simple.JSONArray;

import GlobalConstants.ProbeTypes;

public class RblResult extends BaseResult {

	private Integer IsListed;

	public RblResult(String runnableProbeId, long timestamp, int isListed2) {
		super(runnableProbeId, timestamp);
		this.probeType = ProbeTypes.RBL;

		this.IsListed = isListed2;
	}

	public RblResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public int isIsListed() {
		return IsListed;
	}

	public void setIsListed(int isListed) {
		IsListed = isListed;
	}

	@Override
	public Object getResultObject() {
		JSONArray result = new JSONArray();
		result.add(5);
		if (this.getErrorMessage().equals("")) {
			result.add(IsListed);
		} else
			result.add(this.getErrorMessage());

		return result;
	}
}
