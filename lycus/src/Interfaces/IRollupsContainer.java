package Interfaces;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import Results.BaseResult;

public interface IRollupsContainer {
	boolean addResult(BaseResult result);
	String getAllFinsihedRollups();
	boolean mergeRollups(JSONArray jsonArray);
	String getAllCurrentLiveRollups();
}
