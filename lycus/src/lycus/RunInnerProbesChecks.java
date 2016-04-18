/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import GlobalConstants.GlobalConfig;
import GlobalConstants.LogType;
import GlobalConstants.ProbeTypes;
import Probes.HttpProbe;
import Probes.IcmpProbe;
import Probes.PortProbe;
import Probes.RBLProbe;
import Probes.SnmpProbe;
import Utils.Logit;

/**
 * 
 * @author Roi
 */
public class RunInnerProbesChecks extends Thread {

	/**
	 * @param args
	 *            the command line arguments
	 */
	private static ConcurrentHashMap<String, ScheduledFuture<?>> PingerFutureMap = new ConcurrentHashMap<>();
	private static Integer PingerFutureCounter = 0;
	private static ScheduledExecutorService PingerExec = Executors
			.newScheduledThreadPool(GlobalConfig.getPingerThreadCount());
	private static ConcurrentHashMap<String, ScheduledFuture<?>> PorterFutureMap = new ConcurrentHashMap<>();
	private static Integer PorterFutureCounter = 0;
	private static ScheduledExecutorService PorterExec = Executors
			.newScheduledThreadPool(GlobalConfig.getPorterThreadCount());
	private static ConcurrentHashMap<String, ScheduledFuture<?>> WeberFutureMap = new ConcurrentHashMap<>();
	private static Integer WeberFutureCounter = 0;
	private static ScheduledExecutorService WeberExec = Executors
			.newScheduledThreadPool(GlobalConfig.getWeberThreadCount());
	private static ConcurrentHashMap<String, ScheduledFuture<?>> SnmpProbeFutureMap = new ConcurrentHashMap<>();
	private static Integer SnmpProbeFutureCounter = 0;
	private static ScheduledExecutorService SnmpProbeExec = Executors
			.newScheduledThreadPool(GlobalConfig.getSnmpThreadCount());
	private static ConcurrentHashMap<String, ScheduledFuture<?>> RblProbeFutureMap = new ConcurrentHashMap<>();
	private static Integer RblProbeFutureCounter = 0;
	private static ScheduledExecutorService RblProbeExec = Executors
			.newScheduledThreadPool(GlobalConfig.getRblThreadCount());

	private static ConcurrentHashMap<String, ScheduledFuture<?>> SnmpBatchFutureMap = new ConcurrentHashMap<>();
	private static Integer SnmpBatchFutureCounter = 0;
	private static Integer SnmpProbeBatchFutureCounter = 0;
	private static ScheduledExecutorService SnmpBatchExec = Executors
			.newScheduledThreadPool(GlobalConfig.getSnmpBatchThreadCount());
	
	private static ConcurrentHashMap<String, ScheduledFuture<?>> DiscoveryFutureMap = new ConcurrentHashMap<>();
	private static Integer DiscoveryFutureCounter = 0;
	private static ScheduledExecutorService DiscoveryExec = Executors
			.newScheduledThreadPool(GlobalConfig.getSnmpBatchThreadCount());
	
	private static ConcurrentHashMap<String, ScheduledFuture<?>> BandwidthFutureMap = new ConcurrentHashMap<>();
	private static Integer BandwidthFutureCounter = 0;
	private static ScheduledExecutorService BandwidthProbeExec = Executors
			.newScheduledThreadPool(10);
	
	private static ConcurrentHashMap<String, ScheduledFuture<?>> DiskFutureMap = new ConcurrentHashMap<>();
	private static Integer DiskFutureCounter = 0;
	private static ScheduledExecutorService DiskProbeExec = Executors
			.newScheduledThreadPool(10);

	// Getters/Setters
	public static ConcurrentHashMap<String, ScheduledFuture<?>> getPingerFutureMap() {
		return PingerFutureMap;
	}

	public static ConcurrentHashMap<String, ScheduledFuture<?>> getPorterFutureMap() {
		return PorterFutureMap;
	}

	public static ConcurrentHashMap<String, ScheduledFuture<?>> getRblProbeFutureMap() {
		return RblProbeFutureMap;
	}

	public static ConcurrentHashMap<String, ScheduledFuture<?>> getSnmpBatchFutureMap() {
		return SnmpBatchFutureMap;
	}

