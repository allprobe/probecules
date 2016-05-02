package lycus;

import java.util.HashMap;
import java.util.UUID;

import Interfaces.IRunnableProbeContainer;

public class RunnableProbeContainer implements IRunnableProbeContainer {

	private static RunnableProbeContainer runnableProbeContainer = null;

	private HashMap<String, RunnableProbe> runnableProbes;                       // HashMap<runnableProbeId,RunnableProbe>
	private HashMap<String, HashMap<String, RunnableProbe>> hostRunnableProbes;  // HashMap<hostId, HashMap<runnableProbeId,RunnableProbe>>
	private HashMap<String, HashMap<String, RunnableProbe>> userRunnableProbes;  // HashMap<userId, HashMap<runnableProbeId,RunnableProbe>>
	private HashMap<String, HashMap<String, RunnableProbe>> probeRunnableProbes; // HashMap<probeId, HashMap<runnableProbeId,RunnableProbe>>

	protected RunnableProbeContainer() {
		runnableProbes = new HashMap<String, RunnableProbe>();
		hostRunnableProbes = new HashMap<String, HashMap<String, RunnableProbe>>();
		userRunnableProbes = new HashMap<String, HashMap<String, RunnableProbe>>();
		probeRunnableProbes = new HashMap<String, HashMap<String, RunnableProbe>>();
	}

	public static RunnableProbeContainer getInstanece() {
		if (runnableProbeContainer == null)
			runnableProbeContainer = new RunnableProbeContainer();
		return runnableProbeContainer;
	}

	@Override
	public RunnableProbe get(String runnableProbeId) {
		return runnableProbes.get(runnableProbeId);
	}

	@Override
	public HashMap<String, RunnableProbe> get() {
		return runnableProbes;
	}

	@Override
	public HashMap<String, RunnableProbe> getByUser(String userId) {
		return userRunnableProbes.get(userId);
	}

	@Override
	public HashMap<String, RunnableProbe> getByHost(String hostId) {
		return hostRunnableProbes.get(hostId);
	}

	@Override
	public HashMap<String, RunnableProbe> getByProbe(String probeId) {
		return probeRunnableProbes.get(probeId);
	}

	@Override
	public HashMap<String,RunnableProbe> getByHostTemplate(String templateId, String hostId) {
		HashMap<String, RunnableProbe>  runnableProbes = getByHost(hostId);
		HashMap<String, RunnableProbe> runnableProbesByTemplate = null;
		for (RunnableProbe runnableProbe : runnableProbes.values())
		{
			if (runnableProbe.getProbe().getTemplate_id().equals(templateId))
			{
				if (runnableProbesByTemplate == null)
					runnableProbesByTemplate = new HashMap<String, RunnableProbe>();
				runnableProbesByTemplate.put(runnableProbe.getId(), runnableProbe);
			}
		}
		
		return runnableProbesByTemplate;
	}
	
	@Override
	public boolean add(RunnableProbe runnableProbe) {
		runnableProbes.put(runnableProbe.getId(), runnableProbe);

		String hostId = runnableProbe.getHost().getHostId().toString();
		addToMap(runnableProbe, hostId, hostRunnableProbes);

		String userId = runnableProbe.getProbe().getUser().getUserId().toString();
		addToMap(runnableProbe, userId, userRunnableProbes);

		String probeId = runnableProbe.getProbe().getProbe_id();
		addToMap(runnableProbe, probeId, probeRunnableProbes);

		return true;
	}

	private void addToMap(RunnableProbe runnableProbe, String id,
			HashMap<String, HashMap<String, RunnableProbe>> runnableProbes) {
		HashMap<String, RunnableProbe> newRunnableProbes = runnableProbes.get(id);
		if (newRunnableProbes == null)
			newRunnableProbes = new HashMap<String, RunnableProbe>();
		newRunnableProbes.put(runnableProbe.getId(), runnableProbe);
		runnableProbes.put(id, newRunnableProbes);
	}

	@Override
	public boolean remove(RunnableProbe runnableProbe) {
		runnableProbe.stop();
		runnableProbes.remove(runnableProbe.getId());
		
		UUID hostId = runnableProbe.getHost().getHostId();
		UUID userId = runnableProbe.getProbe().getUser().getUserId();
		String probeId = runnableProbe.getProbe().getProbe_id();
		
		removeFromMap(runnableProbe, hostId.toString(), hostRunnableProbes);
		if (!hostRunnableProbes.containsKey(hostId.toString()))
			UsersManager.removeHost(hostId, userId);
		
		removeFromMap(runnableProbe, userId.toString(), userRunnableProbes);
		if (!userRunnableProbes.containsKey(userId.toString()))
			UsersManager.removeUser(userId);
		
		removeFromMap(runnableProbe, probeId, probeRunnableProbes);
		
		
		
		return true;
	}
	
	private boolean removeFromMap(RunnableProbe runnableProbe, String id, HashMap<String, HashMap<String, RunnableProbe>> runnableProbes)
	{
		HashMap<String, RunnableProbe> runnableProbeSet = runnableProbes.get(id);
		if (runnableProbeSet  == null)
			return true;
		for (String runnableProbeId : runnableProbeSet.keySet())
		{
			if (runnableProbeId.equals(runnableProbe.getId()))
			{
				runnableProbes.remove(runnableProbeId);
				return true;
			}
		}
		
		if (runnableProbeSet.size() == 0)
			runnableProbes.remove(id);
		
		return true;
	}
	
	@Override
	public boolean removeByTemplateId(String teplateId)
	{
		for (RunnableProbe runnableProbe : runnableProbes.values()) {
			if(runnableProbe.getProbe().getTemplate_id().equals(teplateId))
			{
				remove(runnableProbe);
				return true;
			}
		}
		return true;
	}
	
	@Override
	public boolean removeByProbeId(String probeId)
	{
		for (RunnableProbe runnableProbe : getByProbe(probeId).values())
			remove(runnableProbe);
		return true;
	}

	@Override
	public boolean removeByRunnableProbeId(String runnabelProbeId)
	{
		RunnableProbe runnableProbe = runnableProbes.get(runnabelProbeId);
		if (runnableProbe != null)
			remove(runnableProbe);
		return true;
	}
	
	// No more RunnableProbes in host
	public boolean isHostEmpty(String hostId)
	{
		HashMap<String, RunnableProbe> runnableProbes = hostRunnableProbes.get(hostId);
		return runnableProbes == null || runnableProbes.size() == 0;
	}
	
}
