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

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.Host;

/**
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
				Process p = null;
				try {
					StringBuilder b = new StringBuilder();
					b.append("/usr/bin/fping").append(" ").append("-i").append(" ").append("25").append(" ")
							.append("-q").append(" ").append("-c").append(" ").append(numOfPings).append(" ")
							.append("-t").append(" ").append(timeout).append(" ").append("-b").append(" ")
							.append(sizeOfPings).append(" ").append(ip);

					p = Runtime.getRuntime().exec(b.toString());
					// BufferedReader stdInput = new BufferedReader(new
					// InputStreamReader(p.getInputStream()));
					BufferedReader errInput = new BufferedReader(new InputStreamReader(p.getErrorStream()));

					String packetLoss = "100";
					String rttAvg = "0";
					String ttl = "0";

					String fpingOutput = errInput.readLine();
					packetLoss = getPacketLoss(fpingOutput);
					if (!packetLoss.equals("100"))
						rttAvg = getRTT(fpingOutput);

					if ("100".equals(packetLoss)) {
						pingResults.add(System.currentTimeMillis());
						pingResults.add(100);
						pingResults.add(0.0);
						pingResults.add(0);
						return pingResults;

					} else {
						pingResults.add(System.currentTimeMillis());
						pingResults.add(Integer.parseInt(packetLoss));
						pingResults.add(Double.parseDouble(rttAvg));
						pingResults.add(Integer.parseInt(ttl));
						return pingResults;

					}
				} catch (Exception e) {
					pingResults.add(System.currentTimeMillis());
					pingResults.add(100);
					pingResults.add(0.0);
					pingResults.add(0);
					return pingResults;
				} finally {
					if (p != null)
						p.destroy();
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

	private static String getPacketLoss(String line) {
		return line.split("/")[4].split("%")[0];
	}

	private static String getRTT(String line) {
		return line.split("/")[7];
	}

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
		try {
			HttpRequest request;
			String UserPass;
			int response_state = 0;
			ArrayList<Object> webResults = new ArrayList<Object>();

			webResults.add(System.currentTimeMillis());

			if (user != null && pass != null) {
				UserPass = user + ":" + pass;
				request = new HttpRequest(url, requestType.equals("POST") ? RequestTypes.POST : RequestTypes.GET,
						timeout, UserPass);
			} else {
				request = new HttpRequest(url, requestType.equals("POST") ? RequestTypes.POST : RequestTypes.GET,
						timeout);
			}

			request.Execute();

			if (request.getIsTimeOut()) {
				webResults.add(0);
				webResults.add(0L);
				webResults.add(0L);
				webResults.add(1);
				return webResults;
			}
			String contentType = request.getResponseEntity().getContentType().getValue().toString().split(";")[0]
					.toString();
			String contentType1 = contentType.split("/")[0].toString();
			String contentType2 = contentType.split("/")[1].toString();

			HashMap<String, List<String>> mimeTypes = new HashMap<>();
			mimeTypes.put("application",
					Arrays.asList("jason", "javascript", "x-javascript", "xml", "xhtml+xml", "rss+xml", "soap+xml"));
			mimeTypes.put("text", Arrays.asList("x-json", "html", "javascript", "plain", "xml"));

			if (mimeTypes.get(contentType1).contains(contentType2)) {

				if (request.getResponseStatusCode() >= 400) {
					long pageSize = GeneralFunctions.fromStringToBytes(request.getResponsePageContent(),
							"UTF-8").length;
					// status code
					webResults.add(request.getResponseStatusCode());
					// query time
					webResults.add(request.getResponseQueryTime());
					// page size in bytes
					webResults.add(pageSize);
					webResults.add(2);
					return webResults;
				}
				long pageSize = GeneralFunctions.fromStringToBytes(request.getResponsePageContent(), "UTF-8").length;
				// status code
				webResults.add(request.getResponseStatusCode());
				// query time
				webResults.add(request.getResponseQueryTime());
				// page size in bytes
				webResults.add(pageSize);
				webResults.add(3);
				return webResults;
			} else {
				webResults.add(-1);
				webResults.add(-1L);
				webResults.add(-1L);
				webResults.add(2);

				return webResults;
			}
		} catch (Exception e) {
			Logit.LogError("Net - Weber", "Error while running http check! URL: " + url + ", phantomjs output: ", e);
			e.printStackTrace();
			return null;
		}
	}

	public static JSONObject ExtendedWeber(String url, String requestType, String user, String pass, int timeout) {

		Process p = null;
		StringBuilder sb = new StringBuilder();
		try {

			StringBuilder b = new StringBuilder();
			if (user != null && pass != null)
				b.append("phantomjs/phantomjs").append(" ").append("phantomjs/netsniff_auth.js").append(" ").append(url)
						.append(" ").append(user).append(" ").append(pass).append(" ").append(timeout);
			else
				b.append("phantomjs/phantomjs").append(" ").append("phantomjs/netsniff.js").append(" ").append(url)
						.append(" ").append(timeout);

			p = Runtime.getRuntime().exec(b.toString());
			// p = Runtime.getRuntime().exec(new
			// String[]{"bash","-c",b.toString()});

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line;
			while ((line = stdInput.readLine()) != null) {
				sb.append(line);
			}

			String phantomOutput = sb.toString();

			if (phantomOutput.equals(""))
				return null;

			if (phantomOutput.equals("FAIL to load the address")) {
				Logit.LogInfo("Error processing probe - might caused by timeout as well, Failed URL:" + url);
				return null;
			}
			if (phantomOutput.startsWith("ReferenceError")) {
				if (phantomOutput.contains("{")) {
					int firstBracket = -1;
					for (int i = 0; i < phantomOutput.length(); i++) {
						if (phantomOutput.charAt(i) == '{') {
							firstBracket = i;
							break;
						}
					}
					phantomOutput = phantomOutput.substring(firstBracket);
				} else {
					Logit.LogInfo("Error processing probe - might caused by timeout as well, Failed URL:" + url);
					return null;
				}
			}

			JSONObject harFile = (JSONObject) new JSONParser().parse(phantomOutput);
			return harFile;
			// WebClient webClient = new WebClient(BrowserVersion.CHROME);
			// HtmlPage htmlPage = webClient.getPage("http://www.walla.co.il/");
			// long timeStart=System.currentTimeMillis();
			// Document doc = Jsoup.connect("http://www.walla.co.il/").get();
			// long endTime=System.currentTimeMillis();
			//
			//
			// Elements allElements=doc.getAllElements();
			// // get all links in page
			// Elements links = doc.select("a[href]");//a links
			// Elements cssLinks = doc.select("link[href]");//links
			// Elements scripts = doc.select("script[src]"); //scripts
			// Elements images = doc.select("img[src]"); //images
			//
			// for (Element link : allElements) {
			// System.out.println(link.text());
			// System.out.println(link.toString());
			//
			// // get the value from the href attribute
			//// System.out.println("\nimage: " + link.attr("src"));
			//// System.out.println("language: " + link.attr("type"));
			// }
			//// System.out.println(htmlPage.asText());
			//// System.out.println(htmlPage.getDocumentElement());
			//// for (DomElement element : htmlPage.getDomElementDescendants())
			// {
			//// System.out.println(element.toString());
			//// }
			// // fetch the document over HTTP
			//
			// // get the page title
			// String title = doc.title();
			// System.out.println("title: " + title);
			//
			// // get all links in page
			//// Elements links = doc.select("a[href]");
			//// for (Element link : links) {
			//// // get the value from the href attribute
			//// System.out.println("\nlink: " + link.attr("href"));
			//// System.out.println("text: " + link.text());
			// }
		} catch (Exception e) {
			if (e instanceof ParseException) {
				Logit.LogError("Net - ExtendedWeber",
						"Error while running http extended check! unable to parse phantomjs output, URL: " + url, e);
			} else {
				if (e.getMessage().contains("No such file or directory"))
					Logit.LogError("Net - ExtendedWeber",
							"Error while running http extended check! unable to find phantomjs module, URL: " + url, e);
				else
					Logit.LogError("Net - ExtendedWeber", "Error while running http extended check!, URL: " + url
							+ ", phantomjs output: " + sb.toString(), e);
			}
		} finally {
			if (p != null)
				p.destroy();
		}
		return null;
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

		TransportMapping transport = null;
		Snmp snmp = null;

		try {
			Address targetAddress = GenericAddress.parse("udp:" + ip + "/" + port);
			CommunityTarget target = new CommunityTarget();
			target.setAddress(targetAddress);
			target.setRetries(3);
			target.setTimeout(timeout);
			target.setVersion(SnmpConstants.version2c);
			target.setCommunity(new OctetString(comName));

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
					return null;
				}
			}
			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					Logit.LogError(null, "Unable to close SNMP! may cause memory leak!");
					return null;
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
		TransportMapping transport = transportMapping;
		Snmp snmp = snmpInterface;
		if (transport == null || snmp == null)
			return null;

		try {
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
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (IOException e) {
					Logit.LogError(null, "Unable to close TransportMapping! may cause memory leak!");
					return null;
				}
			}
			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					Logit.LogError(null, "Unable to close SNMP! may cause memory leak!");
					return null;
				}
			}
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
		// Logit.LogError("Net - Snmp2GET", "Unable to close TransportMapping!
		// may cause memory leak!");
		// }
		// }
		// if (snmp != null) {
		// try {
		// snmp.close();
		// } catch (IOException e) {
		// Logit.LogError("Net - Snmp2GET", "Unable to close TransportMapping!
		// may cause memory leak!");

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
		// Logit.LogError("Net - Snmp2GET", "Unable to close TransportMapping!
		// may cause memory leak!");

		// }
		// }
		// if (snmp != null) {
		// try {
		// snmp.close();
		// } catch (IOException e) {
		// Logit.LogError("Net - Snmp2GET", "Unable to close TransportMapping!
		// may cause memory leak!");

		//
		// }
		// }
		// }

	}

	public static Map<String, String> Snmp2WalkOLD(final String ip, int port, int timeout, String comName,
			String _oid) {
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
						Logit.LogWarn("The following error occurred during walk:" + e.getErrorMessage() + ", for host: "
								+ ip);
						return;
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
				// try {
				// treeListener.wait();
				// } catch (InterruptedException ex) {
				// Logit.LogError("Net - Snmp2Walk",
				// "Tree retrieval interrupted:" + ex.getMessage() + ", for
				// host: " + ip);
				// Thread.currentThread().interrupt();
				// }
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

	public static Map<String, String> Snmp3WalkOLD(String ip, int port, int timeout, String userName, String userPass,
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
						Logit.LogWarn("The following error occurred during walk:" + e.getErrorMessage());
						return;
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
				// try {
				// treeListener.wait();
				// } catch (InterruptedException ex) {
				// Logit.LogError("Net - Snmp3Walk()", "Tree retrieval
				// interrupted: " + ex.getMessage());
				// Thread.currentThread().interrupt();
				// }
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
					Logit.LogError("Net = Snmp3Walk()",
							"Unable to close TransportMapping! may cause memory leak!" + e.getMessage());
				}
			}
			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					Logit.LogError("Net = Snmp3Walk()",
							"Unable to close SNMP! may cause memory leak!" + e.getMessage());

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
						+ " | tail -n+2 | awk \'{ print $1 \",\" $2 \",\" $3 }\'" };

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

		ArrayList<ArrayList<Object>> convertedRoutes = convertTracerouteOutput(route);
		ArrayList<Object> results = new ArrayList<Object>();
		results.add(System.currentTimeMillis());
		results.add(convertedRoutes);
		return results;

	}

	private static ArrayList<ArrayList<Object>> convertTracerouteOutput(String trcrt) {
		ArrayList<ArrayList<Object>> routes = new ArrayList<ArrayList<Object>>();
		String[] lines = trcrt.split("\\r?\\n");
		for (String line : lines) {
			int index = Integer.parseInt(line.split(",")[0]);
			String route = line.split(",")[1];
			double rtt;
			if (route.equals("*"))
				rtt = -1;
			else
				rtt = Double.parseDouble(line.split(",")[2]);
			ArrayList<Object> hop = new ArrayList<Object>();
			hop.add(route);
			hop.add(rtt);
			routes.add(hop);
		}
		return routes;
	}

	/**
	 * @param ip
	 *            - ip address
	 * @param RBL
	 *            - RBL address
	 * @return timestamp, isListed
	 */
	public static ArrayList<Object> RBLCheck(String ip, String RBL) {
		ArrayList<Object> results = new ArrayList<Object>();
		long probeTimestamp = System.currentTimeMillis();
		results.add(probeTimestamp);
		try {
			Logit.LogCheck("Checking RBL: "+GeneralFunctions.invertIPAddress(ip) + "." + RBL);
			InetAddress address=InetAddress.getByName(GeneralFunctions.invertIPAddress(ip) + "." + RBL);
			Logit.LogCheck("Results RBL: "+GeneralFunctions.invertIPAddress(ip) + "." + RBL+", are: "+address.toString());

			results.add(true);
		} catch (UnknownHostException e) {
			Logit.LogCheck("Results RBL: "+GeneralFunctions.invertIPAddress(ip) + "." + RBL+", are: No record found.");
			results.add(false);
		} catch (Exception e2) {
			Logit.LogError("Net - RBLCheck","OtherException: "+GeneralFunctions.invertIPAddress(ip) + "." + RBL,e2);
			results.add(false);
		}
		return results;
	}

	public static long getDnsResolutionTime(String hostname) {
		long start = System.currentTimeMillis();
		try {
			InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		return end - start;
	}

	public static Map<String, String> Snmp2Walk(String ip, int port, int timeout, String comName, String _oid) {

		if (ip.equals("62.90.132.23"))
			Logit.LogDebug("BREAKPOINT");

		HashMap<String, String> results = new HashMap<String, String>();

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

		try {

			transport = new DefaultUdpTransportMapping();
			snmp = new Snmp(transport);
			transport.listen();

			OID oid = null;
			try {
				oid = new OID(_oid);
			} catch (RuntimeException ex) {
				Logit.LogInfo("OID specified is not valid! - " + _oid.toString());
				return null;
			}

			TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
			List<TreeEvent> events = treeUtils.getSubtree(target, oid);
			if (events == null || events.size() == 0) {
				Logit.LogInfo("No result returned for snmp walk check.");
				return null;
			}

			// Get snmpwalk result.

			for (TreeEvent event : events) {
				if (event != null) {
					if (event.isError()) {
						Logit.LogInfo("error on snmp walk check for OID " + oid + " !");
					}

					VariableBinding[] varBindings = event.getVariableBindings();
					if (varBindings == null || varBindings.length == 0) {
						Logit.LogInfo(
								"No result returned for snmp walk check. varBindings is empty! host checked: " + ip);
					} else
						for (VariableBinding varBinding : varBindings) {
							results.put(varBinding.getOid().toString(), varBinding.getVariable().toString());
						}
				}
			}

			return results;
		} catch (Exception e) {
			Logit.LogError("Net - Snmp2Walk",
					"Unable to run Snmp2 WALK check! " + e.getMessage() + ", for host: " + ip);
			return null;
		} finally {
			try {
				snmp.close();
			} catch (IOException e) {
				Logit.LogError("Net - Snmp2Walk", "Error while closing SNMP connection!", e);
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

			OID oid = null;
			try {
				oid = new OID(_oid);
			} catch (RuntimeException ex) {
				Logit.LogInfo("OID specified is not valid! - " + _oid.toString());
				return null;
			}

			TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
			List<TreeEvent> events = treeUtils.getSubtree(target, oid);
			if (events == null || events.size() == 0) {
				Logit.LogInfo("No result returned for snmp walk check.");
				return null;
			}

			// Get snmpwalk result.

			for (TreeEvent event : events) {
				if (event != null) {
					if (event.isError()) {
						Logit.LogInfo("error on snmp walk check for OID " + oid + " !");
					}

					VariableBinding[] varBindings = event.getVariableBindings();
					if (varBindings == null || varBindings.length == 0) {
						Logit.LogInfo("No result returned for snmp walk check.");
						return null;
					}
					for (VariableBinding varBinding : varBindings) {
						results.put(varBinding.getOid().toString(), varBinding.getVariable().toString());
					}
				}
			}

			return results;
		} catch (Exception e) {
			Logit.LogError("Net = Snmp3Walk()", "Unable to run Snmp2 WALK check! " + e.getMessage());
			return null;
		} finally {
			try {
				snmp.close();
			} catch (IOException e) {
				Logit.LogError("Net - Snmp3Walk", "Error while closing SNMP connection!", e);
			}
		}
	}

}

class SnmpWalkCounts {
	public int requests;
	public int objects;
}
