package lycus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;

public class EventHandler implements Runnable {
	private RunnableProbesHistory history;

	public EventHandler(RunnableProbesHistory history) {
		super();
		this.history = history;
	}

	public RunnableProbesHistory getHistory() {
		return history;
	}

	public void setHistory(RunnableProbesHistory history) {
		this.history = history;
	}

	//@Override
	public void run() {
		try {
			ArrayList<HashMap<String,HashMap<String,String>>> events = null;
			events = this.getAllEvents();

			if (events != null) {
				SysLogger.Record(new Log("Sending events to API...", LogType.Info));
				String stringEvents=this.getHistory().getGson().toJson(events);
				if(!stringEvents.equals("[]"))
					ApiStages.putEvents(stringEvents);
			} else {
				SysLogger.Record(new Log("Unable to process events! events did not sent to API...", LogType.Error));
			}
		} catch (Exception e) {
			e.printStackTrace();
			SysLogger.Record(new Log("Error retrieving all runnable probe events!", LogType.Error, e));
		}
	}

	private ArrayList<HashMap<String,HashMap<String,String>>> getAllEvents() throws Exception {
		ArrayList<HashMap<String,HashMap<String,String>>> events=new ArrayList<HashMap<String,HashMap<String,String>>>();
		for (RunnableProbeResults rpr : this.getHistory().getResults().values()) {
			HashMap<Trigger, TriggerEvent> rprEvents = rpr.getEvents();
			if (rprEvents.size() == 0)
				continue;
			Iterator mapIterator = rprEvents.entrySet().iterator();
			while (mapIterator.hasNext()) {
				Map.Entry<Trigger, TriggerEvent> pair = (Map.Entry<Trigger, TriggerEvent>) mapIterator.next();
				Trigger trigger=pair.getKey();
				TriggerEvent event=pair.getValue();
				if (!event.isSent()&& event.isStatus()) {
					HashMap<String, HashMap<String,String>> sendingEvents = new HashMap<String,HashMap<String,String>>();
					HashMap<String,String> eventValues=new HashMap<String,String>();
					eventValues.put("trigger_id",trigger.getTriggerId().toString());
					eventValues.put("host_id",rpr.getRp().getHost().getHostId().toString());
					eventValues.put("host_name",rpr.getRp().getHost().getName());
					eventValues.put("user_id",rpr.getRp().getProbe().getUser().getUserId().toString());
					eventValues.put("trigger_name",trigger.getName());
					eventValues.put("trigger_severity",trigger.getSvrty().toString());
					eventValues.put("event_timestamp",String.valueOf(event.getTime()));
					eventValues.put("event_status",String.valueOf(event.isStatus()));
					eventValues.put("host_bucket",rpr.getRp().getHost().getBucket());
					if(rpr.getRp().getHost().getNotificationGroups()!=null)
					eventValues.put("host_notifs_groups",rpr.getRp().getHost().getNotificationGroups().toString());
					else
						eventValues.put("host_notifs_groups",null);
					sendingEvents.put(rpr.getRp().getRPString(), eventValues);
					
					events.add(sendingEvents);

					mapIterator.remove();
				}
				else if (!event.isSent()&& !event.isStatus()) {
					HashMap<String, HashMap<String,String>> sendingEvents = new HashMap<String,HashMap<String,String>>();
					HashMap<String,String> eventValues=new HashMap<String,String>();
					eventValues.put("trigger_id",trigger.getTriggerId().toString());
					eventValues.put("host_id",rpr.getRp().getHost().getHostId().toString());
					eventValues.put("host_name",rpr.getRp().getHost().getName());
					eventValues.put("user_id",rpr.getRp().getProbe().getUser().getUserId().toString());
					eventValues.put("trigger_name",trigger.getName());
					eventValues.put("trigger_severity",trigger.getSvrty().toString());
					eventValues.put("event_timestamp",String.valueOf(event.getTime()));
					eventValues.put("event_status",String.valueOf(event.isStatus()));
					eventValues.put("host_bucket",rpr.getRp().getHost().getBucket());
					if(rpr.getRp().getHost().getNotificationGroups()!=null)
					eventValues.put("host_notifs_groups",rpr.getRp().getHost().getNotificationGroups().toString());
					else
						eventValues.put("host_notifs_groups",null);
					sendingEvents.put(rpr.getRp().getRPString(), eventValues);
					
					events.add(sendingEvents);

					event.setSent(true);
				}
			}
		}
		return events;
	}
	
	
	
}
