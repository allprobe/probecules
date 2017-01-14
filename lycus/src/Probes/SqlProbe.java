package Probes;

import java.util.UUID;
import Model.UpdateModel;
import Model.UpdateValueModel;
import NetConnection.NetResults;
import Results.BaseResult;
import Results.SqlResult;
import Utils.Logit;
import lycus.Host;
import lycus.User;

public class SqlProbe extends BaseProbe {
	private String sql_query;
	private String sql_db;
	private int timeout;

	public SqlProbe(User user, String probe_id, UUID template_id, String name, int interval, float multiplier,
			boolean status, int timeout, String sql_db, String sql_query) {
		super(user, probe_id, template_id, name, interval, multiplier, status);
		this.setSql_db(sql_db);
		this.setSql_query(sql_query);
		this.setTimeout(timeout);
	}

	public String getSql_query() {
		return sql_query;
	}

	public void setSql_query(String sql_query) {
		this.sql_query = sql_query;
	}

	public String getSql_db() {
		return sql_db;
	}

	public void setSql_db(String sql_db) {
		this.sql_db = sql_db;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public BaseResult getResult(Host h) {
		if (!h.isHostStatus())
			return null;

		SqlResult result = NetResults.getInstanece().getSqlResult(h, this);

		return result;
	}

//	@Override
//	public String toString() {
//		StringBuilder s = new StringBuilder(super.toString());
//		s.append("Num Of Pings:").append(this.getCount()).append("; ");
//		s.append("Num Of Bytes:").append(this.getBytes()).append("; ");
//		s.append("Timeout:").append(this.getTimeout()).append("; ");
//		return s.toString();`
//	}

	public boolean updateKeyValues(UpdateModel updateModel) {
		super.updateKeyValues(updateModel);
		UpdateValueModel updateValue = updateModel.update_value;
		if (updateValue.key.sql_db != null && updateValue.key.sql_db != this.getSql_db()) {
			this.setSql_db(updateValue.key.sql_db);
			Logit.LogCheck("sql_db count for " + getName() + " has changed to " + updateValue.key.sql_db);
		}

		if (updateValue.key.sql_query != null && updateValue.key.sql_query != this.sql_query) {
			this.setSql_query(updateValue.key.sql_query);
			Logit.LogCheck("sql_query for " + getName() + " has changed to " + updateValue.key.sql_query);
		}

		if (updateValue.key.timeout != null && getTimeout() != updateValue.key.timeout) {
			this.setTimeout(updateValue.key.timeout);
			Logit.LogCheck("Timeout for" + getName() + " has changed to " + updateValue.key.timeout);
		}
		return true;
	}
}
