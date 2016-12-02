package Tasks;

public abstract class BaseTask implements Runnable {
	private long interval = 25; 
	
	public void run() { 
		
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}
}
