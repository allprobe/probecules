package Model;

public class UpdateValueModel {
	public String user_id;
	public Float multiplier;
	public String name;
	public Integer interval;
	public String type;
	public KeyUpdateModel key;
	public String status;
	public String ip;
	public String snmp_template;
	public String sql_template;
	public String bucket;
	public String notifications_group;

	// Trigger
	public TriggerModel[] triggers;

	public String id;
	public String trigger_type;
	public String severity;
	public String xvalue_unit;
	public String probe_id;
	public String tuple;
	public String collector_type;
	public Integer snmp_version;
	public String snmp_community;
	public String snmp_user;
	public String snmp_sec;
	public String snmp_auth_method;
	public String snmp_auth_password;
	public String snmp_crypt_method;
	public String snmp_crypt_password;
	public Integer timeout;
	public Integer snmp_port;
	public String results_vector_type;
	
	public Integer sql_port;
	public String sql_password;
	public String sql_type;
	public String sql_user;
	public String sql_sec;
}
