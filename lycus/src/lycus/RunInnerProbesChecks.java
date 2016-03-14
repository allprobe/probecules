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

import lycus.GlobalConstants.Global;
import lycus.GlobalConstants.LogType;
import lycus.GlobalConstants.ProbeTypes;
import lycus.Probes.PingerProbe;
import lycus.Probes.PorterProbe;
import lycus.Probes.RBLProbe;
import lycus.Probes.SnmpProbe;
import lycus.Probes.WeberProbe;

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
			.newScheduledThreadPool(Global.getPingerThreadCount());
	private static ConcurrentHashMap<String, ScheduledFuture<?>> PorterFutureMap = new ConcurrentHashMap<>();
	private static Integer PorterFutureCounter = 0;
	private static ScheduledExecutorService PorterExec = Executors
			.newScheduledThreadPool(Global.getPorterThreadCount());
	private static ConcurrentHashMap<String, ScheduledFuture<?>> WeberFutureMap = new ConcurrentHashMap<>();
	private static Integer WeberFutureCounter = 0;
	private static ScheduledExecutorService WeberExec = Executors
			.newScheduledThreadPool(Global.getWeberThreadCount());
	private static ConcurrentHashMap<String, ScheduledFuture<?>> SnmpProbeFutureMap = new ConcurrentHashMap<>();
	private static Integer SnmpProbeFutureCounter = 0;
	private static ScheduledExecutorService SnmpProbeExec = Executors
			.newScheduledThreadPool(Global.getSnmpThreadCount());
	private static ConcurrentHashMap<String, ScheduledFuture<?>> RblProbeFutureMap = new ConcurrentHashMap<>();
	private static Integer RblProbeFutureCounter = 0;
	private static ScheduledExecutorService RblProbeExec = Executors
			.newScheduledThreadPool(Global.getRblThreadCount());

	private static ConcurrentHashMap<String, ScheduledFuture<?>> SnmpBatchFutureMap = new ConcurrentHashMap<>();
	private static Integer SnmpBatchFutureCounter = 0;
	private static Integer SnmpProbeBatchFutureCounter = 0;
	private static ScheduledExecutorService SnmpBatchExec = Executors
			.newScheduledThreadPool(Global.getSnmpBatchThreadCount());
	
	private static ConcurrentHashMap<String, ScheduledFuture<?>> DiscoveryFutureMap = new ConcurrentHashMap<>();
	private static Integer DiscoveryFutureCounter = 0;
	private static ScheduledExecutorService DiscoveryExec = Executors
			.newScheduledThreadPool(Global.getSnmpBatchThreadCount());

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
		
		getPingerFutureMap().put(probe.getRPString(), future);
		PingerFutureCounter++;
	}	

	public static void RunPorterThreads(final RunnableProbe probe) {
		ScheduledFuture<?> future;
		future = PorterExec.scheduleAtFixedRate(probe, 0, probe.getProbe()
				.getInterval(), TimeUnit.SECONDS);
		getPorterFutureMap().put(probe.getRPString(), future);
		PorterFutureCounter++;
		
	}

	public static void RunWeberThreads(final RunnableProbe probe) {
		ScheduledFuture<?> future;
		future = WeberExec.scheduleAtFixedRate(probe, 0, probe.getProbe()
				.getInterval(), TimeUnit.SECONDS);
		getWeberFutureMap().put(probe.getRPString(), future);
		WeberFutureCounter++;
	}

	public static void RunSnmpProbeThreads(final RunnableProbe probe) {
		ScheduledFuture<?> future;
		future = SnmpProbeExec.scheduleAtFixedRate(probe, 0, probe.getProbe()
				.getInterval(), TimeUnit.SECONDS);
		getSnmpProbeFutureMap().put(probe.getRPString(), future);
		SnmpProbeFutureCounter++;

	}

	public static void RunRblProbeThreads(RunnableProbe probe) {
		ScheduledFuture<?> future;
		future = RblProbeExec.scheduleAtFixedRate(probe, 0, probe.getProbe()
				.getInterval(), TimeUnit.SECONDS);
		getRblProbeFutureMap().put(probe.getRPString(), future);

		RblProbeFutureCounter++;
	}

	public static void RunDiscoveryProbeThreads(RunnableProbe probe) {
		ScheduledFuture<?> future;
		future = DiscoveryExec.scheduleAtFixedRate(probe, 0, probe.getProbe()
				.getInterval(), TimeUnit.SECONDS);
		getDiscoveryFutureMap().put(probe.getRPString(), future);

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
			case PING:
				RunInnerProbesChecks.RunPingerThreads(rp);
				return true;
			case PORT:
				RunInnerProbesChecks.RunPorterThreads(rp);
				return true;
			case WEB:
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
			default:
				return false;
			}
		} catch (Exception e) {
			SysLogger.Record(new Log("Unable to start Runnable Probe Thread of: "+rp.getRPString()+", check probe type!", LogType.Warn, e));
		}
		return false;
	}
	public static boolean deleteRegularRP(RunnableProbe rp) {
		if(rp==null)
			return false;
		if (rp.getProbe() instanceof PingerProbe) {
			RunInnerProbesChecks.getPingerFutureMap().get(rp.getRPString())
					.cancel(false);
			PingerFutureCounter--;
			return true;
		} else if (rp.getProbe() instanceof PorterProbe) {
			RunInnerProbesChecks.getPorterFutureMap().get(rp.getRPString())
					.cancel(false);
			PorterFutureCounter--;
			return true;
		} else if (rp.getProbe() instanceof WeberProbe) {
			RunInnerProbesChecks.getWeberFutureMap().get(rp.getRPString())
					.cancel(false);
			WeberFutureCounter--;
			return true;
		} else if (rp.getProbe() instanceof RBLProbe) {
			RunInnerProbesChecks.getRblProbeFutureMap().get(rp.getRPString())
					.cancel(false);
			RblProbeFutureCounter--;
			return true;
		} else if (rp.getProbe() instanceof SnmpProbe) {
			RunInnerProbesChecks.getSnmpProbeFutureMap().get(rp.getRPString())
					.cancel(false);
			SnmpProbeFutureCounter--;
			return true;
		}
		return false;
	}

	public static ScheduledFuture<?> getRunnableProbeThread(RunnableProbe rp)
	{
		ProbeTypes probeType;
//		try {
			if ( rp.getProbeType() != null)
				probeType = rp.getProbeType();
			else
				return null;
//		} catch (Exception e) {
//			SysLogger.Record(new Log("Unable to get Runnable Probe Thread of: "+rp.getRPString()+", check probe type!",LogType.Warn ));
//			return null;
//		}
		switch(probeType)
		{
		case PING:return getPingerFutureMap().get(rp.getRPString());
		case PORT:return getPorterFutureMap().get(rp.getRPString());
		case WEB:return getWeberFutureMap().get(rp.getRPString());
		case SNMP:return getPingerFutureMap().get(rp.getRPString());
		case RBL:return getPingerFutureMap().get(rp.getRPString());
		}
		return null;
	}


	public static void decreaseRpInBatches() {
		SnmpProbeBatchFutureCounter--;
		}
	
	
}