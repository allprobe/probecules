package Tasks;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.json.simple.JSONObject;

import DAL.ApiRequest;
import DAL.DAL;
import DAL.FailedRequestsHandler;
import GlobalConstants.Constants;
import GlobalConstants.Enums;
import Interfaces.ISLAContainer;
import Model.ThreadsCount;
import SLA.SLAContainer;
import Utils.Logit;
import lycus.RunnableProbeContainer;

public class DiagnosticTask extends BaseTask {
	private long interval = 300;

	public void run() {
		try {
			Logit.LogInfo("Sending Diagmostic API...");

			ThreadsCount threadsCount = RunnableProbeContainer.getInstanece().getThreadCount();

			JSONObject jsonItem = new JSONObject();
			jsonItem.put("PING", threadsCount.ping);
			jsonItem.put("PORT", threadsCount.port);
			jsonItem.put("WEB", threadsCount.web);
			jsonItem.put("RBL", threadsCount.rbl);
			jsonItem.put("SNMP", threadsCount.snmp);
			jsonItem.put("DISCOVERY", threadsCount.discovery);
			jsonItem.put("NIC", threadsCount.nic);
			jsonItem.put("DISK", threadsCount.disk);
			jsonItem.put("TRACEROUTE", threadsCount.traceroute);
			JSONObject returnJson = new JSONObject();
			returnJson.put("diagnostic_results", jsonItem);

			if (DAL.getInstanece().put(Enums.ApiAction.DiagnosticResults, returnJson) == null)
				FailedRequestsHandler.getInstance()
						.addRequest(new ApiRequest(Enums.ApiAction.DiagnosticResults, returnJson));

			threadsCount = null;
		} catch (Throwable thrown) {
			Logit.LogError("DiagnosticTask - run()", "Sending collected threads diagnostics results!");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			thrown.printStackTrace(pw);
		}
	}
}
