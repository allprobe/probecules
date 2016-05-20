package lycus;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import GlobalConstants.Constants;
import GlobalConstants.GlobalConfig;
import GlobalConstants.ProbeTypes;
import Interfaces.IRunnableProbeContainer;
import Model.RunnableFuture;
import Probes.BaseProbe;
import Utils.Logit;

public class RunnableProbeContainer implements IRunnableProbeContainer {

	private static RunnableProbeContainer runnableProbeContainer = null;

	private HashMap<String, RunnableFuture> runnableProbes; // HashMap<runnableProbeId,RunnableProbe>
	private HashMap<String, HashMap<String, RunnableProbe>> hostRunnableProbes; // HashMap<hostId,
																				// HashMap<runnableProbeId,RunnableProbe>>
	private HashMap<String, HashMap<String, RunnableProbe>> userRunnableProbes; // HashMap<userId,
																				// HashMap<runnableProbeId,RunnableProbe>>
	private HashMap<String, HashMap<String, RunnableProbe>> probeRunnableProbes; // HashMap<probeId,
																					// HashMap<runnableProbeId,RunnableProbe>>

	private ScheduledExecutorService pingerExec = Executors.newScheduledThreadPool(GlobalConfig.getPingerThreadCount());
	private ScheduledExecutorService porterExec = Executors.newScheduledThreadPool(GlobalConfig.getPorterThreadCount());
	private ScheduledExecutorService weberExec = Executors.newScheduledThreadPool(GlobalConfig.getWeberThreadCount());
	private ScheduledExecutorService rblProbeExec = Executors.newScheduledThreadPool(GlobalConfig.getRblThreadCount());
	private ScheduledExecutorService snmpBatchExec = Executors
			.newScheduledThreadPool(GlobalConfig.getSnmpBatchThreadCount());
	private ScheduledExecutorService discoveryExec = Executors
			.newScheduledThreadPool(GlobalConfig.getSnmpBatchThreadCount());
	private ScheduledExecutorService bandwidthProbeExec = Executors.newScheduledThreadPool(10);
	private ScheduledExecutorService diskProbeExec = Executors.newScheduledThreadPool(10);
	private ScheduledExecutorService snmpProbeExec = Executors
			.newScheduledThreadPool(GlobalConfig.getSnmpThreadCount());

	private HashMap<String, SnmpProbesBatch> batches = new HashMap<String, SnmpProbesBatch>(); // batchId:hostId@templateId@interval@batchUUID

	protected RunnableProbeContainer() {
		runnableProbes = new HashMap<String, RunnableFuture>();
		hostRunnableProbes = new HashMap<String, HashMap<String, RunnableProbe>>();
		userRunnableProbes = new HashMap<String, HashMap<String, RunnableProbe>>();
		probeRunnableProbes = new HashMap<String, HashMap<String, RunnableProbe>>();
	}

	public static RunnableProbeContainer getInstanece() {
		if (runnableProbeContainer == null)
			runnableProbeContainer = new RunnableProbeContainer();
		return runnableProbeContainer;
	}

	@Override
	public RunnableProbe get(String runnableProbeId) {
		return runnableProbes.get(runnableProbeId).getRunnableProbe();
	}

	// @Override
	// public HashMap<String, RunnableProbe> get() {
	// return runnableProbes;
	// }

	@Override
	public HashMap<String, RunnableProbe> getByUser(String userId) {
		return userRunnableProbes.get(userId);
	}

	@Override
	public HashMap<String, RunnableProbe> getByHost(String hostId) {
		return hostRunnableProbes.get(hostId);
	}

	@Override
	public HashMap<String, RunnableProbe> getByProbe(String probeId) {
		return probeRunnableProbes.get(probeId);
	}

	@Override
	public HashMap<String, RunnableProbe> getByHostTemplate(String templateId, String hostId) {
		HashMap<String, RunnableProbe> runnableProbes = getByHost(hostId);
		HashMap<String, RunnableProbe> runnableProbesByTemplate = null;
		for (RunnableProbe runnableProbe : runnableProbes.values()) {
			if (runnableProbe.getProbe().getTemplate_id().equals(templateId)) {
				if (runnableProbesByTemplate == null)
					runnableProbesByTemplate = new HashMap<String, RunnableProbe>();
				runnableProbesByTemplate.put(runnableProbe.getId(), runnableProbe);
			}
		}

		return runnableProbesByTemplate;
	}

