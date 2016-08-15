package Interfaces;

import Results.BaseResult;

public interface IResultsContainer {
	boolean addResult(BaseResult result);
	boolean removeSentResults();
	String getResults(); // All results json
//	String getRollups(); // All Rollups Encoded Base64
	String getEvents();  // All Events Encoded Base64
	
}
