package Results;

import org.json.simple.JSONArray;

public class SnmpDeltaResult extends SnmpResult {
	private String data1;
	private String data2;
	
	public SnmpDeltaResult(String runnableProbeId) {
		super(runnableProbeId);
		// TODO Auto-generated constructor stub
	}

	public String getData()
	{
		if (super.getData() != null)
			return super.getData();
		
		if (data1 != null)
			this.setData(((Double)(Double.parseDouble(data2) -  Double.parseDouble(data1))).toString());
		else
			this.setData(data2);
		
		return this.getData();
	}
	
	public void setData(String data1, String data2) {
		this.data1 = data1;
		this.data2 = data2;
	}
	
	@Override
	public String getResultString() {
		JSONArray result=new JSONArray();
		result.add(4);
		result.add(getData());
		return result.toString();
	}
}
