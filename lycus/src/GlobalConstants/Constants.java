package GlobalConstants;

import java.util.Arrays;
import java.util.List;

import org.snmp4j.smi.OID;

public class Constants {
	public final static String https_prefix = "https://";
	public final static String http_prefix = "http://";
	public final static String get = "GET";
	public final static String put = "PUT";

	public final static String newProbe = "npob";
	public final static String updateProbe = "pud";
	public final static String deleteProbe = "pdel";
	public final static String newTrigger = "ntrig";
	public final static String updateTrigger = "utrig";
	public final static String deleteTrigger = "dtrig";
	public final static String deleteEvent = "rle";
	public final static String updateHost = "updh";
	public final static String deleteHost = "delh";
	public final static String updateTemplate = "updt";
	public final static String deleteTemplate = "delt";
	public final static String updateBucket = "updbucket";
	public final static String deleteBucket = "delbucket";
	public final static String updateSnmp = "updcol";
	public final static String deleteSnmp = "delcol";
	// public final static String updateDiscovery = "udisc";
	public final static String updateElement = "ude";
	// public final static String deleteDiscovery = "rdisc";
	public final static String hosts = "hosts";
	public final static String collectors = "collectors";

	public final static String icmp = "icmp";
	public final static String port = "port";
	public final static String http = "http";
	public final static String snmp = "snmp";
	public static final String traceroute = "traceroute";
	public final static String discovery = "discovery";
	public final static String rbl = "rbl";
	public final static String no = "no";
	public final static String sql = "sql";
	
	public final static String integer = "integer";
	public final static String string = "string";
	public final static String _float = "float";
	public final static String _boolean = "boolean";

	public final static String b = "b";
	public final static String B = "B";
	public final static String Kb = "Kb";
	public final static String KB = "KB";
	public final static String Mb = "Mb";
	public final static String MB = "MB";
	public final static String Gb = "Gb";
	public final static String GB = "GB";
	public final static String _int = "int";
	public final static String str = "str";
	public final static String none = "none";

	public final static String notice = "notice";
	public final static String warning = "warning";
	public final static String high = "high";
	public final static String disaster = "disaster";
	public final static String default1 = "default";

	public final static String _true = "1";
	public final static String _false = "0";
	public static final String bw = "bw";
	public static final String ds = "ds";
	public static final String inBW = "inBW";
	public static final String outBW = "outBW";
	public static final String object_changed = "object_changed";
	public static final String object_removed = "object_removed";

	public static final String WRONG_OID = "WRONG_OID";
	public static final String WRONG_VALUE_FORMAT = "WRONG_VALUE_FORMAT";

	public static final String and = "and";
	public static final String or = "or";
	public static final OID ifAll_low = new OID("1.3.6.1.2.1.2.2.1");
	public static final OID ifOutOctetsOID_low = new OID("1.3.6.1.2.1.2.2.1.16");
	public static final OID ifInOctetsOID_low = new OID("1.3.6.1.2.1.2.2.1.10");
	public static final OID ifAll_high = new OID("1.3.6.1.2.1.31.1.1.1");
	public static final OID ifOutOctetsOID_high = new OID("1.3.6.1.2.1.31.1.1.1.10");
	public static final OID ifInOctetsOID_high = new OID("1.3.6.1.2.1.31.1.1.1.6");

	public static final OID storageAll = new OID("1.3.6.1.2.1.25.2.3.1");

	public static final OID sysDescr = new OID("1.3.6.1.2.1.1.1.0");
	public static final List<Integer> okStatus = Arrays.asList(200, 301, 303);
	public static final int pingPacketLostMin = 80;
	public static final int slaInterval = 3600;

	public static final String hourly = "sla1h";
	public static final String daily = "sla1d";
	public static final String snmp_connection_failed = "snmp_connection_failed";
	public static final String snmp_fixed = "snmp_fixed";
	public static final String no_snmp_template = "no_snmp_template";

	public static final String issues = "issues";

	public static final String discoveryBestElementRegex = "((eth|vmbr)0)|(/|/var/lib/vz)";
	
	public static int getBatchesSize() {
		return 1400 / GlobalConfig.getMaxSnmpResponseInBytes();
	}
}
