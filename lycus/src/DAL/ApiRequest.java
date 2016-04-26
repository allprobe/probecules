package DAL;

import org.json.simple.JSONObject;

import GlobalConstants.Constants;
import GlobalConstants.Enums.ApiAction;

public class ApiRequest {
	private ApiAction action;
	private JSONObject requestBody;
	public ApiRequest(ApiAction action, JSONObject requestBody) {
		super();
		this.setAction(action);
		this.setRequestBody(requestBody);
	}
	public ApiRequest(ApiAction action) {
		super();
		this.setAction(action);
	}
	public ApiAction getAction() {
		return action;
	}
	public void setAction(ApiAction action) {
		this.action = action;
	}
	public JSONObject getRequestBody() {
		return requestBody;
	}
	public void setRequestBody(JSONObject requestBody) {
		this.requestBody = requestBody;
	}
	public String getType()
	{
		if(requestBody==null)
		return Constants.get;
		else
			return Constants.put;
	}
}
