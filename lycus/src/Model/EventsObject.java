package Model;

public class EventsObject {
	private String eventsJson;
	private int length;
	
	public EventsObject(String eventsJson, int length){
		this.eventsJson = eventsJson;
		this.length = length;
	}
	
	public String getEventsJson() {
		return eventsJson;
	}
	
	public int getLegth() {
		return length;
	}
}
