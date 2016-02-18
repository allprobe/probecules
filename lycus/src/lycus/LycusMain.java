/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lycus.Config.Updates;


/**
 * 
 * @author Roi
 */
public class LycusMain  {
	
	public static void main(String[] args) {
		if(args.length==0||args[0]=="")
			Global.setConfPath(null);
		else
			Global.setConfPath(args[0]);
		if(!Global.Initialize())
			return;
		SysLogger.Init();
		boolean apiInit=ApiInterface.Initialize();
		if(!apiInit)
			return;
		UsersManager.Initialize();//setup initial config (InitServer)

//		Net.Snmp2Walk("62.90.132.131", 161, 5000, "ADCD-LAN2", "1.3.6.1.2.1.2.2.1");
//		Net.Snmp3Walk("62.90.132.131",161,5000,"snmpv3user","snmpv3allp","md5",null,null,"1.3.6.1.2.1.2.2.1");
//		System.out.println("TEST");
		
//		System.err.println("Finished getting messages");

//		Net.builtInWeber("http://www.allprobe.com/ca/","GET", null,null, null,5000);

		
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
		
		if(!UsersManager.isInitialized())
			return;
		
		// TODO set discovery existing elements

		RunnableProbesHistory history=new RunnableProbesHistory(new ArrayList<User>(UsersManager.getUsers().values()),null,null);
//			SysInfo sysInfo=new SysInfo(history);
//			sysInfo.start();
			UsersManager.runAtStart();
			history.startHistory();
			
			Updates updates = new Updates();
			Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(updates, 0, 30, TimeUnit.SECONDS);
 
		
		

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