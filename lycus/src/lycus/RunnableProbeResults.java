package lycus;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RunnableProbeResults {
	private RunnableProbe rp;
	private Long lastTimestamp;
	private HashMap<Trigger,TriggerEvent> events;
	
	private Gson gson;
	
	public RunnableProbeResults(RunnableProbe rp) {
		this.rp=rp;
		this.gson=new GsonBuilder().setPrettyPrinting().create();
		this.lastTimestamp=null;
		this.setEvents(new HashMap<Trigger,TriggerEvent>());
		
	}
	public RunnableProbe getRp() {
		return rp;
	}
	public void setRp(RunnableProbe rp) {
		this.rp = rp;
	}
	public Gson getGson() {
		return gson;
	}
	public void setGson(Gson gson) {
		this.gson = gson;
	}
	
	public Long getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(Long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}
	


	public synchronized HashMap<Trigger,TriggerEvent> getEvents() {
		return events;
	}
	public synchronized void setEvents(HashMap<Trigger,TriggerEvent> events) {
		this.events = events;
	}
	public void insertResults()
	{
		
	}
	public int getNumberOfRollupTables()
	{
		long interval=getRp().getProbe().getInterval();
		if(interval<240)
			return 6;
		if(interval>=240&&interval<1200)
			return 5;
		if(interval>=1200&&interval<3600)
			return 4;
		if(interval>=3600&&interval<21600)
			return 3;
		if(interval>=21600)
			return 2;
		SysLogger.Record(new Log("Wrong interval at Runnable Probe:"+getRp().toString(),LogType.Error));
		return 0;
	}
	
	/**
	return -1 if no change 
	return n if new is the new number of rollup tables
	 */
	public int isNumberOfRollupTablesChanged(DataPointsRollup[][] dprs)
	{
		int c=0;
		for(int i=0;i<dprs[0].length;i++)
		{
			if(dprs[0][i]!=null)
			c++;
		}
		int currentNumberOfRollupTables=this.getNumberOfRollupTables();
		if(currentNumberOfRollupTables!=c)
			return currentNumberOfRollupTables;
		return -1;
	}
	public void changeNumberRollupTables(DataPointsRollup[][] dprs,int newNumber)
	{
		// TODO Auto-generated method stub
	}
	public void insertExistingRollups(DataPointsRollup[][] existing) {
		// TODO Auto-generated method stub
		
	}
	public DataPointsRollup[][] retrieveExistingRollups() {
		// TODO Auto-generated method stub
		return null;
	}
	public DataPointsRollup[] initRollupSeries(DataPointsRollup[] rollups)
	{
		int n = this.getNumberOfRollupTables();
		if (n == 0) {
			SysLogger.Record(new Log("Unable to init rtt Rollups of Runnable Probe Results: "
					+ this.getRp().getRPString() + " check interval!", LogType.Error));
			return null;
		}
		for (int i = 0; i < n; i++) {
			if (rollups[i] == null) {
				if (i == 0)
					rollups[0] = new DataPointsRollup(this.getRp().getRPString(),
							DataPointsRollupSize._11day);
				if (i == 1)
					rollups[1] = new DataPointsRollup(this.getRp().getRPString(),
							DataPointsRollupSize._36hour);
				if (i == 2)
					rollups[2] = new DataPointsRollup(this.getRp().getRPString(),
							DataPointsRollupSize._6hour);
				if (i == 3)
					rollups[3] = new DataPointsRollup(this.getRp().getRPString(),
							DataPointsRollupSize._1hour);
				if (i == 4)
					rollups[4] = new DataPointsRollup(this.getRp().getRPString(),
							DataPointsRollupSize._20minutes);
				if (i == 5)
					rollups[5] = new DataPointsRollup(this.getRp().getRPString(),
							DataPointsRollupSize._4minutes);
			}

		}
		return rollups;
	}
	/**
	 * @param results - results in arrayList.
	 * @throws Exception 
	 */
	public void acceptResults(ArrayList<Object> results) throws Exception
	{
		
	}
	
   /**
	* 
	* @return 
 * @throws Throwable 
	*/

	public HashMap<String,String> getResults() throws Throwable
	{
		HashMap<String,String> results=new HashMap<String,String>();
		results.put("rpID", this.getRp().getRPString());
		return results;
	}
	
	public HashMap<String,String> getRaw() throws Throwable
	{
		return null;
	}
	
	public HashMap<String,String> getRollups() throws Throwable
	{
		return null;
	}
	
	public void resetRollups()
	{
		
	}
	
	protected void addRollupsFromExistingMemoryDump(DataPointsRollup[] original,DataPointsRollup[] memoryDump) {
		SysLogger.Record(new Log("Merging existing rollup for => "+this.getRp().getRPString(), LogType.Debug));
		for (int i = 0; i < 6; i++) {
			if (original[i] != null) {
				original[i].mergeRollup(memoryDump[i]);
			}
		}
	}
	
}
