/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package NetConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.commons.io.IOUtils;
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
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
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
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeListener;
import org.snmp4j.util.TreeUtils;

import GlobalConstants.GlobalConfig;
import GlobalConstants.LogType;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.Host;

/**
 * 
 * @author Roi
 */
public class Net {

	public static ArrayList<Object> Pinger(String ip, int numOfPings, int sizeOfPings, int timeout) {
		InetAddress inet;
		ArrayList<Object> pingResults = new ArrayList<Object>();
		try {
			inet = InetAddress.getByName(ip);
		} catch (UnknownHostException ex) {
			Logit.LogWarn("No IP address or ipv6 address for host: " + ip);
			pingResults.add(System.currentTimeMillis());
			pingResults.add(100);
			pingResults.add(0.0);
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
						pingResults.add(0.0);
						pingResults.add(0);
						return pingResults;
					} else {
						pingResults.add(System.currentTimeMillis());
						pingResults.add(100);
						pingResults.add(0.0);
						pingResults.add(0);
						return pingResults;
					}
				} catch (IOException ex) {
					Logit.LogError("Net - Pinger", "network error with windows pinger for host: " + ip);
					return null;
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
								ttl = checkLineTTL(s);
						} else {
							if (checkLinePacketLoss(s)!=null) {
								PacketLoss = checkLinePacketLoss(s);
							} else if (checkLineRTT(s)!=null) {
								rtt_avg = checkLineRTT(s);
							}
						}
					}
					p.destroy();

					if ("100".equals(PacketLoss)) {
						pingResults.add(System.currentTimeMillis());
						pingResults.add(100);
						pingResults.add(0.0);
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
					pingResults.add(0.0);
					pingResults.add(0);
					return pingResults;
				}
			}
		} else {
			pingResults.add(System.currentTimeMillis());
			pingResults.add(100);
			pingResults.add(0.0);
			pingResults.add(0);
			return pingResults;
		}
	}

	// #region pinger sub functions
	private static String checkLinePacketLoss(String line) {
		if(!line.contains("packet loss"))
			return null;
		String split[] = line.split(" ");
		for(String s:split)
		{
			if(s.contains("%"))
				return s.split("%")[0];
		}
		return null;
	}

	private static String checkLineTTL(String line) {
		if(!line.contains("ttl"))
			return null;
		String split[] = line.split(" ");
		for(String s:split)
		{
			if(s.contains("ttl"))
				return s.split("=")[1];
		}
		return null;
	}

	private static String checkLineRTT(String line) {
		if(!line.contains("rtt min"))
			return null;
		String rtt_avg = line.split(" ")[3].split("/")[1];
//		String rtt_min = line.split(" ")[3].split("/")[0];
//		String rtt_max = line.split(" ")[3].split("/")[2];
		return rtt_avg;
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
			portResults.add(0L);
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
			Logit.LogWarn("No IP address or ipv6 address for host: " + ip);
			results.add(System.currentTimeMillis());
			results.add(false);
			results.add(0);
			return results;
		}
		try {
			dsock = new DatagramSocket();
			dsock.setSoTimeout(timeout);
		} catch (Exception e) {
			Logit.LogError("Net - UdpPorter", "Error creating socket for udp porter!");
			return null;
		}
		start = System.currentTimeMillis();
		results.add(start);
		try {
			dsock.send(new DatagramPacket(sendMessage.getBytes(), sendMessage.getBytes().length,
					InetAddress.getByName(ip), port));
		} catch (Exception e) {
			Logit.LogError("Net - UdpPorter", "Socket send problem:" + ip + " Exception:" + e.getMessage());
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
		} catch (Exception e) {
			querytime = System.currentTimeMillis() - start;
			Logit.LogInfo("UDP Port closed" + ip + " Exception:" + e.getMessage());
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
			webResults.add(0L);
			webResults.add(0L);
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
				webResults.add(-1L);
				webResults.add(-1L);
				return webResults;
			}

		}

	}

	public static ArrayList<Object> builtInWeber(String _url, String requestType, String authType, String user,
			String pass, int timeout) {

		ArrayList<Object> webResults = new ArrayList<Object>();

		URL url;
		HttpURLConnection con;
		String UserPass;

		webResults.add(System.currentTimeMillis());

		try {
			if (authType != null)
				UserPass = user + ":" + pass;
			else
				UserPass = null;

			url = new URL(_url);
			con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod(requestType);

			if (requestType.equals("POST"))
				con.setDoOutput(true);

			if (UserPass != null)
				con.setRequestProperty("Authorization",
						"Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(UserPass.getBytes()));

			con.setConnectTimeout(timeout);

			long start = System.currentTimeMillis();

			con.connect();
			int code = con.getResponseCode();
			InputStream is = null;
			if (code == 200)
				is = con.getInputStream();

			long end = System.currentTimeMillis();

			String body = null;
			if (is != null)
				body = IOUtils.toString(is, "UTF-8");

			// BufferedReader in=null;
			// if(is!=null)
			// in= new BufferedReader(
			// new InputStreamReader(is));

			int responseCode = code;
			long responseTime = end - start;
			long responseSize = body == null ? -1 : body.getBytes().length;
			// long responseSize=con.getContentLength();

			webResults.add(responseCode);
			webResults.add(responseTime);
			webResults.add(responseSize);

		} catch (Exception e) {
			Logit.LogError(null, "Unable to process http request for URL: " + _url);
			return null;
		}
		return webResults;
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
			Logit.LogError("Net - runSnmpCheckVer1", "socket binding fails for snmpV1check, " + ip + ":" + port);
			return null;
		}
		Snmp snmp = new Snmp(transport);
		try {
			transport.listen();
		} catch (IOException e) {
			Logit.LogError("Net = runSnmpCheckVer1()", "transport listen() " + ip + " Failed");
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
			Logit.LogError("Net = runSnmpCheckVer1()", "snmp send Failed " + e);
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

		TransportMapping transport = null;
		Snmp snmp = null;

		try {
			transport = new DefaultUdpTransportMapping();

			snmp = new Snmp(transport);

			switch (h.getSnmpTemp().getVersion()) {
			case 1: {
				ArrayList<Object> results = Net.runSnmpCheckVer1(h.getHostIp(), h.getSnmpTemp().getPort(),
						h.getSnmpTemp().getCommunityName(), GlobalConfig.getHostSnmpOK(), h.getSnmpTemp().getTimeout());
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
						h.getSnmpTemp().getCommunityName(), GlobalConfig.getHostSnmpOK(), transport, snmp);
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
						GlobalConfig.getHostSnmpOK(), h.getSnmpTemp().getUserName(), h.getSnmpTemp().getAuthPass(),
						h.getSnmpTemp().getAlgo(), h.getSnmpTemp().getCryptPass(), h.getSnmpTemp().getCryptType(),
						transport, snmp);
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
			Logit.LogError(null, "Socket binding for failed for checkHostSnmpActive:" + h.getHostId().toString());
		} finally {
			try {
				if (snmp != null) {
					snmp.close();
				}
				if (transport != null) {
					transport.close();
				}
			} catch (Exception e) {
				Logit.LogError(null, "Memory leak, unable to close network connection!");
			}
		}
		return "host problem";
	}

	// #region snmp requests
	public static Map<String, String> Snmp2GETBULK(String ip, int port, int timeout, String comName, List<String> oids,
			TransportMapping transportMapping, Snmp snmpInterface) {
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

		if (transport == null || snmp == null)
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
			Logit.LogError(null, "Unable to run Snmp2 GETBULK check!");
			return null;
		}
		return oidsValues;
	}

	public static Map<String, String> Snmp2GETBULK(String ip, int port, int timeout, String comName,
			Collection<OID> collection) {
		Map<String, String> oidsValues = new HashMap<String, String>();
		Address targetAddress = GenericAddress.parse("udp:" + ip + "/" + port);
		CommunityTarget target = new CommunityTarget();
		target.setAddress(targetAddress);
		target.setRetries(3);
		target.setTimeout(timeout);
		target.setVersion(SnmpConstants.version2c);
		target.setCommunity(new OctetString(comName));

		TransportMapping transport = null;
		Snmp snmp = null;

		try {

			transport = new DefaultUdpTransportMapping();
			snmp = new Snmp(transport);
			transport.listen();
			PDU pdu = new PDU();
			pdu.setType(PDU.GETBULK);
			pdu.setMaxRepetitions(1);
			pdu.setNonRepeaters(0);
			for (OID oid : collection) {
				pdu.add(new VariableBinding(GeneralFunctions.privOid(oid)));
				oidsValues.put(oid.toString(), null);
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
			Logit.LogError(null, "Unable to run Snmp2 GETBULK check!");
			return null;
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (IOException e) {
					Logit.LogError(null, "Unable to close TransportMapping! may cause memory leak!");
				}
			}
			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					Logit.LogError(null, "Unable to close SNMP! may cause memory leak!");
				}
			}
		}
		return oidsValues;
	}

	public static Map<String, String> Snmp3GETBULK(String ip, int port, int timeout, String userName, String userPass,
			String authAlgo, String cryptPass, String cryptAlgo, List<String> oids, TransportMapping transportMapping,
			Snmp snmpInterface) {
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
		if (transport == null || snmp == null)
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
			Logit.LogError(null, "Unable to run Snmp3 GETBULK check!");
			return null;
		}

		return oidsValues;
	}

	public static Map<String, String> Snmp3GETBULK(String ip, int port, int timeout, String userName, String userPass,
			String authAlgo, String cryptPass, String cryptAlgo, Collection<OID> collection) {

		OctetString _username = userName == null ? null : new OctetString(userName);
		OID _authAlgo = authAlgo == null ? null
				: authAlgo.equals("md5") ? AuthMD5.ID : authAlgo.equals("sha1") ? AuthSHA.ID : null;
		OctetString _authPass = userPass == null ? null : new OctetString(userPass);
		OID _cryptAlgo = cryptAlgo == null ? null
				: cryptAlgo.equals("des") ? PrivDES.ID : cryptAlgo.equals("aes") ? PrivAES256.ID : null;
		OctetString _cryptPass = cryptPass == null ? null : new OctetString(cryptPass);
		Map<String, String> oidsValues = new HashMap<String, String>();
		Address targetAddress = GenericAddress.parse("udp:" + ip + "/" + port);
		UserTarget target = new UserTarget();
		target.setAddress(targetAddress);
		target.setRetries(1);
		target.setTimeout(timeout);
		target.setVersion(SnmpConstants.version3);
		target.setSecurityName(_username);
		UsmUser usera = null;
		if (userPass == null)
			target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
		else if (cryptPass == null)
			target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
		else
			target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
		usera = new UsmUser(_username, // security
				_authAlgo, // authprotocol
				_authPass, // authpassphrase
				_cryptAlgo, // privacyprotocol
				_cryptPass // privacypassphrase
		);
		TransportMapping transport = null;
		Snmp snmp = null;

		try {
			transport = new DefaultUdpTransportMapping();
			snmp = new Snmp(transport);
			transport.listen();
			USM usm;
			usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
			SecurityModels.getInstance().addSecurityModel(usm);
			if (usera != null) {
				snmp.getUSM().addUser(usera.getSecurityName(), usera);
			} else {
				return null;
			}

			ScopedPDU pdu = new ScopedPDU();
			for (OID oid : collection) {
				pdu.add(new VariableBinding(GeneralFunctions.privOid(oid)));
				oidsValues.put(oid.toString(), null);
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
			Logit.LogError(null, "Unable to run Snmp3 GETBULK check!");
			return null;
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (IOException e) {
					Logit.LogError(null, "Unable to close TransportMapping! may cause memory leak!");
				}
			}
			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					Logit.LogError(null, "Unable to close SNMP! may cause memory leak!");
				}
			}
		}

		return oidsValues;
	}

	public static String Snmp2GET(String ip, int port, int timeout, String comName, String oid,
			TransportMapping transportMapping, Snmp snmpInterface) {
		Address targetAddress = GenericAddress.parse("udp:" + ip + "/" + port);
		CommunityTarget target = new CommunityTarget();
		target.setAddress(targetAddress);
		target.setRetries(3);
		target.setTimeout(timeout);
		target.setVersion(SnmpConstants.version2c);
		target.setCommunity(new OctetString(comName));
		TransportMapping transport = transportMapping;
		Snmp snmp = snmpInterface;
		if (transport == null || snmp == null)
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
			Logit.LogError(null, "Unable to run Snmp2 GET check!");
			return null;
		}
		// finally {
		// if (transport != null) {
		// try {
		// transport.close();
		// } catch (IOException e) {
