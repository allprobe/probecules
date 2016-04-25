package Utils;

import org.json.simple.JSONObject;
import com.google.gson.Gson;

public class JsonUtil {
	public static <T> T ToObject(JSONObject jsonObject, Class<T> generic){
		//ThreadsUpdates threadsUpdates = new Gson().fromJson(jsonObject.toString(), ThreadsUpdates.class);
		T t = (T) new Gson().fromJson(jsonObject.toString(), generic);
		return t;
	}
	
	public static <T>String ToJson(T generic){
		return new Gson().toJson(generic);
	}
}