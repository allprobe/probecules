package Interfaces;

import java.util.HashMap;
import java.util.List;

import lycus.RunnableProbe;

public interface IRunnableProbeContainer {
	RunnableProbe get(String runnableProbeId);
//	HashMap<String,RunnableProbe> get();
	HashMap<String,RunnableProbe> getByUser(String userId);
	HashMap<String,RunnableProbe> getByHost(String hostId);
	HashMap<String,RunnableProbe> getByProbe(String probeId);
	HashMap<String,RunnableProbe> getByHostTemplate(String templateId, String hostId);
	boolean add(RunnableProbe runnableProbe);
	boolean remove(RunnableProbe runnableProbe);
	boolean pause(String runnableProbeId, boolean isActive);
	boolean removeByTemplateId(String teplateId);
	boolean removeByProbeId(String probeId);
	boolean removeByRunnableProbeId(String probeId);
	boolean changeInterval(String runnableProbeId, Long interval);
}
