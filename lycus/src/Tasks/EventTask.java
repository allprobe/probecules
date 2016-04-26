package Tasks;

import org.json.simple.JSONObject;

import DAL.ApiInterface;
import GlobalConstants.Enums;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.ResultsContainer;

public class EventTask extends BaseTask {
	private long interval = 300; 

	public void run() {
		try {
			ResultsContainer resultsContainer = ResultsContainer.getInstance();
			String events = resultsContainer.getEvents();

			if (events != null) {
				Logit.LogInfo("Sending events to API...");
				
				if (!events.equals("[]")) {
					String eventsEncoded = GeneralFunctions.Base64Encode(events);
//					String sendString = "{\"events\" : \"" + eventsEncoded + "\"}";
					JSONObject eventsJson=new JSONObject();
					eventsJson.put("events", eventsEncoded);
					
					DAL.DAL.getInstanece().put(Enums.ApiAction.PutEvents, eventsJson);
//					ApiInterface.executeRequest(Enums.ApiAction.PutEvents, "PUT", sendString);
				}
			} else {
				Logit.LogError("EventHandler - run()", "Unable to process events! events did not sent to API...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logit.LogError("EventHandler - run()", "Error retrieving all runnable probe events!");
		}
	}
}
