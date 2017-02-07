package Results;

import java.io.File;
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
		this.sqlResults = sqlResults;
		this.sqlFields = sqlFields;
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

	@Override
	public Object getResultObject() {
		JSONArray result = new JSONArray();
		for (int i = 0; i < sqlResults.length; i++)
		{
			JSONObject jsonObject = new JSONObject();
			if (sqlResults[i].matches("\\-?\\d+"))
			{
				jsonObject.put(sqlFields[i], Integer.parseInt(sqlResults[i]));
			}
			else if (isDouble(sqlResults[i]))
			{
				jsonObject.put(sqlFields[i], Double.parseDouble(sqlResults[i]));
			}
			else
				jsonObject.put(sqlFields[i], sqlResults[i]);
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
