package Results;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import GlobalConstants.Enums.SnmpError;
import GlobalConstants.ProbeTypes;

public class SnmpResult extends BaseResult {

	private String oid;
	private String data;
	private SnmpError error;

	public SnmpResult(String runnableProbeId, long timestamp) {
		super(runnableProbeId, timestamp);
		this.probeType = ProbeTypes.SNMP;
	}

	public SnmpResult(String runnableProbeId, long timestamp, String data) {
		super(runnableProbeId, timestamp);
		this.probeType = ProbeTypes.SNMP;
		this.data = data;
	}

	public SnmpResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public boolean isValidNumber() {
		return this.getNumData() != null;
	}

	public Double getNumData() {
		Double numData = null;

		try {
			if (data == null)
				return null;
			numData = Double.parseDouble(data);
		} catch (Exception e) {
			return null;
		}

		return numData;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	@Override
	public Object getResultObject() {
		JSONArray result = new JSONArray();
		result.add(4);
		if (this.getErrorMessage().equals("")) {
			result.add(data);
		} else
			result.add(this.getErrorMessage());

		return result;
	}

	public SnmpError getError() {
		return error;
	}

	public void setError(SnmpError error) {
		this.error = error;
	}

	@Override
	public String toString() {
		String resultString = "";
		resultString += super.toString();
		JSONObject resultJson = new JSONObject();
		resultJson.put("data", this.data);
		resultJson.put("resultType", ProbeTypes.SNMP.name());
		resultString += resultJson.toJSONString();
		return resultString;

	}

}
