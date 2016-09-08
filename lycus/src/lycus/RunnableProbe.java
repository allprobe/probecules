package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import Functions.BaseFunction;
import Functions.LastFunction;
import Functions.LastTenFunction;
import Functions.LastThreeFunction;
import GlobalConstants.Enums.ResultValueType;
import GlobalConstants.ProbeTypes;
import Probes.BaseProbe;
import Probes.DiscoveryProbe;
import Probes.HttpProbe;
import Probes.IcmpProbe;
import Probes.NicProbe;
import Probes.PortProbe;
import Probes.RBLProbe;
import Probes.SnmpProbe;
import Probes.TracerouteProbe;
import Probes.DiskProbe;
import Results.BaseResult;
import Rollups.RollupsContainer;
import SLA.SLAContainer;
import Utils.Logit;

public class RunnableProbe implements Runnable {
	private Host host;
	private BaseProbe probe;
	private boolean isActive;
	private boolean isRunning;
	private ArrayList<BaseFunction> functions;

	public RunnableProbe(Host host, BaseProbe probe) {
		this.setHost(host);
		this.setProbe(probe);
		this.setRunning(true);
		this.setActive(true);
		if (this.getProbeType() == null) {
			// Must be handled by Roi
			return;
		}

		if (probe.getProbe_id().contains("icmp_cc9a931c-6232-4b17-b2f9-be00b40ce02b"))
			Logit.LogDebug("BREAKPOINT - RunnableProbe");

		this.functions = new ArrayList<BaseFunction>();

		if (!(this.getProbe() instanceof DiscoveryProbe))
			this.setFunctions(probe.getTriggers());
	}

	// Must remove the functions that are not there.
	private void setFunctions(HashMap<String, Trigger> triggers) {
		for (Trigger trigger : triggers.values()) {
			for (TriggerCondition condition : trigger.getCondtions()) {
				BaseFunction function = getFunctionOfCondition(condition);
				if (isFunctionExists(function))
					continue;
				this.functions.add(function);
			}
		}
	}

	private boolean isFunctionExists(BaseFunction function) {
		for (BaseFunction existing : this.functions) {
			if (existing.isEqual(function))
				return true;
		}
		return false;
	}

