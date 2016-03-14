package lycus.ResultsTasks;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import lycus.DataPointsRollup;
import lycus.ResultsContainer;
import lycus.DAL.ApiInterface;
import lycus.GlobalConstants.Enums;
import lycus.Results.BaseResult;
import lycus.Utils.GeneralFunctions;
import lycus.Utils.Logit;

public class RollupsMemoryDump extends BaseTask {
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
		ResultsContainer resultsContainer = ResultsContainer.getInstance();
		String rollups = resultsContainer.getRollups();
		String rollupsEncoded = GeneralFunctions.Base64Encode(rollups);
		
		if (!this.isRollupsMerged())           // check if existing rollups pulled from API
			this.mergeExistingRollupsFromMemDump();
		Logit.LogDebug("Sending MEMDUMP of rollups to DB...");
		String sendString = "{\"last_rollups\":\"" + rollupsEncoded + "\"}";
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
						probeRollups[j][z] = gson.fromJson(singleProbeResultRollups.get(z),
								DataPointsRollup.class);
					}
				}
				allRollupsDeserialized.add(probeRollups);
			}
		} catch (Exception e) {
			Logit.LogError("RollupsMemoryDump - deserializeRollups()", "Error deserialize rollups, check existing rollups!");
		}

		return allRollupsDeserialized;
	}

	public void mergeExistingRollupsFromMemDump() {
		Logit.LogDebug("Retrieving existing rollups from DB...");
		Object rollupsUnDecoded = ApiInterface.executeRequest(Enums.ApiAction.GetServerMemoryDump, "GET", null);

		if (rollupsUnDecoded == null || ((String) rollupsUnDecoded).equals("0\n")) {
			Logit.LogWarn("Unable to retrieve existing rollups, trying again in about 30 secs...");
			return;
		}

		String rollups = ((String) rollupsUnDecoded).substring(1, ((String) rollupsUnDecoded).length() - 1);

		ArrayList<DataPointsRollup[][]> rollupses = this.deserializeRollups(rollups);
		for (DataPointsRollup[][] rollupsResult : rollupses) {
			DataPointsRollup sampleRollup = rollupsResult[0][0];
			String rpID = sampleRollup.getRunnableProbeId();
			
			BaseResult rpr = ResultsContainer.getInstance().getResult(rpID);
			if (rpr != null)
				rpr.insertExistingRollups(rollupsResult);
			else {
				// handle runnable probe without results object
			}
		}
		
		setRollupsMerged(true);
	}

}
