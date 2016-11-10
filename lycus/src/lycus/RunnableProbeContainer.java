package lycus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import GlobalConstants.Constants;
import GlobalConstants.GlobalConfig;
import GlobalConstants.ProbeTypes;
import Interfaces.IRunnableProbeContainer;
import Model.ThreadsCount;
import Probes.BaseProbe;
import Probes.DiscoveryProbe;
import Probes.DiskProbe;
import Probes.HttpProbe;
import Probes.IcmpProbe;
import Probes.NicProbe;
import Probes.PortProbe;
import Probes.RBLProbe;
import Probes.SnmpProbe;
import Probes.TracerouteProbe;
import Utils.Logit;

public class RunnableProbeContainer implements IRunnableProbeContainer {

	private static RunnableProbeContainer runnableProbeContainer = null;

	private ConcurrentHashMap<String, RunnableProbe> runnableProbes; // HashMap<runnableProbeId,RunnableProbe>
	private ConcurrentHashMap<String, ConcurrentHashMap<String, RunnableProbe>> hostRunnableProbes; // HashMap<hostId,
	// HashMap<runnableProbeId,RunnableProbe>>
	private ConcurrentHashMap<String, ConcurrentHashMap<String, RunnableProbe>> userRunnableProbes; // HashMap<userId,
	// HashMap<runnableProbeId,RunnableProbe>>
	private ConcurrentHashMap<String, ConcurrentHashMap<String, RunnableProbe>> probeRunnableProbes; // HashMap<probeId,
	// HashMap<runnableProbeId,RunnableProbe>>
	private ConcurrentHashMap<String, ConcurrentHashMap<String, RunnableProbe>> templateRunnableProbes; // HashMap<templateId,
	// HashMap<runnableProbeId,RunnableProbe>>

	private ExecutorService pingerExec = Executors.newFixedThreadPool(GlobalConfig.getPingerThreadCount());
	private ExecutorService porterExec = Executors.newFixedThreadPool(GlobalConfig.getPorterThreadCount());
	private ExecutorService weberExec = Executors.newFixedThreadPool(GlobalConfig.getWeberThreadCount());
	private ExecutorService rblProbeExec = Executors.newFixedThreadPool(GlobalConfig.getRblThreadCount());
	private ExecutorService snmpBatchExec = Executors.newFixedThreadPool(GlobalConfig.getSnmpBatchThreadCount());
	private ExecutorService discoveryExec = Executors.newFixedThreadPool(GlobalConfig.getSnmpBatchThreadCount());
	private ExecutorService bandwidthProbeExec = Executors.newFixedThreadPool(GlobalConfig.getBandwidthThreadCount());
	private ExecutorService diskProbeExec = Executors.newFixedThreadPool(GlobalConfig.getDiskhreadCount());
	private ExecutorService tracerouteExec = Executors.newFixedThreadPool(GlobalConfig.getTracerouteThreadCount());

	private HashMap<String, SnmpProbesBatch> batches = new HashMap<String, SnmpProbesBatch>(); // HashMap<runnableProbeId,

	// SnmpProbesBatch>

	// private final Object lock = new Lock();

	protected RunnableProbeContainer() {
		runnableProbes = new ConcurrentHashMap<String, RunnableProbe>();
		hostRunnableProbes = new ConcurrentHashMap<String, ConcurrentHashMap<String, RunnableProbe>>();
		userRunnableProbes = new ConcurrentHashMap<String, ConcurrentHashMap<String, RunnableProbe>>();
		probeRunnableProbes = new ConcurrentHashMap<String, ConcurrentHashMap<String, RunnableProbe>>();
		templateRunnableProbes = new ConcurrentHashMap<String, ConcurrentHashMap<String, RunnableProbe>>();
	}

	public static RunnableProbeContainer getInstanece() {
		if (runnableProbeContainer == null)
			runnableProbeContainer = new RunnableProbeContainer();
		return runnableProbeContainer;
	}

	@Override
	public RunnableProbe get(String runnableProbeId) {
		return runnableProbes.get(runnableProbeId);
	}

