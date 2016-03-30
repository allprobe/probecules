package lycus.ResultsTasks;

import lycus.ResultsContainer;
import lycus.DAL.ApiInterface;
import lycus.GlobalConstants.Enums;
import lycus.Utils.GeneralFunctions;
import lycus.Utils.Logit;

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
					String sendString = "{\"events\" : \"" + eventsEncoded + "\"}";
					ApiInterface.executeRequest(Enums.ApiAction.PutEvents, "PUT", sendString);
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
