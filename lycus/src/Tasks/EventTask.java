package Tasks;

import org.json.simple.JSONObject;
import DAL.ApiRequest;
import DAL.FailedRequestsHandler;
import GlobalConstants.Enums;
import GlobalConstants.Enums.ApiAction;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.ResultsContainer;

public class EventTask extends BaseTask {
	private long interval = 300;

	public void run() {
		try {
			ResultsContainer resultsContainer = ResultsContainer.getInstance();
			String events = resultsContainer.getEventsPerRunnableProbe();

			if (events != null && events.length() > 2) {
				Logit.LogInfo("Sending collected Events to API...");

				if (!events.equals("[]")) {
					String eventsEncoded = GeneralFunctions.Base64Encode(events);
					JSONObject eventsJson = new JSONObject();
					eventsJson.put("events", eventsEncoded);

					if (FailedRequestsHandler.getInstance().getNumberOfFailedRequests() != 0)
						FailedRequestsHandler.getInstance().executeRequests();
					if (DAL.DAL.getInstanece().put(Enums.ApiAction.PutEvents, eventsJson) == null)
						FailedRequestsHandler.getInstance().addRequest(new ApiRequest(ApiAction.PutEvents, eventsJson));

					Logit.LogInfo("Packet with " + events.split(",").length + " events was just sent.");
					resultsContainer.cleanEvents();
				}
			} else {
				Logit.LogInfo("No Events changed! events did not sent to API...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logit.LogError("EventHandler - run()", "Error prepairing/seding all runnable probe events!");
		}
	}
}
