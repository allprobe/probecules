package Interfaces;

import Events.Event;
import Model.EventsObject;

public interface IEventsQueue {
	void add(Event event);
	void clearAll();
	EventsObject getEventsPerRunnableProbe();
}
