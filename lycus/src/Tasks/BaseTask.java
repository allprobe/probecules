package Tasks;

public abstract class BaseTask implements Runnable {
	private long interval = 30; 
	
	public void run() { 
		
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}
	
}