//		Logit.LogError("Net - Snmp2GET", "Unable to close TransportMapping! may cause memory leak!");
		// }
		// }
		// if (snmp != null) {
		// try {
		// snmp.close();
		// } catch (IOException e) {
//		Logit.LogError("Net - Snmp2GET", "Unable to close TransportMapping! may cause memory leak!");

		//
		// }
		// }
		// }

	}

	public static String Snmp3GET(String ip, int port, int timeout, String oid, String userName, String userPass,
			String authAlgo, String cryptPass, String cryptAlgo, TransportMapping transportMapping,
			Snmp snmpInterface) {
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
		if (transport == null || snmp == null)
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
			Logit.LogError(null, "Unable to run Snmp3 GET check!");
			return null;
		}
		// finally {
		// if (transport != null) {
		// try {
		// transport.close();
		// } catch (IOException e) {
//		Logit.LogError("Net - Snmp2GET", "Unable to close TransportMapping! may cause memory leak!");

		// }
		// }
		// if (snmp != null) {
		// try {
		// snmp.close();
		// } catch (IOException e) {
//		Logit.LogError("Net - Snmp2GET", "Unable to close TransportMapping! may cause memory leak!");

		//
		// }
		// }
		// }

	}

	public static Map<String, String> Snmp2Walk(final String ip, int port, int timeout, String comName, String _oid) {
		Address targetAddress = GenericAddress.parse("udp:" + ip + "/" + port);
		CommunityTarget target = new CommunityTarget();
		target.setAddress(targetAddress);
		target.setRetries(3);
		target.setTimeout(timeout);
		target.setVersion(SnmpConstants.version2c);
		target.setCommunity(new OctetString(comName));
		// target.setMaxSizeRequestPDU(65535);

		TransportMapping transport = null;
		Snmp snmp = null;
		final SnmpWalkCounts counts = new SnmpWalkCounts();
		final HashMap<String, String> walkResults = new HashMap<String, String>();

		try {

			transport = new DefaultUdpTransportMapping();
			snmp = new Snmp(transport);
			snmp.listen();

			PDU request = new PDU();
			request.setType(PDU.GETBULK);
			request.add(new VariableBinding(new OID(_oid)));
			request.setMaxRepetitions(10);
			request.setNonRepeaters(0);

			PDU response = null;

			TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
			OID[] oids = new OID[1];
			oids[0] = new OID(_oid);
			// List<TreeEvent> events = treeUtils.getSubtree(target, oid);
			// List<TreeEvent> events = treeUtils.walk(target, ifaces);

			final long startTime = System.nanoTime();
			TreeListener treeListener = new TreeListener() {
				private boolean finished;

				public boolean next(TreeEvent e) {
					counts.requests++;
					if (e.getVariableBindings() != null) {
						VariableBinding[] vbs = e.getVariableBindings();
						counts.objects += vbs.length;
						for (VariableBinding vb : vbs) {
							walkResults.put(vb.getOid().toString(), vb.getVariable().toString());
						}
					}
					return true;
				}

				public void finished(TreeEvent e) {
					if ((e.getVariableBindings() != null) && (e.getVariableBindings().length > 0)) {
						next(e);
					}
					// System.out.println();
					// System.out.println("Total requests sent:
					// "+counts.requests);
					// System.out.println("Total objects received:
					// "+counts.objects);
					// System.out.println("Total walk time: "+
					// (System.nanoTime()-startTime)/SnmpConstants.MILLISECOND_TO_NANOSECOND+"
					// milliseconds");
					if (e.isError()) {
						Logit.LogError("Net - Snmp2Walk", "The following error occurred during walk:"
								+ e.getErrorMessage() + ", for host: " + ip);
					}
					finished = true;
					synchronized (this) {
						this.notify();
					}
				}

				public boolean isFinished() {
					return finished;
				}

			};
			synchronized (treeListener) {
				treeUtils.getSubtree(target, new OID(_oid), null, treeListener);
				try {
					treeListener.wait();
				} catch (InterruptedException ex) {
					Logit.LogError("Net - Snmp2Walk",
							"Tree retrieval interrupted:" + ex.getMessage() + ", for host: " + ip);
					Thread.currentThread().interrupt();
				}
			}
			return walkResults;
		} catch (Exception e) {
			Logit.LogError("Net - Snmp2Walk",
					"Unable to run Snmp2 WALK check! " + e.getMessage() + ", for host: " + ip);
			return null;
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (IOException e) {
					Logit.LogError("Net - Snmp2Walk", "Unable to close TransportMapping! may cause memory leak! "
							+ e.getMessage() + ", for host: " + ip);
				}
			}
			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					Logit.LogError("Net - Snmp2Walk",
							"Unable to close SNMP! may cause memory leak! " + e.getMessage() + ", for host: " + ip);
				}
			}
		}
	}

	public static Map<String, String> Snmp3Walk(String ip, int port, int timeout, String userName, String userPass,
			String authAlgo, String cryptPass, String cryptAlgo, String _oid) {
		Map<String, String> results = new HashMap<String, String>();

		OctetString _username = userName == null ? null : new OctetString(userName);
		OID _authAlgo = authAlgo == null ? null
				: authAlgo.equals("md5") ? AuthMD5.ID : authAlgo.equals("sha1") ? AuthSHA.ID : null;
		OctetString _authPass = userPass == null ? null : new OctetString(userPass);
		OID _cryptAlgo = cryptAlgo == null ? null
				: cryptAlgo.equals("des") ? PrivDES.ID : cryptAlgo.equals("aes") ? PrivAES256.ID : null;
		OctetString _cryptPass = cryptPass == null ? null : new OctetString(cryptPass);

		Address targetAddress = GenericAddress.parse("udp:" + ip + "/" + port);
		UserTarget target = new UserTarget();
		target.setAddress(targetAddress);
		target.setRetries(3);
		target.setTimeout(timeout);
		target.setVersion(SnmpConstants.version3);
		target.setSecurityName(_username);
		// target.setMaxSizeRequestPDU(65535);
		UsmUser usera = null;
		if (userPass == null)
			target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
		else if (cryptPass == null)
			target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
		else
			target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
		usera = new UsmUser(_username, // security
				_authAlgo, // authprotocol
				_authPass, // authpassphrase
				_cryptAlgo, // privacyprotocol
				_cryptPass // privacypassphrase
		);

		TransportMapping transport = null;
		Snmp snmp = null;
		final SnmpWalkCounts counts = new SnmpWalkCounts();
		final HashMap<String, String> walkResults = new HashMap<String, String>();

		try {

			transport = new DefaultUdpTransportMapping();
			snmp = new Snmp(transport);
			snmp.listen();

			USM usm;
			usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
			SecurityModels.getInstance().addSecurityModel(usm);
			if (usera != null) {
				snmp.getUSM().addUser(usera.getSecurityName(), usera);
			} else {
				return null;
			}

			// ScopedPDU request = new ScopedPDU();
			PDU request = new PDU();
			request.setType(PDU.GETBULK);
			request.add(new VariableBinding(new OID(_oid)));
			request.setMaxRepetitions(10);
			request.setNonRepeaters(0);

			PDU response = null;

			TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
			OID[] oids = new OID[1];
			oids[0] = new OID(_oid);
			// List<TreeEvent> resulllts=treeUtils.getSubtree(target, new
			// OID(_oid));
			// List<TreeEvent> events = treeUtils.getSubtree(target, oid);
			// List<TreeEvent> events = treeUtils.walk(target, ifaces);

			final long startTime = System.nanoTime();
			TreeListener treeListener = new TreeListener() {

				private boolean finished;

				public boolean next(TreeEvent e) {
					counts.requests++;
					if (e.getVariableBindings() != null) {
						VariableBinding[] vbs = e.getVariableBindings();
						counts.objects += vbs.length;
						for (VariableBinding vb : vbs) {
							walkResults.put(vb.getOid().toString(), vb.getVariable().toString());
						}
					}
					return true;
				}

				public void finished(TreeEvent e) {
					if ((e.getVariableBindings() != null) && (e.getVariableBindings().length > 0)) {
						next(e);
					}
					// System.out.println();
					// System.out.println("Total requests sent:
					// "+counts.requests);
					// System.out.println("Total objects received:
					// "+counts.objects);
					// System.out.println("Total walk time: "+
					// (System.nanoTime()-startTime)/SnmpConstants.MILLISECOND_TO_NANOSECOND+"
					// milliseconds");
					if (e.isError()) {
						Logit.LogError("Net - Snmp3Walk", "The following error occurred during walk:"+e.getErrorMessage());
					}
					finished = true;
					synchronized (this) {
						this.notify();
					}
				}

				public boolean isFinished() {
					return finished;
				}

			};
			synchronized (treeListener) {
				treeUtils.getSubtree(target, new OID(_oid), null, treeListener);
				try {
					treeListener.wait();
				} catch (InterruptedException ex) {
					Logit.LogError("Net - Snmp3Walk()", "Tree retrieval interrupted: " + ex.getMessage());
					Thread.currentThread().interrupt();
				}
				return walkResults;
			}
		} catch (Exception e) {
			Logit.LogError("Net = Snmp3Walk()", "Unable to run Snmp2 WALK check! " + e.getMessage());
			return null;
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (IOException e) {
					Logit.LogError("Net = Snmp3Walk()", "Unable to close TransportMapping! may cause memory leak!" + e.getMessage());
				}
			}
			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					Logit.LogError("Net = Snmp3Walk()", "Unable to close SNMP! may cause memory leak!" + e.getMessage());

				}
			}
		}
	}

	// public static String Snmp3Walk(String ip, int port, int timeout, String
	// oid, String userName, String userPass,
	// String authAlgo, String cryptPass, String cryptAlgo, TransportMapping
	// transportMapping,
	// Snmp snmpInterface) {
	// Address targetAddress = GenericAddress.parse("udp:" + targetAddr + "/" +
	// portNum);
	// TransportMapping transport = new DefaultUdpTransportMapping();
	// Snmp snmp = new Snmp(transport);
	// transport.listen();
	//
	// // setting up target
	// CommunityTarget target = new CommunityTarget();
	// target.setCommunity(new OctetString(commStr));
	// target.setAddress(targetAddress);
	// target.setRetries(3);
	// target.setTimeout(1000 * 3);
	// target.setVersion(snmpVersion);
	//
	// OID oid = null;
	// try {
	// oid = new OID(oidStr);
	// } catch (RuntimeException ex) {
	// System.out.println("OID is not specified correctly.");
	// System.exit(1);
	// }
	//
	// TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
	// List<TreeEvent> events = treeUtils.getSubtree(target, oid);
	// if (events == null || events.size() == 0) {
	// System.out.println("No result returned.");
	// System.exit(1);
	// }
	//
	// // Get snmpwalk result.
	// for (TreeEvent event : events) {
	// if (event != null) {
	// if (event.isError()) {
	// System.err.println("oid [" + oid + "] " + event.getErrorMessage());
	// }
	//
	// VariableBinding[] varBindings = event.getVariableBindings();
	// if (varBindings == null || varBindings.length == 0) {
	// System.out.println("No result returned.");
	// }
	// for (VariableBinding varBinding : varBindings) {
	// System.out.println(varBinding.getOid() + " : " +
	// varBinding.getVariable().getSyntaxString() + " : "
	// + varBinding.getVariable());
	// }
	// }
	// }
	// snmp.close();
	// }

	// #endregion

	public static ArrayList<Object> Traceroute(String ip) {
		InetAddress endAddress;
		try {
			endAddress = InetAddress.getByName(ip);
		} catch (UnknownHostException ex) {
			Logit.LogInfo("Problem With Host IP:" + ip + "\n" + ex.getMessage());
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
				Logit.LogInfo("Error while processing traceroute on address" + ip);
				return null;
			}
		} catch (IOException e) {
			Logit.LogInfo("Error while processing traceroute on address" + ip);
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

class SnmpWalkCounts {
	public int requests;
	public int objects;
}
