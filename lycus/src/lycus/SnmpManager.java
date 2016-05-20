package lycus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import GlobalConstants.Constants;
import GlobalConstants.GlobalConfig;
import GlobalConstants.LogType;
import Probes.BaseProbe;
import Utils.Logit;

public class SnmpManager {
//	private static ScheduledExecutorService SnmpBatchExec = Executors.newScheduledThreadPool(GlobalConfig.getSnmpBatchThreadCount());
//	
//	private User user;
//	private HashMap<String, SnmpProbesBatch> batches;// batchId:hostId@templateId@interval@batchUUID
//	private Set<Host> inactiveHostSnmpProblem;
//
//	public SnmpManager(User u) {
//		this.setUser(u);
//		this.setBatches(new HashMap<String, SnmpProbesBatch>());
//		this.setInactiveHostSnmpProblem(new HashSet<Host>());
//	}

	// Getters/Setters
//	public HashMap<String, SnmpProbesBatch> getBatches() {
//		return batches;
//	}
//
//	public void setBatches(HashMap<String, SnmpProbesBatch> batches) {
//		this.batches = batches;
//	}

//	public User getUser() {
//		return user;
//	}
//
//	public void setUser(User user) {
//		this.user = user;
//	}

//	public Set<Host> getInactiveHostSnmpProblem() {
//		return inactiveHostSnmpProblem;
//	}
//
//	public void setInactiveHostSnmpProblem(Set<Host> inactiveHostSnmpProblem) {
//		this.inactiveHostSnmpProblem = inactiveHostSnmpProblem;
//	}

//	public HashMap<String, RunnableProbe> getAllRunnableProbes() {
//		HashMap<String, RunnableProbe> rps = new HashMap<String, RunnableProbe>();
//		HashMap<String, SnmpProbesBatch> batches = this.getBatches();
//		for (Map.Entry<String, SnmpProbesBatch> entry : batches.entrySet()) {
//			rps.putAll(entry.getValue().getSnmpProbes());
//		}
//		return rps;
//	}

//	public void runRunnsbleProbes(List<RunnableProbe> runnableProbes) {
//		for (RunnableProbe rp : runnableProbes) {
//			boolean status = this.startProbe(rp);
//			if (!status) {
//				Logit.LogWarn("Failed to run runnable probe: " + rp.getId());
//			}
//		}
//	}

//	public void runAllBatches() {
//		for (SnmpProbesBatch _batch : this.getBatches().values()) {
//			RunInnerProbesChecks.RunSnmpBatchThreads(_batch);
//		}
//	}

	/**
	 * Return SnmpProbesBatch if new created, Return null if only added to
	 * SnmpProbesBatch
	 */
//	private SnmpProbesBatch addRunnableProbeToBatches(RunnableProbe runnableProbe) {
//		Map<String, SnmpProbesBatch> _batches = this.getBatches();
//		for (Map.Entry<String, SnmpProbesBatch> _batch : _batches.entrySet()) {
//			try {
//				SnmpProbesBatch batch = _batch.getValue();
//				Host host = runnableProbe.getHost();
//				BaseProbe probe = runnableProbe.getProbe();
//				if (batch.getBatchId().contains(host.getHostId().toString() + "@" + probe.getTemplate_id().toString()
//						+ "@" + probe.getInterval()) && batch.getSnmpProbes().size() < Constants.getBatchesSize()) {
//					batch.getSnmpProbes().put(runnableProbe.getId(), runnableProbe);
//					return batch;
//				}
//			} catch (Exception e) {
//				Logit.LogWarn("Unable to add Runnable Probe to existing batch: " + runnableProbe.getId() + " \n" + e.getMessage());
//				return null;
//			}
//		}
//
//		try {
//			SnmpProbesBatch newBatch = new SnmpProbesBatch(runnableProbe);
//			_batches.put(newBatch.getBatchId(), newBatch);
//			return newBatch;
//		} catch (Exception e) {
//			Logit.LogWarn("Unable to add Runnable Probe to new batch: " + runnableProbe.getId() + "\n" + e.getMessage());
//			return null;
//		}
//	}

	

	// returns stopped or not (return false if no such probe found)
//	public boolean stopProbe(RunnableProbe runnableProbe) {
//		try {
//			ConcurrentHashMap<String, ScheduledFuture<?>> snmpBatchThreads = RunInnerProbesChecks
//					.getSnmpBatchFutureMap();
//			for (Map.Entry<String, ScheduledFuture<?>> batchThread : snmpBatchThreads.entrySet()) {
//				if (batchThread.getKey().contains(runnableProbe.getHost().getHostId().toString() + "@"
//						+ runnableProbe.getProbe().getTemplate_id().toString() + "@" + runnableProbe.getProbe().getInterval())) {
//					SnmpProbesBatch batch = this.getBatches().get(batchThread.getKey());
//					if (batch.getSnmpProbes().get(runnableProbe.getId()) != null) {
//						batch.deleteSnmpProbe(runnableProbe);
//						if (batch.getSnmpProbes().size() == 0) {
//							RunInnerProbesChecks.getSnmpBatchFutureMap().get(batch.getBatchId()).cancel(true);
//							this.getBatches().remove(batch.getBatchId());
//						}
//						return true;
//					} else
//						return false;
//				}
//			}
//			return false;
//		} catch (Exception e) {
//			Logit.LogWarn("Unable to stop running probe: " + runnableProbe.getId()+ ",\n" + e.getMessage());
//			return false;
//		}
//	}

//	public boolean startProbe(RunnableProbe runnableProbe) {
//		
//		String rpStr = runnableProbe.getId();
//		if (rpStr.contains(
//				"ca49f95f-3676-4129-86d9-34f87433314c@7352a46f-5189-428c-b4c0-fb98dedd10b1@inner_7be55137-c5d8-438e-bca7-325f56656071"))
//				Logit.LogDebug("BREAKPOINT");
//		
//		if (runnableProbe != null) {
//			SnmpProbesBatch batch = this.addRunnableProbeToBatches(runnableProbe);
//			if (batch == null)
//				return false;
//			if (batch.isRunning())
//				return true;
//			
//			ScheduledFuture<?> future = SnmpBatchExec.scheduleAtFixedRate(batch, 0, batch.getInterval(), TimeUnit.SECONDS);
////			RunInnerProbesChecks.RunSnmpBatchThreads(batch);
//			return true;
//		}
//		return false;
//	}
}
