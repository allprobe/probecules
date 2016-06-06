package Results;

import org.json.simple.JSONArray;

public class SnmpDeltaResult extends SnmpResult {
	private String previosData;
	private String currentData;
//	private boolean isFirst;
	
	public SnmpDeltaResult(String runnableProbeId) {
		super(runnableProbeId);
//		isFirst = true;
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
		
		if (data1 == null || data2 == null)
			super.setData(null);
		else 
			super.setData(((Double)(Double.parseDouble(data2) -  Double.parseDouble(data1))).toString());
//		else
//			super.setData(data2);
		
//		isFirst = false;
	}
	
//	public boolean isFirst()
//	{
//		return isFirst;
//	}
	
	@Override
	public String getResultString() {
		JSONArray result = new JSONArray();
		result.add(4);
		result.add(getData());
		return result.toString();
	}
}
