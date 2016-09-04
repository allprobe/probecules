package Tasks;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

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

	public void run() {
		try {
			Logit.LogInfo("Sending collected SLA to API...");
			
			ISLAContainer slaContainer = SLAContainer.getInstance();
			JSONObject sendJson = slaContainer.getHourlySLA();
			
			if (DAL.getInstanece().put(Enums.ApiAction.PutSlaBatches, sendJson) == null)
				FailedRequestsHandler.getInstance()
						.addRequest(new ApiRequest(Enums.ApiAction.PutSlaBatches, sendJson));

			Date currentTime = new Date();
			if (currentTime.getHours() >= 0 && currentTime.getHours() < 1 )
			{
				JSONObject sendDailyJson = slaContainer.getDailySLA();

				if (DAL.getInstanece().put(Enums.ApiAction.PutSlaBatches, sendDailyJson) == null)
					FailedRequestsHandler.getInstance()
							.addRequest(new ApiRequest(Enums.ApiAction.PutSlaBatches, sendDailyJson));
			}
		} catch (Throwable thrown) {
			Logit.LogError("ResultsTask - run()", "Sending collected SLA to API failed!");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			thrown.printStackTrace(pw);
		}
	}
}