	public static ConcurrentHashMap<String, ScheduledFuture<?>> getSnmpProbeFutureMap() {
		return SnmpProbeFutureMap;
	}

	public static ConcurrentHashMap<String, ScheduledFuture<?>> getDiscoveryFutureMap() {
		return DiscoveryFutureMap;
	}

	public static void setDiscoveryFutureMap(ConcurrentHashMap<String, ScheduledFuture<?>> discoveryFutureMap) {
		DiscoveryFutureMap = discoveryFutureMap;
	}

	public static ConcurrentHashMap<String, ScheduledFuture<?>> getWeberFutureMap() {
		return WeberFutureMap;
	}

	public static Integer getPingerFutureCounter() {
		return PingerFutureCounter;
	}

	public static Integer getPorterFutureCounter() {
		return PorterFutureCounter;
	}

	public static Integer getWeberFutureCounter() {
		return WeberFutureCounter;
	}

	public static ConcurrentHashMap<String, ScheduledFuture<?>> getBandwidthFutureMap() {
		return BandwidthFutureMap;
	}


	public static ConcurrentHashMap<String, ScheduledFuture<?>> getDiskFutureMap() {
		return DiskFutureMap;
	}


	public static Integer getSnmpProbeFutureCounter() {
		return SnmpProbeFutureCounter;
	}

	public static Integer getSnmpProbeBatchFutureCounter() {
		return SnmpProbeBatchFutureCounter;
	}

	public static Integer getRblProbeFutureCounter() {
		return RblProbeFutureCounter;
	}

	public static Integer getSnmpBatchFutureCounter() {
		return SnmpBatchFutureCounter;
	}

	

	public static Integer getDiscoveryFutureCounter() {
		return DiscoveryFutureCounter;
	}

	public static void setDiscoveryFutureCounter(Integer discoveryFutureCounter) {
		DiscoveryFutureCounter = discoveryFutureCounter;
	}

	// Run Threads
	public static void RunPingerThreads(final RunnableProbe probe) {
		
		
		
		ScheduledFuture<?> future;
		future = PingerExec.scheduleAtFixedRate(probe, 0, probe.getProbe()
				.getInterval(), TimeUnit.SECONDS);
		
		getPingerFutureMap().put(probe.getId(), future);
		PingerFutureCounter++;
	}	

	public static void RunPorterThreads(final RunnableProbe probe) {
		ScheduledFuture<?> future;
		future = PorterExec.scheduleAtFixedRate(probe, 0, probe.getProbe()
				.getInterval(), TimeUnit.SECONDS);
		getPorterFutureMap().put(probe.getId(), future);
		PorterFutureCounter++;
		
	}

	public static void RunWeberThreads(final RunnableProbe probe) {
		ScheduledFuture<?> future;
		future = WeberExec.scheduleAtFixedRate(probe, 0, probe.getProbe()
				.getInterval(), TimeUnit.SECONDS);
		getWeberFutureMap().put(probe.getId(), future);
		WeberFutureCounter++;
	}

	public static void RunSnmpProbeThreads(final RunnableProbe probe) {
		ScheduledFuture<?> future;
		future = SnmpProbeExec.scheduleAtFixedRate(probe, 0, probe.getProbe()
				.getInterval(), TimeUnit.SECONDS);
		getSnmpProbeFutureMap().put(probe.getId(), future);
		SnmpProbeFutureCounter++;

	}

	public static void RunRblProbeThreads(RunnableProbe probe) {
		ScheduledFuture<?> future;
		future = RblProbeExec.scheduleAtFixedRate(probe, 0, probe.getProbe()
				.getInterval(), TimeUnit.SECONDS);
		getRblProbeFutureMap().put(probe.getId(), future);

		RblProbeFutureCounter++;
	}

	public static void RunDiscoveryProbeThreads(RunnableProbe probe) {
		ScheduledFuture<?> future;
		future = DiscoveryExec.scheduleAtFixedRate(probe, 0, probe.getProbe()
				.getInterval(), TimeUnit.SECONDS);
		getDiscoveryFutureMap().put(probe.getId(), future);

		DiscoveryFutureCounter++;
	}
	
