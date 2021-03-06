package Tasks;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.json.simple.JSONObject;
import DAL.ApiRequest;
import DAL.FailedRequestsHandler;
import GlobalConstants.Enums;
import Interfaces.IResultsContainer;
import Interfaces.IRollupsContainer;
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
			// ResultsContainer.getInstance().clear();

			if (results.contains("f84b117c-03fb-40aa-8003-4283a72c35e4@13eff8bc-8fab-4fd6-b8ce-181483d05d18@discovery_3ee653fc-adaa-468e-9430-b1793b1d1c7d"))
				Logit.LogDebug("BREAKPOINT - ResultsTask");

			IRollupsContainer rollupsContainer = RollupsContainer.getInstance();
			String rollups = rollupsContainer.getAllFinsihedRollups();

			if (rollups != null)
				Logit.LogDebug("BREAKPOINT - ResultsTask");

			if (results == null)
				Logit.LogError("ResultsTask - run()", "BREAKPOINT");

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

			// RollupsContainer.getInstance().clear();
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
