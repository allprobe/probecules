package lycus.ResultsTasks;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.json.simple.JSONObject;

import lycus.ResultsContainer;
import lycus.DAL.ApiInterface;
import lycus.GlobalConstants.Enums;
import lycus.Rollups.RollupsContainer;
import lycus.Utils.GeneralFunctions;
import lycus.Utils.Logit;

public class ResultsTask extends BaseTask {
	private long interval = 30;

	public void run() {
		try {
			Logit.LogInfo("Sending collected results to API...");

			ResultsContainer resultsContainer = ResultsContainer.getInstance();
			String results = resultsContainer.getResults();

			if (results.contains("rtt") || results.contains("packetLost"))
				System.err.println("BREAKPOINT - ResultsTask");

			RollupsContainer rollupsContainer = RollupsContainer.getInstance();
			String rollups = rollupsContainer.getAllFinsihedRollups();

			if (rollups != null)
				System.err.println("BREAKPOINT - ResultsTask");

			if (results == null) {
				System.err.println("BREAKPOINT - ResultsTask");
			}
			String rpStr = results;
			if (rpStr.contains("fc46cf87-0872-4e5d-9b83-c44a3d1f3ea6@icmp_1f1aed08-7331-4126-97ef-225e90b4a969"))
				System.out.println("BREAKPOINT");

			String encodedResults = GeneralFunctions.Base64Encode(results);
			String encodedRollups = GeneralFunctions.Base64Encode(rollups);

			JSONObject jsonToSend = new JSONObject();
			jsonToSend.put("raw_results", encodedResults);
			jsonToSend.put("rollups_results", encodedRollups);

			// String sendString = "{\"results\" : \"" + encodedResults + "\"}";
			String sendString = jsonToSend.toString();

			if (results.contains(
					"47d364cf-50e3-4a3e-b3de-f58a0d6c3802@74cda666-3d85-4e56-a804-9d53c4e16259@discovery_777938b0-e4b0-4ec6-b0f2-ea880a0c09ef"))
				System.out.println("BREAKPOINT - RunnableProbesHistory");

			// DAL.getInstanece().put(Enums.ApiAction.InsertDatapointsBatches,
			// sendString);
			ApiInterface.executeRequest(Enums.ApiAction.InsertDatapointsBatches, "PUT", sendString);
			
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
