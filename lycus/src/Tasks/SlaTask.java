package Tasks;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.json.simple.JSONObject;

import DAL.ApiRequest;
import DAL.DAL;
import DAL.FailedRequestsHandler;
import GlobalConstants.Enums;
import Interfaces.ISLAContainer;
import SLA.SLAContainer;
import Utils.Logit;

public class SlaTask extends BaseTask {
	private long interval = 3600;
	private int hourCount = 0;

	public void run() {
		try {
			Logit.LogInfo("Sending collected SLA to API...");
			
			ISLAContainer slaContainer = SLAContainer.getInstance();
			JSONObject sendJson = slaContainer.getHourlySLA();

			if (DAL.getInstanece().put(Enums.ApiAction.PutSlaBatches, sendJson) == null)
				FailedRequestsHandler.getInstance()
						.addRequest(new ApiRequest(Enums.ApiAction.PutSlaBatches, sendJson));

			if (hourCount++ > 23)
			{
				JSONObject sendDailyJson = slaContainer.getDailySLA();

				if (DAL.getInstanece().put(Enums.ApiAction.PutSlaBatches, sendDailyJson) == null)
					FailedRequestsHandler.getInstance()
							.addRequest(new ApiRequest(Enums.ApiAction.PutSlaBatches, sendDailyJson));
				
				hourCount = 0;  //todo: check if the sla was sent first before celaring the count.
			}
		} catch (Throwable thrown) {
			Logit.LogError("ResultsTask - run()", "Sending collected SLA to API failed!");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			thrown.printStackTrace(pw);
		}
	}
}
