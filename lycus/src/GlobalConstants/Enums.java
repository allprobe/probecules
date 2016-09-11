package GlobalConstants;

public class Enums {
	public static enum ApiAction {
		InitServer, GetServerMemoryDump, FlushServerMemory, InsertDatapointsBatches, PutEvents, GetServerLiveEvents, GetThreadsUpdates, DevGetThreadsUpdates, GetHosts, GetSnmpTemplates, PutSlaBatches, DiagnosticResults
	}

	public static enum DiscoveryElementType {
		bw, ds // bw = band width, ds = discs space/block device
		, unknown
	}

	public static enum SnmpStoreAs {
		asIs, delta, deltaBytesPerSecond
	}

	public static enum UpdateType {
		pod, // Update Probe
		npob, // New Probe
		pdel, // Delete probe
		utrig, // Update trigger
		ntrig, // New trigger
		dtrig, // Delete trigger
		updh, // Update host
		delh, // Delete host
		updt, // Update template
		delt, // Delete template
		updbucket, // Update bucket
		delbucket, // Delete bucket
		updsnmp, // Update snmp
		delsnmp, // Delete snmp
		udisc, // Update discovery
		ude, // Update element
		rdisc // Delete discovery
	}

	public static enum Action {
		New, Update, Delete
	}

	public static enum HostType {
		Windows, Linux
	}

	public static enum SnmpError {
		NO_COMUNICATION, EXCEPTION_ON_REQUEST;
	}

	public static enum ElementChange {
		addedElement, removedElement, indexElementChanged

	}

	public static enum ResultValueType {
		PRT, WRT, RC, PS, ST, RTA, PL, DFDS, DUDS, DBI, DBO, WSERT, WAERC, TRARHRT, TRDHRT, DTDS

	}

	// public static enum XValueUnit {
	// as_is, b, B, Kb, KB, Mb, MB, Gb, GB
	// }

	public static enum Condition {
		no_trigger, bigger, tinier, equal, not_equal
	}

	public static enum Function {
		nothing, avg, delta, max, delta_avg
	}

	public static enum LastType {
		N, // Last-N
		P, // Last-Period
		H, // Last-Hours
		K
	}

	public static final String Action = null;
}
