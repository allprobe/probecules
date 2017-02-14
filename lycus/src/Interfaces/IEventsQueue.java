package Interfaces;

import Events.Event;
import Model.EventsObject;
import Results.BaseResult;

public interface IEventsQueue {
	void add(Event event, BaseResult result);
	void clearAll();
	EventsObject getEventsPerRunnableProbe();
}
