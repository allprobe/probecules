/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import GlobalConstants.Constants;
import GlobalConstants.GlobalConfig;
import Tasks.DiagnosticTask;
import Tasks.EventTask;
import Tasks.ResultsTask;
import Tasks.RollupsDumpTask;
import Tasks.SlaTask;
import Updates.Updates;
import Utils.Logit;
import org.apache.log4j.PropertyConfigurator;

/**
 * 
 * @author Roi
 */
public class LycusMain {

	public static void main(String[] args) {

		Logit.LogCheck("Starting Probecules Version: 0.250 (LogCheck)");
		if (Logit.isDebug())
			Logit.LogDebug("Starting Probecules in DEBUG mode (LogDebug)");

		System.setProperty("log4j.debug", "true");

		if (args.length == 0 || args[0] == "")
			GlobalConfig.setConfPath(null);
		else
			GlobalConfig.setConfPath(args[0]);
		if (!GlobalConfig.Initialize())
			return;

		UsersManager.Initialize(); // setup initial config (InitServer)

		if (!UsersManager.isInitialized())
			return;

		startAllTasks();
	}

	private static boolean startAllTasks() {
		ResultsTask resultsTask = new ResultsTask();
		ScheduledExecutorService resultsThread = Executors.newSingleThreadScheduledExecutor();
		resultsThread.scheduleAtFixedRate(resultsTask, 0, resultsTask.getInterval(), TimeUnit.SECONDS);

		EventTask eventTask = new EventTask();
		ScheduledExecutorService eventsThread = Executors.newSingleThreadScheduledExecutor();
		eventsThread.scheduleAtFixedRate(eventTask, 0, eventTask.getInterval(), TimeUnit.SECONDS);

		RollupsDumpTask rollupsMemoryDump = new RollupsDumpTask();
		ScheduledExecutorService rollupsThread = Executors.newSingleThreadScheduledExecutor();
		rollupsThread.scheduleAtFixedRate(rollupsMemoryDump, 0, rollupsMemoryDump.getInterval(), TimeUnit.SECONDS);

		SlaTask slaTask = new SlaTask();
		slaTask.setInterval(Constants.slaInterval);
		ScheduledExecutorService slaThread = Executors.newSingleThreadScheduledExecutor();

		Date currentTime = new Date();
		long initialDelay = (60 - currentTime.getMinutes()) * 60 - (60 - currentTime.getSeconds());
		slaThread.scheduleAtFixedRate(slaTask, initialDelay, slaTask.getInterval(), TimeUnit.SECONDS);

		DiagnosticTask diagnosticTask = new DiagnosticTask();
		ScheduledExecutorService DiagnosticThread = Executors.newSingleThreadScheduledExecutor();
		rollupsThread.scheduleAtFixedRate(diagnosticTask, 0, 300, TimeUnit.SECONDS);

		Updates updates = new Updates();
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(updates, 0, 30, TimeUnit.SECONDS);
		return true;
	}
}