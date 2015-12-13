//package lycus;
//
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.Random;
//import java.util.Set;
//import java.util.UUID;
//import java.util.Vector;
//
//import me.prettyprint.cassandra.serializers.StringSerializer;
//import me.prettyprint.cassandra.serializers.UUIDSerializer;
//import me.prettyprint.hector.api.exceptions.HectorException;
//import me.prettyprint.hector.api.factory.HFactory;
//import me.prettyprint.hector.api.mutation.Mutator;
//
//import org.apache.commons.collections4.queue.CircularFifoQueue;
//import org.json.simple.JSONObject;
//import org.snmp4j.CommunityTarget;
//import org.snmp4j.PDU;
//import org.snmp4j.ScopedPDU;
//import org.snmp4j.Snmp;
//import org.snmp4j.Target;
//import org.snmp4j.TransportMapping;
//import org.snmp4j.UserTarget;
//import org.snmp4j.event.ResponseEvent;
//import org.snmp4j.mp.MPv3;
//import org.snmp4j.mp.SnmpConstants;
//import org.snmp4j.security.AuthMD5;
//import org.snmp4j.security.AuthSHA;
//import org.snmp4j.security.PrivAES128;
//import org.snmp4j.security.PrivDES;
//import org.snmp4j.security.SecurityLevel;
//import org.snmp4j.security.SecurityModels;
//import org.snmp4j.security.SecurityProtocols;
//import org.snmp4j.security.USM;
//import org.snmp4j.security.UsmUser;
//import org.snmp4j.smi.Address;
//import org.snmp4j.smi.GenericAddress;
//import org.snmp4j.smi.OID;
//import org.snmp4j.smi.OctetString;
//import org.snmp4j.smi.VariableBinding;
//import org.snmp4j.transport.DefaultUdpTransportMapping;
//
//import com.google.gson.Gson;
//
//import me.prettyprint.cassandra.serializers.StringSerializer;
//import me.prettyprint.cassandra.serializers.UUIDSerializer;
//
//public class TestEnvironment {
//	public static void main(String[] args) throws IOException {
//		List<String> oids = new ArrayList<String>();
//		if (!Global.Initialize())
//			System.exit(0);
//		ApiStages.Initialize();
//		SysLogger.Init();
//
//		// HostAndPort hnp=HostAndPortUtil.getRedisServers().get(0);
//		// Jedis jds2=new Jedis("localhost");
//		// JedisPool pool = new JedisPool(new JedisPoolConfig(),
//		// "localhost",7001,Protocol.DEFAULT_TIMEOUT);
//		// Jedis jds=pool.getResource();
//
//		// System.out.println(jds.get("I"));
//		// String uptime = "1.3.6.1.2.1.1.3.0";
//		// String name = "1.3.6.1.2.1.1.5.0";
//		// String ram = "1.3.6.1.4.1.2021.4.5.0";
//		// String desc = "1.3.6.1.2.1.1.1.0";
//		// for (int i = 0; i < 1; i++) {
//		// // oids.add(uptime);
//		// // oids.add(desc);
//		// oids.add(desc);
//		// }
//		// PDU resultsMap = SnmpTester("62.90.132.178", 161, 1000, "RAN-HOME",
//		// oids);
//		//
//		// System.out.println("TEST");
//		// System.out.println(resultsMap.getBERPayloadLength());
//		// System.out.println(resultsMap.getBERLength());
//		// System.out.println(resultsMap.size());
//		// int i = 1;
//		// for (VariableBinding x : resultsMap.getVariableBindings()) {
//		// System.out.println("-------" + i + "-------");
//		// i++;
//		// System.out.println(x.getVariable().toString());
//		// }
//		// System.out.println(Net.TcpPorter("62.90.102.43", 22, 3000));
//		// System.out.println(Net.Pinger("62.90.102.55", 4, 64, 3000));
//		// LinkedHashMap<UUID, JSONObject> updates =
//		// ApiStages.GETthreads_updates_by_server();
//		// for(Map.Entry<UUID, JSONObject> update:updates.entrySet())
//		// {
//		//
//		// System.out.println(GeneralFunctions.valuesOrdered(update.getValue().get("update_value").toString()).get(1));
//		// }
//		// System.out.println(ApiStages.cleanThreadsUpdates());
//		// DataPointsRollup[][] rollups=new DataPointsRollup[2][6];
//		// rollups[0][0]=new
//		// DataPointsRollup("PROBEID1,1",DataPointsRollupSize._4minutes);
//		// rollups[0][1]=new
//		// DataPointsRollup("PROBEID1,2",DataPointsRollupSize._20minutes);
//		// rollups[1][0]=new
//		// DataPointsRollup("PROBEID2,1",DataPointsRollupSize._4minutes);
//		// rollups[1][1]=new
//		// DataPointsRollup("PROBEID2,2",DataPointsRollupSize._20minutes);
//		// rollups[1][2]=new
//		// DataPointsRollup("PROBEID2,3",DataPointsRollupSize._1hour);
//		// Gson gson =new Gson();
//		// String rollupses=gson.toJson(rollups);
//		// try {
//		// //write converted json data to a file named "file.json"
//		// FileWriter writer = new FileWriter("/home/roi/test.json");
//		// writer.write(rollupses);
//		// writer.close();
//		//
//		// } catch (IOException e) {
//		// e.printStackTrace();
//		// }
//		//
//		//
//		//
//		// DataPointsRollup[][] rolls=gson.fromJson(rollupses,
//		// DataPointsRollup[][].class);
//		// System.out.println("OK");
//		// System.out.println("OK");
//		Random rand = new Random();
//		// for(int i=0;i<10;i++)
//		// {
//		//
//		// System.out.println(n);
//		// }
//		
////		DataPointsRollup dpr=new DataPointsRollup("^PROBEID^",DataPointsRollupSize._4minutes);
////		dpr.add(System.currentTimeMillis(), 49);
////		dpr.add(System.currentTimeMillis(), 70);
////		dpr.add(System.currentTimeMillis(), 32);
////		dpr.add(System.currentTimeMillis(), 2);
////		
////		try {
////			Thread.sleep(250000);
////		} catch (InterruptedException e1) {
////			// TODO Auto-generated catch block
////			e1.printStackTrace();
////		}
////		System.out.println(dpr.getAvg());
//		
//		boolean exit = true;
////		HashMap<String,UUID> test=ApiStages.getInitRPs(ApiStages.InitServer().get("long_ids"));
////		GeneralFunctions.printMap(test);
//		
//		System.out.println(ApiStages.insertDatapointsBatches("BlABLA"));
//		if (exit)
//			return;
//		Host h = new Host(UUID.randomUUID(), "^HOSTNAME^", "^HOSTIP^", null, true, false);
//		//Probe p = new PingerProbe("^PROBEID^", UUID.randomUUID(), "^PROBENAME^", 60, 1, true, 3000, 4, 64);
//		RunnableProbe rp = null;
//		try {
//			//rp = new RunnableProbe(h, p);
//		} catch (Exception e) {
//			System.out.println("OK");
//		}
//		RunnableProbeResults rpr = null;
//		if (rp != null)
//			rpr = new RunnablePingerProbeResults(rp);
//		while (true) {
//
//			int n = rand.nextInt(50) + 1;
//			ArrayList<Object> results = new ArrayList<Object>();
//			System.out.println("OK");
//
//			long ntime = System.currentTimeMillis();
//			int n1 = rand.nextInt(100) + 1;
//			long n2 = rand.nextInt(10000) + 1;
//			int n3 = rand.nextInt(255) + 1;
//			results.add(ntime);
//			results.add(n1);
//			results.add(n2);
//			results.add(n3);
//			if (rpr != null)
//				rpr.acceptResults(results);
//			HashMap<String,String> resultsMap = rpr.getResults();
//			System.out.println(rpr.getGson().toJson(resultsMap));
//			for (String key : resultsMap.keySet()) {
//				if (key.contains("ROLLUP"))
//					rpr.resetRollups();
//			}
//			try {
//				Thread.sleep(60000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		// ApiStages.insertExistingRollups("{\"error\": {\"code\":
//		// 404,\"message\": \"Not Found\"}}");
//		// Gson g=new Gson();
//	}
//
//	public static PDU SnmpTester(String ip, int port, int timeout, String comName, List<String> oids) {
//		Address targetAddress = GenericAddress.parse("udp:" + ip + "/" + port);
//		CommunityTarget target = new CommunityTarget();
//		target.setAddress(targetAddress);
//		target.setRetries(3);
//		target.setTimeout(timeout);
//		target.setVersion(SnmpConstants.version2c);
//		target.setCommunity(new OctetString(comName));
//		TransportMapping transport;
//		try {
//			transport = new DefaultUdpTransportMapping();
//		} catch (IOException e) {
//			return null;
//		}
//		Snmp snmp = new Snmp(transport);
//		try {
//			transport.listen();
//		} catch (IOException e) {
//			return null;
//		}
//		PDU pdu = new PDU();
//		pdu.setType(PDU.GETBULK);
//		pdu.setMaxRepetitions(1);
//		pdu.setNonRepeaters(0);
//		for (String oid : oids) {
//			pdu.add(new VariableBinding(GeneralFunctions.privOid(new OID(oid))));
//		}
//		ResponseEvent event = null;
//		try {
//			event = snmp.send(pdu, target, null);
//		} catch (IOException e) {
//			return null;
//		}
//		if (event.getResponse() == null || event.getResponse().getErrorStatus() == SnmpConstants.SNMP_ERROR_TOO_BIG) {
//			return null;
//		} else {
//			return event.getResponse();
//		}
//	}
//
//	int check(int[] arr, int n, int thrshld) {
//		if (n == 1) {
//			if (arr[0] > thrshld) {
//				return -1;
//			}
//			return 1;
//		} else {
//			int _1st = check(Arrays.copyOfRange(arr, 0, n / 2), n / 2, thrshld);
//			int _2nd = check(Arrays.copyOfRange(arr, n / 2, n), (n / 2) + (n % 2), thrshld);
//			if (_1st == -1 || _2nd == -1) {
//				return -1;
//			}
//			return _1st > _2nd ? _1st : _2nd;
//		}
//	}
//}