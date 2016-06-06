package Interfaces;

import java.util.concurrent.ConcurrentHashMap;

import lycus.RunnableProbe;

public interface IRunnableProbeContainer {
	RunnableProbe get(String runnableProbeId);
//	HashMap<String,RunnableProbe> get();
	ConcurrentHashMap<String,RunnableProbe> getByUser(String userId);
	ConcurrentHashMap<String,RunnableProbe> getByHost(String hostId);
	ConcurrentHashMap<String,RunnableProbe> getByProbe(String probeId);
	ConcurrentHashMap<String,RunnableProbe> getByHostTemplate(String templateId, String hostId);
	boolean add(RunnableProbe runnableProbe);
	boolean remove(RunnableProbe runnableProbe);
//	boolean pause(String runnableProbeId, boolean isActive);
	boolean removeByTemplateId(String teplateId);
	boolean removeByProbeId(String probeId);
	boolean removeByRunnableProbeId(String probeId);
	boolean changeInterval(RunnableProbe runnableProbe, Long interval);
}