	private BaseFunction getFunctionOfCondition(TriggerCondition condition) {
		switch (condition.getFunction()) {
		case 1:
			return new LastFunction(condition.getElementType());
		case 2:
			return new LastThreeFunction(condition.getElementType());
		case 3:
			return new LastTenFunction(condition.getElementType());
		}
		return null;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public BaseProbe getProbe() {
		return probe;
	}

	public void setProbe(BaseProbe probe) {
		this.probe = probe;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	private boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		String rpStr3 = this.getId();
		if (rpStr3.contains(
				"8b0104e7-5902-4419-933f-668582fc3acd@6975cb58-8aa4-4ecd-b9fc-47b78c0d7af8@snmp_5d937636-eb75-4165-b339-38a729aa2b7d"))
			Logit.LogDebug("BREAKPOINT - RunnableProbe");

		this.isRunning = isRunning;
	}

	public BaseFunction getConditionFunction(Trigger trigger, TriggerCondition condition) {
		for (int i = 0; i < this.functions.size(); i++) {
			if (this.functions.get(i).getValueType().equals(condition.getElementType()))
				if (this.functions.get(i).getFunctionId() == condition.getFunction())
					return this.functions.get(i);
		}
		return null;
	}

	public String getId() {
		return this.getProbe().getTemplate_id().toString() + "@" + this.getHost().getHostId().toString() + "@"
				+ this.getProbe().getProbe_id();
	}

	public ProbeTypes getProbeType() {
		if (getProbe() instanceof IcmpProbe)
			return ProbeTypes.ICMP;
		if (getProbe() instanceof PortProbe)
			return ProbeTypes.PORT;
		if (getProbe() instanceof HttpProbe)
			return ProbeTypes.HTTP;
		if ((getProbe() instanceof SnmpProbe))
			return ProbeTypes.SNMP;
		if (getProbe() instanceof RBLProbe)
			return ProbeTypes.RBL;
		if (getProbe() instanceof DiscoveryProbe)
			return ProbeTypes.DISCOVERY;
		if (getProbe() instanceof NicProbe)
			return ProbeTypes.BANDWIDTH_ELEMENT;
		if (getProbe() instanceof DiskProbe)
			return ProbeTypes.DISK_ELEMENT;
		if (getProbe() instanceof TracerouteProbe)
			return ProbeTypes.TRACEROUTE;
		return null;
	}

	public void run() {
		// isRunning = false will stop the thread
		while (isRunning()) {
			BaseResult result = null;
			try {
				String rpStr = this.getId();
				if (rpStr.contains("01179751-b842-4dbb-a72e-30082c677249@snmp_43c19a22-2569-4aa2-aba4-bc0aca2e38cd"))
					Logit.LogDebug("BREAKPOINT - RunnableProbe");

				// isActive = false will pause the thread
				if (!isActive() || !getProbe().isActive())
					continue;

				long timeStamp = System.currentTimeMillis();

				try {
					result = getProbe().getResult(this.getHost());
				} catch (Exception e) {
					result = new BaseResult(this.getId());
					result.setErrorMessage("RESULT_EXCEPTION");
					Logit.LogError("RunnableProbe - run()",
							"Error, getting runnable probe results from probe! getResult() throws exception "
									+ this.getProbeType() + " " + this.getProbe().getName() + ", \nRunnabelProbeId: "
									+ this.getId(),
							e);
				}

				if (result == null) {
					result = new BaseResult(this.getId());
					result.setErrorMessage("RESULT_OBJECT_NULL");
					Logit.LogError("RunnableProbe - run()",
							"Error, getting runnable probe results from probe! Returned object is null "
									+ this.getProbeType() + " " + this.getProbe().getName() + ", \nRunnabelProbeId: "
									+ this.getId());
				}

				try {
					// if (result.getLastTimestamp() == null) {
					// Logit.LogError("RunnableProbe - run()",
					// "Error getting runnable probe results! last timestamp is
					// null! "
					// + this.getProbe().getName() + ", \nRunnabelProbeId: " +
					// this.getId());
					// continue;
					// }

					// Set the timeStamp to the value before the probe.
					result.setLastTimestamp(timeStamp);
					this.addToAllFunctions(result);
					ResultsContainer.getInstance().addResult(result);
				} catch (Exception e) {
					Logit.LogError("RunnableProbe - run()",
							"Error processing runnable probe results to results container! " + this.getId());
				}

				try {
					if (!(this.getProbe() instanceof DiscoveryProbe))
						result.checkIfTriggerd(getProbe().getTriggers());
				} catch (Exception e) {
					Logit.LogError("RunnableProbe - run()",
							"Error triggering runnable probe results!  " + this.getProbeType() + " "
									+ this.getProbe().getName() + ", \nRunnabelProbeId: " + this.getId());
					// continue;
				}

				try {
					RollupsContainer.getInstance().addResult(result);
				} catch (Exception e) {
					Logit.LogError("RunnableProbe - run()",
							"Error processing runnable probe results to rollups container!" + this.getId(), e);
				}

				try {
					SLAContainer.getInstance().addToSLA(result);
				} catch (Exception e) {
					Logit.LogError("RunnableProbe - run()",
							"Error processing runnable probe results to SLA container!" + this.getId(), e);
				}

			} finally {
				try {
					Logit.LogInfo("Running Probe: " + this.getId() + " at Host: " + this.getHost().getHostIp() + "("
							+ this.getHost().getName() + ")" + ", Results: " + result + " ...");

					synchronized (this) {
						wait(this.getProbe().getInterval() * 1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}

	private void addToAllFunctions(BaseResult result) {
		for (BaseFunction function : this.functions) {
			function.add(result);
		}
	}
}
