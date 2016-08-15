package Tasks;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import GlobalConstants.Enums;
import Rollups.RollupsContainer;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.DataPointsRollup;

public class RollupsDumpTask extends BaseTask {
	private long interval = 30;
	private boolean isRollupsMerged;

	public boolean isRollupsMerged() {
		return isRollupsMerged;
	}

	public void setRollupsMerged(boolean isRollupsMerged) {
		this.isRollupsMerged = isRollupsMerged;
	}

	@Override
	public void run() {
		if (!this.isRollupsMerged()) // check if existing rollups pulled from
			// API
			
		{
			if (RollupsContainer.getInstance().mergeExistingRollupsFromMemDump())
				this.setRollupsMerged(true);
			else
				;
//				return;
		}

		String rollups = RollupsContainer.getInstance().getAllCurrentLiveRollups();
		
		if(rollups.contains("788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_7be55137-c5d8-438e-bca7-325f56656071"))
			Logit.LogDebug("BREAKPOINT");
		String rollupsEncoded = GeneralFunctions.Base64Encode(rollups);

		Logit.LogInfo("Sending current live rollups dump to API...");

		JSONObject jsonObject=new JSONObject();
		jsonObject.put("last_rollups", rollupsEncoded);

//		String sendString="{ \"last_rollups\" : \""+rollupsEncoded+"\" }";
		String sendString=jsonObject.toString();
		DAL.DAL.getInstanece().put(Enums.ApiAction.FlushServerMemory,jsonObject);
//		ApiInterface.executeRequest(Enums.ApiAction.FlushServerMemory, "PUT", sendString);
	}

	public ArrayList<DataPointsRollup[][]> deserializeRollups(String rollups) {
		ArrayList<DataPointsRollup[][]> allRollupsDeserialized = new ArrayList<DataPointsRollup[][]>();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			String decoded = GeneralFunctions.Base64Decode(rollups);
			JsonParser parser = new JsonParser();
			JsonElement jelement = parser.parse(decoded);
			JsonArray allRollups = jelement.getAsJsonArray();
			for (int i = 0; i < allRollups.size(); i++) {
				JsonArray singleProbeRollups = null;
				singleProbeRollups = allRollups.get(i).getAsJsonArray();

				DataPointsRollup[][] probeRollups = new DataPointsRollup[singleProbeRollups.size()][6];
				for (int j = 0; j < singleProbeRollups.size(); j++) {
					JsonArray singleProbeResultRollups = null;
					singleProbeResultRollups = singleProbeRollups.get(j).getAsJsonArray();

					for (int z = 0; z < 6; z++) {
						probeRollups[j][z] = gson.fromJson(singleProbeResultRollups.get(z), DataPointsRollup.class);
					}
				}
				allRollupsDeserialized.add(probeRollups);
			}
		} catch (Exception e) {
			Logit.LogError("RollupsMemoryDump - deserializeRollups()",
					"Error deserialize rollups, check existing rollups!");
		}

		return allRollupsDeserialized;
	}

}
