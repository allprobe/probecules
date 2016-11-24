package lycus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
	private boolean snmpErrorSent = false;
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

	public boolean isSnmpErrorSent() {
		return snmpErrorSent;
	}

	public void setSnmpErrorSent(boolean snmpError) {
		this.snmpErrorSent = snmpError;
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

	public void run() {
		try {
			while (isRunning()) {
				try {
					String batchID = this.getBatchId();
					if (batchID.contains(
							"6975cb58-8aa4-4ecd-b9fc-47b78c0d7af8@8b0104e7-5902-4419-933f-668582fc3acd@40@6ca75402-dd7f-4aee-bcc3-1b22463d2dd8"))
						Logit.LogDebug("BREAKPOINT");

					if (this.getHost().isHostStatus() && this.getHost().isSnmpStatus()) {
						Host host = this.getHost();

						Collection<RunnableProbe> snmpProbes = this.getSnmpProbes().values();

						if (host.getSnmpTemp() == null) {
							for (RunnableProbe rp : snmpProbes) {
								SnmpResult result = new SnmpResult(rp.getId());
								result.setErrorMessage("no snmp template");
								ResultsContainer.getInstance().addResult(result);
								Logit.LogInfo(
										"Snmp Probe doesn't run: " + rp.getId() + ", no SNMP template configured!");
							}
							return;
						}

						List<SnmpProbe> _snmpProbes = new ArrayList<SnmpProbe>();
						List<RunnableProbe> _runnableProbes = new ArrayList<RunnableProbe>();

						for (RunnableProbe runnableProbe : snmpProbes) {

							String rpStr = runnableProbe.getId();
							if (rpStr.contains(
									"8b0104e7-5902-4419-933f-668582fc3acd@6b999cd6-fcbb-4ca8-9936-5529b4c66976@snmp_5d937636-eb75-4165-b339-38a729aa2b7d"))
								Logit.LogDebug("BREAKPOINT");

							_runnableProbes.add(runnableProbe);
							_snmpProbes.add((SnmpProbe) runnableProbe.getProbe());
						}

						List<SnmpResult> response = NetResults.getInstanece().getSnmpResults(this.getHost(),
								_snmpProbes);

						if (response == null) {
							for (RunnableProbe runnableProbe : _runnableProbes) {
								SnmpResult result = new SnmpResult(runnableProbe.getId());
								result.setErrorMessage("no response for snmp request");
								ResultsContainer.getInstance().addResult(result);
								Logit.LogWarn("Unable Probing Runnable Probe of: " + runnableProbe.getId());
							}
							Logit.LogError("SnmpProbesBatch - run()",
									"Failed running  snmp batch - host: " + this.getHost().getHostIp()
											+ ", snmp template:" + this.getHost().getSnmpTemp().toString());
							return;
						} else {
							long resultsTimestamp = System.currentTimeMillis();
							for (SnmpResult result : response) {

								RunnableProbe runnableProbe = RunnableProbeContainer.getInstanece()
										.get(result.getRunnableProbeId());

								if (runnableProbe.getId()
										.equals("8b0104e7-5902-4419-933f-668582fc3acd@6b999cd6-fcbb-4ca8-9936-5529b4c66976@snmp_5d937636-eb75-4165-b339-38a729aa2b7d")
										|| runnableProbe.getId().equals(
												"8b0104e7-5902-4419-933f-668582fc3acd@6975cb58-8aa4-4ecd-b9fc-47b78c0d7af8@snmp_5d937636-eb75-4165-b339-38a729aa2b7d")) {
									System.out.println(
											"Interval: " + this.getInterval() + ", Number of Probes: " + response.size()
													+ ", Number of Probes: " + response.size() + ", " + "Probe name: "
													+ runnableProbe.getProbe().getName() + ", Time: " + new Date());
								}
								SnmpStoreAs storeAs = ((SnmpProbe) runnableProbe.getProbe()).getStoreAs();
								if (storeAs == SnmpStoreAs.asIs) {
									result.setLastTimestamp(resultsTimestamp);
									ResultsContainer.getInstance().addResult(result);
									RollupsContainer.getInstance().addResult(result);
									runnableProbe.addResultToTrigger(result);

								} else if (storeAs == SnmpStoreAs.delta) {
									SnmpDeltaResult snmpDeltaResult = getSnmpDeltaResult(result, resultsTimestamp);
									ResultsContainer.getInstance().addResult(snmpDeltaResult);
									RollupsContainer.getInstance().addResult(snmpDeltaResult);
									runnableProbe.addResultToTrigger(snmpDeltaResult);
								}
								if (result.getError() == SnmpError.NO_COMUNICATION) {
									if (!this.isSnmpErrorSent()) {
										for (Trigger trigger : runnableProbe.getProbe().getTriggers().values()) {
											ResultsContainer.getInstance().resendEvents(trigger.getTriggerId(),
													GlobalConstants.Constants.snmp_connection_failed);
										}
										this.setSnmpErrorSent(true);
									}
								} else if (this.isSnmpErrorSent()) {
									for (Trigger trigger : runnableProbe.getProbe().getTriggers().values()) {
										ResultsContainer.getInstance().resendEvents(trigger.getTriggerId(),
												GlobalConstants.Constants.snmp_connection_failed_fixed);
									}
									this.setSnmpErrorSent(false);
								}
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
