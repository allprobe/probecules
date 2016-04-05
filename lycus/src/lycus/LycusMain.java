/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import lycus.GlobalConstants.GlobalConfig;
import lycus.ResultsTasks.EventTask;
import lycus.ResultsTasks.ResultsTask;
import lycus.ResultsTasks.RollupsDumpTask;
import lycus.Updates.Updates;
import lycus.Utils.Logit;
import lycus.DAL.ApiInterface;;

/**
 * 
 * @author Roi
 */
public class LycusMain {

	public static void main(String[] args) {
		System.setProperty("log4j.debug","true");
		if (args.length == 0 || args[0] == "")
			GlobalConfig.setConfPath(null);
		else
			GlobalConfig.setConfPath(args[0]);
		if (!GlobalConfig.Initialize())
			return;
		Logit.LogInfo("Probecules Version: 0.7.3");
		boolean apiInit = ApiInterface.Initialize();
		if (!apiInit)
			return;
		UsersManager.Initialize();// setup initial config (InitServer)
		


		// System.err.println("Finished getting messages");

		// byte[]
		// tryMe=Encoding.hexStringToByteArray("bc855616fc5b801f020e0cd2080045000028a64e00003a1173e63e5a6626c0a801680035e84e0014a4275c78b0110000000000000000");
		// for(byte b:tryMe)
		// {
		// System.out.println(b);
		// }
		// SocketChecker udpCheck = new SocketChecker("62.90.102.38", 53, 1,
		// 5000,"00\\x00\\x10\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00");
		// udpCheck.run();

		// for(byte b:"walla.co.il".getBytes())
		// {
		// System.out.println(b);
		// }

		// byte[] send={0,0,16,0,0,0,0,0,0,0,0,0};
		// System.out.println(new String(send));
		// Net.UdpPorter("208.67.222.222", 53, 5000,new String(send),null);
		//

		if (!UsersManager.isInitialized())
			return;

//		UsersManager.runAtStart();
		startResultsTasks();

		Updates updates = new Updates();
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(updates, 0, 30, TimeUnit.SECONDS);

	}

	private static boolean startResultsTasks() {
		ResultsTask resultsTask = new ResultsTask();
		ScheduledExecutorService resultsThread = Executors.newSingleThreadScheduledExecutor();
		resultsThread.scheduleAtFixedRate(resultsTask, 0, resultsTask.getInterval(), TimeUnit.SECONDS);
		
		ResultsContainer.getInstance().pullCurrentLiveEvents();
		EventTask eventHandler = new EventTask();
		ScheduledExecutorService eventsThread = Executors.newSingleThreadScheduledExecutor();
		eventsThread.scheduleAtFixedRate(eventHandler, 0, eventHandler.getInterval(), TimeUnit.SECONDS);
		
		RollupsDumpTask rollupsMemoryDump = new RollupsDumpTask();
		ScheduledExecutorService rollupsThread = Executors.newSingleThreadScheduledExecutor();
		rollupsThread.scheduleAtFixedRate(rollupsMemoryDump, 0, rollupsMemoryDump.getInterval(), TimeUnit.SECONDS);
		
		return true;

	}
}