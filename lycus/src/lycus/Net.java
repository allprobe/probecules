/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.PrivAES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.google.common.collect.Lists;

/**
 * 
 * @author Roi
 */
public class Net {

	private static SysLogger logger = new SysLogger();

	public static ArrayList<Object> Pinger(String ip, int numOfPings, int sizeOfPings, int timeout) {
		InetAddress inet;
		ArrayList<Object> pingResults = new ArrayList<Object>();
		try {
			inet = InetAddress.getByName(ip);
		} catch (UnknownHostException ex) {
			SysLogger.Record(new Log("Problem With Host IP:" + ip, "Net", "pinger", LogType.Debug, ex));
			pingResults.add(System.currentTimeMillis());
			pingResults.add(100);
			pingResults.add(0);
			pingResults.add(0);
			return pingResults;
		}

		if (inet != null) {
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				try {
					/* Windows */
					if (inet.isReachable(timeout)) {
						pingResults.add(System.currentTimeMillis());
						pingResults.add(0);
						pingResults.add(0);
						pingResults.add(0);
						return pingResults;
					} else {
						pingResults.add(System.currentTimeMillis());
						pingResults.add(100);
						pingResults.add(0);
						pingResults.add(0);
						return pingResults;
					}
				} catch (IOException ex) {
					pingResults.add(System.currentTimeMillis());
					pingResults.add(100);
					pingResults.add(0);
					pingResults.add(0);
					SysLogger.Record(new Log("windows pinger problem " + ip, LogType.Error, ex));
					return pingResults;
				}
			} else {
				/* Linux & OSX */
				try {
					StringBuilder b = new StringBuilder();
					Integer buffer = (timeout / 1000);
					b.append("ping").append(" ").append("-c").append(" ").append(numOfPings).append(" ").append("-W")
							.append(" ").append(String.valueOf(buffer)).append(" ").append("-s").append(" ")
							.append(sizeOfPings).append(" ").append(ip);
					Process p = Runtime.getRuntime().exec(b.toString());
					BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
					List<Object> lines;
					lines = new ArrayList<Object>();
					String s;
					String PacketLoss = "100";
					String rtt_avg = "0";
					String ttl = "0";

					while ((s = stdInput.readLine()) != null) {
						lines.add(s);
						if (hasPingResult(s)) {
							if ("0".equals(ttl)) {
								// System.out.println(s);
								ttl = checkLineTTL(s);
							}
						} else {
							if (checkLinePacketLoss(s)) {
								// System.out.println(s);
								String split[] = s.split(" ");
								PacketLoss = split[5].replace("%", "");
							} else if (checkLineRTT(s)) {
								// System.out.println(s);
								String split[] = s.split(" ");
								String rtt[] = split[3].split("/");
								rtt_avg = rtt[1];
							}
						}
					}
					p.destroy();

					if ("100".equals(PacketLoss)) {
						pingResults.add(System.currentTimeMillis());
						pingResults.add(100);
						pingResults.add(0);
						pingResults.add(0);
						return pingResults;

					} else {
						pingResults.add(System.currentTimeMillis());
						pingResults.add(Integer.parseInt(PacketLoss));
						pingResults.add(Double.parseDouble(rtt_avg));
						pingResults.add(Integer.parseInt(ttl));
						return pingResults;

					}
				} catch (Exception e) {
					pingResults.add(System.currentTimeMillis());
					pingResults.add(100);
					pingResults.add(0);
					pingResults.add(0);
					return pingResults;
				}
			}
		} else {
			pingResults.add(System.currentTimeMillis());
			pingResults.add(100);
			pingResults.add(0);
			pingResults.add(0);
			return pingResults;
		}
	}

	// #region pinger sub functions
	private static boolean checkLinePacketLoss(String line) {
		return line.contains("packets transmitted");
	}

	private static String checkLineTTL(String line) {
		String split[] = line.split(" ");
		String ttl = split[5].split("=")[1];
		return ttl;
	}

	private static boolean checkLineRTT(String line) {
		return line.contains("rtt min");
	}

	private static boolean hasPingResult(String line) {
		return line.contains("bytes from");
	}

	// #endregion

	public static ArrayList<Object> TcpPorter(String ip, int port, int timeout) {
		long querytime;
		ArrayList<Object> portResults = new ArrayList<Object>();// open/close|check
		long start = System.currentTimeMillis();

		try {
			Socket socket;
			socket = new Socket();
			socket.setSoTimeout(timeout);
			socket.connect(new InetSocketAddress(ip, port), timeout);
			long end = System.currentTimeMillis();
			querytime = (end - start);
			socket.close();
			portResults.add(System.currentTimeMillis());
			portResults.add(true);
			portResults.add(querytime);
			return portResults;
		} catch (Exception e) {
			portResults.add(System.currentTimeMillis());
			portResults.add(false);
			portResults.add(0);
			return portResults;
		}
	}

	public static ArrayList<Object> UdpPorter(String ip, int port, int timeout, String sendString,
			String receiveString) {
		ArrayList<Object> results = new ArrayList<Object>();
		long querytime;
		byte[] bytes;
		InetAddress address;
		DatagramSocket dsock = null;
		DatagramPacket packet = null;
		String sendMessage;
		String receiveMessage;
		long start;
		if (sendString != null && receiveString != null) {
			sendMessage = sendString;
			receiveMessage = receiveString;
		} else if (sendString != null && receiveString == null) {
			sendMessage = sendString;
			receiveMessage = null;
		} else {
			sendMessage = "Probe Box!";
			receiveMessage = null;
		}

		try {
			address = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			SysLogger.Record(new Log("incorrect host address" + ip, "Net", "UdpPorter", LogType.Error));
			results.add(System.currentTimeMillis());
			results.add(false);
			results.add(0);
			return results;
		}
		try {
			dsock = new DatagramSocket();
			dsock.setSoTimeout(timeout);
		} catch (SocketException e) {
			SysLogger.Record(new Log("Socket creation problem" + ip, "Net", "UdpPorter", LogType.Error, e));
			results.add(System.currentTimeMillis());
			results.add(false);
			results.add(0);
			return results;
		} catch (Exception e) {
			SysLogger.Record(new Log("Socket problem" + ip, "Net", "UdpPorter", LogType.Error, e));
			results.add(System.currentTimeMillis());
			results.add(false);
			results.add(0);
			return results;
		}
		start = System.currentTimeMillis();
		results.add(start);
		try {
			dsock.send(new DatagramPacket(sendMessage.getBytes(), sendMessage.getBytes().length,
					InetAddress.getByName(ip), port));
		} catch (Exception e) {
			SysLogger.Record(new Log("Socket send problem:" + ip + " Exception:" + e.getClass().getName(), "Net",
					"UdpPorter", LogType.Error, e));
			results.add(start);
			results.add(false);
			results.add(0);
			return results;
		} finally {
			dsock.close();
		}
		byte[] buf = new byte[4096];
		packet = new DatagramPacket(buf, buf.length);
		try {
			dsock.receive(packet);
			querytime = System.currentTimeMillis() - start;
			if (receiveMessage == null) {
				results.add(true);
				results.add(querytime);
				return results;
			} else if (packet.getData() == receiveMessage.getBytes()) {
				results.add(true);
				results.add(querytime);
				return results;
			} else {
				results.add(false);
				results.add(querytime);
				return results;
			}
		} catch (SocketTimeoutException ste) {
			SysLogger.Record(new Log("UDP Port closed", "Net", "UdpPorter", LogType.Debug, ste));
			results.add(false);
			results.add(timeout);
			return results;
		} catch (IOException ioe) {
			querytime = System.currentTimeMillis() - start;
			SysLogger.Record(new Log("UDP Port closed", "Net", "UdpPorter", LogType.Debug, ioe));
			results.add(false);
			results.add(querytime);
			return results;
		} catch (Exception e) {
			querytime = System.currentTimeMillis() - start;
			SysLogger.Record(new Log("UDP Port closed" + ip + " Exception:" + e.getClass().getName(), "Net",
					"UdpPorter", LogType.Error, e));
			results.add(false);
			results.add(querytime);
			return results;
		}
	}

	public static ArrayList<Object> Weber(String url, String requestType, String user, String pass, int timeout) {
		HttpRequest request;
		String UserPass;
		ArrayList<Object> webResults = new ArrayList<Object>();

		webResults.add(System.currentTimeMillis());
		if (user != null && pass != null) {
			UserPass = user + ":" + pass;
			request = new HttpRequest(url, requestType.equals("POST") ? RequestTypes.POST : RequestTypes.GET, timeout,
					UserPass);
		} else {
			request = new HttpRequest(url, requestType.equals("POST") ? RequestTypes.POST : RequestTypes.GET, timeout);
		}

		request.Execute();

		if (request.getIsTimeOut()) {
			webResults.add(0);
			webResults.add(0);
			webResults.add(0);
			return webResults;
		} else {

			String contentType = request.getResponseEntity().getContentType().getValue().toString().split(";")[0]
					.toString();
			String contentType1 = contentType.split("/")[0].toString();
			String contentType2 = contentType.split("/")[1].toString();

			HashMap<String, List<String>> mimeTypes = new HashMap<>();
			mimeTypes.put("application",
					Arrays.asList("jason", "javascript", "x-javascript", "xml", "xhtml+xml", "rss+xml", "soap+xml"));
			mimeTypes.put("text", Arrays.asList("x-json", "html", "javascript", "plain", "xml"));

			if (mimeTypes.get(contentType1).contains(contentType2)) {
				long pageSize = GeneralFunctions.fromStringToBytes(request.getResponsePageContent(), "UTF-8").length;
				// status code
				webResults.add(request.getResponseStatusCode());
				// query time
				webResults.add(request.getResponseQueryTime());
				// page size in bytes
				webResults.add(pageSize);
				return webResults;
			} else {
				webResults.add(-1);
				webResults.add(-1);
				webResults.add(-1);
				return webResults;
			}

		}

	}

	public static ArrayList<Object> runSnmpCheckVer1(String ip, int port, String communityName, String oid,
			int timeout) {
		ArrayList<Object> results = new ArrayList<Object>();
		Address targetAddress = GenericAddress.parse("udp:" + ip + "/" + port);
		CommunityTarget target = new CommunityTarget();
		target.setAddress(targetAddress);
		target.setRetries(3);
		target.setTimeout(timeout);
		target.setVersion(SnmpConstants.version1);
		target.setCommunity(new OctetString(communityName));
		TransportMapping transport;
		try {
			transport = new DefaultUdpTransportMapping();
		} catch (IOException e) {
			SysLogger.Record(new Log("Snmp Check " + ip + " Failed", "Net", "runSnmpCheckVer1", LogType.Error));
			results.add(System.currentTimeMillis());
			results.add("");
			return results;
		}
		Snmp snmp = new Snmp(transport);
		try {
			transport.listen();
		} catch (IOException e) {
			SysLogger.Record(new Log("transport listen() " + ip + " Failed", "Net", "runSnmpCheckVer1", LogType.Error));
			results.add(System.currentTimeMillis());
			results.add("");
			return results;
		}
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(oid)));
		pdu.setType(PDU.GET);
		ResponseEvent event = null;
		try {
			event = snmp.send(pdu, target, null);
		} catch (IOException e) {
			SysLogger.Record(new Log("snmp send() " + ip + " Failed", "Net", "runSnmpCheckVer1", LogType.Error));
			results.add(System.currentTimeMillis());
			results.add("");
			return results;
		}
		if (event.getResponse() == null) {
			results.add(System.currentTimeMillis());
			results.add("");
			return results;
		} else {
			if (!event.getResponse().get(0).getVariable().toString().equals("noSuchInstance")) {
				results.add(System.currentTimeMillis());
				results.add("wrongOID");
				return results;
			} else {
				results.add(System.currentTimeMillis());
				results.add(event.getResponse().get(0).getVariable().toString());
				return results;
			}
		}
	}

	/** host problem || snmp problem || no problem */
	public static String checkHostSnmpActive(Host h) {
		
		
		ArrayList<Object> pingResults = Net.Pinger(h.getHostIp(), 1, 64, 5000);
		if (((int) pingResults.get(1)) != 0)
			return "host problem";
		int version = h.getSnmpTemp().getVersion();

		TransportMapping transport=null;
	    Snmp snmp=null;
		
		try {
			transport=new DefaultUdpTransportMapping();
		
		snmp=new Snmp(transport);

		switch (h.getSnmpTemp().getVersion()) {
		case 1: {
			ArrayList<Object> results = Net.runSnmpCheckVer1(h.getHostIp(), h.getSnmpTemp().getPort(),
					h.getSnmpTemp().getCommunityName(), Global.getHostSnmpOK(), h.getSnmpTemp().getTimeout());
			if (results == null) {
				return "snmp problem";
			} else {
				if (((String) results.get(0)).equals("wrongOID")) {
					return "snmp problem";
				} else {
					return "no problem";
				}
			}
		}
		case 2: {
			String results = Net.Snmp2GET(h.getHostIp(), h.getSnmpTemp().getPort(), h.getSnmpTemp().getTimeout(),
					h.getSnmpTemp().getCommunityName(), Global.getHostSnmpOK(),transport,snmp);
			if (results == null) {
				return "snmp problem";
			} else {
				if (results.equals("wrongOID")) {
					return "snmp problem";
				} else {
					return "no problem";
				}
			}

		}
		case 3: {
			String results = Net.Snmp3GET(h.getHostIp(), h.getSnmpTemp().getPort(), h.getSnmpTemp().getPort(),
					Global.getHostSnmpOK(), h.getSnmpTemp().getUserName(), h.getSnmpTemp().getAuthPass(),
					h.getSnmpTemp().getAlgo(), h.getSnmpTemp().getCryptPass(), h.getSnmpTemp().getCryptType(),transport,snmp);
			if (results == null) {
				return "snmp problem";
			} else {
				if (results.equals("wrongOID")) {
					return "snmp problem";
				} else {
					return "no problem";
				}
			}
		}
		}
		} catch (IOException e) {
			SysLogger.Record(new Log("Socket binding for failed for checkHostSnmpActive:"+h.getHostId().toString(), LogType.Error, e));
		}
		finally {
			try{
	        	if (snmp != null) {
	        			snmp.close();
	        	}
	        	if (transport != null) {
						transport.close();
				}
	        }catch(Exception e){
	            SysLogger.Record(new Log("Memory leak, unable to close network connection!",LogType.Error,e));
	        }
		}
		return "host problem";
	}

	// #region snmp requests
	public static Map<String, String> Snmp2GETBULK(String ip, int port, int timeout, String comName,
			List<String> oids,TransportMapping transportMapping,Snmp snmpInterface) {
		Map<String, String> oidsValues = new HashMap<String, String>();
		Address targetAddress = GenericAddress.parse("udp:" + ip + "/" + port);
		CommunityTarget target = new CommunityTarget();
		target.setAddress(targetAddress);
		target.setRetries(3);
		target.setTimeout(timeout);
		target.setVersion(SnmpConstants.version2c);
		target.setCommunity(new OctetString(comName));
		TransportMapping transport = transportMapping;
		Snmp snmp = snmpInterface;
		
		if(transport==null||snmp==null)
			return null;
		
		try {
			
			PDU pdu = new PDU();
			pdu.setType(PDU.GETBULK);
			pdu.setMaxRepetitions(1);
			pdu.setNonRepeaters(0);
			for (String oid : oids) {
				pdu.add(new VariableBinding(GeneralFunctions.privOid(new OID(oid))));
				oidsValues.put(oid, null);
			}
			ResponseEvent event = null;

			event = snmp.send(pdu, target, null);
			if (event.getResponse() == null
					|| event.getResponse().getErrorStatus() == SnmpConstants.SNMP_ERROR_TOO_BIG) {
				return null;
			} else {
				for (VariableBinding var : event.getResponse().getVariableBindings()) {
					String _oid = var.getOid().toString();
					String _value = var.getVariable().toString();
					if (oidsValues.containsKey(_oid) && (!_value.equals("endOfMibView"))) {
						oidsValues.put(_oid, _value);
					}
				}
			}
		} catch (Exception e) {
			SysLogger.Record(new Log("Unable to run Snmp2 GETBULK check!", LogType.Error, e));
			return null;
		} 
		return oidsValues;
	}
	
	public static Map<String, String> Snmp2GETBULK(String ip, int port, int timeout, String comName,
			List<String> oids) {
		Map<String, String> oidsValues = new HashMap<String, String>();
		Address targetAddress = GenericAddress.parse("udp:" + ip + "/" + port);
		CommunityTarget target = new CommunityTarget();
		target.setAddress(targetAddress);
		target.setRetries(3);
		target.setTimeout(timeout);
		target.setVersion(SnmpConstants.version2c);
		target.setCommunity(new OctetString(comName));
		
		TransportMapping transport =null;
		Snmp snmp = null;
		
		try {
			
			transport=new DefaultUdpTransportMapping();
			snmp=new Snmp(transport);
			
			PDU pdu = new PDU();
			pdu.setType(PDU.GETBULK);
			pdu.setMaxRepetitions(1);
			pdu.setNonRepeaters(0);
			for (String oid : oids) {
				pdu.add(new VariableBinding(GeneralFunctions.privOid(new OID(oid))));
				oidsValues.put(oid, null);
			}
			ResponseEvent event = null;

			event = snmp.send(pdu, target, null);
			if (event.getResponse() == null
					|| event.getResponse().getErrorStatus() == SnmpConstants.SNMP_ERROR_TOO_BIG) {
				return null;
			} else {
				for (VariableBinding var : event.getResponse().getVariableBindings()) {
					String _oid = var.getOid().toString();
					String _value = var.getVariable().toString();
					if (oidsValues.containsKey(_oid) && (!_value.equals("endOfMibView"))) {
						oidsValues.put(_oid, _value);
					}
				}
			}
		} catch (Exception e) {
			SysLogger.Record(new Log("Unable to run Snmp2 GETBULK check!", LogType.Error, e));
			return null;
		} 
		finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (IOException e) {
					SysLogger.Record(
							new Log("Unable to close TransportMapping! may cause memory leak!", LogType.Error, e));
				}
			}
			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					SysLogger.Record(new Log("Unable to close SNMP! may cause memory leak!", LogType.Error, e));

				}
			}
		}
		return oidsValues;
	}
	

	public static Map<String, String> Snmp3GETBULK(String ip, int port, int timeout, String userName, String userPass,
			String authAlgo, String cryptPass, String cryptAlgo, List<String> oids,TransportMapping transportMapping,Snmp snmpInterface) {
		OID _authAlgo = authAlgo == null ? null
				: authAlgo.equals("md5") ? AuthMD5.ID : authAlgo.equals("sha1") ? AuthSHA.ID : null;
		OctetString _authPass = userPass == null ? null : new OctetString(userPass);
		OID _cryptAlgo = cryptAlgo == null ? null
				: cryptAlgo.equals("des") ? PrivDES.ID : cryptAlgo.equals("aes") ? PrivAES128.ID : null;
		OctetString _cryptPass = cryptPass == null ? null : new OctetString(cryptPass);
		Map<String, String> oidsValues = new HashMap<String, String>();
		Address targetAddress = GenericAddress.parse("udp:" + ip + "/" + port);
		UserTarget target = new UserTarget();
		target.setAddress(targetAddress);
		target.setRetries(3);
		target.setTimeout(timeout);
		target.setVersion(SnmpConstants.version3);
		target.setSecurityName(new OctetString(userName));
		UsmUser usera = null;
		if (userPass == null)
			target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
		else if (cryptPass == null)
			target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
		else
			target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
		usera = new UsmUser(new OctetString(userName), // security
				_authAlgo, // authprotocol
				_authPass, // authpassphrase
				_cryptAlgo, // privacyprotocol
				_cryptPass // privacypassphrase
		);
		TransportMapping transport = transportMapping;
		Snmp snmp = snmpInterface;
		if(transport==null||snmp==null)
			return null;
		
		try {
			
			USM usm;
			usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
			SecurityModels.getInstance().addSecurityModel(usm);
			if (usera != null) {
				snmp.getUSM().addUser(usera);
			} else {
				return null;
			}

			ScopedPDU pdu = new ScopedPDU();
			for (String oid : oids) {
				pdu.add(new VariableBinding(GeneralFunctions.privOid(new OID(oid))));
				oidsValues.put(oid, null);
			}
			pdu.setType(PDU.GETBULK);
			pdu.setMaxRepetitions(1);
			pdu.setNonRepeaters(0);
			ResponseEvent event = null;
			event = snmp.send(pdu, target, null);
			if (event.getResponse() == null
					|| event.getResponse().getErrorStatus() == SnmpConstants.SNMP_ERROR_TOO_BIG) {
				return null;
			} else {
				for (VariableBinding var : event.getResponse().getVariableBindings()) {
					String _oid = var.getOid().toString();
					String _value = var.getVariable().toString();
					if (oidsValues.containsKey(_oid) && (!_value.equals("endOfMibView"))) {
						oidsValues.put(_oid, _value);
					}
				}
			}

		} catch (Exception e) {
			SysLogger.Record(new Log("Unable to run Snmp3 GETBULK check!", LogType.Error, e));
			return null;
		} 

		return oidsValues;
	}

	public static Map<String, String> Snmp3GETBULK(String ip, int port, int timeout, String userName, String userPass,
			String authAlgo, String cryptPass, String cryptAlgo, List<String> oids) {
		OID _authAlgo = authAlgo == null ? null
				: authAlgo.equals("md5") ? AuthMD5.ID : authAlgo.equals("sha1") ? AuthSHA.ID : null;
		OctetString _authPass = userPass == null ? null : new OctetString(userPass);
		OID _cryptAlgo = cryptAlgo == null ? null
				: cryptAlgo.equals("des") ? PrivDES.ID : cryptAlgo.equals("aes") ? PrivAES128.ID : null;
		OctetString _cryptPass = cryptPass == null ? null : new OctetString(cryptPass);
		Map<String, String> oidsValues = new HashMap<String, String>();
		Address targetAddress = GenericAddress.parse("udp:" + ip + "/" + port);
		UserTarget target = new UserTarget();
		target.setAddress(targetAddress);
		target.setRetries(3);
		target.setTimeout(timeout);
		target.setVersion(SnmpConstants.version3);
		target.setSecurityName(new OctetString(userName));
		UsmUser usera = null;
		if (userPass == null)
			target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
		else if (cryptPass == null)
			target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
		else
			target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
		usera = new UsmUser(new OctetString(userName), // security
				_authAlgo, // authprotocol
				_authPass, // authpassphrase
				_cryptAlgo, // privacyprotocol
				_cryptPass // privacypassphrase
		);
		TransportMapping transport = null;
		Snmp snmp = null;
		
		
		try {
			transport=new DefaultUdpTransportMapping();
			snmp=new Snmp(transport);
			USM usm;
			usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
			SecurityModels.getInstance().addSecurityModel(usm);
			if (usera != null) {
				snmp.getUSM().addUser(usera);
			} else {
				return null;
			}

			ScopedPDU pdu = new ScopedPDU();
			for (String oid : oids) {
				pdu.add(new VariableBinding(GeneralFunctions.privOid(new OID(oid))));
				oidsValues.put(oid, null);
			}
			pdu.setType(PDU.GETBULK);
			pdu.setMaxRepetitions(1);
			pdu.setNonRepeaters(0);
			ResponseEvent event = null;
			event = snmp.send(pdu, target, null);
			if (event.getResponse() == null
					|| event.getResponse().getErrorStatus() == SnmpConstants.SNMP_ERROR_TOO_BIG) {
				return null;
			} else {
				for (VariableBinding var : event.getResponse().getVariableBindings()) {
					String _oid = var.getOid().toString();
					String _value = var.getVariable().toString();
					if (oidsValues.containsKey(_oid) && (!_value.equals("endOfMibView"))) {
						oidsValues.put(_oid, _value);
					}
				}
			}

		} catch (Exception e) {
			SysLogger.Record(new Log("Unable to run Snmp3 GETBULK check!", LogType.Error, e));
			return null;
		} 
		finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (IOException e) {
					SysLogger.Record(
							new Log("Unable to close TransportMapping! may cause memory leak!", LogType.Error, e));
				}
			}
			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					SysLogger.Record(new Log("Unable to close SNMP! may cause memory leak!", LogType.Error, e));

				}
			}
		}

		return oidsValues;
	}
	
	
	public static String Snmp2GET(String ip, int port, int timeout, String comName, String oid,TransportMapping transportMapping,Snmp snmpInterface) {
		Address targetAddress = GenericAddress.parse("udp:" + ip + "/" + port);
		CommunityTarget target = new CommunityTarget();
		target.setAddress(targetAddress);
		target.setRetries(3);
		target.setTimeout(timeout);
		target.setVersion(SnmpConstants.version2c);
		target.setCommunity(new OctetString(comName));
		TransportMapping transport = transportMapping;
		Snmp snmp = snmpInterface;
		if(transport==null||snmp==null)
			return null;
		
		try {

			PDU pdu = new PDU();
			pdu.setType(PDU.GET);
			pdu.add(new VariableBinding(new OID(oid)));
			ResponseEvent event = null;
			event = snmp.send(pdu, target, null);

			if (event.getResponse() != null) {
				if (event.getResponse().get(0).getVariable().toString().equals("noSuchInstance"))
					return "wrongOID";
				return event.getResponse().get(0).getVariable().toString();
			} else {
				return null;
			}
		} catch (Exception e) {
			SysLogger.Record(new Log("Unable to run Snmp2 GET check!", LogType.Error, e));
			return null;
		} 
//		finally {
//			if (transport != null) {
//				try {
//					transport.close();
//				} catch (IOException e) {
//					SysLogger.Record(
//							new Log("Unable to close TransportMapping! may cause memory leak!", LogType.Error, e));
//				}
//			}
//			if (snmp != null) {
//				try {
//					snmp.close();
//				} catch (IOException e) {
//					SysLogger.Record(new Log("Unable to close SNMP! may cause memory leak!", LogType.Error, e));
//
//				}
//			}
//		}

	}

	public static String Snmp3GET(String ip, int port, int timeout, String oid, String userName, String userPass,
			String authAlgo, String cryptPass, String cryptAlgo,TransportMapping transportMapping,Snmp snmpInterface) {
		Address targetAddress = GenericAddress.parse("udp:" + ip + "/" + port);
		UserTarget target = new UserTarget();
		target.setAddress(targetAddress);
		target.setRetries(3);
		target.setTimeout(timeout);
		target.setVersion(SnmpConstants.version3);
		target.setSecurityName(new OctetString(userName));
		UsmUser usera = null;
		if (userPass == null) {
			target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
			usera = new UsmUser(new OctetString(userName), // security
					null, // authprotocol
					null, // authpassphrase
					null, // privacyprotocol
					null // privacypassphrase
			);
		} else if (cryptPass == null) {
			target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
			usera = new UsmUser(new OctetString(userName), // security
					authAlgo.equals("md5") ? AuthMD5.ID : AuthSHA.ID, // authprotocol
					new OctetString(userPass), // authpassphrase
					null, // privacyprotocol
					null // privacypassphrase
			);
		} else {
			target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
			usera = new UsmUser(new OctetString(userName), // security
					authAlgo.equals("md5") ? AuthMD5.ID : AuthSHA.ID, // authprotocol
					new OctetString(userPass), // authpassphrase
					cryptAlgo.equals("des") ? PrivDES.ID : PrivAES128.ID, // privacyprotocol
					new OctetString(cryptPass) // privacypassphrase
			);
		}
		TransportMapping transport = transportMapping;
		Snmp snmp = snmpInterface;
		if(transport==null||snmp==null)
			return null;
		
		try {
			
			USM usm;
			usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
			SecurityModels.getInstance().addSecurityModel(usm);
			if (usera != null) {
				snmp.getUSM().addUser(usera);
			} else {
				return null;
			}

			ScopedPDU pdu = new ScopedPDU();
			pdu.add(new VariableBinding(new OID(oid)));
			pdu.setType(PDU.GET);
			ResponseEvent event = null;
			try {
				event = snmp.send(pdu, target, null);
			} catch (IOException e) {
				return null;
			}
			if (event.getResponse() != null) {
				if (!event.getResponse().get(0).getVariable().toString().equals("noSuchInstance"))
					return event.getResponse().get(0).getVariable().toString();
				else
					return "wrongOID";
			} else {
				return null;
			}
		} catch (Exception e) {
			SysLogger.Record(new Log("Unable to run Snmp3 GET check!", LogType.Error, e));
			return null;
		} 
//		finally {
//			if (transport != null) {
//				try {
//					transport.close();
//				} catch (IOException e) {
//					SysLogger.Record(
//							new Log("Unable to close TransportMapping! may cause memory leak!", LogType.Error, e));
//				}
//			}
//			if (snmp != null) {
//				try {
//					snmp.close();
//				} catch (IOException e) {
//					SysLogger.Record(new Log("Unable to close SNMP! may cause memory leak!", LogType.Error, e));
//
//				}
//			}
//		}

	}

	// #endregion

	public static ArrayList<Object> Traceroute(String ip) {
		InetAddress endAddress;
		try {
			endAddress = InetAddress.getByName(ip);
		} catch (UnknownHostException ex) {
			SysLogger.Record(new Log("Problem With Host IP:" + ip, "Net", "traceroute", LogType.Debug, ex));
			return null;
		}
		String route = "";
		try {
			Process traceRt;
			if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
				traceRt = Runtime.getRuntime().exec("tracert " + endAddress.getHostAddress());
			else {
				String[] cmd = { "/bin/sh", "-c", "traceroute -w 0.9 -n " + endAddress.getHostAddress()
						+ " | tail -n+2 | awk \'{ print $1 \",\" $2 }\'" };

				traceRt = Runtime.getRuntime().exec(cmd);

			}
			// read the output from the command
			route = GeneralFunctions.convertStreamToString(traceRt.getInputStream());

			// read any errors from the attempted command
			String errors = GeneralFunctions.convertStreamToString(traceRt.getErrorStream());
			if (errors != "") {
				SysLogger.Record(new Log("Error while processing traceroute on address" + ip, LogType.Debug));
				return null;
			}
		} catch (IOException e) {
			SysLogger.Record(new Log("Error while processing traceroute on address" + ip, LogType.Debug));
			return null;
		}

		ArrayList<String> convertedRoutes = convertTracerouteOutput(route);
		ArrayList<Object> results = new ArrayList<Object>();
		results.add(System.currentTimeMillis());
		results.add(convertedRoutes);
		return results;

	}

	private static ArrayList<String> convertTracerouteOutput(String trcrt) {
		ArrayList<String> routes = new ArrayList<String>();
		String[] lines = trcrt.split("\\r?\\n");
		for (String line : lines) {
			int index = Integer.parseInt(line.split(",")[0]);
			String route = line.split(",")[1];
			routes.add(route);
		}
		return routes;
	}

	/**
	 * 
	 * 
	 * @param ip
	 *            - ip address
	 * @param RBL
	 *            - RBL address
	 * @return timestamp,isListed
	 */
	public static ArrayList<Object> RBLCheck(String ip, String RBL) {
		ArrayList<Object> results = new ArrayList<Object>();
		long probeTimestamp = System.currentTimeMillis();
		results.add(probeTimestamp);
		try {
			InetAddress.getByName(invertIPAddress(ip) + "." + RBL);
			results.add(true);
		} catch (UnknownHostException e) {
			results.add(false);
		}
		return results;
	}

	private static String invertIPAddress(String originalIPAddress) {

		StringTokenizer t = new StringTokenizer(originalIPAddress, ".");
		String inverted = t.nextToken();

		while (t.hasMoreTokens()) {
			inverted = t.nextToken() + "." + inverted;
		}

		return inverted;
	}

}