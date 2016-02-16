package lycus;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class RollupsMemoryDump implements Runnable {

	private RunnableProbesHistory history;
	private boolean isRollupsMerged;

	public RollupsMemoryDump(RunnableProbesHistory history) {
		super();
		this.history = history;
		this.isRollupsMerged = false;
	}

	public RunnableProbesHistory getHistory() {
		return history;
	}

	public void setHistory(RunnableProbesHistory history) {
		this.history = history;
	}

	public boolean isRollupsMerged() {
		return isRollupsMerged;
	}

	public void setRollupsMerged(boolean isRollupsMerged) {
		this.isRollupsMerged = isRollupsMerged;
	}

	@Override
	public void run() {
		String rollups = this.serializeRollups(this.getAllRollups());
		if (!this.isRollupsMerged())// check if existing rollups pulled from API
			this.mergeExistingRollupsFromMemDump();
		SysLogger.Record(new Log("Sending MEMDUMP of rollups to DB...", LogType.Debug));
		String sendString = "{\"last_rollups\":\"" + rollups + "\"}";
		ApiInterface.executeRequest(Enums.ApiAction.FlushServerMemory, "PUT", sendString);
	}

	private ArrayList<DataPointsRollup[][]> getAllRollups() {
		ArrayList<DataPointsRollup[][]> dataRollups = new ArrayList<DataPointsRollup[][]>();
		ArrayList<RunnableProbeResults> historyResults = new ArrayList<RunnableProbeResults>(
				this.getHistory().getResults().values());
		for (RunnableProbeResults results : historyResults) {
			dataRollups.add(results.retrieveExistingRollups());
		}
		return dataRollups;
	}

	private String serializeRollups(ArrayList<DataPointsRollup[][]> rollups) {
		return GeneralFunctions.Base64Encode(this.getHistory().getGson().toJson(rollups));
	}

	public ArrayList<DataPointsRollup[][]> deserializeRollups(String rollups) {
		ArrayList<DataPointsRollup[][]> allRollupsDeserialized = new ArrayList<DataPointsRollup[][]>();
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
						probeRollups[j][z] = this.getHistory().getGson().fromJson(singleProbeResultRollups.get(z),
								DataPointsRollup.class);
					}
				}
				allRollupsDeserialized.add(probeRollups);
			}
		} catch (Exception e) {
			SysLogger.Record(new Log("Error deserialize rollups, check existing rollups!", LogType.Error, e));
		}

		return allRollupsDeserialized;
	}

	public void mergeExistingRollupsFromMemDump() {
		SysLogger.Record(new Log("Retrieving existing rollups from DB...", LogType.Debug));
		Object rollupsUnDecoded = ApiInterface.executeRequest(Enums.ApiAction.GetServerMemoryDump, "GET", null);

		if (rollupsUnDecoded == null || ((String) rollupsUnDecoded).equals("0\n")) {
			SysLogger.Record(
					new Log("Unable to retrieve existing rollups, trying again in about 30 secs...", LogType.Warn));
			return;
		}

		String rollups = ((String) rollupsUnDecoded).substring(1, ((String) rollupsUnDecoded).length() - 1);

		ArrayList<DataPointsRollup[][]> rollupses = this.deserializeRollups(rollups);
		for (DataPointsRollup[][] rollupsResult : rollupses) {
			DataPointsRollup sampleRollup = rollupsResult[0][0];
			String rpID = sampleRollup.getRunnableProbeId();
			RunnableProbeResults rpr = this.getHistory().getResults().get(rpID);
			if (rpr != null)
				rpr.insertExistingRollups(rollupsResult);
			else {
				// handle runnable probe without results object
			}
		}
	}

}
