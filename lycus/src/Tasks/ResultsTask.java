package Tasks;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.json.simple.JSONObject;
import DAL.ApiRequest;
import DAL.FailedRequestsHandler;
import GlobalConstants.Enums;
import Interfaces.IResultsContainer;
import Rollups.RollupsContainer;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.ResultsContainer;

public class ResultsTask extends BaseTask {
	private long interval = 30;

	public void run() {
		try {
			Logit.LogInfo("Sending collected results to API...");

			IResultsContainer resultsContainer = ResultsContainer.getInstance();
			String results = resultsContainer.getResults();

			if (results.contains("ed334812-4559-435a-9e43-0b52693be309@7352a46f-5189-428c-b4c0-fb98dedd10b1@http_01fe4920-086b-46b3-98f6-98be105a8aae"))
				Logit.LogDebug("BREAKPOINT - ResultsTask");
			RollupsContainer rollupsContainer = RollupsContainer.getInstance();
			String rollups = rollupsContainer.getAllFinsihedRollups();
			// System.out.println(rollups);
//			 System.out.println(results);
			if (rollups != null)
				Logit.LogDebug("BREAKPOINT - ResultsTask");

			if (results == null) {
				Logit.LogError("ResultsTask - run()", "BREAKPOINT");
			}
			String rpStr = rollups;
			if (rollups != null && rpStr.contains("discovery_45035c45-2679-4af6-84ca-e924e78dd7bc")) {
				Logit.LogDebug("BREAKPOINT - ResultsTask");
			}
			String encodedResults = null;
			String encodedRollups = null;
			try {
				encodedRollups = GeneralFunctions.Base64Encode(rollups);
			} catch (Exception e) {
				Logit.LogFatal("ResultsTask - run()",
						"Error encoding results and rollups to BASE64! E: " + e.getMessage(), e);
			}
			encodedResults = GeneralFunctions.Base64Encode(results);

			JSONObject jsonToSend = new JSONObject();
			try {
				jsonToSend.put("raw_results", encodedResults);
				jsonToSend.put("rollups_results", encodedRollups);
			} catch (Exception e) {
				Logit.LogFatal("ResultsTask - run()",
						"Error adding encoded results and rollups to JSONObject! E: " + e.getMessage(), e);
			}
			String sendString = jsonToSend.toString();
			if (results.contains(
					"47d364cf-50e3-4a3e-b3de-f58a0d6c3802@74cda666-3d85-4e56-a804-9d53c4e16259@discovery_777938b0-e4b0-4ec6-b0f2-ea880a0c09ef"))
				Logit.LogDebug("BREAKPOINT - ResultsTask");

			if (FailedRequestsHandler.getInstance().getNumberOfFailedRequests() != 0)
				FailedRequestsHandler.getInstance().executeRequests();
			if (DAL.DAL.getInstanece().put(Enums.ApiAction.InsertDatapointsBatches, jsonToSend) == null)
				FailedRequestsHandler.getInstance()
						.addRequest(new ApiRequest(Enums.ApiAction.InsertDatapointsBatches, jsonToSend));

			ResultsContainer.getInstance().clear();
			RollupsContainer.getInstance().clear();
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
