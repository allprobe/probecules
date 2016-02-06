package lycus;

public class Enums {
	public static enum ApiAction
	{
		InitServer,GetServerMemoryDump,FlushServerMemory,InsertDatapointsBatches,PutEvents,GetServerLiveEvents,GetThreadsUpdates
	}
		public static enum DiscoveryElementType
		{
		nics,disks	
		}
	public static enum SnmpStoreAs
	{
		asIs,delta
	}
}
