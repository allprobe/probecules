package Results;

import java.io.File;

import org.json.simple.JSONArray;
import GlobalConstants.ProbeTypes;

public class SqlResult extends BaseResult {
	private String[] sqlResults;
	private ProbeTypes probeType;
	
	public SqlResult(String runnableProbeId, long timestamp, String[] sqlResults) {
		super(runnableProbeId, timestamp);
		this.probeType = ProbeTypes.SQL;
		this.sqlResults = sqlResults;
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
		for (String res : sqlResults) 
		{
			result.add(res);
		}
		
//		if (this.getErrorMessage().equals("")) {
//			result.add(packetLoss);
//			result.add(rtt);
//			result.add(ttl);
//		} else
//			result.add(this.getErrorMessage());
		return result;
	}
}
