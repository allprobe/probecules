/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import DAL.ApiInterface;
import DAL.ApiRequest;
import DAL.FailedRequestsHandler;
import GlobalConstants.Constants;
import GlobalConstants.Enums.ApiAction;
import GlobalConstants.GlobalConfig;
import NetConnection.Net;
import Tasks.EventTask;
import Tasks.ResultsTask;
import Tasks.RollupsDumpTask;
import Tasks.SlaTask;
import Updates.Updates;
import Utils.Logit;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;;

/**
 * 
 * @author Roi
 */
public class LycusMain {

	public static void main(String[] args) {
		System.out.println("Starting Probecules Version: 0.218");
		Logit.LogCheck("Starting Probecules Version: 0.218");
		
		System.setProperty("log4j.debug","true");
		if (args.length == 0 || args[0] == "")
			GlobalConfig.setConfPath(null);
		else
			GlobalConfig.setConfPath(args[0]);
		if (!GlobalConfig.Initialize())
			return;

		
//		Net.ExtendedWeber(null, null, null, null, 0);
		
//		 System.err.println("Finished getting messages");
		
		Net.ExtendedWeber("http://allfdfinternet.co.il", "", "", "", 5);

		
		 UsersManager.Initialize();// setup initial config (InitServer)
		
		
		


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
		
		SlaTask slaTask = new SlaTask();
		slaTask.setInterval(Constants.slaInterval);
		ScheduledExecutorService slaThread = Executors.newSingleThreadScheduledExecutor();
		slaThread.scheduleAtFixedRate(slaTask, 0, slaTask.getInterval(), TimeUnit.SECONDS);
		
		return true;

	}
}