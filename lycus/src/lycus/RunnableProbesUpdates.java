/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.snmp4j.smi.OID;

/**
 * 
 * @author Roi
 */
public class RunnableProbesUpdates implements Runnable {
	private ScheduledExecutorService schdExctr;
	private long interval;
	private LinkedBlockingQueue<SingleUpdate> updates;

	public RunnableProbesUpdates(long interval) {
		schdExctr = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

			@Override
			public Thread newThread(Runnable r) {
				Thread t = defaultThreadFactory.newThread(r);
				t.setPriority(Thread.MAX_PRIORITY);
				return t;
			}
		});
		this.setInterval(interval);
		this.setUpdates(new LinkedBlockingQueue<SingleUpdate>());
	}

	// Getters/Setters
	private LinkedBlockingQueue<SingleUpdate> getUpdates() {
		return updates;
	}

	private void setUpdates(LinkedBlockingQueue<SingleUpdate> updates) {
		this.updates = updates;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public void start() {
		schdExctr.scheduleAtFixedRate(this, 0, this.getInterval(), TimeUnit.SECONDS);
	}

	public void run() {
		//LinkedHashMap<UUID, JSONObject> updates = ApiStages.GETthreads_updates_by_server();
		if (updates == null) {
			SysLogger.Record(
					new Log("==============================Failed Receive Probes Updates/There Isnt Any Update!==============================",
							LogType.Info));
			return;
		}
		//this.insertUpdatesToQueue(updates);
	}

	private void insertUpdatesToQueue(LinkedHashMap<UUID, JSONObject> updates) {
		for (Map.Entry<UUID, JSONObject> entry : updates.entrySet()) {
			UUID updateId = entry.getKey();
			String updateType = (String) entry.getValue().get("update_type");
			String objectId = (String) entry.getValue().get("object_id");
			UUID userId = UUID.fromString((String) entry.getValue().get("user_id"));
			UUID templateId = UUID.fromString((String) entry.getValue().get("template_id"));
			UUID hostId = UUID.fromString((String) entry.getValue().get("host_id"));
			List<String> updateValue = GeneralFunctions.valuesOrdered((String) entry.getValue().get("update_value"));
			this.addUpdate(
					new SingleUpdate(updateId, updateType, objectId, userId, templateId, hostId, updateValue, 2));
		}
	}

	private void addUpdate(SingleUpdate update) {
		try {
			this.getUpdates().put(update);
		} catch (InterruptedException e) {
			SysLogger.Record(new Log("Update: " + update.getUpdateId().toString() + " could not be added, interrupted.",
					LogType.Error));
		}
	}

//	public void executeUpdates() {
//		LinkedBlockingQueue<SingleUpdate> updates = this.getUpdates();
//		while (!updates.isEmpty()) {
//			SingleUpdate update = updates.poll();
//			boolean updateSucceed=false;
//			if(update.getExecuteAttempts()>0)
//				updateSucceed=update.execute();
//			else
//				return;
//			if (updateSucceed)
//				ApiStages.deleteThreadUpdate(update.getUpdateId());
//			else
//			{
//				this.addUpdate(update);
//				update.decreaseExecuteAttempts();
//			}
//		}
//	}

}
