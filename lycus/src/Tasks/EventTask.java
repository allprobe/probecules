package Tasks;

import Collectors.CollectorIssuesContainer;
import org.json.simple.JSONObject;
import DAL.ApiRequest;
import DAL.FailedRequestsHandler;
import Events.EvenetsQueue;
import GlobalConstants.Enums;
import GlobalConstants.Enums.ApiAction;
import Model.EventsObject;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.ResultsContainer;

public class EventTask extends BaseTask {
	private long interval = 300;

	public void run() {
		try {
			EvenetsQueue evenetsQueue = EvenetsQueue.getInstance();
			EventsObject eventsObject = evenetsQueue.getEventsPerRunnableProbe();
			String events = eventsObject.getEventsJson();
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

					Logit.LogInfo("Packet with " + eventsObject.getLegth() + " events was just sent.");
//					evenetsQueue.clearAll();
				}
			} else {
				Logit.LogInfo("No Events changed! events did not sent to API...");
			}} catch (Exception e) {
			e.printStackTrace();
			Logit.LogError("EventHandler - run()", "Error prepairing/seding all runnable probe events!");
		}
		try{
			CollectorIssuesContainer issues=CollectorIssuesContainer.getInstance();
			int issuesLength=issues.length();
			if(issuesLength!=0)
			{
				if (FailedRequestsHandler.getInstance().getNumberOfFailedRequests() != 0)
					FailedRequestsHandler.getInstance().executeRequests();
				if (DAL.DAL.getInstanece().put(Enums.ApiAction.PutCollectorsIssue, issues.getAllIssues()) == null)
					FailedRequestsHandler.getInstance().addRequest(new ApiRequest(ApiAction.PutCollectorsIssue, issues.getAllIssues()));

				Logit.LogInfo("Packet with " + issuesLength + " collectors issues was just sent.");
			}else {
				Logit.LogInfo("No collectors issues found! issues did not sent to API...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logit.LogError("EventHandler - run()", "Error prepairing/seding all hosts collectors issues!");
		}

	}
}
