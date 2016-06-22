package lycus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
	private ConcurrentHashMap<String, RunnableProbe> snmpProbes;
	private Host host;
	private long interval;
	private boolean snmpError;
	private boolean isRunning;
//	private boolean isActive;

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
		this.setSnmpProbes(new ConcurrentHashMap<String, RunnableProbe>());
		this.getSnmpProbes().put(rp.getId(), rp);
		this.batchId = this.getHost().getHostId().toString() + "@" + rp.getProbe().getTemplate_id().toString() + "@"
				+ rp.getProbe().getInterval() + "@" + UUID.randomUUID().toString();
		this.setRunning(false);
//		this.setActive(true);
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

//	public boolean isActive() {
//		return isActive;
//	}

//	public void setActive(boolean isActive) {
//		this.isActive = isActive;
//	}

	// #endregion

	public void run() {
		while (isRunning()) {
			try {
				String rpStr = this.getBatchId();
				if (rpStr.contains("8b0104e7-5902-4419-933f-668582fc3acd@6975cb58-8aa4-4ecd-b9fc-47b78c0d7af8@snmp_5d937636-eb75-4165-b339-38a729aa2b7d"))
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
					List<RunnableProbe> _runnableProbes = new ArrayList<RunnableProbe>();
					
					for (RunnableProbe runnableProbe : snmpProbes) {

						String rpStr2 = runnableProbe.getId();
						if (rpStr.contains(
								"36897eaf-db96-4533-b261-3476bb4e90a2@7352a46f-5189-428c-b4c0-fb98dedd10b1@snmp_50bdfcc0-f01b-4aad-95c1-791442744c3e"))
							Logit.LogDebug("BREAKPOINT");
						
						if (runnableProbe.isActive() && runnableProbe.getProbe().isActive()) {
							if (rpStr.contains(
									"9f2929aa-b0fe-4c85-a563-1d40178ba34f@74cda666-3d85-4e56-a804-9d53c4e16259@snmp_3d2224a8-2500-4ea5-8d37-f631204ffb18"))
								Logit.LogDebug("BREAKPOINT");
							
							_runnableProbes.add(runnableProbe);
							_snmpProbes.add((SnmpProbe) runnableProbe.getProbe());
						}
					}

					List<SnmpResult> response = NetResults.getInstanece().getSnmpResults(this.getHost(), _snmpProbes);

					if (response == null) {
						for (RunnableProbe runnableProbe : _runnableProbes) {
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
//								if (!snmpDeltaResult.isFirst()) {
								ResultsContainer.getInstance().addResult(snmpDeltaResult);
								RollupsContainer.getInstance().addResult(snmpDeltaResult);
//								}
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
//		if (snmpPreviousResults != null) {
//			// snmpPreviousData.setLastTimestamp(timeStamp);
//			snmpDeltaResult.setData(snmpPreviousData.getData(), result.getData());
//		} else
//			snmpDeltaResult.setData(null, result.getData());

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
