package Results;

import org.json.simple.JSONArray;

public class SnmpDeltaResult extends SnmpResult {
	private String previosData;
	private String currentData;
	
	public SnmpDeltaResult(String runnableProbeId) {
		super(runnableProbeId);
		// TODO SnmpDeltaResult()
	}

//	public String getData()
//	{
//		if (super.getData() != null)
//			return super.getData();
//		
//		if (previosData != null)
//			this.setData(((Double)(Double.parseDouble(data2) -  Double.parseDouble(previosData))).toString());
//		else
//			this.setData(data2);
//		
//		return super.getData();
//	}
	
	public void setData(String data1, String data2) {
		this.previosData = data1;
		this.currentData = data2;
		
		if (data1 != null)
			super.setData(((Double)(Double.parseDouble(data2) -  Double.parseDouble(data1))).toString());
		else
			super.setData(data2);
	}
	
	public boolean isFirst()
	{
		return previosData == null;
	}
	
	@Override
	public String getResultString() {
		JSONArray result = new JSONArray();
		result.add(4);
		result.add(getData());
		return result.toString();
	}
}