	public static void RunSnmpBatchThreads(final SnmpProbesBatch batch) {
		ScheduledFuture<?> future;
		future = SnmpBatchExec.scheduleAtFixedRate(batch, 0,
				batch.getInterval(), TimeUnit.SECONDS);
		getSnmpBatchFutureMap().put(batch.getBatchId(), future);
		batch.setRunning(true);
		SnmpProbeBatchFutureCounter+=batch.getSnmpProbes().size();
		SnmpBatchFutureCounter++;
	}
	
	

	public static boolean addRegularRP(RunnableProbe rp) {
		if(rp==null)
			return false;
		try {
			switch(rp.getProbeType())
			{
			case ICMP:
				RunInnerProbesChecks.RunPingerThreads(rp);
				return true;
			case PORT:
				RunInnerProbesChecks.RunPorterThreads(rp);
				return true;
			case HTTP:
				RunInnerProbesChecks.RunWeberThreads(rp);
				return true;
			case TRACEROUTE:
				break;
			case RBL:
				RunInnerProbesChecks.RunRblProbeThreads(rp);
				return true;
			case DISCOVERY:
				RunInnerProbesChecks.RunDiscoveryProbeThreads(rp);
				return true;
			case BANDWIDTH:
				RunInnerProbesChecks.RunBandwidthProbeThreads(rp);
				return true;
			case DISK:
				RunInnerProbesChecks.RunDiskProbeThreads(rp);
				return true;
			default:
				return false;
			}
		} catch (Exception e) {
			Logit.LogWarn("Unable to start Runnable Probe Thread of: "+rp.getId()+", check probe type!\n" + e.getMessage());
		}
		return false;
	}
	private static void RunDiskProbeThreads(RunnableProbe probe) {
		ScheduledFuture<?> future;
		future = DiskProbeExec.scheduleAtFixedRate(probe, 0, probe.getProbe()
				.getInterval(), TimeUnit.SECONDS);
		getDiskFutureMap().put(probe.getId(), future);
		DiskFutureCounter++;
	}

	private static void RunBandwidthProbeThreads(RunnableProbe probe) {
		ScheduledFuture<?> future;
		future = BandwidthProbeExec.scheduleAtFixedRate(probe, 0, probe.getProbe()
				.getInterval(), TimeUnit.SECONDS);
		getBandwidthFutureMap().put(probe.getId(), future);
		BandwidthFutureCounter++;
	}

	public static boolean deleteRegularRP(RunnableProbe rp) {
		if(rp==null)
			return false;
		if (rp.getProbe() instanceof IcmpProbe) {
			RunInnerProbesChecks.getPingerFutureMap().get(rp.getId())
					.cancel(false);
			PingerFutureCounter--;
			return true;
		} else if (rp.getProbe() instanceof PortProbe) {
			RunInnerProbesChecks.getPorterFutureMap().get(rp.getId())
					.cancel(false);
			PorterFutureCounter--;
			return true;
		} else if (rp.getProbe() instanceof HttpProbe) {
			RunInnerProbesChecks.getWeberFutureMap().get(rp.getId())
					.cancel(false);
			WeberFutureCounter--;
			return true;
		} else if (rp.getProbe() instanceof RBLProbe) {
			RunInnerProbesChecks.getRblProbeFutureMap().get(rp.getId())
					.cancel(false);
			RblProbeFutureCounter--;
			return true;
		} else if (rp.getProbe() instanceof SnmpProbe) {
			RunInnerProbesChecks.getSnmpProbeFutureMap().get(rp.getId())
					.cancel(false);
			SnmpProbeFutureCounter--;
			return true;
		}
		return false;
	}

	public static ScheduledFuture<?> getRunnableProbeThread(RunnableProbe rp)
	{
		ProbeTypes probeType;
			if ( rp.getProbeType() != null)
				probeType = rp.getProbeType();
			else
				return null;
		switch(probeType)
		{
		case ICMP:return getPingerFutureMap().get(rp.getId());
		case PORT:return getPorterFutureMap().get(rp.getId());
		case HTTP:return getWeberFutureMap().get(rp.getId());
		case SNMP:return getPingerFutureMap().get(rp.getId());
		case RBL:return getPingerFutureMap().get(rp.getId());
		}
		return null;
	}


	public static void decreaseRpInBatches() {
		SnmpProbeBatchFutureCounter--;
		}
	
	
}