package lycus.Rollups;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import lycus.DataPointsRollup;
import lycus.ResultsContainer;
import lycus.DAL.ApiInterface;
import lycus.GlobalConstants.Enums;
import lycus.Interfaces.IRollupsContainer;
import lycus.Results.BaseResult;
import lycus.Results.PingResult;
import lycus.Results.PortResult;
import lycus.Results.SnmpResult;
import lycus.Results.WebResult;
import lycus.Utils.GeneralFunctions;
import lycus.Utils.JsonUtil;
import lycus.Utils.Logit;

public class RollupsContainer implements IRollupsContainer {

	private static RollupsContainer instance;
	private HashMap<String, DataPointsRollup[]> packetLossRollups;
	private HashMap<String, DataPointsRollup[]> rttRollups;
	private HashMap<String, DataPointsRollup[]> portResponseTimeRollups;
	private HashMap<String, DataPointsRollup[]> webResponseTimeRollups;
	private HashMap<String, DataPointsRollup[]> snmpDataRollups;

	private HashMap<String, ArrayList<DataPointsRollup>> rollups4m;
	private HashMap<String, ArrayList<DataPointsRollup>> rollup20m;
	private HashMap<String, ArrayList<DataPointsRollup>> rollup1h;
	private HashMap<String, ArrayList<DataPointsRollup>> rollup6h;
	private HashMap<String, ArrayList<DataPointsRollup>> rollup36h;
	private HashMap<String, ArrayList<DataPointsRollup>> rollup11d;

	public static RollupsContainer getInstance() {
		if (instance == null)
			instance = new RollupsContainer();
		return instance;
	}

	@Override
	public boolean addResult(BaseResult result) {
		if (result instanceof PingResult) {
			addPingerResult(result);
		} else if (result instanceof PortResult) {
			addPorterResult(result);
		} else if (result instanceof WebResult) {
			addWeberResult(result);
		} else if (result instanceof SnmpResult) {
			addSnmpResult(result);
		}

		return false;
	}

	@Override
	public String getAllFinsihedRollups() {
		for (int i = 0; i < 6; i++) {
			for (DataPointsRollup[] rolups : packetLossRollups.values()) {
				addFinished(i, rolups);
			}
			for (DataPointsRollup[] rolups : rttRollups.values()) {
				addFinished(i, rolups);
			}
			for (DataPointsRollup[] rolups : webResponseTimeRollups.values()) {
				addFinished(i, rolups);
			}
			for (DataPointsRollup[] rolups : portResponseTimeRollups.values()) {
				addFinished(i, rolups);
			}
			for (DataPointsRollup[] rolups : snmpDataRollups.values()) {
				addFinished(i, rolups);
			}
		}
		 
		 return JsonUtil.ToJson(rollups4m);
	}

	private void addFinished(int i, DataPointsRollup[] rolups) {
		DataPointsRollup currentDataRollup = rolups[i];
		DataPointsRollup finishedDataRollup = currentDataRollup.getLastFinishedRollup();
		if (finishedDataRollup == null)
			return;

		addRollupTo(i, finishedDataRollup);
		currentDataRollup.setLastFinishedRollup(null);
	}

	@Override
	public boolean mergeRollups(JSONArray jsonArray) {
		Logit.LogDebug("Retrieving existing rollups from DB...");
		Object rollupsEncoded = ApiInterface.executeRequest(Enums.ApiAction.GetServerMemoryDump, "GET", null);

		if (rollupsEncoded == null || ((String) rollupsEncoded).equals("0\n")) {
			Logit.LogWarn("Unable to retrieve existing rollups, trying again in about 30 secs...");
			return false;
		}

		String rollups = ((String) rollupsEncoded).substring(1, ((String) rollupsEncoded).length() - 1);

		ArrayList<DataPointsRollup[][]> rollupses = this.deserializeRollups(rollups);
		for (DataPointsRollup[][] rollupsResult : rollupses) {
			DataPointsRollup sampleRollup = rollupsResult[0][0];
			String rpID = sampleRollup.getRunnableProbeId();
			
			BaseResult rpr = ResultsContainer.getInstance().getResult(rpID);
			
			//TODO: insertExistingRollups - implement
//			if (rpr != null)
//				rpr.insertExistingRollups(rollupsResult);
//			else {
				// handle runnable probe without results object
//			}
		}
		
		return false;
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

	private boolean addRollupTo(int rollupType, DataPointsRollup dataPointsRollup) {
		switch (rollupType) {
		case 0:
			rollups4m.get(dataPointsRollup.getRunnableProbeId()).add(dataPointsRollup);
			break;
		case 1:
			rollup20m.get(dataPointsRollup.getRunnableProbeId()).add(dataPointsRollup);
			break;
		case 2:
			rollup1h.get(dataPointsRollup.getRunnableProbeId()).add(dataPointsRollup);
			break;
		case 3:
			rollup6h.get(dataPointsRollup.getRunnableProbeId()).add(dataPointsRollup);
			break;
		case 4:
			rollup36h.get(dataPointsRollup.getRunnableProbeId()).add(dataPointsRollup);
			break;
		case 5:
			rollup11d.get(dataPointsRollup.getRunnableProbeId()).add(dataPointsRollup);
			break;
		}

		return true;
	}

	private void addSnmpResult(BaseResult result) {
		SnmpResult snmpResults = (SnmpResult) result;
		for (int i = 0; i < snmpDataRollups.get(result.getRunnableProbeId()).length; i++) {
			DataPointsRollup snmpDataRollup = snmpDataRollups.get(result.getRunnableProbeId())[i];
			if (snmpDataRollup == null)
				continue;

			snmpDataRollup.add(snmpResults.getLastTimestamp(), snmpResults.getNumData());
		}
	}

	private void addWeberResult(BaseResult result) {
		WebResult weberResults = (WebResult) result;
		for (int i = 0; i < webResponseTimeRollups.get(result.getRunnableProbeId()).length; i++) {
			DataPointsRollup responseTimeRollup = webResponseTimeRollups.get(result.getRunnableProbeId())[i];
			if (responseTimeRollup == null)
				continue;

			responseTimeRollup.add(weberResults.getLastTimestamp(), weberResults.getResponseTime());
		}
	}

	private void addPorterResult(BaseResult result) {
		PortResult porterResults = (PortResult) result;
		for (int i = 0; i < portResponseTimeRollups.get(result.getRunnableProbeId()).length; i++) {
			DataPointsRollup responseTimeRollup = portResponseTimeRollups.get(result.getRunnableProbeId())[i];
			if (responseTimeRollup == null)
				continue;

			responseTimeRollup.add(porterResults.getLastTimestamp(), porterResults.getResponseTime());
		}
	}

	private void addPingerResult(BaseResult result) {
		PingResult pingerResults = (PingResult) result;
		for (int i = 0; i < packetLossRollups.get(result.getRunnableProbeId()).length; i++) {
			DataPointsRollup packetLostRollup = packetLossRollups.get(result.getRunnableProbeId())[i];
			DataPointsRollup rttRollup = rttRollups.get(result.getRunnableProbeId())[i];

			packetLostRollup.add(pingerResults.getLastTimestamp(), pingerResults.getPacketLost());
			rttRollup.add(pingerResults.getLastTimestamp(), pingerResults.getRtt());
		}
	}

}
