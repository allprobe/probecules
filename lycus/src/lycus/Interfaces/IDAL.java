package lycus.Interfaces;

import org.json.simple.JSONObject;

import lycus.Enums;

public interface IDAL {
	JSONObject get(Enums.ApiAction action);
	JSONObject put(Enums.ApiAction action, JSONObject reqBody);
}
