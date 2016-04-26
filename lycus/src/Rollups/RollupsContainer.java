package Rollups;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;

import DAL.ApiInterface;
import DAL.DAL;
import GlobalConstants.DataPointsRollupSize;
import GlobalConstants.Enums;
import GlobalConstants.Enums.SnmpError;
import Interfaces.IRollupsContainer;
import Results.BaseResult;
import Results.NicResult;
import Results.PingResult;
import Results.PortResult;
import Results.SnmpResult;
import Results.WebResult;
import Utils.GeneralFunctions;
import Utils.JsonUtil;
import Utils.Logit;
import lycus.DataPointsRollup;
import lycus.ResultsContainer;
import lycus.RunnableProbeContainer;

public class RollupsContainer implements IRollupsContainer {

	private static RollupsContainer instance;
	private HashMap<String, DataPointsRollup[]> packetLossRollups = new HashMap<String, DataPointsRollup[]>();
	private HashMap<String, DataPointsRollup[]> rttRollups = new HashMap<String, DataPointsRollup[]>();
	private HashMap<String, DataPointsRollup[]> portResponseTimeRollups = new HashMap<String, DataPointsRollup[]>();
	private HashMap<String, DataPointsRollup[]> webResponseTimeRollups = new HashMap<String, DataPointsRollup[]>();
	private HashMap<String, DataPointsRollup[]> snmpDataRollups = new HashMap<String, DataPointsRollup[]>();
	private HashMap<String, DataPointsRollup[]> nicInDataRollups = new HashMap<String, DataPointsRollup[]>();
	private HashMap<String, DataPointsRollup[]> nicOutDataRollups = new HashMap<String, DataPointsRollup[]>();

	// Those are finished rollups
	// private JSONObject finishedRollups4m = new JSONObject(); //
	// JSONObject<runnableProbeId,JSONArray<DataPointsRollup>>
	// private JSONObject finishedRollups20m = new JSONObject();
	// private JSONObject finishedRollups1h = new JSONObject();
	// private JSONObject finishedRollups6h = new JSONObject();
	// private JSONObject finishedRollups36h = new JSONObject();
	// private JSONObject finishedRollups11d = new JSONObject();

