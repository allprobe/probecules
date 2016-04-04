package lycus.ResultsTasks;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import lycus.ResultsContainer;
import lycus.DAL.ApiInterface;
import lycus.GlobalConstants.Enums;
import lycus.Results.BaseResult;
import lycus.Rollups.RollupsContainer;
import lycus.DataPointsRollup;
import lycus.Utils.GeneralFunctions;
import lycus.Utils.Logit;

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
				return;
		}

		String rollups = RollupsContainer.getInstance().getAllCurrentLiveRollups();
		String rollupsEncoded = GeneralFunctions.Base64Encode(rollups);

		Logit.LogInfo("Sending current live rollups dump to API...");

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("last_rollups", rollupsEncoded);

		String sendString = jsonObject.toString();
		ApiInterface.executeRequest(Enums.ApiAction.FlushServerMemory, "PUT", sendString);
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
