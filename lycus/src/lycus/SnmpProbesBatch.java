package lycus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import Collectors.CollectorIssuesContainer;
import GlobalConstants.Enums;
import org.snmp4j.Snmp;

import GlobalConstants.Enums.SnmpError;
import GlobalConstants.Enums.SnmpStoreAs;
import NetConnection.NetResults;
import Probes.SnmpProbe;
import Results.SnmpDeltaResult;
import Results.SnmpResult;
import Rollups.RollupsContainer;
import Triggers.Trigger;
import Utils.Logit;

public class SnmpProbesBatch implements Runnable {
	private String batchId; // hostId@templateId@interval@batchUUID
	private ConcurrentHashMap<String, RunnableProbe> snmpProbes;
	private Host host;
	private long interval;
	private boolean isRunning;
	private Map<String, SnmpResult> snmpPreviousResults; // Map<runnableProbeId,
	private Snmp snmp;
	private Object lockSnmpProbe = new Object();

	public SnmpProbesBatch(RunnableProbe rp) {
		snmpPreviousResults = new HashMap<String, SnmpResult>();
		this.setHost(rp.getHost());
		this.setInterval(rp.getProbe().getInterval());
		this.setSnmpProbes(new ConcurrentHashMap<String, RunnableProbe>());
		this.getSnmpProbes().put(rp.getId(), rp);
		this.batchId = this.getHost().getHostId().toString() + "@" + rp.getProbe().getTemplate_id().toString() + "@"
				+ rp.getProbe().getInterval() + "@" + UUID.randomUUID().toString();
		this.setRunning(false);
		setSnmp(null);
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public Map<String, RunnableProbe> getSnmpProbes() {
		return snmpProbes;
	}

	public void setSnmpProbes(ConcurrentHashMap<String, RunnableProbe> snmpProbes) {
		this.snmpProbes = snmpProbes;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public Snmp getSnmp() {
		return snmp;
	}

	public void setSnmp(Snmp snmp) {
		this.snmp = snmp;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public String getBatchId() {
		return batchId;
	}

	public int size() {
		return snmpProbes.size();
	}

	public void run() {
		try {
			while (isRunning()) {
				try {
					String batchID = this.getBatchId();
					if (batchID.contains(
							"6975cb58-8aa4-4ecd-b9fc-47b78c0d7af8@8b0104e7-5902-4419-933f-668582fc3acd@40@6ca75402-dd7f-4aee-bcc3-1b22463d2dd8"))
						Logit.LogDebug("BREAKPOINT");

					if (this.getHost().isHostStatus()) {
						Host host = this.getHost();

						if (this.getHost().getHostIp().contains("62.90.132.124"))
							Logit.LogDebug("BP");

						Collection<RunnableProbe> snmpProbes = this.getSnmpProbes().values();

						if (host.getSnmpCollector() == null) {
							noSmpmCollectorForHost(host, snmpProbes);
						} else {
							List<SnmpProbe> _snmpProbes = new ArrayList<SnmpProbe>();
							List<RunnableProbe> _runnableProbes = new ArrayList<RunnableProbe>();

							for (RunnableProbe runnableProbe : snmpProbes) {

								String rpStr = runnableProbe.getId();
								if (rpStr.contains(
										"6a10a32d-0d33-415b-a1f6-e9aeb2826d03@98437013-a93f-4b27-9963-a4800860b90f@snmp_1e189e8e-ec48-40bf-baba-88b61b18978a"))
									Logit.LogDebug("BREAKPOINT");

								_runnableProbes.add(runnableProbe);
								Logit.LogInfo(host.getName() + ": " + runnableProbe.getProbe().getInterval()
										+ ", Probe name: " + runnableProbe.getProbe().getName() + ", RunnableProbeId: "
										+ runnableProbe.getId());
								_snmpProbes.add((SnmpProbe) runnableProbe.getProbe());
							}

							List<SnmpResult> response = NetResults.getInstanece().getSnmpResults(this.getHost(),
									_snmpProbes);

							if (response == null) {
								isNoResponse(_runnableProbes);
							} else {
								createResponse(response);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					Logit.LogError("SnmpProbesBatch - run()",
							"Error running snmp probes batch: " + this.getBatchId() + " at Host: "
									+ this.getHost().getHostIp() + "(" + this.getHost().getName()
									+ "was thrown an exception: " + e.getMessage());

					for (RunnableProbe runnableProbe : this.getSnmpProbes().values()) {
						SnmpResult result = new SnmpResult(runnableProbe.getId());
						result.setErrorMessage("exception for snmp probe!");
						ResultsContainer.getInstance().addResult(result);
						Logit.LogWarn("Unable Probing Runnable Probe of: " + runnableProbe.getId());
					}

				} finally {
					try {
						synchronized (this) {
							wait(this.getInterval() * 1000);
						}
					} catch (InterruptedException e) {
						Logit.LogError("SnmpProbesBatch - run()", "Error waiting interval. ", e);
					}
				}
			}
		} catch (Exception e) {
			Logit.LogError("RunnableProbe - run()",
					"Error, The Batch Thread was Interrupted, Probe type: SNMP Batch of Interval: " + this.getInterval()
							+ ", BatchId: " + this.getBatchId() + "\nException: " + e.getMessage());
		}

		Logit.LogInfo("Snmp Batch probe was terminated, Host: " + this.getHost() + " Interval: " + this.getInterval());
	}

	private void noSmpmCollectorForHost(Host host, Collection<RunnableProbe> snmpProbes) {
		Logit.LogError("SnmpProbesBatch - run()", "No Snmp collector for host: " + host.getName());

		for (RunnableProbe rp : snmpProbes) {
			SnmpResult result = new SnmpResult(rp.getId());
			result.setErrorMessage("NO_SNMP_TEMPLATE");
			ResultsContainer.getInstance().addResult(result);
			Logit.LogInfo("Snmp Probe doesn't run: " + rp.getId() + ", no SNMP template configured!");
		}

		CollectorIssuesContainer.getInstance().addIssue(this.getHost(), Enums.CollectorType.Snmp,
				GlobalConstants.Constants.no_snmp_template, 1);
	}

	private void createResponse(List<SnmpResult> response) {
		long resultsTimestamp = System.currentTimeMillis();
		for (SnmpResult result : response) {

			RunnableProbe runnableProbe = createSnmpErrorResult(result);
			addSnmpResult(resultsTimestamp, result, runnableProbe);
		}
	}

	private void addSnmpResult(long resultsTimestamp, SnmpResult result, RunnableProbe runnableProbe) {
		try {
			SnmpStoreAs storeAs = ((SnmpProbe) runnableProbe.getProbe()).getStoreAs();

			if (storeAs == SnmpStoreAs.asIs) {
				result.setLastTimestamp(resultsTimestamp);
				ResultsContainer.getInstance().addResult(result);
				RollupsContainer.getInstance().addResult(result);
				if ((result.getErrorMessage() == "" || result.getErrorMessage() == null)
						&& (result.getData() != null || result.getNumData() != null))
					runnableProbe.addResultToTrigger(result);

			} else if (storeAs == SnmpStoreAs.delta) {
				SnmpDeltaResult snmpDeltaResult = getSnmpDeltaResult(result, resultsTimestamp);
				ResultsContainer.getInstance().addResult(snmpDeltaResult);
				RollupsContainer.getInstance().addResult(snmpDeltaResult);
				if (result.getData() != null || result.getNumData() != null)
					runnableProbe.addResultToTrigger(snmpDeltaResult);
			}
		} catch (Exception e) {
			Logit.LogError("SnmpProbesBatch - addSnmpResult()",
					"Error adding snmp error result. runnableProbe: " + runnableProbe == null ? null
							: runnableProbe.getId(),
					e);
			e.printStackTrace();
		}
	}

	private RunnableProbe createSnmpErrorResult(SnmpResult result) {
		RunnableProbe runnableProbe = null;
		try {
			runnableProbe = RunnableProbeContainer.getInstanece().get(result.getRunnableProbeId());

			if (runnableProbe.getId().contains(
					"6a10a32d-0d33-415b-a1f6-e9aeb2826d03@7352a46f-5189-428c-b4c0-fb98dedd10b1@snmp_1e189e8e-ec48-40bf-baba-88b61b18978a"))
				Logit.LogDebug("BP");

			if (result.getError() == SnmpError.NO_COMUNICATION) {
				if (this.getHost().getHostIp().contains("62.90.132.124"))
					Logit.LogDebug("BP");
				CollectorIssuesContainer.getInstance().addIssue(this.getHost(), Enums.CollectorType.Snmp,
						GlobalConstants.Constants.snmp_connection_failed, 1);
			} else {
				CollectorIssuesContainer.getInstance().addIssue(this.getHost(), Enums.CollectorType.Snmp,
						GlobalConstants.Constants.snmp_fixed, 0);
			}
			return runnableProbe;
		} catch (Exception e) {
			Logit.LogError("SnmpProbesBatch - createSnmpErrorResult()",
					"Error creating snmp error result. runnableProbe: " + runnableProbe == null ? null
							: runnableProbe.getId(),
					e);
			e.printStackTrace();
			return runnableProbe;
		}
	}

	private void isNoResponse(List<RunnableProbe> _runnableProbes) {
		for (RunnableProbe runnableProbe : _runnableProbes) {
			SnmpResult result = new SnmpResult(runnableProbe.getId());
			result.setErrorMessage("no response for snmp request");
			ResultsContainer.getInstance().addResult(result);
			Logit.LogWarn("Unable Probing Runnable Probe of: " + runnableProbe.getId());
		}
		Logit.LogError("SnmpProbesBatch - run()", "Failed running  snmp batch - host: " + this.getHost().getHostIp()
				+ ", snmp template:" + this.getHost().getSnmpCollector().toString());
		return;
	}

	public SnmpDeltaResult getSnmpDeltaResult(SnmpResult result, long timeStamp) {
		SnmpDeltaResult snmpDeltaResult = new SnmpDeltaResult(result.getRunnableProbeId());
		SnmpResult snmpPreviousData = snmpPreviousResults.get(result.getRunnableProbeId());

		snmpDeltaResult.setData(snmpPreviousData != null ? snmpPreviousData.getData() : null, result.getData());
		snmpDeltaResult.setLastTimestamp(timeStamp);

		snmpPreviousResults.put(result.getRunnableProbeId(), result);
		return snmpDeltaResult;
	}

	public void deleteSnmpProbe(RunnableProbe rp) {
		synchronized (lockSnmpProbe) {
			this.getSnmpProbes().remove(rp.getId());
		}
	}

	public void addSnmpProbe(RunnableProbe rp) {
		synchronized (lockSnmpProbe) {
			this.getSnmpProbes().put(rp.getId(), rp);
		}
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("Snmp Probes Batch: " + this.getBatchId() + ":\n");
		for (RunnableProbe p : this.getSnmpProbes().values()) {
			s.append(p.toString()).append("\n");
		}
		return s.toString();
	}

	public boolean isExist(String runnableProbeId) // hostId@templateId@interval
	{
		return snmpProbes.containsKey(runnableProbeId);
	}

	@Override
	protected void finalize() throws Throwable {
		// stopSnmpListener();
	}
}