	@Override
	public ConcurrentHashMap<String, RunnableProbe> getByUser(String userId) {
		return userRunnableProbes.get(userId);
	}

	@Override
	public ConcurrentHashMap<String, RunnableProbe> getByHost(String hostId) {
		return hostRunnableProbes.get(hostId);
	}

	@Override
	public ConcurrentHashMap<String, RunnableProbe> getByProbe(String probeId) {
		return probeRunnableProbes.get(probeId);
	}

	@Override
	public ConcurrentHashMap<String, RunnableProbe> getByTemplate(String templateId) {
		return templateRunnableProbes.get(templateId);
	}

	@Override
	public boolean add(RunnableProbe runnableProbe) {
		Boolean isStarted = startProbe(runnableProbe);
		if (!isStarted)
			return false;

		runnableProbes.put(runnableProbe.getId(), runnableProbe);

		String hostId = runnableProbe.getHost().getHostId().toString();
		addToMap(runnableProbe, hostId, hostRunnableProbes);

		String userId = runnableProbe.getProbe().getUser().getUserId().toString();
		addToMap(runnableProbe, userId, userRunnableProbes);

		String probeId = runnableProbe.getProbe().getProbe_id();
		addToMap(runnableProbe, probeId, probeRunnableProbes);

		String templateId = runnableProbe.getProbe().getTemplate_id().toString();
		addToMap(runnableProbe, templateId, templateRunnableProbes);

		return true;
	}

	private void addToMap(RunnableProbe runnableProbe, String id,
			ConcurrentHashMap<String, ConcurrentHashMap<String, RunnableProbe>> runnableProbes) {
		ConcurrentHashMap<String, RunnableProbe> newRunnableProbes = runnableProbes.get(id);
		if (newRunnableProbes == null)
			newRunnableProbes = new ConcurrentHashMap<String, RunnableProbe>();
		newRunnableProbes.put(runnableProbe.getId(), runnableProbe);
		runnableProbes.put(id, newRunnableProbes);
	}

	@Override
	public boolean remove(RunnableProbe runnableProbe) { // Stop the probe and
															// removes it from
															// the system

		runnableProbe.removeAllEvents();
		runnableProbes.get(runnableProbe.getId()).setRunning(false);
		runnableProbe.setRunning(false);
		runnableProbes.remove(runnableProbe.getId());

		UUID hostId = runnableProbe.getHost().getHostId();
		UUID userId = runnableProbe.getProbe().getUser().getUserId();
		String probeId = runnableProbe.getProbe().getProbe_id();

		removeFromMap(runnableProbe, hostId.toString(), hostRunnableProbes);
		if (!hostRunnableProbes.containsKey(hostId.toString()))
			UsersManager.removeHost(hostId, userId);

		removeFromMap(runnableProbe, userId.toString(), userRunnableProbes);
		if (!userRunnableProbes.containsKey(userId.toString()))
			UsersManager.removeUser(userId);

		removeFromMap(runnableProbe, probeId, probeRunnableProbes);

		String templateId = runnableProbe.getProbe().getTemplate_id().toString();
		removeFromMap(runnableProbe, templateId, templateRunnableProbes);

		if (runnableProbe.getProbeType() == ProbeTypes.SNMP) {
			// todo: add proper error
			boolean isSnmpStart = stopSnmpProbe(runnableProbe);
			if (!isSnmpStart)
				return false;
			return true;
		}

		return true;
	}

	private boolean removeFromMap(RunnableProbe runnableProbe, String id,
			ConcurrentHashMap<String, ConcurrentHashMap<String, RunnableProbe>> runnableProbes) {
		ConcurrentHashMap<String, RunnableProbe> runnableProbeSet = runnableProbes.get(id);
		if (runnableProbeSet == null)
			return true;
		for (String runnableProbeId : runnableProbeSet.keySet()) {
			if (runnableProbeId.equals(runnableProbe.getId())) {
				runnableProbes.remove(runnableProbeId);
				return true;
			}
		}

		if (runnableProbeSet.size() == 0)
			runnableProbes.remove(id);

		return true;
	}

