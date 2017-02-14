package Events;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import Interfaces.IEventsQueue;
import Model.EventsObject;
import Results.BaseResult;

public class EvenetsQueue implements IEventsQueue {
	private static EvenetsQueue instance;
	private JSONArray eventsArray;
	private int count;
	private Object lock = new Object();

	public static EvenetsQueue getInstance() {
		if (instance == null) {
			instance = new EvenetsQueue();
		}
		return instance;
	}

	public EvenetsQueue() {
		eventsArray = new JSONArray();
		int count = 0;
	}

	public void add(Event event, BaseResult result) {
		JSONObject runnableEventJson = new JSONObject();
		JSONObject eventJson = new JSONObject();
		eventJson.put("trigger_id", event.getTriggerId());
		eventJson.put("event_timestamp", String.valueOf(event.getTime()));
		eventJson.put("event_status", String.valueOf(event.getIsStatus()));
		eventJson.put("user_id", event.getUserId());
		eventJson.put("host_id", event.getRunnableProbeId().split("@")[1]);
		eventJson.put("host_bucket", event.getBucketId());
		eventJson.put("extra_info", event.getExtraInfo());
		eventJson.put("event_sub_type", event.getSubType());
		eventJson.put("trigger_name", event.getTriggerName());
		eventJson.put("trigger_severity", event.getTriggerSeverity());
		eventJson.put("host_name", event.getHostName());
		eventJson.put("host_notifs_groups", event.getHostNotificationGroup());
		if (result != null)
			eventJson.put("result", result.getResultObject());
		if (event.isDeleted())
			eventJson.put("remove_object", "true");

		if (event.getIsStatus())
			eventJson.put("origin_timestamp", String.valueOf(event.getOriginalTimeStamp()));

		runnableEventJson.put(event.getRunnableProbeId(), eventJson);

		synchronized (lock) {
			eventsArray.add(runnableEventJson);
		}
	}

	public void clearAll() {
		eventsArray.clear();
		count = 0;
	}

	public EventsObject getEventsPerRunnableProbe() {
		String eventsToSend = null;
		synchronized (lock) {
			eventsToSend = eventsArray.toJSONString();
			clearAll();
		}
		return new EventsObject(eventsToSend, count);
	}
}
