package lycus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.snmp4j.Snmp;
import GlobalConstants.Enums.SnmpStoreAs;
import NetConnection.NetResults;
import Probes.SnmpProbe;
import Results.SnmpDeltaResult;
import Results.SnmpResult;
import Rollups.RollupsContainer;
import Utils.Logit;

public class SnmpProbesBatch implements Runnable {
	private String batchId;// hostId@templateId@interval@batchUUID
	private Map<String, RunnableProbe> snmpProbes;
	private Host host;
	private long interval;
	private boolean snmpError;
	private boolean isRunning;
	private boolean isActive;

	private Map<String, SnmpResult> snmpPreviousResults; // Map<runnableProbeId,
															// SnmpResult> for
															// calculating Delta
															// result

	// check vars
	// private TransportMapping transport;
	private Snmp snmp;
	private Object lockSnmpProbe = new Object();

	public SnmpProbesBatch(RunnableProbe rp) {
		snmpPreviousResults = new HashMap<String, SnmpResult>();
		this.setHost(rp.getHost());
		this.setInterval(rp.getProbe().getInterval());
		this.setSnmpProbes(new HashMap<String, RunnableProbe>());
		this.getSnmpProbes().put(rp.getId(), rp);
		this.batchId = this.getHost().getHostId().toString() + "@" + rp.getProbe().getTemplate_id().toString() + "@"
				+ rp.getProbe().getInterval() + "@" + UUID.randomUUID().toString();
		this.setRunning(false);
		this.setActive(true);
		// setTransport(null);
		setSnmp(null);
		// this.startSnmpListener();

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

	public void setSnmpProbes(Map<String, RunnableProbe> snmpProbes) {
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

	public boolean isSnmpError() {
		return snmpError;
	}

	public void setSnmpError(boolean snmpError) {
		this.snmpError = snmpError;
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

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	// #endregion

	public void run() {
		while (isActive()) {
			try {
				String rpStr = this.getBatchId();
				if (rpStr.contains("9dc99972-e28a-4e90-aabd-7e8bad61b232@0b05919c-6cc0-42cc-a74b-de3b0dcd4a2a@60"))
					Logit.LogDebug("BREAKPOINT");

				if (this.getHost().isHostStatus() && this.getHost().isSnmpStatus()) {
					Host host = this.getHost();

					Collection<RunnableProbe> snmpProbes = this.getSnmpProbes().values();

					if (host.getHostId().toString().contains("788b1b9e-d753-4dfa-ac46-61c4374eeb84"))
						Logit.LogDebug("BREAKPOINT");

					if (host.getSnmpTemp() == null) {
						for (RunnableProbe rp : snmpProbes) {
							Logit.LogInfo("Snmp Probe doesn't run: " + rp.getId() + ", no SNMP template configured!");
						}
						continue;
					}

					List<SnmpProbe> _snmpProbes = new ArrayList<SnmpProbe>();

					for (RunnableProbe runnableProbe : snmpProbes) {

						if (runnableProbe.getProbe().isActive()) {
							if (rpStr.contains(
									"788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_036f81e0-4ec0-468a-8396-77c21dd9ae5a"))
								Logit.LogDebug("BREAKPOINT");

							_snmpProbes.add((SnmpProbe) runnableProbe.getProbe());
						}
					}

					List<SnmpResult> response = NetResults.getInstanece().getSnmpResults(this.getHost(), _snmpProbes);

					if (response == null) {
						for (RunnableProbe runnableProbe : snmpProbes) {
							Logit.LogWarn("Unable Probing Runnable Probe of: " + runnableProbe.getId());
						}
						Logit.LogInfo("Failed running  snmp batch - host: " + this.getHost().getHostIp()
								+ ", snmp template:" + this.getHost().getSnmpTemp().toString());
						continue;
					} else {
						long resultsTimestamp = System.currentTimeMillis();
						for (SnmpResult result : response) {
							SnmpStoreAs storeAs = ((SnmpProbe) RunnableProbeContainer.getInstanece()
									.get(result.getRunnableProbeId()).getProbe()).getStoreAs();
							if (storeAs == SnmpStoreAs.asIs) {
								result.setLastTimestamp(resultsTimestamp);
								ResultsContainer.getInstance().addResult(result);
								RollupsContainer.getInstance().addResult(result);
							} else if (storeAs == SnmpStoreAs.delta) {
								SnmpDeltaResult snmpDeltaResult = getSnmpDeltaResult(result, resultsTimestamp);
								if (!snmpDeltaResult.isFirst()) {
									ResultsContainer.getInstance().addResult(snmpDeltaResult);
									RollupsContainer.getInstance().addResult(snmpDeltaResult);
								}
							}

						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				Logit.LogError("SnmpProbesBatch - run()", "Error running snmp probes batch:" + this.getBatchId(), ex);
			}
			finally {
				try {
					synchronized (this) {
						wait(this.getInterval() * 1000);
					}
				} catch (InterruptedException e) {
					Logit.LogError("SnmpProbesBatch - run()", "Error waiting interval. ", e);
				}
			}
		}
	}

	public SnmpDeltaResult getSnmpDeltaResult(SnmpResult result, long timeStamp) {
		SnmpDeltaResult snmpDeltaResult = new SnmpDeltaResult(result.getRunnableProbeId());
		SnmpResult snmpPreviousData = snmpPreviousResults.get(result.getRunnableProbeId());
		if (snmpPreviousData != null) {
			// snmpPreviousData.setLastTimestamp(timeStamp);
			snmpDeltaResult.setData(snmpPreviousData.getData(), result.getData());
		} else
			snmpDeltaResult.setData(null, result.getData());

		snmpDeltaResult.setData(snmpPreviousData != null ? snmpPreviousData.getData() : null, result.getData());
		snmpDeltaResult.setLastTimestamp(timeStamp);

		snmpPreviousResults.put(result.getRunnableProbeId(), result);
		// snmpPreviousResults.remove(snmpPreviousData);
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