	@Override
	public boolean add(RunnableProbe runnableProbe) {
		ScheduledFuture<?> future = startProbe(runnableProbe);
		if (future == null)
			return false;

		RunnableFuture runnableFuture = new RunnableFuture(future, runnableProbe);
		runnableProbes.put(runnableProbe.getId(), runnableFuture);

		String hostId = runnableProbe.getHost().getHostId().toString();
		addToMap(runnableProbe, hostId, hostRunnableProbes);

		String userId = runnableProbe.getProbe().getUser().getUserId().toString();
		addToMap(runnableProbe, userId, userRunnableProbes);

		String probeId = runnableProbe.getProbe().getProbe_id();
		addToMap(runnableProbe, probeId, probeRunnableProbes);

		return true;
	}

	private void addToMap(RunnableProbe runnableProbe, String id,
			HashMap<String, HashMap<String, RunnableProbe>> runnableProbes) {
		HashMap<String, RunnableProbe> newRunnableProbes = runnableProbes.get(id);
		if (newRunnableProbes == null)
			newRunnableProbes = new HashMap<String, RunnableProbe>();
		newRunnableProbes.put(runnableProbe.getId(), runnableProbe);
		runnableProbes.put(id, newRunnableProbes);
	}

	@Override
	public boolean remove(RunnableProbe runnableProbe) { // Stop the probe and
															// removes it from
															// the system
		runnableProbes.get(runnableProbe.getId()).getFuture().cancel(false);
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
			HashMap<String, HashMap<String, RunnableProbe>> runnableProbes) {
		HashMap<String, RunnableProbe> runnableProbeSet = runnableProbes.get(id);
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
		for (RunnableFuture runnableFuture : runnableProbes.values()) {
			if (runnableFuture.getRunnableProbe().getProbe().getTemplate_id().toString().equals(teplateId)) {
				remove(runnableFuture.getRunnableProbe());
			}
		}
		return true;
	}

	@Override
	public boolean removeByProbeId(String probeId) {
		for (RunnableProbe runnableProbe : getByProbe(probeId).values())
			remove(runnableProbe);
		return true;
	}

	@Override
	public boolean removeByRunnableProbeId(String runnabelProbeId) {
		RunnableProbe runnableProbe = runnableProbes.get(runnabelProbeId).getRunnableProbe();
		if (runnableProbe != null)
			remove(runnableProbe);
		return true;
	}

	// No more RunnableProbes in host
	public boolean isHostEmpty(String hostId) {
		HashMap<String, RunnableProbe> runnableProbes = hostRunnableProbes.get(hostId);
		return runnableProbes == null || runnableProbes.size() == 0;
	}

	private ScheduledFuture<?> startProbe(RunnableProbe runnableProbe) {
		if (runnableProbe == null)
			return null;
		try {
			ScheduledFuture<?> future;
			switch (runnableProbe.getProbeType()) {
			case SNMP:
				String rpStr = runnableProbe.getId();
				if (runnableProbe == null)
					return null;

				SnmpProbesBatch batch = addSnmpRunnableProbeToBatches(runnableProbe);
				if (batch == null)
					return null;
				if (batch.isRunning()) {
					RunnableFuture runnableFuture = runnableProbes.get(runnableProbe.getId());
					if (runnableFuture != null) {
						runnableProbe.setRunning(true);
						return runnableFuture.getFuture();
					} else
						return null;
				}

				future = snmpBatchExec.scheduleAtFixedRate(batch, 0, batch.getInterval(), TimeUnit.SECONDS);
				break;
			case ICMP:
				future = pingerExec.scheduleAtFixedRate(runnableProbe, 0, runnableProbe.getProbe().getInterval(),
						TimeUnit.SECONDS);
				break;
			case PORT:
				future = porterExec.scheduleAtFixedRate(runnableProbe, 0, runnableProbe.getProbe().getInterval(),
						TimeUnit.SECONDS);
				break;
			case HTTP:
				future = weberExec.scheduleAtFixedRate(runnableProbe, 0, runnableProbe.getProbe().getInterval(),
						TimeUnit.SECONDS);
				break;
			case RBL:
				future = rblProbeExec.scheduleAtFixedRate(runnableProbe, 0, runnableProbe.getProbe().getInterval(),
						TimeUnit.SECONDS);
				break;
			case DISCOVERY:
				future = discoveryExec.scheduleAtFixedRate(runnableProbe, 0, runnableProbe.getProbe().getInterval(),
						TimeUnit.SECONDS);
				break;
			case DISCBANDWIDTH:
				future = bandwidthProbeExec.scheduleAtFixedRate(runnableProbe, 0,
						runnableProbe.getProbe().getInterval(), TimeUnit.SECONDS);
				break;
			case DISCDISK:
				future = diskProbeExec.scheduleAtFixedRate(runnableProbe, 0, runnableProbe.getProbe().getInterval(),
						TimeUnit.SECONDS);
				break;
			case TRACEROUTE:
				return null;
			default:
				return null;
			}
			if (future != null) {
				runnableProbe.setRunning(true);
				return future;
			}
		} catch (Exception e) {
			Logit.LogWarn("Unable to start Runnable Probe Thread of: " + runnableProbe.getId() + ", check probe type!\n"
					+ e.getMessage());

			return null;
		}

		return null;
	}

