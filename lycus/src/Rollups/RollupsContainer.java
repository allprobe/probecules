package Rollups;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
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
import DAL.DAL;
import GlobalConstants.DataPointsRollupSize;
import GlobalConstants.DataPointsRollupType;
import GlobalConstants.Enums;
import GlobalConstants.Enums.SnmpError;
import Interfaces.IRollupsContainer;
import Results.BaseResult;
import Results.DiskResult;
import Results.NicResult;
import Results.PingResult;
import Results.PortResult;
import Results.SnmpResult;
import Results.SqlResult;
import Results.WebResult;
import Utils.GeneralFunctions;
import Utils.JsonUtil;
import Utils.Logit;
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
	private HashMap<String, DataPointsRollup[]> diskSizeDataRollups = new HashMap<String, DataPointsRollup[]>();
	private HashMap<String, DataPointsRollup[]> diskUsedDataRollups = new HashMap<String, DataPointsRollup[]>();
	private HashMap<String, DataPointsRollup[]> diskFreeDataRollups = new HashMap<String, DataPointsRollup[]>();
	private HashMap<String, HashMap<String, DataPointsRollup[]>> sqlDataRollups = new HashMap<String, HashMap<String, DataPointsRollup[]>>();

	private JSONArray finishedRollups = new JSONArray();
	private Object lockFinishedRollups = new Object();
	private boolean rollupsMergedAtStart;
	// private Object lockRollupsTable = new Object();

	public static RollupsContainer getInstance() {
		if (instance == null) {
			instance = new RollupsContainer();
		}
		return instance;
	}

	@Override
	public boolean addResult(BaseResult result) {

		// if (!this.isRollupsMergedAtStart())
		// return false;

		if (result.getRunnableProbeId().contains("discovery_d3c95875-4947-4388-989f-64ffd863c704@dmVuZXQw"))
			Logit.LogDebug("BREAKPOINT");

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
		} else if (result instanceof DiskResult) {
			addDiskResult(result);
		} else if (result instanceof SqlResult) {
			addSqlResult(result);
		}
		return true;
	}

	@Override
	public synchronized String getAllFinsihedRollups() {
		String allRollups;
		try {
			synchronized (lockFinishedRollups) {
				allRollups = finishedRollups.toString();
				finishedRollups = new JSONArray();
			}

			return allRollups;
		} catch (Exception e) {
			Logit.LogFatal("RollupsContainer - getAllFinsihedRollups()",
					"Error getting finished rollups from rollupsContainer! E: " + e.getMessage(), e);
			return null;
		}
	}

	@Override
	public synchronized String getAllCurrentLiveRollups() {
		JSONObject rollups = new JSONObject();

		for (DataPointsRollupType rollupType : DataPointsRollupType.values()) {
			rollups.put(rollupType.name(), JsonUtil.ToJson(getRollupsTable(rollupType)));
		}

		if (rollups.toString()
				.contains("788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_7be55137-c5d8-438e-bca7-325f56656071"))
			Logit.LogDebug("BREAKPOINT");

		return rollups.toString();
	}

	private HashMap<String, DataPointsRollup[]> getRollupsTable(DataPointsRollupType rollupType) {
		switch (rollupType) {
		case packetLoss:
			return this.packetLossRollups;
		case rtt:
			return this.rttRollups;
		case webResponseTime:
			return this.webResponseTimeRollups;
		case snmpData:
			return this.snmpDataRollups;
		case portResponseTime:
			return this.portResponseTimeRollups;
		case nicInData:
			return this.nicInDataRollups;
		case nicOutData:
			return this.nicOutDataRollups;
		case diskSize:
			return this.diskSizeDataRollups;
		case diskUsed:
			return this.diskSizeDataRollups;
		}
		return null;
	}

	private void addFinished(int i, DataPointsRollup[] rolups) {
		if (rolups == null)
			return;
		DataPointsRollup currentDataRollup = rolups[i];
		if (currentDataRollup == null)
			return;
		DataPointsRollup finishedDataRollup = currentDataRollup.isCompleted() ? currentDataRollup : null;

		if (finishedDataRollup == null)
			return;

		addFinishedRollup(finishedDataRollup);
		rolups[i] = null;
	}

	private void addFinished(int i, DataPointsRollup[] rolups1, DataPointsRollup[] rolups2) {
		if (rolups1 == null || rolups2 == null)
			return;

		DataPointsRollup currentDataRollup1 = rolups1[i];
		DataPointsRollup currentDataRollup2 = rolups2[i];

		if (currentDataRollup1 == null || currentDataRollup2 == null)
			return;

		DataPointsRollup finishedDataRollup1 = currentDataRollup1.isCompleted() ? currentDataRollup1 : null;
		DataPointsRollup finishedDataRollup2 = currentDataRollup2.isCompleted() ? currentDataRollup2 : null;

		if (finishedDataRollup1 == null || finishedDataRollup2 == null)
			return;

		String rpStr = finishedDataRollup1.getRunnableProbeId();
		if (rpStr.contains("discovery_d3c95875-4947-4388-989f-64ffd863c704"))
			Logit.LogDebug("BREAKPOINT");

		addFinishedRollup(finishedDataRollup1, finishedDataRollup2);
		rolups1[i] = null;
		rolups2[i] = null;
	}

	private void addFinished(int i, DataPointsRollup[] rolups1, DataPointsRollup[] rolups2,
			DataPointsRollup[] rolups3) {
		if (rolups1 == null || rolups2 == null || rolups3 == null)
			return;

		DataPointsRollup currentDataRollup1 = rolups1[i];
		DataPointsRollup currentDataRollup2 = rolups2[i];
		DataPointsRollup currentDataRollup3 = rolups3[i];

		if (currentDataRollup1 == null || currentDataRollup2 == null || currentDataRollup3 == null)
			return;

		DataPointsRollup finishedDataRollup1 = currentDataRollup1.isCompleted() ? currentDataRollup1 : null;
		DataPointsRollup finishedDataRollup2 = currentDataRollup2.isCompleted() ? currentDataRollup2 : null;
		DataPointsRollup finishedDataRollup3 = currentDataRollup3.isCompleted() ? currentDataRollup3 : null;

		if (finishedDataRollup1 == null || finishedDataRollup2 == null || finishedDataRollup3 == null)
			return;

		String rpStr = finishedDataRollup1.getRunnableProbeId();
		if (rpStr.contains("discovery_d3c95875-4947-4388-989f-64ffd863c704"))
			Logit.LogDebug("BREAKPOINT");

		addFinishedRollup(finishedDataRollup1, finishedDataRollup2, finishedDataRollup3);
		rolups1[i] = null;
		rolups2[i] = null;
		rolups3[i] = null;
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

	private boolean addFinishedRollup(DataPointsRollup dataPointsRollup) {
		synchronized (lockFinishedRollups) {
			finishedRollups.add(rollupResultsDBFormat(dataPointsRollup));
		}
		return true;
	}

	private boolean addFinishedRollup(DataPointsRollup dataPointsRollup1, DataPointsRollup dataPointsRollup2) {
		synchronized (lockFinishedRollups) {
			finishedRollups.add(rollupResultsDBFormat(dataPointsRollup1, dataPointsRollup2));
		}
		return true;
	}

	private boolean addFinishedRollup(String field, DataPointsRollup dataPointsRollup) {
		synchronized (lockFinishedRollups) {
			finishedRollups.add(rollupResultsSqlDBFormat(field, dataPointsRollup));
		}
		return true;
	}

	private boolean addFinishedRollup(DataPointsRollup dataPointsRollup1, DataPointsRollup dataPointsRollup2,
			DataPointsRollup dataPointsRollup3) {
		synchronized (lockFinishedRollups) {
			finishedRollups.add(rollupResultsDBFormat(dataPointsRollup1, dataPointsRollup2, dataPointsRollup3));
		}
		return true;
	}

	private JSONObject rollupResultsDBFormat(DataPointsRollup dataPointsRollup1, DataPointsRollup dataPointsRollup2) {
		JSONObject rollup = new JSONObject();

		rollup.put("RESULTS_TIME", System.currentTimeMillis());
		JSONArray resultsStrings = new JSONArray();
		resultsStrings.add(dataPointsRollup1.getRollupObj());
		resultsStrings.add(dataPointsRollup2.getRollupObj());
		rollup.put("RESULTS", resultsStrings.toString());
		rollup.put("RUNNABLE_PROBE_ID", dataPointsRollup1.getRunnableProbeId());
		rollup.put("ROLLUP_SIZE", dataPointsRollup1.getTimePeriod().toString());
		rollup.put("USER_ID", RunnableProbeContainer.getInstanece().get(dataPointsRollup1.getRunnableProbeId())
				.getProbe().getUser().getUserId().toString());

		return rollup;
	}

	private JSONObject rollupResultsDBFormat(DataPointsRollup dataPointsRollup1, DataPointsRollup dataPointsRollup2,
			DataPointsRollup dataPointsRollup3) {
		JSONObject rollup = new JSONObject();

		rollup.put("RESULTS_TIME", System.currentTimeMillis());
		JSONArray resultsStrings = new JSONArray();
		resultsStrings.add(dataPointsRollup1.getRollupObj());
		resultsStrings.add(dataPointsRollup2.getRollupObj());
		resultsStrings.add(dataPointsRollup3.getRollupObj());
		rollup.put("RESULTS", resultsStrings.toString());
		rollup.put("RUNNABLE_PROBE_ID", dataPointsRollup1.getRunnableProbeId());
		rollup.put("ROLLUP_SIZE", dataPointsRollup1.getTimePeriod().toString());
		rollup.put("USER_ID", RunnableProbeContainer.getInstanece().get(dataPointsRollup1.getRunnableProbeId())
				.getProbe().getUser().getUserId().toString());

		return rollup;
	}

	private JSONObject rollupResultsDBFormat(DataPointsRollup dataPointsRollup) {
		JSONObject rollup = new JSONObject();

		if (dataPointsRollup.getRunnableProbeId().contains(
				"0b05919c-6cc0-42cc-a74b-de3b0dcd4a2a@98437013-a93f-4b27-9963-a4800860b90f@snmp_924430db-e1d7-43ce-ba98-a9b7883a440a"))
			Logit.LogDebug("BREAKPOINT");
		rollup.put("RESULTS_TIME", System.currentTimeMillis());
		JSONArray resultsStrings = new JSONArray();
		resultsStrings.add(dataPointsRollup.getRollupObj());
		rollup.put("RESULTS", resultsStrings.toString());
		rollup.put("RUNNABLE_PROBE_ID", dataPointsRollup.getRunnableProbeId());
		rollup.put("ROLLUP_SIZE", dataPointsRollup.getTimePeriod().toString());
		rollup.put("USER_ID", RunnableProbeContainer.getInstanece().get(dataPointsRollup.getRunnableProbeId())
				.getProbe().getUser().getUserId().toString());
		return rollup;
	}

	private JSONObject rollupResultsSqlDBFormat(String field, DataPointsRollup dataPointsRollup) {
		JSONObject rollup = new JSONObject();

		if (dataPointsRollup.getRunnableProbeId().contains(
				"0b05919c-6cc0-42cc-a74b-de3b0dcd4a2a@98437013-a93f-4b27-9963-a4800860b90f@snmp_924430db-e1d7-43ce-ba98-a9b7883a440a"))
			Logit.LogDebug("BREAKPOINT");
		rollup.put("RESULTS_TIME", System.currentTimeMillis());
		JSONObject resultObject = new JSONObject();
		JSONArray resultsStrings = new JSONArray();
		resultsStrings.add(dataPointsRollup.getRollupObj());
		resultObject.put(field, resultsStrings.toString());
		rollup.put("RESULTS", resultObject);
		rollup.put("RUNNABLE_PROBE_ID", dataPointsRollup.getRunnableProbeId());
		rollup.put("ROLLUP_SIZE", dataPointsRollup.getTimePeriod().toString());
		rollup.put("USER_ID", RunnableProbeContainer.getInstanece().get(dataPointsRollup.getRunnableProbeId())
				.getProbe().getUser().getUserId().toString());
		return rollup;
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

		if (result.getRunnableProbeId() == null)
			return;

		if (result.getRunnableProbeId()
				.contains("788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_7be55137-c5d8-438e-bca7-325f56656071"))
			Logit.LogDebug("BREAKPOINT");

		DataPointsRollup[] snmpRollups = snmpDataRollups.get(result.getRunnableProbeId());
		if (snmpRollups == null) {
			snmpRollups = new DataPointsRollup[6];
			snmpDataRollups.put(result.getRunnableProbeId(), snmpRollups);
		}
		for (int i = 0; i < result.getNumberOfRollupTables(); i++) {
			// if (result == null)
			// continue;
			DataPointsRollup snmpDataRollup = snmpRollups[i];
			if (snmpDataRollup == null) {
				snmpDataRollup = new DataPointsRollup(result.getRunnableProbeId(), this.getRollupSize(i));
				snmpRollups[i] = snmpDataRollup;
			}
			if (snmpResults.getNumData() == null || snmpResults.getLastTimestamp() == null)
				break;
			snmpDataRollup.add(snmpResults.getLastTimestamp(), snmpResults.getNumData());

			addFinished(i, snmpRollups);
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
			if (nicResults != null)
				if (nicResults.getOutBW() != null)
					if (nicResults.getOutBW() == 0)
						Logit.LogDebug("BREAKPOINT");
			if (nicResults.getInBW() == null || nicResults.getOutBW() == null)
				continue;
			nicInRollup.add(nicResults.getLastTimestamp(), nicResults.getInBW());
			nicOutRollup.add(nicResults.getLastTimestamp(), nicResults.getOutBW());

			addFinished(i, nicInRollups, nicOutRollups);
		}
	}

	private void addDiskResult(BaseResult result) {
		DiskResult diskResults = (DiskResult) result;

		if (diskResults.getError() == SnmpError.NO_COMUNICATION)
			return;

		DataPointsRollup[] diskSizeRollups = diskSizeDataRollups.get(result.getRunnableProbeId());
		DataPointsRollup[] diskUsedRollups = diskUsedDataRollups.get(result.getRunnableProbeId());
		DataPointsRollup[] diskFreeRollups = diskFreeDataRollups.get(result.getRunnableProbeId());

		if (diskSizeRollups == null || diskUsedRollups == null || diskFreeRollups == null) {
			diskSizeDataRollups.put(result.getRunnableProbeId(), new DataPointsRollup[6]);
			diskUsedDataRollups.put(result.getRunnableProbeId(), new DataPointsRollup[6]);
			diskFreeDataRollups.put(result.getRunnableProbeId(), new DataPointsRollup[6]);

		}
		for (int i = 0; i < result.getNumberOfRollupTables(); i++) {
			DataPointsRollup diskSizeRollup = diskSizeDataRollups.get(result.getRunnableProbeId())[i];
			DataPointsRollup diskUsedRollup = diskUsedDataRollups.get(result.getRunnableProbeId())[i];
			DataPointsRollup diskFreeRollup = diskFreeDataRollups.get(result.getRunnableProbeId())[i];

			if (diskSizeRollup == null || diskUsedRollup == null || diskFreeRollup == null) {
				diskSizeRollup = new DataPointsRollup(result.getRunnableProbeId(), this.getRollupSize(i));
				diskUsedRollup = new DataPointsRollup(result.getRunnableProbeId(), this.getRollupSize(i));
				diskFreeRollup = new DataPointsRollup(result.getRunnableProbeId(), this.getRollupSize(i));

				diskSizeDataRollups.get(result.getRunnableProbeId())[i] = diskSizeRollup;
				diskUsedDataRollups.get(result.getRunnableProbeId())[i] = diskUsedRollup;
				diskFreeDataRollups.get(result.getRunnableProbeId())[i] = diskFreeRollup;
			}

			Logit.LogDebug("BREAKPOINT");
			if (diskResults.getStorageSize() == null || diskResults.getStorageUsed() == null
					|| diskResults.getStorageFree() == null)
				continue;
			diskSizeRollup.add(diskResults.getLastTimestamp(), diskResults.getStorageSize());
			diskUsedRollup.add(diskResults.getLastTimestamp(), diskResults.getStorageUsed());
			diskFreeRollup.add(diskResults.getLastTimestamp(), diskResults.getStorageFree());

			addFinished(i, diskSizeRollups, diskUsedRollups, diskFreeRollups);

		}
	}

	private void addWeberResult(BaseResult result) {
		WebResult weberResults = (WebResult) result;

		if (weberResults.getStatusCode() == null)
			return;

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
			addFinished(i, responseTimeRollups);
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
			addFinished(i, responseTimeRollups);
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

			addFinished(i, packetLostRollups, pingResponseTimeRollups);
		}
	}

	private void addSqlResult(BaseResult result) {
		String runnableProbeId = result.getRunnableProbeId();
		SqlResult sqlResult = (SqlResult) result;
		HashMap<String, DataPointsRollup[]> sqlRollups = sqlDataRollups.get(runnableProbeId);

		if (sqlRollups == null) {
			sqlRollups = new HashMap<String, DataPointsRollup[]>();
			sqlDataRollups.put(runnableProbeId, sqlRollups);
		}

		for (String field : sqlResult.getSqlFields()) {
			DataPointsRollup[] fieldRollup = sqlRollups.get(field);

			if (fieldRollup == null) {
				fieldRollup = new DataPointsRollup[6];
				sqlRollups.put(field, fieldRollup);
			}

			for (int i = 0; i < result.getNumberOfRollupTables(); i++) {
				DataPointsRollup sqlRollup = null;
				if (fieldRollup[i] == null)
					fieldRollup[i] = new DataPointsRollup(runnableProbeId, this.getRollupSize(i));

				sqlRollup = fieldRollup[i];

				Double fieldResult = sqlResult.getSqlResult(field);
				sqlRollup.add(sqlResult.getLastTimestamp(), fieldResult);

				// addFinished(i, sqlResult, pingResponseTimeRollups);
				DataPointsRollup finishedDataRollup = sqlRollup.isCompleted() ? sqlRollup : null;

				if (finishedDataRollup == null)
					continue;

				addFinishedRollup(field, finishedDataRollup);
				// fieldRollup[i] = null;
			}
		}
	}

	@Override
	public boolean mergeExistingRollupsFromMemDump() {
		Logit.LogInfo("Retrieving existing rollups from DB...");

		// TODO: finish mergeExistingRollupsFromMemDump()

		JSONObject rollupsUnDecoded = DAL.getInstanece().get(Enums.ApiAction.GetServerMemoryDump);

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

		Type rollupsMapType = new TypeToken<HashMap<String, DataPointsRollup[]>>() {
		}.getType();

		for (DataPointsRollupType rollupType : DataPointsRollupType.values()) {
			JSONObject rollupsJsonSpecificType = null;
			HashMap<String, DataPointsRollup[]> rollupsDumpSpecificType;
			try {
				String jsonString = (String) rollupsJson.get(rollupType.name());
				if (jsonString == null)
					continue;
				rollupsJsonSpecificType = (JSONObject) jsonParser.parse((String) rollupsJson.get(rollupType.name()));
			} catch (ParseException e) {
				Logit.LogError("RollupsContainer - mergeExistingRollupsFromMemDump()",
						"Error parsing rollups from type " + rollupType.name() + " json string is: "
								+ (String) rollupsJson.get(rollupType.name()));
				continue;
			} catch (Exception e1) {
				e1.printStackTrace();
				Logit.LogError("RollupsContainer - mergeExistingRollupsFromMemDump()",
						"Error parsing rollups from type " + rollupType.name() + " json string is: "
								+ (String) rollupsJson.get(rollupType.name()));
				continue;
			}
			try {
				rollupsDumpSpecificType = JsonUtil.ToObject(rollupsJsonSpecificType, rollupsMapType);
			} catch (Exception e) {
				Logit.LogError("RollupsContainer - mergeExistingRollupsFromMemDump",
						"Unable to parse rollups dump to JSON objects. E: " + e.getMessage());
				continue;
			}
			this.mergeRollups(rollupsDumpSpecificType, getRollupsTable(rollupType));
		}

		return true;
	}

	private void mergeRollups(HashMap<String, DataPointsRollup[]> rollupsFromDump,
			HashMap<String, DataPointsRollup[]> currentRollups) {
		synchronized (currentRollups) {
			for (Map.Entry<String, DataPointsRollup[]> runnableProbeIdRollups : rollupsFromDump.entrySet()) {

				if (runnableProbeIdRollups.getKey()
						.contains("788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_7be55137-c5d8-438e-bca7-325f56656071")) {
					Logit.LogDebug("BREAKPOINT");
				}
				DataPointsRollup dumpRollups[] = new DataPointsRollup[6];
				DataPointsRollup[] existingRollups = new DataPointsRollup[6];
				try {
					dumpRollups = runnableProbeIdRollups.getValue();
					existingRollups = currentRollups.get(runnableProbeIdRollups.getKey());
				} catch (Exception e) {
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
						else if (dumpRollups[i] == null && existingRollups[i] == null)
							continue;
						else
							existingRollups[i].mergeRollup(dumpRollups[i]);
					}
				}
			}
		}
	}

	public boolean isRollupsMergedAtStart() {
		return rollupsMergedAtStart;
	}

	public void setRollupsMergedAtStart(boolean rollupsMergedAtStart) {
		this.rollupsMergedAtStart = rollupsMergedAtStart;
	}

	// public void clear() {
	// // TODO Auto-generated method stub
	// finishedRollups.clear();
	// }
}
