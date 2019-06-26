package Results;

import org.json.simple.JSONArray;
import GlobalConstants.Constants;
import Utils.Logit;

public class SnmpDeltaResult extends SnmpResult {
	private String previosData;
	private String currentData;

	public SnmpDeltaResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public void setData(String data1, String data2) {
		this.previosData = data1;
		this.currentData = data2;

		if (data1 == null || data2 == null)
			super.setData(null);
		else if (!data2.equals(Constants.WRONG_OID) && !data1.equals(Constants.WRONG_OID)
				&& !data2.equals(Constants.WRONG_VALUE_FORMAT) && !data1.equals(Constants.WRONG_VALUE_FORMAT))
			try {
				super.setData(((Double) (Double.parseDouble(data2) - Double.parseDouble(data1))).toString());
			}
			catch (Exception e)
			{
				Logit.LogWarn("Error (parsing or otherwise) when calculating snmp delta: RPID="+super.getRunnableProbeId());
			}
	}

	@Override
	public Object getResultObject() {
		JSONArray result = new JSONArray();
		result.add(4);
		if (this.getErrorMessage().equals("")) {
			result.add(getData());
		} else
			result.add(this.getErrorMessage());

		return result;
	}
}