	@Override
	public boolean changeInterval(String runnableProbeId, Long interval) {
		RunnableFuture runnableFuture = runnableProbes.get(runnableProbeId);
		if (runnableFuture == null)
			return false;

		RunnableProbe runnableProbe = runnableFuture.getRunnableProbe();
		if (runnableProbe == null)
			return false;
		
		remove(runnableProbe);
		runnableProbe.getProbe().setInterval(interval);
		add(runnableProbe);
		
//		if (runnableProbe.getProbeType() == ProbeTypes.SNMP) {
//			remove(runnableProbe);
//			runnableProbe.getProbe().setInterval(interval);
//			add(runnableProbe);
//		}
//		else
//		{
//			runnableFuture.getRunnableProbe().getProbe().setInterval(interval);
//			runnableFuture.getFuture().cancel(false);
//			// remove(handle.getRunnableProbe());
//			startProbe(runnableFuture.getRunnableProbe());
//		}

		return true;
	}

	private SnmpProbesBatch addSnmpRunnableProbeToBatches(RunnableProbe runnableProbe) {
		for (Map.Entry<String, SnmpProbesBatch> _batch : batches.entrySet()) {
			try {
				SnmpProbesBatch batch = _batch.getValue();
				Host host = runnableProbe.getHost();
				BaseProbe probe = runnableProbe.getProbe();
				if (batch
						.getBatchId().contains(host.getHostId().toString() + "@" + probe.getTemplate_id().toString()
								+ "@" + probe.getInterval())
						&& batch.getSnmpProbes().size() < Constants.getBatchesSize()) {
					batch.getSnmpProbes().put(runnableProbe.getId(), runnableProbe);
					return batch;
				}
			} catch (Exception e) {
				Logit.LogWarn("Unable to add Runnable Probe to existing batch: " + runnableProbe.getId() + " \n"
						+ e.getMessage());
				return null;
			}
		}

		try {
			SnmpProbesBatch newBatch = new SnmpProbesBatch(runnableProbe);
			batches.put(newBatch.getBatchId(), newBatch);
			return newBatch;
		} catch (Exception e) {
			Logit.LogWarn(
					"Unable to add Runnable Probe to new batch: " + runnableProbe.getId() + "\n" + e.getMessage());
			return null;
		}
	}

	private boolean stopSnmpProbe(RunnableProbe runnableProbe) {
		try {
			ConcurrentHashMap<String, ScheduledFuture<?>> snmpBatchThreads = RunInnerProbesChecks
					.getSnmpBatchFutureMap();
			for (Map.Entry<String, ScheduledFuture<?>> batchThread : snmpBatchThreads.entrySet()) {
				if (batchThread.getKey()
						.contains(runnableProbe.getHost().getHostId().toString() + "@"
								+ runnableProbe.getProbe().getTemplate_id().toString() + "@"
								+ runnableProbe.getProbe().getInterval())) {
					SnmpProbesBatch batch = batches.get(batchThread.getKey());
					if (batch.getSnmpProbes().get(runnableProbe.getId()) != null) {
						batch.deleteSnmpProbe(runnableProbe);
						if (batch.getSnmpProbes().size() == 0) {
							RunInnerProbesChecks.getSnmpBatchFutureMap().get(batch.getBatchId()).cancel(true);
							batches.remove(batch.getBatchId());
						}
						return true;
					} else
						return false;
				}
			}
			return false;
		} catch (Exception e) {
			Logit.LogWarn("Unable to stop running probe: " + runnableProbe.getId() + ",\n" + e.getMessage());
			return false;
		}
	}

}
