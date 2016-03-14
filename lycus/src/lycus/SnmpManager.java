package lycus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import lycus.GlobalConstants.Global;
import lycus.GlobalConstants.LogType;
import lycus.Probes.BaseProbe;

public class SnmpManager {
	private User user;
	private HashMap<String, SnmpProbesBatch> batches;// batchId:hostId@templateId@interval@batchUUID
	private Set<Host> inactiveHostSnmpProblem;

	public SnmpManager(User u) {
		this.setUser(u);
		this.setBatches(new HashMap<String, SnmpProbesBatch>());
		this.setInactiveHostSnmpProblem(new HashSet<Host>());
	}

	// Getters/Setters
	public HashMap<String, SnmpProbesBatch> getBatches() {
		return batches;
	}

	public void setBatches(HashMap<String, SnmpProbesBatch> batches) {
		this.batches = batches;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Set<Host> getInactiveHostSnmpProblem() {
		return inactiveHostSnmpProblem;
	}

	public void setInactiveHostSnmpProblem(Set<Host> inactiveHostSnmpProblem) {
		this.inactiveHostSnmpProblem = inactiveHostSnmpProblem;
	}

	public HashMap<String, RunnableProbe> getAllRPS() {
		HashMap<String, RunnableProbe> rps = new HashMap<String, RunnableProbe>();
		HashMap<String, SnmpProbesBatch> batches = this.getBatches();
		for (Map.Entry<String, SnmpProbesBatch> entry : batches.entrySet()) {
			rps.putAll(entry.getValue().getSnmpProbes());
		}
		return rps;
	}

	public void runRPlist(List<RunnableProbe> rps) {
		for (RunnableProbe rp : rps) {
			boolean status = this.startProbe(rp);
			if (!status) {
				SysLogger.Record(new Log("Failed to run runnable probe: " + rp.getRPString(), LogType.Warn));
			}
		}
	}

	public void runAllBatches() {
		for (SnmpProbesBatch _batch : this.getBatches().values()) {
			RunInnerProbesChecks.RunSnmpBatchThreads(_batch);
		}
	}

	/**
	 * Return SnmpProbesBatch if new created, Return null if only added to
	 * SnmpProbesBatch
	 */
	private SnmpProbesBatch addRPtoBatches(RunnableProbe rp) {
		Map<String, SnmpProbesBatch> _batches = this.getBatches();
		for (Map.Entry<String, SnmpProbesBatch> _batch : _batches.entrySet()) {
			try {
				SnmpProbesBatch batch = _batch.getValue();
				Host host = rp.getHost();
				BaseProbe probe = rp.getProbe();
				if (batch.getBatchId().contains(host.getHostId().toString() + "@" + probe.getTemplate_id().toString()
						+ "@" + probe.getInterval()) && batch.getSnmpProbes().size() < this.getBatchesSize()) {
					batch.getSnmpProbes().put(rp.getRPString(), rp);
					return batch;
				}
			} catch (Exception e) {
				SysLogger.Record(new Log("Unable to add Runnable Probe to existing batch: " + rp.getRPString(),
						LogType.Warn, e));
				return null;
			}
		}

		try {
			SnmpProbesBatch newBatch = new SnmpProbesBatch(this, rp);
			_batches.put(newBatch.getBatchId(), newBatch);
			return newBatch;
		} catch (Exception e) {
			SysLogger
					.Record(new Log("Unable to add Runnable Probe to new batch: " + rp.getRPString(), LogType.Warn, e));
			return null;
		}
	}

	public int getBatchesSize() {
		return 1400 / Global.getMaxSnmpResponseInBytes();
	}

	// returns stopped or not (return false if no such probe found)
	public boolean stopProbe(RunnableProbe rp) {
		try {
			ConcurrentHashMap<String, ScheduledFuture<?>> snmpBatchThreads = RunInnerProbesChecks
					.getSnmpBatchFutureMap();
			for (Map.Entry<String, ScheduledFuture<?>> batchThread : snmpBatchThreads.entrySet()) {
				if (batchThread.getKey().contains(rp.getHost().getHostId().toString() + "@"
						+ rp.getProbe().getTemplate_id().toString() + "@" + rp.getProbe().getInterval())) {
					SnmpProbesBatch batch = this.getBatches().get(batchThread.getKey());
					if (batch.getSnmpProbes().get(rp.getRPString()) != null) {
						batch.deleteSnmpProbe(rp);
						if (batch.getSnmpProbes().size() == 0) {
							RunInnerProbesChecks.getSnmpBatchFutureMap().get(batch.getBatchId()).cancel(true);
							this.getBatches().remove(batch.getBatchId());
						}
						return true;
					} else
						return false;
				}
			}
			return false;
		} catch (Exception e) {
			SysLogger.Record(new Log("Unable to stop running probe: " + rp.getRPString(), LogType.Warn, e));
			return false;
		}
	}

	public boolean startProbe(RunnableProbe rp) {
		
		String rpStr = rp.getRPString();
		if (rpStr.contains(
				"ca49f95f-3676-4129-86d9-34f87433314c@7352a46f-5189-428c-b4c0-fb98dedd10b1@inner_7be55137-c5d8-438e-bca7-325f56656071"))
			System.out.println("BREAKPOINT");
		
		if (rp != null) {
			SnmpProbesBatch batch = this.addRPtoBatches(rp);
			if (batch == null)
				return false;
			if (batch.isRunning())
				return true;
			RunInnerProbesChecks.RunSnmpBatchThreads(batch);
			return true;
		}
		return false;
	}
}
