package Tasks;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.json.simple.JSONObject;

import DAL.ApiInterface;
import GlobalConstants.Enums;
import Rollups.RollupsContainer;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.ResultsContainer;

public class ResultsTask extends BaseTask {
	private long interval = 30;

	public void run() {
//		Logit.LogError("ResultsTask - run()", "thread results executed!");

		try {
			Logit.LogInfo("Sending collected results to API...");

			ResultsContainer resultsContainer = ResultsContainer.getInstance();
			String results = resultsContainer.getResults();

			if (results.contains("rtt") || results.contains("packetLost"))
				Logit.LogDebug("BREAKPOINT - ResultsTask");
			RollupsContainer rollupsContainer = RollupsContainer.getInstance();
			String rollups = rollupsContainer.getAllFinsihedRollups();

			if (rollups != null)
				Logit.LogDebug("BREAKPOINT - ResultsTask");

			if (results == null) {
				Logit.LogError("ResultsTask - run()", "BREAKPOINT");
			}
			String rpStr = rollups;
			if (rollups !=null && rpStr.contains(
					"0b05919c-6cc0-42cc-a74b-de3b0dcd4a2a@7352a46f-5189-428c-b4c0-fb98dedd10b1@snmp_50a71fa7-b794-46c0-b506-6732ac4af944")) {
				Logit.LogDebug("BREAKPOINT - ResultsTask");
				System.out.println(rollups);
			}
			String encodedResults = null;
			String encodedRollups = null;
			try {
				encodedRollups = GeneralFunctions.Base64Encode(rollups);
			} catch (Exception e) {
				Logit.LogFatal("ResultsTask - run()",
						"Error encoding results and rollups to BASE64! E: " + e.getMessage());
				Logit.LogFatal("ResultsTask - run()", "trace: " + e.getStackTrace().toString());
			}
			encodedResults = GeneralFunctions.Base64Encode(results);

			JSONObject jsonToSend = new JSONObject();
			try {
				jsonToSend.put("raw_results", encodedResults);
				jsonToSend.put("rollups_results", encodedRollups);
			} catch (Exception e) {
				Logit.LogFatal("ResultsTask - run()",
						"Error adding encoded results and rollups to JSONObject! E: " + e.getMessage());
				Logit.LogFatal("ResultsTask - run()", "trace: " + e.getStackTrace().toString());
			}
			// String sendString = "{\"results\" : \"" + encodedResults + "\"}";
			String sendString = jsonToSend.toString();

			if (results.contains(
					"47d364cf-50e3-4a3e-b3de-f58a0d6c3802@74cda666-3d85-4e56-a804-9d53c4e16259@discovery_777938b0-e4b0-4ec6-b0f2-ea880a0c09ef"))
				Logit.LogDebug("BREAKPOINT - ResultsTask");

			// DAL.getInstanece().put(Enums.ApiAction.InsertDatapointsBatches,
			// sendString);
			DAL.DAL.getInstanece().get(Enums.ApiAction.InsertDatapointsBatches);

			ResultsContainer.getInstance().clear();

		} catch (Throwable thrown) {
			Logit.LogError("ResultsTask - run()", "Sending collected results to API failed!");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			thrown.printStackTrace(pw);

			Logit.LogError("ResultsTask - run()", sw.toString());
			Logit.LogError("ResultsTask - run()", thrown.getMessage().toString());
			Logit.LogError("ResultsTask - run()", "Results insertion failed! does not run anymore!");
		}
	}
}
