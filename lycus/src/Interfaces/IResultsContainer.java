package Interfaces;

import Model.EventsObject;
import Results.BaseResult;

public interface IResultsContainer {
	boolean addResult(BaseResult result);
	boolean removeSentResults();
	String getResults(); // All results json
//	String getRollups(); // All Rollups Encoded Base64
	EventsObject getEventsPerRunnableProbe();  // All Events Encoded Base64
	
}
