package Model;

public class EventsObject {
	private String eventsJson;
	private int legth;
	
	public EventsObject(String eventsJson, int length){
		this.eventsJson = eventsJson;
		this.legth = legth;
	}
	
	public String getEventsJson() {
		return eventsJson;
	}
	
	public int getLegth() {
		return legth;
	}
}
