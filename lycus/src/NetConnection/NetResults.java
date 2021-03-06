package NetConnection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import javax.persistence.EnumType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.snmp4j.smi.OID;
//import com.mysql.jdbc.ResultSetMetaData;
import Collectors.SnmpCollector;
import Collectors.SqlCollector;
import Elements.BaseElement;
import Elements.DiskElement;
import Elements.NicElement;
import GlobalConstants.Constants;
import GlobalConstants.Enums;
import GlobalConstants.Enums.HostType;
import GlobalConstants.Enums.SnmpError;
import Interfaces.INetResults;
import Probes.BaseProbe;
import Probes.DiscoveryProbe;
import Probes.HttpProbe;
import Probes.IcmpProbe;
import Probes.NicProbe;
import Probes.PortProbe;
import Probes.RBLProbe;
import Probes.SnmpProbe;
import Probes.SqlProbe;
import Probes.TracerouteProbe;
import Probes.DiskProbe;
import Results.DOMElement;
import Results.DiscoveryResult;
import Results.DiskResult;
import Results.NicResult;
import Results.PingResult;
import Results.PortResult;
import Results.RblResult;
import Results.SnmpResult;
import Results.SqlResult;
import Results.TraceRouteResult;
import Results.WebExtendedResult;
import Results.WebResult;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.Host;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class NetResults implements INetResults {
	private static NetResults netResults = null;

	private Map<String, NicResult> nicPreviousResults; // Map<runnableProbeId,

	// NicResult> for
	// calculating Delta
	// result

	protected NetResults() {
		nicPreviousResults = new HashMap<String, NicResult>();
	}

	public static NetResults getInstanece() {
		if (netResults == null)
			netResults = new NetResults();
		return netResults;
	}

	@Override
	public PingResult getPingResult(Host host, IcmpProbe probe) {
		ArrayList<Object> rawResults = Net.Pinger(host.getHostIp(), probe.getCount(), probe.getBytes(),
				probe.getTimeout());
		if (rawResults == null || rawResults.size() == 0)
			return null;

		long timestamp = (long) rawResults.get(0);
		int packetLoss = (int) rawResults.get(1);
		double rtt = (double) rawResults.get(2);
		int ttl = (int) rawResults.get(3);

		PingResult pingerResult = new PingResult(getRunnableProbeId(probe, host), timestamp, packetLoss, rtt, ttl);

		return pingerResult;
	}

	@Override
	public TraceRouteResult getTracerouteResult(Host host, TracerouteProbe probe) {
		ArrayList<Object> rawResults = Net.Traceroute(host.getHostIp());
		if (rawResults == null || rawResults.size() == 0)
			return null;

		long timestamp = (long) rawResults.get(0);
		ArrayList<ArrayList<Object>> route = (ArrayList<ArrayList<Object>>) rawResults.get(1);

		TraceRouteResult tracerouteResult = new TraceRouteResult(getRunnableProbeId(probe, host), timestamp);
		tracerouteResult.setRoutes(route);
		return tracerouteResult;
	}

	@Override
	public PortResult getPortResult(Host host, PortProbe probe) {
		ArrayList<Object> rawResults = null;
		switch (probe.getProto()) {
		case "TCP":
			rawResults = Net.TcpPorter(host.getHostIp(), probe.getPort(), probe.getTimeout());
			break;
		case "UDP":
			rawResults = Net.UdpPorter(host.getHostIp(), probe.getPort(), probe.getTimeout(), probe.getSendString(),
					probe.getReceiveString());
			break;
		}
		if (rawResults == null || rawResults.size() == 0)
			return null;

		long timestamp = (long) rawResults.get(0);
		int portState = ((boolean) rawResults.get(1)) ? 1 : 0;
		long responseTime = (long) rawResults.get(2);

		PortResult porterResult = new PortResult(getRunnableProbeId(probe, host), timestamp, portState, responseTime);

		return porterResult;
	}

	@Override
	public WebResult getWebResult(Host host, HttpProbe probe) {

		if (probe.getProbe_id().contains("http_c1fc4f98-8c31-44a5-99e8-004ba5dfc73e"))
			Logit.LogDebug("BREAKPOINT");

		ArrayList<Object> rawResults = Net.Weber(probe.getUrl(), probe.getHttpRequestType(), probe.getAuthUsername(),
				probe.getAuthPassword(), probe.getTimeout());
		if (rawResults == null || rawResults.size() == 0)
			return null;

		long timestamp = (long) rawResults.get(0);
		int responseCode = (int) rawResults.get(1);
		long responseTime = (long) rawResults.get(2);
		long responseSize = (long) rawResults.get(3);
		int stateCode = (int) rawResults.get(4);

		WebResult weberResult = new WebResult(getRunnableProbeId(probe, host), timestamp, responseCode, responseTime,
				responseSize, stateCode);

		return weberResult;
	}

	@Override
	public WebExtendedResult getWebExtendedResult(Host host, HttpProbe probe) {

		if (probe.getProbe_id().contains("http_c1fc4f98-8c31-44a5-99e8-004ba5dfc73e"))
			Logit.LogDebug("BREAKPOINT");

		JSONObject rawResults = Net.ExtendedWeber(probe.getUrl(), probe.getHttpRequestType(), probe.getAuthUsername(),
				probe.getAuthPassword(), probe.getTimeout());

		if (probe.getProbe_id().contains("http_c1fc4f98-8c31-44a5-99e8-004ba5dfc73e"))
			Logit.LogDebug("BREAKPOINT");

		if (rawResults == null || rawResults.size() == 0) {
			WebExtendedResult result = new WebExtendedResult(
					GeneralFunctions.getRunnableProbeId(probe.getTemplate_id(), host.getHostId(), probe.getProbe_id()),
					System.currentTimeMillis(), 1);
			result.setErrorMessage("Issue while running extended http probe - might be timeout");
			return result;
		}
		Long timestamp = fromHarTimeToEpoch(
				(String) ((JSONObject) ((JSONArray) ((JSONObject) rawResults.get("log")).get("pages")).get(0))
						.get("startedDateTime"));
		long responseTime = -1;
		int responseCode = -1;
		long responseSize = -1;
		try {

			responseTime = (long) ((JSONObject) ((JSONObject) ((JSONArray) ((JSONObject) rawResults.get("log"))
					.get("pages")).get(0)).get("pageTimings")).get("onLoad");
			responseCode = ((Long) ((JSONObject) ((JSONObject) ((JSONArray) ((JSONObject) rawResults.get("log"))
					.get("entries")).get(0)).get("response")).get("status")).intValue();
			responseSize = (long) ((JSONObject) ((JSONObject) ((JSONArray) ((JSONObject) rawResults.get("log"))
					.get("entries")).get(0)).get("response")).get("bodySize");

		} catch (Exception e) {
			Logit.LogWarn("Error while processing deep web result! RPID= " + GeneralFunctions
					.getRunnableProbeId(probe.getTemplate_id(), host.getHostId(), probe.getProbe_id()));

		}

		ArrayList<DOMElement> allElements = convertDOMElementsResult(
				((JSONArray) ((JSONObject) rawResults.get("log")).get("entries")));

		WebExtendedResult result;

		if (responseCode >= 400)
			result = new WebExtendedResult(getRunnableProbeId(probe, host), timestamp, responseTime, responseCode,
					responseSize, 2);
		else
			result = new WebExtendedResult(getRunnableProbeId(probe, host), timestamp, responseTime, responseCode,
					responseSize, 3);

		result.setAllElementsResults(allElements);

		return result;
	}

	private ArrayList<DOMElement> convertDOMElementsResult(JSONArray allElementsJson) {
		ArrayList<DOMElement> allElements = new ArrayList<DOMElement>();
		for (int i = 0; i < allElementsJson.size(); i++) {
			JSONObject elementJson = null;
			String nameEncoded = null;
			Long startTime = null;
			Long time = null;
			Integer responseStatusCode = null;
			Long size = null;
			Long waitTime = null;
			Long dnsTime = null;
			String mimeType = null;
			try {
				elementJson = (JSONObject) allElementsJson.get(i);
				nameEncoded = GeneralFunctions
						.Base64Encode(((String) ((JSONObject) elementJson.get("request")).get("url")));
				startTime = fromHarTimeToEpoch((String) elementJson.get("startedDateTime"));
				time = (long) elementJson.get("time");
				if (((JSONObject) elementJson.get("response")).get("status") == null)
					responseStatusCode = 400;
				else
					responseStatusCode = ((Long) ((JSONObject) elementJson.get("response")).get("status")).intValue();
				size = (long) ((JSONObject) elementJson.get("response")).get("bodySize");
				waitTime = ((long) ((JSONObject) elementJson.get("timings")).get("wait"));
				dnsTime = time - waitTime;
				mimeType = (String) ((JSONObject) ((JSONObject) elementJson.get("response")).get("content"))
						.get("mimeType");
			} catch (Exception e) {
				Logit.LogError("NetResults - convertDOMElementsResult",
						"Unable to process one of http extended check website's element! " + elementJson, e);
				continue;
			}
			DOMElement element = new DOMElement(nameEncoded, startTime, time, dnsTime, responseStatusCode, size,
					mimeType);
			allElements.add(element);
		}
		return allElements;
	}

	private Long fromHarTimeToEpoch(String dateHar) {
		dateHar = dateHar.replace("T", " ");
		dateHar = dateHar.replace("Z", "");
		SimpleDateFormat harDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = null;
		try {
			date = harDateFormat.parse(dateHar);
		} catch (ParseException e) {
			Logit.LogError("NetResults - fromHarTimeToEpoch", "Unable to parse date for http extended probe!", e);
			return null;
		}
		long epoch = date.getTime();
		return epoch;
	}

	@Override
	public RblResult getRblResult(Host host, RBLProbe probe) {

		// Logit.LogCheck("Checking RBL:
		// "+GeneralFunctions.invertIPAddress(host.getHostIp()) + "." +
		// probe.getRBL());

		ArrayList<Object> rawResults = Net.RBLCheck(host.getHostIp(), probe.getRBL());
		if (rawResults == null || rawResults.size() == 0)
			return null;

		long timestamp = (long) rawResults.get(0);
		int isListed = ((boolean) rawResults.get(1)) ? 1 : 0;

		RblResult rblResult = new RblResult(getRunnableProbeId(probe, host), timestamp, isListed);

		return rblResult;
	}

	@Override
	public List<SnmpResult> getSnmpResults(Host host, List<SnmpProbe> snmpProbes) {

		List<SnmpResult> allResults = new ArrayList<SnmpResult>();

		try {
			SnmpCollector snmpCollector = host.getSnmpCollector();

			HashMap<String, OID> probesOids = new HashMap<String, OID>();
			for (SnmpProbe snmpProbe : snmpProbes)
				probesOids.put(getRunnableProbeId(snmpProbe, host), snmpProbe.getOid());

			Map<String, String> rawResults = null;

			long timestamp = System.currentTimeMillis();
			switch (snmpCollector.getVersion()) {
			case 2:
				rawResults = Net.Snmp2GETBULK(host.getHostIp(), snmpCollector.getPort(), snmpCollector.getTimeout(),
						snmpCollector.getCommunityName(), probesOids.values());
				break;
			case 3:
				rawResults = Net.Snmp3GETBULK(host.getHostIp(), snmpCollector.getPort(), snmpCollector.getTimeout(),
						snmpCollector.getUserName(), snmpCollector.getAuthPass(), snmpCollector.getAlgo(),
						snmpCollector.getCryptPass(), snmpCollector.getCryptType(), probesOids.values());
				break;
			}
			if (rawResults == null || rawResults.size() == 0) {
				for (SnmpProbe snmpProbe : snmpProbes) {
					SnmpResult snmpResult = new SnmpResult(getRunnableProbeId(snmpProbe, host), timestamp);
					snmpResult.setError(SnmpError.NO_COMUNICATION);
					allResults.add(snmpResult);
				}

				return allResults;
			}

			for (SnmpProbe snmpProbe : snmpProbes) {
				String stringResult = rawResults.get((snmpProbe).getOid().toString());
				SnmpResult snmpResult;
				if (stringResult == null)
					snmpResult = new SnmpResult(getRunnableProbeId(snmpProbe, host), timestamp, Constants.WRONG_OID);
				else
					snmpResult = new SnmpResult(getRunnableProbeId(snmpProbe, host), timestamp, stringResult);
				allResults.add(snmpResult);
			}

		} catch (Exception e) {
			for (SnmpProbe snmpProbe : snmpProbes) {
				SnmpResult snmpResult = new SnmpResult(getRunnableProbeId(snmpProbe, host), System.currentTimeMillis());
				snmpResult.setError(SnmpError.EXCEPTION_ON_REQUEST);
				allResults.add(snmpResult);
			}
			return allResults;
		}
		return allResults;
	}

	@Override
	public NicResult getNicResult(Host host, NicProbe probe) {

		SnmpCollector snmpTemplate = host.getSnmpCollector();

		Set<OID> nicOids = new HashSet<OID>();
		nicOids.add(probe.getIfinoctetsOID());
		nicOids.add(probe.getIfoutoctetsOID());

		Map<String, String> rawResults = null;

		long timestamp = System.currentTimeMillis();
		switch (snmpTemplate.getVersion()) {
		case 2:
			rawResults = Net.Snmp2GETBULK(host.getHostIp(), snmpTemplate.getPort(), snmpTemplate.getTimeout(),
					snmpTemplate.getCommunityName(), nicOids);
			break;
		case 3:
			rawResults = Net.Snmp3GETBULK(host.getHostIp(), snmpTemplate.getPort(), snmpTemplate.getTimeout(),
					snmpTemplate.getUserName(), snmpTemplate.getAuthPass(), snmpTemplate.getAlgo(),
					snmpTemplate.getCryptPass(), snmpTemplate.getCryptType(), nicOids);
			break;
		}

		if (rawResults == null || rawResults.size() == 0) {
			NicResult nicResult = new NicResult(getRunnableProbeId(probe, host));
			nicResult.setError(SnmpError.NO_COMUNICATION);
			return nicResult;
		}
		long interfaceInOctets = Long.parseLong(rawResults.get(probe.getIfinoctetsOID().toString()));
		long interfaceOutOctets = Long.parseLong(rawResults.get(probe.getIfoutoctetsOID().toString()));

		NicResult nicResut = getNicDeltaTimeResult(getRunnableProbeId(probe, host), timestamp, interfaceInOctets,
				interfaceOutOctets);

		return nicResut;
	}

	private NicResult getNicDeltaTimeResult(String rpID, long timeStamp, long interfaceInOctets,
			long interfaceOutOctets) {
		NicResult nicResult = new NicResult(rpID);
		NicResult nicPrivResult = nicPreviousResults.get(rpID);

		if (nicPrivResult != null) {
			nicResult.setPreviousTimestamp(nicPrivResult.getCurrentTimestamp());
			nicResult.setPreviousInterfaceInOctets(nicPrivResult.getCurrrentInterfaceInOctets());
			nicResult.setPreviousInterfaceOutOctets(nicPrivResult.getCurrentInterfaceOutOctets());
		}
		nicResult.setCurrentTimestamp(timeStamp);
		nicResult.setCurrrentInterfaceInOctets(interfaceInOctets);
		nicResult.setCurrentInterfaceOutOctets(interfaceOutOctets);

		nicPreviousResults.put(nicResult.getRunnableProbeId(), nicResult);

		return nicResult;
	}

	private String getRunnableProbeId(BaseProbe probe, Host host) {
		return probe.getTemplate_id().toString() + "@" + host.getHostId().toString() + "@" + probe.getProbe_id();
	}

	@Override
	public DiscoveryResult getDiscoveryResult(Host host, DiscoveryProbe probe) {

		DiscoveryResult discoveryResult = null;

		String walkOid;

		if (probe.getProbe_id().equals("discovery_3ee653fc-adaa-468e-9430-b1793b1d1c7d")
				&& host.getHostId().toString().equals("bf4e7e1c-4c44-4e0f-bee5-871aadfe1174")) {
			Logit.LogDebug("test");
		}

		long timestamp = System.currentTimeMillis();
		HashMap<String, BaseElement> elements = null;
		switch (probe.getType()) {
		case bw:
			elements = this.getNicElements(host);
			break;
		case ds:
			elements = this.getDiskElements(host);
			break;
		}
		if (elements == null) {
			discoveryResult = new DiscoveryResult(getRunnableProbeId(probe, host), timestamp, null);
			discoveryResult.setErrorMessage("no elements found!");
		} else
			discoveryResult = new DiscoveryResult(getRunnableProbeId(probe, host), timestamp, elements);
		return discoveryResult;
	}

	private HashMap<String, BaseElement> getDiskElements(Host host) {
		long checkTime;

		Map<String, String> hrStorageResults = null;
		SnmpCollector snmpTemplate = host.getSnmpCollector();

		if (snmpTemplate == null)
		{
			Logit.LogWarn("Unable to get host's snmp collector: hostID=" + host.getHostId());
		}
		int snmpVersion = snmpTemplate.getVersion();
		if (snmpVersion == 2) {
			hrStorageResults = Net.Snmp2Walk(host.getHostIp(), snmpTemplate.getPort(),
					host.getSnmpCollector().getTimeout(), snmpTemplate.getCommunityName(),
					Constants.storageAll.toString());
		} else if (snmpVersion == 3) {
			hrStorageResults = Net.Snmp3Walk(host.getHostIp(), snmpTemplate.getPort(), snmpTemplate.getTimeout(),
					snmpTemplate.getName(), snmpTemplate.getAuthPass(), snmpTemplate.getAlgo(),
					snmpTemplate.getCryptPass(), snmpTemplate.getCryptType(), Constants.storageAll.toString());
		}

		if (hrStorageResults == null || hrStorageResults.size() == 0)
			return null;

		HashMap<String, BaseElement> lastScanElements = this.convertDisksWalkToElements(hrStorageResults);
		return lastScanElements;
	}

	private HashMap<String, BaseElement> getNicElements(Host host) {

		if (host.getHostIp().contains("62.90.102.34"))
			Logit.LogDebug("BP");
		long checkTime;

		Map<String, String> ifDescrResults = null;
		Map<String, String> sysDescrResults = null;

		SnmpCollector snmpTemplate = host.getSnmpCollector();

		ArrayList<OID> oids = new ArrayList<OID>();
		oids.add(Constants.sysDescr);

		String ifAllOid = Constants.ifAll_high.toString();

		Enums.InterfaceSpeed nicSpeed = Enums.InterfaceSpeed.high;

		int snmpVersion = snmpTemplate.getVersion();
		if (snmpVersion == 2) {
			ifDescrResults = Net.Snmp2Walk(host.getHostIp(), snmpTemplate.getPort(),
					host.getSnmpCollector().getTimeout(), snmpTemplate.getCommunityName(), ifAllOid);
			sysDescrResults = Net.Snmp2GETBULK(host.getHostIp(), snmpTemplate.getPort(),
					host.getSnmpCollector().getTimeout(), snmpTemplate.getCommunityName(), oids);
		} else if (snmpVersion == 3) {
			ifDescrResults = Net.Snmp3Walk(host.getHostIp(), snmpTemplate.getPort(),
					host.getSnmpCollector().getTimeout(), snmpTemplate.getUserName(), snmpTemplate.getAuthPass(),
					snmpTemplate.getAlgo(), snmpTemplate.getCryptPass(), snmpTemplate.getCryptType(), ifAllOid);
			sysDescrResults = Net.Snmp3GETBULK(host.getHostIp(), snmpTemplate.getPort(), snmpTemplate.getTimeout(),
					snmpTemplate.getUserName(), snmpTemplate.getAuthPass(), snmpTemplate.getAlgo(),
					snmpTemplate.getCryptPass(), snmpTemplate.getCryptType(), oids);
		}
		if (ifDescrResults == null || sysDescrResults == null || ifDescrResults.size() == 0
				|| sysDescrResults.size() == 0) {
			ifAllOid = Constants.ifAll_low.toString();
			if (snmpVersion == 2) {
				ifDescrResults = Net.Snmp2Walk(host.getHostIp(), snmpTemplate.getPort(),
						host.getSnmpCollector().getTimeout(), snmpTemplate.getCommunityName(), ifAllOid);
				sysDescrResults = Net.Snmp2GETBULK(host.getHostIp(), snmpTemplate.getPort(),
						host.getSnmpCollector().getTimeout(), snmpTemplate.getCommunityName(), oids);
			} else if (snmpVersion == 3) {
				ifDescrResults = Net.Snmp3Walk(host.getHostIp(), snmpTemplate.getPort(),
						host.getSnmpCollector().getTimeout(), snmpTemplate.getUserName(), snmpTemplate.getAuthPass(),
						snmpTemplate.getAlgo(), snmpTemplate.getCryptPass(), snmpTemplate.getCryptType(), ifAllOid);
				sysDescrResults = Net.Snmp3GETBULK(host.getHostIp(), snmpTemplate.getPort(), snmpTemplate.getTimeout(),
						snmpTemplate.getUserName(), snmpTemplate.getAuthPass(), snmpTemplate.getAlgo(),
						snmpTemplate.getCryptPass(), snmpTemplate.getCryptType(), oids);
			}
			if (ifDescrResults == null || sysDescrResults == null || ifDescrResults.size() == 0
					|| sysDescrResults.size() == 0)
				return null;
			nicSpeed = Enums.InterfaceSpeed.low;
		}
		Enums.HostType hostType = Utils.GeneralFunctions
				.getHostType(sysDescrResults.get(Constants.sysDescr.toString()));

		HashMap<String, BaseElement> lastScanElements = this.convertNicsWalkToElements(ifDescrResults, hostType,
				nicSpeed);
		return lastScanElements;
	}

	private HashMap<String, BaseElement> convertNicsWalkToElements(Map<String, String> nicsWalk, HostType hostType,
			Enums.InterfaceSpeed nicSpeed) {

		HashMap<String, Integer> duplicationCounters = new HashMap<String, Integer>();

		HashMap<String, BaseElement> lastElements = new HashMap<String, BaseElement>();
		if (hostType == null)
			return null;
		for (Map.Entry<String, String> entry : nicsWalk.entrySet()) {
			if (!(entry.getKey().toString().contains("1.3.6.1.2.1.2.2.1.1.")
					|| entry.getKey().toString().contains("1.3.6.1.2.1.31.1.1.1.1.")))
				continue;
			int index;
			String name;
			long ifSpeed;

			if (nicSpeed == Enums.InterfaceSpeed.low) {
				index = Integer.parseInt(entry.getValue());
				ifSpeed = Long.parseLong(nicsWalk.get("1.3.6.1.2.1.2.2.1.5." + index));
				switch (hostType) {
				case Windows:
					name = GeneralFunctions.convertHexToString(nicsWalk.get("1.3.6.1.2.1.2.2.1.2." + index));
					if (name == null) {
						continue;
					}
					break;
				case Linux:
					name = nicsWalk.get("1.3.6.1.2.1.2.2.1.2." + index);
					break;
				default:
					return null;
				}

			} else {
				index = Integer.parseInt(
						entry.getKey().toString().split("\\.")[entry.getKey().toString().split("\\.").length - 1]);
				ifSpeed = Long.parseLong(nicsWalk.get("1.3.6.1.2.1.31.1.1.1.15." + index));
				name = nicsWalk.get("1.3.6.1.2.1.31.1.1.1.1." + index);
			}
			if (index == 0) {
				continue;
			}
			if (duplicationCounters.containsKey(name)) {
				String newName = name + "_" + duplicationCounters.get(name);
				duplicationCounters.put(name, duplicationCounters.get(name) + 1);
				NicElement nicElement = new NicElement(index, newName, hostType, ifSpeed, nicSpeed);
				lastElements.put(newName, nicElement);

			} else if (lastElements.containsKey(name) && !duplicationCounters.containsKey(name)) {
				String newNameFirstDuplication = name + "_0";
				lastElements.get(name).setName(newNameFirstDuplication);
				lastElements.put(newNameFirstDuplication, lastElements.get(name));
				lastElements.remove(name);
				duplicationCounters.put(name, 1);
				String newName = name + "_" + duplicationCounters.get(name);
				duplicationCounters.put(name, duplicationCounters.get(name) + 1);
				NicElement nicElement = new NicElement(index, newName, hostType, ifSpeed, nicSpeed);
				lastElements.put(newName, nicElement);
			} else {
				NicElement nicElement = new NicElement(index, name, hostType, ifSpeed, nicSpeed);
				lastElements.put(name, nicElement);
			}
		}

		if (lastElements.size() == 0)
			return null;

		return lastElements;
	}

	private HashMap<String, BaseElement> convertDisksWalkToElements(Map<String, String> disksWalk) {
		HashMap<String, BaseElement> lastElements = new HashMap<String, BaseElement>();
		for (Map.Entry<String, String> entry : disksWalk.entrySet()) {
			if (!entry.getKey().toString().contains("1.3.6.1.2.1.25.2.3.1.1."))
				continue;
			int index = Integer.parseInt(entry.getValue());
			if (index == 0) {
				continue;
			}

			String name;
			long hrStorageAllocationUnits;
			long hrStorageSize;
			long hrStorageUsed;

			name = disksWalk.get("1.3.6.1.2.1.25.2.3.1.3." + index);
			// hrStorageAllocationUnits =
			// Long.parseLong(disksWalk.get("1.3.6.1.2.1.25.2.3.1.1.4." +
			// index));
			// hrStorageSize =
			// Long.parseLong(disksWalk.get("1.3.6.1.2.1.25.2.3.1.1.5." +
			// index));
			// hrStorageUsed =
			// Long.parseLong(disksWalk.get("1.3.6.1.2.1.25.2.3.1.1.6." +
			// index));

			DiskElement diskElement = new DiskElement(index, name, false);
			lastElements.put(name, diskElement);
		}

		if (lastElements.size() == 0)
			return null;

		return lastElements;
	}

	public DiskResult getDiskResult(Host host, DiskProbe probe) {
		SnmpCollector snmpTemplate = host.getSnmpCollector();

		Set<OID> storageOids = new HashSet<OID>();
		storageOids.add(probe.getHrstorageallocationunitsoid());
		storageOids.add(probe.getHrstoragesizeoid());
		storageOids.add(probe.getHrstorageusedoid());

		Map<String, String> rawResults = null;

		long timestamp = System.currentTimeMillis();
		switch (snmpTemplate.getVersion()) {
		case 2:
			rawResults = Net.Snmp2GETBULK(host.getHostIp(), snmpTemplate.getPort(), snmpTemplate.getTimeout(),
					snmpTemplate.getCommunityName(), storageOids);
			break;
		case 3:
			rawResults = Net.Snmp3GETBULK(host.getHostIp(), snmpTemplate.getPort(), snmpTemplate.getTimeout(),
					snmpTemplate.getUserName(), snmpTemplate.getAuthPass(), snmpTemplate.getAlgo(),
					snmpTemplate.getCryptPass(), snmpTemplate.getCryptType(), storageOids);
			break;
		}

		if (rawResults == null) {
			DiskResult result = new DiskResult(
					GeneralFunctions.getRunnableProbeId(probe.getTemplate_id(), host.getHostId(), probe.getProbe_id()));
			result.setErrorMessage(SnmpError.NO_COMUNICATION.name());
			return result;
		}

		if (rawResults.size() == 0) {
			DiskResult result = new DiskResult(
					GeneralFunctions.getRunnableProbeId(probe.getTemplate_id(), host.getHostId(), probe.getProbe_id()));
			result.setErrorMessage(SnmpError.NO_COMUNICATION.name());
			return result;
		}
		long hrstorageallocationunitsoid = Long
				.parseLong(rawResults.get(probe.getHrstorageallocationunitsoid().toString()));
		long hrstoragesizeoid = Long.parseLong(rawResults.get(probe.getHrstoragesizeoid().toString()));
		long hrstorageusedoid = Long.parseLong(rawResults.get(probe.getHrstorageusedoid().toString()));

		DiskResult diskResut = new DiskResult(getRunnableProbeId(probe, host), timestamp, hrstorageusedoid,
				hrstoragesizeoid, hrstorageallocationunitsoid);

		return diskResut;
	}

	@Override
	public SqlResult getSqlResult(Host host, SqlProbe probe) {
		try {
			SqlCollector sqlTemplate = host.getSqlCollector();
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			// jdbc:mysql://localhost/test?user=fred&password=secret&ssl=true
			String ssl = (sqlTemplate.getSql_sec().equals("1")) ? "?ssl=true" : "";
			Connection con = DriverManager
					.getConnection(
							"jdbc:mysql://" + host.getHostIp() + ":" + sqlTemplate.getSql_port() + "/"
									+ probe.getSql_db() + ssl,
							sqlTemplate.getSql_user(), host.getSqlCollector().getSql_password());

			Statement stmt = con.createStatement();
			String sql = probe.getSql_query();
			// sql = "SHOW STATUS LIKE 'Ssl_cipher';";
			ResultSet rs = stmt.executeQuery(sql);
			String[] sqlResults = new String[probe.getSql_fields().length];
			rs.next();
			int index = 0;

			for (String columnName : probe.getSql_fields()) {
				int column = rs.findColumn(columnName);
				sqlResults[index++] = rs.getString(column);
			}

			SqlResult result = new SqlResult(getRunnableProbeId(probe, host), sqlTemplate.getTimeout(), sqlResults,
					probe.getSql_fields());
			con.close();
			return result;
		} catch (Exception e) {
			Logit.LogError("NetResults - getSqlResult", "Error getting sql results, Probe name: " + probe.getName(), e);
		}

		return null;
	}

}
