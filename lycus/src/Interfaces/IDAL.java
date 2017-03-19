package Interfaces;

import org.json.simple.JSONObject;

import GlobalConstants.Enums;

public interface IDAL {
	JSONObject get(Enums.ApiAction action);

	JSONObject put(Enums.ApiAction action, JSONObject reqBody);
}
