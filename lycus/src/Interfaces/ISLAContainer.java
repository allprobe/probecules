package Interfaces;

import org.json.simple.JSONObject;
import Results.BaseResult;

public interface ISLAContainer {
	boolean addToSLA(BaseResult result);
	JSONObject getHourlySLA();
	JSONObject getDailySLA();
}