	@Override
	public boolean removeByTemplateId(String teplateId) {
		ConcurrentHashMap<String, RunnableProbe> templates = getByTemplate(teplateId);
		if (templates == null)
			return true;
		for (RunnableProbe runnableProbe : templates.values())
			remove(runnableProbe);
		return true;
	}

	@Override
	public boolean removeByProbeId(String probeId) {
		ConcurrentHashMap<String, RunnableProbe> probes = getByProbe(probeId);
		if (probes == null)
			return true;
		for (RunnableProbe runnableProbe : probes.values())
			remove(runnableProbe);
		return true;
	}

	@Override
	public boolean removeByRunnableProbeId(String runnabelProbeId) {
		RunnableProbe runnableProbe = runnableProbes.get(runnabelProbeId);
		if (runnableProbe != null)
			remove(runnableProbe);
		return true;
	}

	// No more RunnableProbes in host
	public boolean isHostEmpty(String hostId) {
		ConcurrentHashMap<String, RunnableProbe> runnableProbes = hostRunnableProbes.get(hostId);
		return runnableProbes == null || runnableProbes.size() == 0;
	}

	private Boolean startProbe(RunnableProbe runnableProbe) {
		if (runnableProbe == null)
			return false;
		try {
			switch (runnableProbe.getProbeType()) {
			case SNMP:
				addSnmpRunnableProbeToBatches(runnableProbe);
				break;
			case ICMP:
				pingerExec.execute(runnableProbe);
				break;
			case PORT:
				porterExec.execute(runnableProbe);
				break;
			case HTTP:
				weberExec.execute(runnableProbe);
				break;
			case RBL:
				rblProbeExec.execute(runnableProbe);
				break;
			case DISCOVERY:
				discoveryExec.execute(runnableProbe);
				break;
			case BANDWIDTH_ELEMENT:
				bandwidthProbeExec.execute(runnableProbe);
				break;
			case DISK_ELEMENT:
				diskProbeExec.execute(runnableProbe);
				break;
			case TRACEROUTE:
				tracerouteExec.execute(runnableProbe);
				break;
			default:
				return true;
			}
			runnableProbe.setRunning(true);
			return true;
		} catch (Exception e) {
			Logit.LogWarn("Unable to start Runnable Probe Thread of: " + runnableProbe.getId() + ", check probe type!\n"
					+ e.getMessage());

			return false;
		}
	}

	@Override
	public boolean changeInterval(RunnableProbe runnableProbe, Integer interval) {
		if (runnableProbe == null)
			return false;

		runnableProbe.getProbe().setInterval(interval);
		if (runnableProbe.getProbeType() == ProbeTypes.SNMP) {
			changeSnmpProbeInterval(runnableProbe, interval);
		}

		return true;
	}

	private Boolean changeSnmpProbeInterval(RunnableProbe runnableProbe, Integer interval) {
		stopSnmpProbe(runnableProbe);
		addSnmpRunnableProbeToBatches(runnableProbe);
		return true;
	}

	private Boolean addSnmpRunnableProbeToBatches(RunnableProbe runnableProbe) {
		SnmpProbesBatch batch = null;
		try {
			for (Map.Entry<String, SnmpProbesBatch> _batch : batches.entrySet()) {
				try {
					batch = _batch.getValue();
					Host host = runnableProbe.getHost();
					BaseProbe probe = runnableProbe.getProbe();
					if (batch.getBatchId()
							.contains(host.getHostId().toString() + "@" + probe.getTemplate_id().toString() + "@"
									+ probe.getInterval())
							&& batch.getSnmpProbes().size() < Constants.getBatchesSize()) {
						batch.getSnmpProbes().put(runnableProbe.getId(), runnableProbe);
						batches.put(runnableProbe.getId(), batch);
						batch.setRunning(true);
						return true;
					}
				} catch (Exception e) {
					Logit.LogWarn("Unable to add Runnable Probe to existing batch: " + runnableProbe.getId() + " \n"
							+ e.getMessage());
					return null;
				}
			}
		} catch (Exception e) {
			Logit.LogWarn(
					"Unable to add Runnable Probe to existing batch: " + runnableProbe.getId() + "\n" + e.getMessage());
			return false;
		}

		try {
			SnmpProbesBatch newBatch = new SnmpProbesBatch(runnableProbe);
			snmpBatchExec.execute(newBatch);
			batches.put(runnableProbe.getId(), newBatch);
			newBatch.setRunning(true);
			return true;
		} catch (Exception e) {
			Logit.LogWarn(
					"Unable to add Runnable Probe to new batch: " + runnableProbe.getId() + "\n" + e.getMessage());
			return false;
		}
	}

