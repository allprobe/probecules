package Model;

public class ProbeParams {
	public String user_id;
	public String probe_id;
	public String template_id;
	public String name;
	public Integer interval;
	public Float multiplier;
	public boolean is_active;
	public String type;
	public Integer npings;
	public Integer bytes;
	public Integer timeout;
	public String protocol;
	public Integer port;
	public String send_type;
	public String port_extra;
	public String rbl;
	public String oid;
	public String snmp_datatype;
	public String snmp_unit;
	public Integer snmp_store_as;
	public String http_request;
	public String http_auth;
	public String http_auth_username;
	public String http_auth_password;
	public String url;
	public Integer element_interval;
	public Integer http_deep;
	
	//SQL
	public String sql_query;
	public String sql_db;
	
	// Discovery
	public String discovery_type;
	public String severity;
	public String tuple;
	public String xvalue_unit;
	public String triggerName;
	public String triggerId;
	
	public TriggerModel[] triggers;
}
