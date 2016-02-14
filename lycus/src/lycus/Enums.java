package lycus;

public class Enums {
	public static enum ApiAction
	{
		InitServer,
		GetServerMemoryDump,
		FlushServerMemory,
		InsertDatapointsBatches,
		PutEvents,
		GetServerLiveEvents,
		GetThreadsUpdates, 
		DevGetThreadsUpdates,
		GetHosts
	}
	
	public static enum DiscoveryElementType
	{
		nics,disks	
		}
	
	public static enum SnmpStoreAs
	{
		asIs,delta
	}
	
	public static enum UpdateType
	{
		pod,   // Update Probe
		npob,  // New Probe
		pdel,  // Delete probe
		utrig, // Update trigger
		ntrig, // New trigger
		dtrig, // Delete trigger
		updh,  // Update host
		delh,  // Delete host
		updt,  // Update template
		delt,  // Delete template
		updbucket, // Update bucket
		delbucket, // Delete bucket
		updsnmp,   // Update snmp
		delsnmp,   // Delete snmp
		udisc,     // Update discovery
		ude,       // Update element
		rdisc      // Delete discovery
	}
	public static enum Action {
		New,
		Update,
		Delete
	}

	public static final String Action = null;
}
