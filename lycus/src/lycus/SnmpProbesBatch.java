package lycus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import NetConnection.NetResults;
import lycus.GlobalConstants.LogType;
import lycus.Probes.SnmpProbe;
import lycus.Results.SnmpResult;
import lycus.Utils.Logit;

public class SnmpProbesBatch implements Runnable {
	private String batchId;// hostId@templateId@interval@batchUUID
	private SnmpManager snmpManager;
	private Map<String, RunnableProbe> snmpProbes;
	private Host host;
	private long interval;
	private boolean snmpError;
	private boolean isRunning;

	// check vars
	private TransportMapping transport;
	private Snmp snmp;

	public SnmpProbesBatch(SnmpManager SM, RunnableProbe rp) {
		this.setHost(rp.getHost());
		this.setInterval(rp.getProbe().getInterval());
		this.setSnmpProbes(new HashMap<String, RunnableProbe>());
		this.getSnmpProbes().put(rp.getId(), rp);
		this.batchId = this.getHost().getHostId().toString() + "@" + rp.getProbe().getTemplate_id().toString() + "@"
				+ rp.getProbe().getInterval() + "@" + UUID.randomUUID().toString();
		this.setRunning(false);

		setTransport(null);
		setSnmp(null);
		// this.startSnmpListener();

	}

	private void startSnmpListener() throws Throwable {
		try {
			this.setTransport(new DefaultUdpTransportMapping());
			this.getTransport().listen();
		} catch (IOException e) {
			Logit.LogError("SnmpProbesBatch - startSnmpListener()", "Socket binding for failed for Snmp Batch:" + this.getBatchId() + "\n" + e.getMessage());
		}
		this.setSnmp(new Snmp(this.getTransport()));
	}

	private void stopSnmpListener() throws Throwable {
		try {
			if (this.getSnmp() != null) {
				this.getSnmp().close();
			}
			if (this.getTransport() != null) {
				this.getTransport().close();
			}
		} catch (Throwable t) {
			Logit.LogError("SnmpProbesBatch - startSnmpListener()", "Memory leak, unable to close network connection!");
			throw t;
		} finally {
			super.finalize();
		}

	}

	// #region Getters/Setters

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

	public SnmpManager getSnmpManager() {
		return snmpManager;
	}

	public void setSnmpManager(SnmpManager snmpManager) {
		this.snmpManager = snmpManager;
	}

	public TransportMapping getTransport() {
		return transport;
	}

	public void setTransport(TransportMapping transport) {
		this.transport = transport;
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

	// #endregion

	public void run() {

		try {

			if (this.getHost().isHostStatus() && this.getHost().isSnmpStatus()) {
				Host host = this.getHost();

				List<RunnableProbe> snmpProbes = new ArrayList<RunnableProbe>(this.getSnmpProbes().values());

				if (host.getSnmpTemp() == null) {
					for (RunnableProbe rp : snmpProbes) {
						Logit.LogInfo("Snmp Probe doesn't run: " + rp.getId() + ", no SNMP template configured!");
					}
					return;
				}

				List<SnmpProbe> _snmpProbes = new ArrayList<SnmpProbe>();

				for (RunnableProbe rp : snmpProbes) {
					_snmpProbes.add((SnmpProbe) rp.getProbe());
				}

				List<SnmpResult> response = NetResults.getInstanece().getSnmpResults(this.getHost(), _snmpProbes);

				if (response == null) {
					for (RunnableProbe runnableProbe : snmpProbes) {
						Logit.LogWarn("Unable Probing Runnable Probe of: " + runnableProbe.getId());
					}
					Logit.LogInfo("Failed running  snmp batch - host: " + this.getHost().getHostIp()
							+ ", snmp template:" + this.getHost().getSnmpTemp().toString());
					return;
					// switch (Net.checkHostSnmpActive(host)) {
					// case "host problem":
					// SysLogger.Record(new Log(
					// "Snmp Batch Failed - caused By Host: "
					// + this.getHost().toString()
					// + " didn't responsed! ", LogType.Debug));
					// break;
					// case "snmp problem":
					// SysLogger.Record(new Log(
					// "Snmp Batch Failed - caused By Snmp Template: "
					// + this.getHost().getSnmpTemp().toString(),
					// LogType.Debug));
					// this.setSnmpError(true);
					// break;
					// case "no problem":
					// SysLogger.Record(new Log(
					// "Snmp Batch Failed - caused By Unknown, SNMP Batch:"
					// + this.toString(),
					// LogType.Error));
					// break;
					// }
				} else {
					long resultsTimestamp = System.currentTimeMillis();
					// if(this.isSnmpError())
					// this.setSnmpError(false);
					for (SnmpResult result : response) {
						ResultsContainer.getInstance().addResult(result);

						// String rpStr = _rp.getId();
						// if (rpStr.contains(
						// "788b1b9e-d753-4dfa-ac46-61c4374eeb84@inner_d5be36d2-87ff-414a-88ba-be2da43adabf"))
						// System.out.println("BREAKPOINT -
						// RunnableSnmpProbeResults");
						// if (_rp.isActive()) {

						// SnmpProbe snmpProbe = (SnmpProbe) _rp.getProbe();
						// SnmpResults snmpResult =
						// response.get(snmpProbe.getOid().toString());
						//
						// if (snmpResult != null) {
						// ResultsContainer.getInstance().addResult(snmpResult);
						// RollupsContainer.getInstance().addResult(snmpResult);
						// Logit.LogDebug("Running Probe: " + _rp.getId() + " at
						// Host: "
						// + this.getHost().getHostIp() + "(" +
						// this.getHost().getName() + ")"
						// + ", Results: " + snmpResult + " ...");
						// } else {
						// Logit.LogError("SnmpProbesBatch - run()","Unable to
						// get results for SNMP Probe: " + _rp.getId()
						// + " oid issue (" + snmpProbe.getOid() + ")");
						// }
					}
				}
			}
		} catch (Throwable th) {
			Logit.LogError("SnmpProbesBatch - run()", "Error running snmp probes batch:" + this.getBatchId());
		}
	}

	public void deleteSnmpProbe(RunnableProbe rp) {
		this.getSnmpProbes().remove(rp.getId());
	}

	public void addSnmpProbe(RunnableProbe rp) {
		this.getSnmpProbes().put(rp.getId(), rp);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("Snmp Probes Batch: " + this.getBatchId() + ":\n");
		for (RunnableProbe p : this.getSnmpProbes().values()) {
			s.append(p.toString()).append("\n");
		}
		return s.toString();
	}

	@Override
	protected void finalize() throws Throwable {
		// stopSnmpListener();
	}
}
