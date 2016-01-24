package lycus.Interfaces;

import org.json.simple.JSONObject;

import lycus.Enums;

public interface IDAL {
	JSONObject get(Enums.ApiAction action);
	Boolean put(Enums.ApiAction action, JSONObject reqBody);
}