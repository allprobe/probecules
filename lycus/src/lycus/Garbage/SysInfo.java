package lycus.Garbage;

import java.io.File;
import java.text.NumberFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lycus.Utils.GeneralFunctions;
import lycus.Log;
import lycus.RunInnerProbesChecks;
import lycus.ResultsContainer;
import lycus.SysLogger;
import lycus.GlobalConstants.LogType;

public class SysInfo implements Runnable {
	private Runtime runtime;
	ScheduledExecutorService scheduledExecutorService;
	private ResultsContainer history;
	public SysInfo(ResultsContainer history) {
		this.runtime = Runtime.getRuntime();
		this.scheduledExecutorService =
		        Executors.newSingleThreadScheduledExecutor();
	this.setHistory(history);
	}
	
    public String Info() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.OsInfo());	
        sb.append(this.MemInfo());
        sb.append(this.DiskInfo());
        return sb.toString();
    }

    public String OSname() {
        return System.getProperty("os.name");
    }

    public String OSversion() {
        return System.getProperty("os.version");
    }

    public String OsArch() {
        return System.getProperty("os.arch");
    }

    public long totalMem() {
        return Runtime.getRuntime().totalMemory();
    }

    public long usedMem() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public String MemInfo() {
        NumberFormat format = NumberFormat.getInstance();
        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        sb.append("Free memory: ");
        sb.append(format.format(freeMemory / 1024));
        sb.append(" KB\n");
        sb.append("Allocated memory: ");
        sb.append(format.format(allocatedMemory / 1024));
        sb.append(" KB\n");
        sb.append("Max memory: ");
        sb.append(format.format(maxMemory / 1024));
        sb.append(" KB\n");
        sb.append("Total free memory: ");
        sb.append(format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
        sb.append(" KB\n");
        return sb.toString();

    }

    public String OsInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("OS: ");
        sb.append(this.OSname());
        sb.append("\n");
        sb.append("Version: ");
        sb.append(this.OSversion());
        sb.append("\n");
        sb.append(": ");
        sb.append(this.OsArch());
        sb.append("\n");
        sb.append("Available processors (cores): ");
        sb.append(runtime.availableProcessors());
        sb.append("\n");
        return sb.toString();
    }

    public String DiskInfo() {
        /* Get a list of all filesystem roots on this system */
        File[] roots = File.listRoots();
        StringBuilder sb = new StringBuilder();

        /* For each filesystem root, print some info */
        for (File root : roots) {
            sb.append("File system root: ");
            sb.append(root.getAbsolutePath());
            sb.append("\n");
            sb.append("Total space: ");
            sb.append(GeneralFunctions.humanReadableByteCount(root.getTotalSpace()));
            sb.append("\n");
            sb.append("Free space: ");
            sb.append(GeneralFunctions.humanReadableByteCount(root.getFreeSpace()));
            sb.append("\n");
            sb.append("Usable space: ");
            sb.append(GeneralFunctions.humanReadableByteCount(root.getUsableSpace()));
            sb.append("\n");
        }
        return sb.toString();
    }
    public ResultsContainer getHistory() {
		return history;
	}

	public void setHistory(ResultsContainer history) {
		this.history = history;
	}

	public void run()
    {
		try{
    	StringBuilder info=new StringBuilder();
    	info.append("---------------------Server Info---------------------");
    	info.append("\n");
    	info.append(this.MemInfo());
    	info.append("\n");
    	info.append(this.DiskInfo());
    	info.append("\n");
    	info.append("--------------------------------------------------");
    	info.append("\n");
    	
    	int pingers = RunInnerProbesChecks.getPingerFutureMap().size();
		int porters = RunInnerProbesChecks.getPorterFutureMap().size();
		int webers = RunInnerProbesChecks.getWeberFutureMap().size();
		int rbls = RunInnerProbesChecks.getRblProbeFutureMap().size();
		int bsnmps = RunInnerProbesChecks.getSnmpBatchFutureMap().size();
		int snmpbs = RunInnerProbesChecks.getSnmpBatchFutureCounter();
		
		
		info.append("---------------------System Info---------------------");
    	info.append("\n");
    	info.append("pingers:" + pingers);
    	info.append("\n");
    	info.append("porters:" + porters);
    	info.append("\n");
    	info.append("webers:" + webers);
    	info.append("\n");
    	info.append("rbls:" + rbls);
    	info.append("\n");	
    	info.append("bsnmp:" + bsnmps);
    	info.append("\n");
    	info.append("batchsnmp:" + snmpbs);
    	info.append("\n");
    	info.append("Total RPS Threads:"+(pingers + porters + webers + rbls 
				+ bsnmps));
    	info.append("\n");
    	info.append("--------------------------------------------------");
    	info.append("\n");	
    	
    	SysLogger.Record(new Log("!Probecules Information!\n"+info,LogType.Info));
		}
		catch(Exception e)
		{
			SysLogger.Record(new Log("Error with sys info thread!",LogType.Error));
		}
    }
    public void start()
    {
    	this.scheduledExecutorService.scheduleWithFixedDelay(this, 0,10 , TimeUnit.SECONDS);
    }
}
