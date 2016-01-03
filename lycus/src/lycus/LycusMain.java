/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.cassandra.thrift.Cassandra.system_add_column_family_args;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
/**
 * 
 * @author Roi
 */
public class LycusMain  {

	public static void main(String[] args) {
		Global.setConfPath(args[0]);
		if(!Global.Initialize())
			return;
		SysLogger.Init();
		boolean apiInit=ApiInterface.Initialize();
		if(apiInit)
		UsersManager.Initialize();
//		ApiBuffer failedApiBuffer=new ApiBuffer();

//		Net.Snmp2Walk("62.90.132.131", 161, 3000, "ADCD-LAN2", "1.3.6.1.2.1.2.2.1.2.0");
//		System.err.println("Finished getting messages");

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
		
//		for(int i=0;i<10;i++)
//		{
//		ApiInterface.insertDatapointsBatches("TEST "+i);
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		}
//		if(1+2==3)
//		return;
//		
		
		if (UsersManager.isInitialized()) {
			RunnableProbesHistory history=new RunnableProbesHistory(new ArrayList<User>(UsersManager.getUsers().values()),null);
//			SysInfo sysInfo=new SysInfo(history);
//			sysInfo.start();
			UsersManager.runAtStart();
			history.startHistory();
		}
		
		//
		// RunnableProbesUpdates updates=new RunnableProbesUpdates(5);
		// updates.start();

		// for (String key :
		// MainContainer.getProbesUpdates().getProbesUpdates().keySet()) {
		// System.out.println("Key = " + key);
		// }
		// for(Probe probe:probes)
		// {
		// if(probe.getProbeName().equals("dns"))
		// { System.out.println(probe.toString());}
		// }
		// Map
		// map=cassandra.GetProbesUpdates(Global.DataCenterID+"-"+Global.ThisHostToken);
		// Encoding.printMap(map);
		//
		//
		//
		// Map<String, RunnableProbe> probes2=new HashMap();
		// for (Iterator<Probe> it = probes.iterator(); it.hasNext();) {
		// Probe p = it.next();
		// RunnableProbe activeProbe=new RunnableProbe(p);
		// activeProbe.start();
		// probes.put(p.getProbe_id(), activeProbe);
		// }

	}
}