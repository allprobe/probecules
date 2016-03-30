package lycus.Rollups;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import lycus.DataPointsRollup;
import lycus.ResultsContainer;
import lycus.DAL.ApiInterface;
import lycus.GlobalConstants.DataPointsRollupSize;
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
	private HashMap<String, DataPointsRollup[]> packetLossRollups = new HashMap<String, DataPointsRollup[]>();
	private HashMap<String, DataPointsRollup[]> rttRollups = new HashMap<String, DataPointsRollup[]>();
	private HashMap<String, DataPointsRollup[]> portResponseTimeRollups = new HashMap<String, DataPointsRollup[]>();
	private HashMap<String, DataPointsRollup[]> webResponseTimeRollups = new HashMap<String, DataPointsRollup[]>();
	private HashMap<String, DataPointsRollup[]> snmpDataRollups = new HashMap<String, DataPointsRollup[]>();

	// Those are finished rollups
	private HashMap<String, ArrayList<DataPointsRollup>> finishedRollups4m = new HashMap<String, ArrayList<DataPointsRollup>>();
	private HashMap<String, ArrayList<DataPointsRollup>> finishedRollups20m = new HashMap<String, ArrayList<DataPointsRollup>>();
	private HashMap<String, ArrayList<DataPointsRollup>> finishedRollups1h = new HashMap<String, ArrayList<DataPointsRollup>>();
	private HashMap<String, ArrayList<DataPointsRollup>> finishedRollups6h = new HashMap<String, ArrayList<DataPointsRollup>>();
	private HashMap<String, ArrayList<DataPointsRollup>> finishedRollups36h = new HashMap<String, ArrayList<DataPointsRollup>>();
	private HashMap<String, ArrayList<DataPointsRollup>> finishedRollups11d = new HashMap<String, ArrayList<DataPointsRollup>>();

	public static RollupsContainer getInstance() {
		if (instance == null) {
			instance = new RollupsContainer();
		}
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
	public synchronized String getAllFinsihedRollups() {

		JSONObject rollups = new JSONObject();

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

		if (finishedRollups4m.size() != 0)
			rollups.put("rollups4m", JsonUtil.ToJson(finishedRollups4m));

		if (finishedRollups20m.size() != 0)
			rollups.put("rollups20m", JsonUtil.ToJson(finishedRollups20m));

		if (finishedRollups1h.size() != 0)
			rollups.put("rollups1h", JsonUtil.ToJson(finishedRollups1h));

		if (finishedRollups6h.size() != 0)
			rollups.put("rollups6h", JsonUtil.ToJson(finishedRollups6h));

		if (finishedRollups36h.size() != 0)
			rollups.put("rollups36h", JsonUtil.ToJson(finishedRollups36h));

		if (finishedRollups11d.size() != 0)
			rollups.put("rollups11d", JsonUtil.ToJson(finishedRollups11d));

		if (rollups.size() == 0)
			return null;

		// rollups.put("packetLossRollups", JsonUtil.ToJson(packetLossRollups));
		// rollups.put("rttRollups", JsonUtil.ToJson(rttRollups));
		// rollups.put("portResponseTimeRollups",
		// JsonUtil.ToJson(portResponseTimeRollups));
		// rollups.put("webResponseTimeRollups",
		// JsonUtil.ToJson(webResponseTimeRollups));
		// rollups.put("snmpDataRollups", JsonUtil.ToJson(snmpDataRollups));

		return rollups.toString();
	}

	@Override
	public synchronized String getAllCurrentLiveRollups() {
		JSONObject rollups = new JSONObject();

		rollups.put("packetLossRollups", JsonUtil.ToJson(packetLossRollups));
		rollups.put("rttRollups", JsonUtil.ToJson(rttRollups));
		rollups.put("portResponseTimeRollups", JsonUtil.ToJson(portResponseTimeRollups));
		rollups.put("webResponseTimeRollups", JsonUtil.ToJson(webResponseTimeRollups));
		rollups.put("snmpDataRollups", JsonUtil.ToJson(snmpDataRollups));
		return rollups.toString();
	}

	private void addFinished(int i, DataPointsRollup[] rolups) {
		DataPointsRollup currentDataRollup = rolups[i];
		if (currentDataRollup == null)
			return;
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

			// TODO: insertExistingRollups - implement
			// if (rpr != null)
			// rpr.insertExistingRollups(rollupsResult);
			// else {
			// handle runnable probe without results object
			// }
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

	private boolean addRollupTo(int rollupType, DataPointsRollup dataPointsRollup) {
		switch (rollupType) {
		case 0:
			if (finishedRollups4m.get(dataPointsRollup.getRunnableProbeId()) == null)
				finishedRollups4m.put(dataPointsRollup.getRunnableProbeId(), new ArrayList<DataPointsRollup>());
			finishedRollups4m.get(dataPointsRollup.getRunnableProbeId()).add(dataPointsRollup);
			break;
		case 1:
			if (finishedRollups20m.get(dataPointsRollup.getRunnableProbeId()) == null)
				finishedRollups20m.put(dataPointsRollup.getRunnableProbeId(), new ArrayList<DataPointsRollup>());
			finishedRollups20m.get(dataPointsRollup.getRunnableProbeId()).add(dataPointsRollup);
			break;
		case 2:
			if (finishedRollups1h.get(dataPointsRollup.getRunnableProbeId()) == null)
				finishedRollups1h.put(dataPointsRollup.getRunnableProbeId(), new ArrayList<DataPointsRollup>());
			finishedRollups1h.get(dataPointsRollup.getRunnableProbeId()).add(dataPointsRollup);
			break;
		case 3:
			if (finishedRollups6h.get(dataPointsRollup.getRunnableProbeId()) == null)
				finishedRollups6h.put(dataPointsRollup.getRunnableProbeId(), new ArrayList<DataPointsRollup>());
			finishedRollups6h.get(dataPointsRollup.getRunnableProbeId()).add(dataPointsRollup);
			break;
		case 4:
			if (finishedRollups36h.get(dataPointsRollup.getRunnableProbeId()) == null)
				finishedRollups36h.put(dataPointsRollup.getRunnableProbeId(), new ArrayList<DataPointsRollup>());
			finishedRollups36h.get(dataPointsRollup.getRunnableProbeId()).add(dataPointsRollup);
			break;
		case 5:
			if (finishedRollups11d.get(dataPointsRollup.getRunnableProbeId()) == null)
				finishedRollups11d.put(dataPointsRollup.getRunnableProbeId(), new ArrayList<DataPointsRollup>());
			finishedRollups11d.get(dataPointsRollup.getRunnableProbeId()).add(dataPointsRollup);
			break;
		}

		return true;
	}

	private DataPointsRollupSize getRollupSize(int i) {
		switch (i) {
		case 0:
			return DataPointsRollupSize._11day;
		case 1:
			return DataPointsRollupSize._36hour;
		case 2:
			return DataPointsRollupSize._6hour;
		case 3:
			return DataPointsRollupSize._1hour;
		case 4:
			return DataPointsRollupSize._20minutes;
		case 5:
			return DataPointsRollupSize._4minutes;
		default:
			return null;
		}
	}

	private void addSnmpResult(BaseResult result) {
		SnmpResult snmpResults = (SnmpResult) result;
		DataPointsRollup[] snmpRollups = snmpDataRollups.get(result.getRunnableProbeId());
		if (snmpRollups == null)
			snmpDataRollups.put(result.getRunnableProbeId(), new DataPointsRollup[6]);

		for (int i = 0; i < result.getNumberOfRollupTables(); i++) {
			DataPointsRollup snmpDataRollup = snmpDataRollups.get(result.getRunnableProbeId())[i];
			if (snmpDataRollup == null) {
				snmpDataRollup = new DataPointsRollup(result.getRunnableProbeId(), this.getRollupSize(i));
				snmpDataRollups.get(result.getRunnableProbeId())[i] = snmpDataRollup;
			}
			snmpDataRollup.add(snmpResults.getLastTimestamp(), snmpResults.getNumData());
		}
	}

	private void addWeberResult(BaseResult result) {
		WebResult weberResults = (WebResult) result;
		DataPointsRollup[] responseTimeRollups = webResponseTimeRollups.get(result.getRunnableProbeId());
		if (responseTimeRollups == null)
			webResponseTimeRollups.put(result.getRunnableProbeId(), new DataPointsRollup[6]);

		for (int i = 0; i < result.getNumberOfRollupTables(); i++) {
			DataPointsRollup responseTimeRollup = webResponseTimeRollups.get(result.getRunnableProbeId())[i];
			if (responseTimeRollup == null) {
				responseTimeRollup = new DataPointsRollup(result.getRunnableProbeId(), this.getRollupSize(i));
				webResponseTimeRollups.get(result.getRunnableProbeId())[i] = responseTimeRollup;
			}
			responseTimeRollup.add(weberResults.getLastTimestamp(), weberResults.getResponseTime());
		}
	}

	private void addPorterResult(BaseResult result) {
		PortResult porterResults = (PortResult) result;
		DataPointsRollup[] responseTimeRollups = portResponseTimeRollups.get(result.getRunnableProbeId());
		if (responseTimeRollups == null)
			portResponseTimeRollups.put(result.getRunnableProbeId(), new DataPointsRollup[6]);
		for (int i = 0; i < result.getNumberOfRollupTables(); i++) {
			DataPointsRollup responseTimeRollup = portResponseTimeRollups.get(result.getRunnableProbeId())[i];
			if (responseTimeRollup == null) {
				responseTimeRollup = new DataPointsRollup(result.getRunnableProbeId(), this.getRollupSize(i));
				portResponseTimeRollups.get(result.getRunnableProbeId())[i] = responseTimeRollup;
			}
			responseTimeRollup.add(porterResults.getLastTimestamp(), porterResults.getResponseTime());
		}
	}

	private void addPingerResult(BaseResult result) {
		PingResult pingerResults = (PingResult) result;
		DataPointsRollup[] packetLostRollups = packetLossRollups.get(result.getRunnableProbeId());
		DataPointsRollup[] pingResponseTimeRollups = rttRollups.get(result.getRunnableProbeId());

		if (packetLostRollups == null || pingResponseTimeRollups == null) {
			packetLossRollups.put(result.getRunnableProbeId(), new DataPointsRollup[6]);
			rttRollups.put(result.getRunnableProbeId(), new DataPointsRollup[6]);

		}
		for (int i = 0; i < result.getNumberOfRollupTables(); i++) {
			DataPointsRollup packetLostRollup = packetLossRollups.get(result.getRunnableProbeId())[i];
			DataPointsRollup rttRollup = rttRollups.get(result.getRunnableProbeId())[i];

			if (packetLostRollup == null || rttRollup == null) {
				packetLostRollup = new DataPointsRollup(result.getRunnableProbeId(), this.getRollupSize(i));
				packetLossRollups.get(result.getRunnableProbeId())[i] = packetLostRollup;
				rttRollup = new DataPointsRollup(result.getRunnableProbeId(), this.getRollupSize(i));
				rttRollups.get(result.getRunnableProbeId())[i] = rttRollup;
			}
			packetLostRollup.add(pingerResults.getLastTimestamp(), pingerResults.getPacketLost());
			rttRollup.add(pingerResults.getLastTimestamp(), pingerResults.getRtt());
		}
	}

}
