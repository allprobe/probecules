package lycus;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MemoryDump implements Runnable {
	
	private RunnableProbesHistory history;
	
	public MemoryDump(RunnableProbesHistory history) {
		super();
		this.history=history;
	}

	public RunnableProbesHistory getHistory() {
		return history;
	}

	public void setHistory(RunnableProbesHistory history) {
		this.history = history;
	}


	@Override
	public void run() {
		String rollups=this.serializeRollups(this.getAllRollups());
		if(this.getHistory().getRetrieveExistingRollupsCounter()==-1)//check if existing rollups pulled from API, only after pull it push
		{
			SysLogger.Record(new Log("Sending MEMDUMP of rollups to DB...",LogType.Debug));
			ApiStages.insertExistingRollups(rollups);
		}
		}
	private ArrayList<DataPointsRollup[][]> getAllRollups()
	{
		ArrayList<DataPointsRollup[][]> dataRollups=new ArrayList<DataPointsRollup[][]>();
		ArrayList<RunnableProbeResults> historyResults=new ArrayList<RunnableProbeResults>(this.getHistory().getResults().values());
		for(RunnableProbeResults results:historyResults)
		{
			dataRollups.add(results.retrieveExistingRollups());
		}
		return dataRollups;
	}
	private String serializeRollups(ArrayList<DataPointsRollup[][]> rollups)
	{
		return GeneralFunctions.Base64Encode(this.getHistory().getGson().toJson(rollups));
	}
	public ArrayList<DataPointsRollup[][]> deserializeRollups(String rollups)
	{
		ArrayList<DataPointsRollup[][]> allRollupsDeserialized=new ArrayList<DataPointsRollup[][]>();
		try{
		String decoded=GeneralFunctions.Base64Decode(rollups);
		JsonParser parser = new JsonParser();
		JsonElement jelement=parser.parse(decoded);
		JsonArray allRollups=jelement.getAsJsonArray();
		for(int i=0;i<allRollups.size();i++)
		{
			JsonArray singleProbeRollups=null;
			singleProbeRollups=allRollups.get(i).getAsJsonArray();
		
			DataPointsRollup[][] probeRollups=new DataPointsRollup[singleProbeRollups.size()][6];
			for(int j=0;j<singleProbeRollups.size();j++)
			{
				JsonArray singleProbeResultRollups=null;
				singleProbeResultRollups=singleProbeRollups.get(j).getAsJsonArray();
				
				for(int z=0;z<6;z++)
				{
					probeRollups[j][z]=this.getHistory().getGson().fromJson(singleProbeResultRollups.get(z), DataPointsRollup.class);
				}
			}
			allRollupsDeserialized.add(probeRollups);
		}
		}
		catch(Exception e)
		{
			SysLogger.Record(new Log("Error deserialize rollups, check existing rollups!",LogType.Error,e));
		}
		
		return allRollupsDeserialized;
	}
}