	private JSONArray finishedRollups = new JSONArray();

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
		} else if (result instanceof NicResult) {
			addNicResult(result);
		}
		return false;
	}

	@Override
	public synchronized String getAllFinsihedRollups() {

		try {
			JSONObject rollups = new JSONObject();

			for (int i = 0; i < 6; i++) {
				for (Map.Entry<String, DataPointsRollup[]> rolups : packetLossRollups.entrySet()) {
					DataPointsRollup[] dataPointsRollups = rttRollups.get(rolups.getKey());
					addFinished(i, rolups.getValue(), dataPointsRollups);

				}
				// for (DataPointsRollup[] rolups : packetLossRollups.values())
				// {
				//
				// addFinished(i, rolups);
				// }

				// finishedRollups.a
				// for (DataPointsRollup[] rolups : rttRollups.values()) {
				// addFinished(i, rolups);
				// }

				// finishedRollups // otef otam

				for (DataPointsRollup[] rolups : webResponseTimeRollups.values()) {
					addFinished(i, rolups);
				}
				for (DataPointsRollup[] rolups : portResponseTimeRollups.values()) {
					addFinished(i, rolups);
				}
				for (DataPointsRollup[] rolups : snmpDataRollups.values()) {
					addFinished(i, rolups);
				}

				for (Map.Entry<String, DataPointsRollup[]> rolups : nicInDataRollups.entrySet()) {
					DataPointsRollup[] dataPointsRollups = nicOutDataRollups.get(rolups.getKey());
					addFinished(i, rolups.getValue(), dataPointsRollups);

				}
			}

			// finishedRollups // otef hakol

			// if (finishedRollups4m.size() != 0)
			// rollups.put("rollups4m", JsonUtil.ToJson(finishedRollups4m));
			//
			// if (finishedRollups20m.size() != 0)
			// rollups.put("rollups20m", JsonUtil.ToJson(finishedRollups20m));
			//
			// if (finishedRollups1h.size() != 0)
			// rollups.put("rollups1h", JsonUtil.ToJson(finishedRollups1h));
			//
			// if (finishedRollups6h.size() != 0)
			// rollups.put("rollups6h", JsonUtil.ToJson(finishedRollups6h));
			//
			// if (finishedRollups36h.size() != 0)
			// rollups.put("rollups36h", JsonUtil.ToJson(finishedRollups36h));
			//
			// if (finishedRollups11d.size() != 0)
			// rollups.put("rollups11d", JsonUtil.ToJson(finishedRollups11d));

			if (finishedRollups.size() == 0)
				return null;

			// rollups.put("packetLossRollups",
			// JsonUtil.ToJson(packetLossRollups));
			// rollups.put("rttRollups", JsonUtil.ToJson(rttRollups));
			// rollups.put("portResponseTimeRollups",
			// JsonUtil.ToJson(portResponseTimeRollups));
			// rollups.put("webResponseTimeRollups",
			// JsonUtil.ToJson(webResponseTimeRollups));
			// rollups.put("snmpDataRollups", JsonUtil.ToJson(snmpDataRollups));

			if (finishedRollups.contains(
					"0b05919c-6cc0-42cc-a74b-de3b0dcd4a2a@e7ecd619-b7a5-49b7-a849-b2c9b1b64bf3@snmp_18e51b8d-807e-45c0-b5bc-d047da3e0876"))
				Logit.LogDebug("BREAKPOINT");

			return finishedRollups.toString();
		} catch (Exception e) {
			Logit.LogFatal("RollupsContainer - getAllFinsihedRollups()",
					"Error getting finished rollups from rollupsContainer! E: " + e.getMessage());
			Logit.LogFatal("RollupsContainer - getAllFinsihedRollups()", "trace: " + e.getStackTrace().toString());
			return null;
		}
	}

	@Override
	public synchronized String getAllCurrentLiveRollups() {
		JSONObject rollups = new JSONObject();

		rollups.put("packetLossRollups", JsonUtil.ToJson(packetLossRollups));
		rollups.put("rttRollups", JsonUtil.ToJson(rttRollups));
		rollups.put("portResponseTimeRollups", JsonUtil.ToJson(portResponseTimeRollups));
		rollups.put("webResponseTimeRollups", JsonUtil.ToJson(webResponseTimeRollups));
		rollups.put("snmpDataRollups", JsonUtil.ToJson(snmpDataRollups));

		if (rollups.toString().contains(
				"788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_7be55137-c5d8-438e-bca7-325f56656071"))
			Logit.LogDebug("BREAKPOINT");

		return rollups.toString();
	}

	private void addFinished(int i, DataPointsRollup[] rolups) {
		DataPointsRollup currentDataRollup = rolups[i];
		if (currentDataRollup == null)
			return;
		DataPointsRollup finishedDataRollup = currentDataRollup.getLastFinishedRollup();
		if (finishedDataRollup == null)
			return;

		addFinishedRollup(finishedDataRollup);
		rolups[i] = null;

		// addRollupTo(i, finishedDataRollup);
		currentDataRollup.setLastFinishedRollup(null);
	}

	private void addFinished(int i, DataPointsRollup[] rolups1, DataPointsRollup[] rolups2) {
		DataPointsRollup currentDataRollup1 = rolups1[i];
		DataPointsRollup currentDataRollup2 = rolups2[i];

		if (currentDataRollup1 == null || currentDataRollup2 == null)
			return;
		DataPointsRollup finishedDataRollup1 = currentDataRollup1.getLastFinishedRollup();
		DataPointsRollup finishedDataRollup2 = currentDataRollup2.getLastFinishedRollup();

		if (finishedDataRollup1 == null || finishedDataRollup2 == null)
			return;

		addFinishedRollup(finishedDataRollup1, finishedDataRollup2);
		rolups1[i] = null;
		rolups2[i] = null;

		// addRollupTo(i, finishedDataRollup);
		currentDataRollup1.setLastFinishedRollup(null);
		currentDataRollup2.setLastFinishedRollup(null);

	}

	// @Override
	// public boolean mergeRollups(JSONArray jsonArray) {
	// Logit.LogInfo("Retrieving existing rollups from DB...");
	// Object rollupsEncoded =
	// ApiInterface.executeRequest(Enums.ApiAction.GetServerMemoryDump, "GET",
	// null);
	//
	// if (rollupsEncoded == null || ((String) rollupsEncoded).equals("0\n")) {
	// Logit.LogWarn("Unable to retrieve existing rollups, trying again in about
	// 30 secs...");
	// return false;
	// }
	//
	// String rollups = ((String) rollupsEncoded).substring(1, ((String)
	// rollupsEncoded).length() - 1);
	//
	// ArrayList<DataPointsRollup[][]> rollupses =
	// this.deserializeRollups(rollups);
	// for (DataPointsRollup[][] rollupsResult : rollupses) {
	// DataPointsRollup sampleRollup = rollupsResult[0][0];
	// String rpID = sampleRollup.getRunnableProbeId();
	//
	// BaseResult rpr = ResultsContainer.getInstance().getResult(rpID);
	//
	// // if (rpr != null)
	// // rpr.insertExistingRollups(rollupsResult);
	// // else {
	// // handle runnable probe without results object
	// // }
	// }
	//
	// return false;
	// }

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

	// private boolean addRollupTo(int rollupType, DataPointsRollup
	// dataPointsRollup) {
	//
	//
	//
	// switch (rollupType) {
	// case 5:
	// if (finishedRollups4m.get(dataPointsRollup.getRunnableProbeId()) == null)
	// finishedRollups4m.put(dataPointsRollup.getRunnableProbeId(), new
	// JSONArray());
	// ((JSONArray)finishedRollups4m.get(dataPointsRollup.getRunnableProbeId())).add(rollupResultsDBFormat(dataPointsRollup));
	// break;
	// case 4:
	// if (finishedRollups20m.get(dataPointsRollup.getRunnableProbeId()) ==
	// null)
	// finishedRollups20m.put(dataPointsRollup.getRunnableProbeId(), new
	// JSONArray());
	// ((JSONArray)finishedRollups20m.get(dataPointsRollup.getRunnableProbeId())).add(rollupResultsDBFormat(dataPointsRollup));
	// break;
	// case 3:
	// if (finishedRollups1h.get(dataPointsRollup.getRunnableProbeId()) == null)
	// finishedRollups1h.put(dataPointsRollup.getRunnableProbeId(), new
	// JSONArray());
	// ((JSONArray)finishedRollups1h.get(dataPointsRollup.getRunnableProbeId())).add(rollupResultsDBFormat(dataPointsRollup));
	// break;
	// case 2:
	// if (finishedRollups6h.get(dataPointsRollup.getRunnableProbeId()) == null)
	// finishedRollups6h.put(dataPointsRollup.getRunnableProbeId(), new
	// JSONArray());
	// ((JSONArray)finishedRollups6h.get(dataPointsRollup.getRunnableProbeId())).add(rollupResultsDBFormat(dataPointsRollup));
	// break;
	// case 1:
	// if (finishedRollups36h.get(dataPointsRollup.getRunnableProbeId()) ==
	// null)
	// finishedRollups36h.put(dataPointsRollup.getRunnableProbeId(), new
	// JSONArray());
	// ((JSONArray)finishedRollups36h.get(dataPointsRollup.getRunnableProbeId())).add(rollupResultsDBFormat(dataPointsRollup));
	// break;
	// case 0:
	// if (finishedRollups11d.get(dataPointsRollup.getRunnableProbeId()) ==
	// null)
	// finishedRollups11d.put(dataPointsRollup.getRunnableProbeId(), new
	// JSONArray());
	// ((JSONArray)finishedRollups11d.get(dataPointsRollup.getRunnableProbeId())).add(rollupResultsDBFormat(dataPointsRollup));
	// break;
	// }
	//
	// return true;
	// }

	private boolean addFinishedRollup(DataPointsRollup dataPointsRollup) {

		finishedRollups.add(rollupResultsDBFormat(dataPointsRollup));
		return true;
	}

	private boolean addFinishedRollup(DataPointsRollup dataPointsRollup1, DataPointsRollup dataPointsRollup2) {

		finishedRollups.add(rollupResultsDBFormat(dataPointsRollup1, dataPointsRollup2));
		return true;
	}

	private String rollupResultsDBFormat(DataPointsRollup dataPointsRollup1, DataPointsRollup dataPointsRollup2) {
		JSONObject rollup = new JSONObject();

		// rollup.put("USER_ID",
		// rp.getProbe().getUser().getUserId().toString());
		rollup.put("RESULTS_TIME", dataPointsRollup1.getEndTime());
		// rollup.put("RESULTS_NAME", resultkey.split("@")[1]);
		JSONArray resultsStrings = new JSONArray();
		resultsStrings.add(dataPointsRollup1.getResultString());
		resultsStrings.add(dataPointsRollup2.getResultString());
		rollup.put("RESULTS", resultsStrings.toString());
		rollup.put("RUNNABLE_PROBE_ID", dataPointsRollup1.getRunnableProbeId());
		rollup.put("ROLLUP_SIZE", dataPointsRollup1.getTimePeriod().toString());
		rollup.put("USER_ID", RunnableProbeContainer.getInstanece().get(dataPointsRollup1.getRunnableProbeId())
				.getProbe().getUser().getUserId().toString());

		return rollup.toString();

	}

	private String rollupResultsDBFormat(DataPointsRollup dataPointsRollup) {
		JSONObject rollup = new JSONObject();

		if (dataPointsRollup.getRunnableProbeId().contains(
				"0b05919c-6cc0-42cc-a74b-de3b0dcd4a2a@98437013-a93f-4b27-9963-a4800860b90f@snmp_924430db-e1d7-43ce-ba98-a9b7883a440a"))
			Logit.LogDebug("BREAKPOINT");
		// rollup.put("USER_ID",
		// rp.getProbe().getUser().getUserId().toString());
		rollup.put("RESULTS_TIME", dataPointsRollup.getEndTime());
		// rollup.put("RESULTS_NAME", resultkey.split("@")[1]);
		JSONArray resultsStrings = new JSONArray();
		resultsStrings.add(dataPointsRollup.getResultString());
		rollup.put("RESULTS", resultsStrings.toString());
		rollup.put("RUNNABLE_PROBE_ID", dataPointsRollup.getRunnableProbeId());
		rollup.put("ROLLUP_SIZE", dataPointsRollup.getTimePeriod().toString());
		rollup.put("USER_ID", RunnableProbeContainer.getInstanece().get(dataPointsRollup.getRunnableProbeId())
				.getProbe().getUser().getUserId().toString());
		return rollup.toString();

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

		if (result.getRunnableProbeId()
				.contains("788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_7be55137-c5d8-438e-bca7-325f56656071"))
			Logit.LogDebug("BREAKPOINT");

		DataPointsRollup[] snmpRollups = snmpDataRollups.get(result.getRunnableProbeId());
		if (snmpRollups == null)
			snmpDataRollups.put(result.getRunnableProbeId(), new DataPointsRollup[6]);

		for (int i = 0; i < result.getNumberOfRollupTables(); i++) {
			DataPointsRollup snmpDataRollup = snmpDataRollups.get(result.getRunnableProbeId())[i];
			if (snmpDataRollup == null) {
				snmpDataRollup = new DataPointsRollup(result.getRunnableProbeId(), this.getRollupSize(i));
				snmpDataRollups.get(result.getRunnableProbeId())[i] = snmpDataRollup;
			}
			if (snmpResults.getNumData() == null)
				break;
			snmpDataRollup.add(snmpResults.getLastTimestamp(), snmpResults.getNumData());
		}
	}

	private void addNicResult(BaseResult result) {
		NicResult nicResults = (NicResult) result;

		if (nicResults.getError() == SnmpError.NO_COMUNICATION)
			return;

		DataPointsRollup[] nicInRollups = nicInDataRollups.get(result.getRunnableProbeId());
		DataPointsRollup[] nicOutRollups = nicOutDataRollups.get(result.getRunnableProbeId());

		if (nicInRollups == null || nicOutRollups == null) {
			nicInDataRollups.put(result.getRunnableProbeId(), new DataPointsRollup[6]);
			nicOutDataRollups.put(result.getRunnableProbeId(), new DataPointsRollup[6]);
		}
		for (int i = 0; i < result.getNumberOfRollupTables(); i++) {
			DataPointsRollup nicInRollup = nicInDataRollups.get(result.getRunnableProbeId())[i];
			DataPointsRollup nicOutRollup = nicOutDataRollups.get(result.getRunnableProbeId())[i];

			if (nicInRollup == null || nicOutRollup == null) {
				nicInRollup = new DataPointsRollup(result.getRunnableProbeId(), this.getRollupSize(i));
				nicOutRollup = new DataPointsRollup(result.getRunnableProbeId(), this.getRollupSize(i));

				nicInDataRollups.get(result.getRunnableProbeId())[i] = nicInRollup;
				nicOutDataRollups.get(result.getRunnableProbeId())[i] = nicOutRollup;

			}
			Logit.LogDebug("BREAKPOINT");
			if(nicResults.getInBW()==null||nicResults.getOutBW()==null)
				continue;
			nicInRollup.add(nicResults.getLastTimestamp(), nicResults.getInBW());
			nicOutRollup.add(nicResults.getLastTimestamp(), nicResults.getOutBW());

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

	@Override
	public boolean mergeExistingRollupsFromMemDump() {
		Logit.LogInfo("Retrieving existing rollups from DB...");

		// TODO: finish mergeExistingRollupsFromMemDump()

		JSONObject rollupsUnDecoded = DAL.getInstanece().get(Enums.ApiAction.GetServerMemoryDump);

		// Object rollupsUnDecoded =
		// ApiInterface.executeRequest(Enums.ApiAction.GetServerMemoryDump,
		// "GET", null);

		if (rollupsUnDecoded == null) {
			Logit.LogWarn("Unable to retrieve existing rollups!");
			return false;
		}

		if (rollupsUnDecoded.get("last_rollups") == null) {
			return true;
		}

		String rollups = (String) rollupsUnDecoded.get("last_rollups");

		String rollupsDecoded = GeneralFunctions.Base64Decode(rollups);

		JSONParser jsonParser = new JSONParser();
		JSONObject rollupsJson = null;
		try {
			rollupsJson = (JSONObject) jsonParser.parse(rollupsDecoded);
		} catch (Exception e) {
			Logit.LogError("RollupsContainer - mergeExistingRollupsFromMemDump",
					"Unable to parse rollups dump to JSON objects. E: " + e.getMessage());
			return false;
		}

		JSONObject pakcketLossRollupsJson = null;
		JSONObject rttRollupsJson = null;
		JSONObject portResponseTimeRollupsJson = null;
		JSONObject webResponseTimeRollupsJson = null;
		JSONObject snmpDataRollupsJson = null;

		try {
			pakcketLossRollupsJson = (JSONObject) jsonParser.parse((String) rollupsJson.get("packetLossRollups"));
			rttRollupsJson = (JSONObject) jsonParser.parse((String) rollupsJson.get("rttRollups"));
			portResponseTimeRollupsJson = (JSONObject) jsonParser
					.parse((String) rollupsJson.get("portResponseTimeRollups"));
			webResponseTimeRollupsJson = (JSONObject) jsonParser
					.parse((String) rollupsJson.get("webResponseTimeRollups"));
			snmpDataRollupsJson = (JSONObject) jsonParser.parse((String) rollupsJson.get("snmpDataRollups"));
		} catch (Exception e) {
			Logit.LogError("RollupsContainer - mergeExistingRollupsFromMemDump",
					"Unable to parse rollups dump to JSON objects. E: " + e.getMessage());
			return false;
		}
		HashMap<String, DataPointsRollup[]> packetLossRollupsFromDump;
		HashMap<String, DataPointsRollup[]> rttRollupsFromDump;
		HashMap<String, DataPointsRollup[]> portResponseTimeRollupsFromDump;
		HashMap<String, DataPointsRollup[]> webResponseTimeRollupsFromDump;
		HashMap<String, DataPointsRollup[]> snmpDataRollupsFromDump;
		try {
			Type rollupsMapType = new TypeToken<HashMap<String, DataPointsRollup[]>>() {}.getType();
			
			packetLossRollupsFromDump =JsonUtil.ToObject(pakcketLossRollupsJson, rollupsMapType);
			rttRollupsFromDump =JsonUtil.ToObject(rttRollupsJson, rollupsMapType);
			portResponseTimeRollupsFromDump =JsonUtil.ToObject(portResponseTimeRollupsJson, rollupsMapType);
			webResponseTimeRollupsFromDump =JsonUtil.ToObject(webResponseTimeRollupsJson, rollupsMapType);
			snmpDataRollupsFromDump =JsonUtil.ToObject(snmpDataRollupsJson, rollupsMapType);


		} catch (Exception e) {
			Logit.LogError("RollupsContainer - mergeExistingRollupsFromMemDump",
					"Unable to parse rollups dump to JSON objects. E: " + e.getMessage());
			return false;

		}

		this.mergeRollups(packetLossRollupsFromDump, packetLossRollups);
		this.mergeRollups(rttRollupsFromDump, rttRollups);
		this.mergeRollups(portResponseTimeRollupsFromDump, portResponseTimeRollups);
		this.mergeRollups(webResponseTimeRollupsFromDump, webResponseTimeRollups);
		this.mergeRollups(snmpDataRollupsFromDump, snmpDataRollups);

		return true;
	}

	private void mergeRollups(HashMap<String, DataPointsRollup[]> rollupsFromDump,
			HashMap<String, DataPointsRollup[]> currentRollups) {
		for (Map.Entry<String, DataPointsRollup[]> runnableProbeIdRollups : rollupsFromDump.entrySet()) {

			if (runnableProbeIdRollups.getKey()
					.contains("788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_7be55137-c5d8-438e-bca7-325f56656071")) {
				Logit.LogDebug("BREAKPOINT");
			}DataPointsRollup dumpRollups[]=new DataPointsRollup[6];DataPointsRollup[] existingRollups=new DataPointsRollup[6];
			try{
				 dumpRollups = runnableProbeIdRollups.getValue();
				existingRollups = currentRollups.get(runnableProbeIdRollups.getKey());
			}
			catch(Exception e)
			{
				Logit.LogDebug("BREAKPOINT");
				continue;
			}
			if (dumpRollups == null || existingRollups == null)
				continue;
			else {
				for (int i = 0; i < dumpRollups.length; i++) {
					if (dumpRollups[i] != null && existingRollups[i] == null)
						existingRollups[i] = dumpRollups[i];
					else if (dumpRollups[i] == null && existingRollups[i] != null)
						continue;
					else
						existingRollups[i].mergeRollup(dumpRollups[i]);
				}
			}
		}
	}

}
