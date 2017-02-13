package Results;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import GlobalConstants.ProbeTypes;

public class SqlResult extends BaseResult {
	private String[] sqlResults;
	private String[] sqlFields;
	private ProbeTypes probeType;
	
	public SqlResult(String runnableProbeId, long timestamp, String[] sqlResults, String[] sqlFields) {
		super(runnableProbeId, timestamp);
		this.probeType = ProbeTypes.SQL;
		this.setSqlResults(sqlResults);
		this.setSqlFields(sqlFields);
	}

	@Override
	public String getName() {
		return super.getName();
	}

	// TODO: Oren ask ran what is true?
	public Boolean isActive() {
		//	return packetLoss < Constants.pingPacketLostMin;
		return true;
	}

	public String[] getSqlFields() {
		return sqlFields;
	}

	public void setSqlFields(String[] sqlFields) {
		this.sqlFields = sqlFields;
	}

	public String[] getSqlResults() {
		return sqlResults;
	}

	public void setSqlResults(String[] sqlResults) {
		this.sqlResults = sqlResults;
	}
	
	public Double getSqlResult(String field) {
		for (int i = 0; i < getSqlResults().length; i++)
		{
			if (getSqlFields()[i].equals(field))
				return Double.parseDouble(getSqlResults()[i]);
		}
		return null;
	}
	
	@Override
	public Object getResultObject() {
		JSONArray result = new JSONArray();
		for (int i = 0; i < getSqlResults().length; i++)
		{
			JSONObject jsonObject = new JSONObject();
			if (getSqlResults()[i].matches("\\-?\\d+"))
			{
				jsonObject.put(getSqlFields()[i], Integer.parseInt(getSqlResults()[i]));
			}
			else if (isDouble(getSqlResults()[i]))
			{
				jsonObject.put(getSqlFields()[i], Double.parseDouble(getSqlResults()[i]));
			}
			else
				jsonObject.put(getSqlFields()[i], getSqlResults()[i]);
			result.add(jsonObject);
		}
		return result;
	}  
	
	private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
