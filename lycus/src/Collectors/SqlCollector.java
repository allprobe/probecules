package Collectors;

public class SqlCollector extends BaseCollector {
	private String user_id;
	private String name;
	private String id;
	private int timeout;
	private int sql_port;
	private String sql_sec;
	private String sql_user;
	private String sql_type;
	private String sql_password;

	public SqlCollector(String id, String name, String user_id, int timeout, int sql_port, String sql_sec,
			String sql_user, String sql_type, String sql_password) {
		setId(id);
		setName(name);
		setUser_id(user_id);
		setTimeout(timeout);
		setSql_port(sql_port);
		setSql_sec(sql_sec);
		setSql_user(sql_user);
		setSql_type(sql_type);
		setSql_password(sql_password);
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getSql_port() {
		return sql_port;
	}

	public void setSql_port(int sql_port) {
		this.sql_port = sql_port;
	}

	public String getSql_sec() {
		return sql_sec;
	}

	public void setSql_sec(String sql_sec) {
		this.sql_sec = sql_sec;
	}

	public String getSql_user() {
		return sql_user;
	}

	public void setSql_user(String sql_user) {
		this.sql_user = sql_user;
	}

	public String getSql_type() {
		return sql_type;
	}

	public void setSql_type(String sql_type) {
		this.sql_type = sql_type;
	}

	public String getSql_password() {
		return sql_password;
	}

	public void setSql_password(String sql_password) {
		this.sql_password = sql_password;
	}
}