	private boolean stopSnmpProbe(RunnableProbe runnableProbe) {
		try {
			SnmpProbesBatch batch = batches.get(runnableProbe.getId());
			if (batch.isExist(runnableProbe.getId())) {
				batch.deleteSnmpProbe(runnableProbe);
				batches.remove(runnableProbe.getId());

				if (batch.getSnmpProbes().size() == 0) {
					batch.setRunning(false);
					batch = null;
				}
				return true;
			}
			return false;
		} catch (Exception e)

		{
			Logit.LogWarn("Unable to stop running probe: " + runnableProbe.getId() + ",\n" + e.getMessage());
			return false;
		}
	}

	public ThreadsCount getThreadCount() {
		ThreadsCount threadCount = new ThreadsCount();
		for (RunnableProbe runnableProbe : runnableProbes.values()) {
			if (runnableProbe.isActive() && runnableProbe.getProbe().isActive()) {
				if (runnableProbe.getProbe() instanceof IcmpProbe)
					threadCount.ping++;
				if (runnableProbe.getProbe() instanceof PortProbe)
					threadCount.port++;
				if (runnableProbe.getProbe() instanceof HttpProbe)
					threadCount.web++;
				if (runnableProbe.getProbe() instanceof SnmpProbe)
					threadCount.snmp++;
				if (runnableProbe.getProbe() instanceof RBLProbe)
					threadCount.rbl++;
				if (runnableProbe.getProbe() instanceof DiscoveryProbe)
					threadCount.discovery++;
				if (runnableProbe.getProbe() instanceof NicProbe)
					threadCount.nic++;
				if (runnableProbe.getProbe() instanceof DiskProbe)
					threadCount.disk++;
				if (runnableProbe.getProbe() instanceof TracerouteProbe)
					threadCount.traceroute++;
			}
		}
		return threadCount;
	}

	// @Override
	// public boolean pause(String runnableProbeId, boolean isActive) {
	// if (!runnableProbeId.contains("@@")) {
	// RunnableProbe runnableProbe = runnableProbes.get(runnableProbeId);
	// if (runnableProbe == null)
	// return false;
	//
	// pause(runnableProbe, isActive);
	// } else {
	// String templateId = runnableProbeId.split("@@")[0];
	// String probeId = runnableProbeId.split("@@")[1];
	// HashMap<String, RunnableProbe> runnableProbesHash = getByProbe(probeId);
	// for (String rpId : runnableProbesHash.keySet()) {
	// if (rpId.contains(templateId))
	// {
	// RunnableProbe runnableProbe = runnableProbes.get(rpId);
	// pause(runnableProbe, isActive);
	// }
	// }
	// }
	// return true;
	// }

	// private boolean pause(RunnableProbe runnableProbe, boolean isActive) {
	// if (runnableProbe.getProbeType() == ProbeTypes.SNMP) {
	//
	// } else {
	// try {
	// if (!isActive) {
	// synchronized (lock) {
	// runnableProbe.wait();
	// }
	// } else {
	// synchronized (lock) {
	// runnableProbe.notify();
	// }
	// }
	// } catch (Exception ex) {
	// if (!isActive) {
	// Logit.LogError("RunnableProbeContainer - pause()", "The runnable probe
	// did not pause due to error");
	// } else {
	// Logit.LogError("RunnableProbeContainer - pause()",
	// "The runnable probe did not restart due to error");
	// }
	//
	// return false;
	// }
	// }
	// return true;
	// }
}
