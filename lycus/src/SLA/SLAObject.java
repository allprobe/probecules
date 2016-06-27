package SLA;

public class SLAObject {
	private long count;
	private double percentage;
	private double dailyPercentage;
	private double dailyCount;
	
	public SLAObject(){
		count = 0;
		dailyCount = 0;
		setPercentage(100);
		setDailyPercentage(100);
	}
	
	public boolean addResult(Boolean iaActive)
	{
		if (iaActive == null)
			return false;
		if ((iaActive && getPercentage() != 100) || !iaActive)
		{
			setPercentage(getPercentage() + 1/count);
		}
		if ((iaActive && getDailyPercentage() != 100) || !iaActive)
		{
			setDailyPercentage(getDailyPercentage() + 1/dailyCount);
		}
		
		count++;
		return true;
	}

	private double getPercentage() {
		return percentage;
	}

	private void setPercentage(double percentage) {
		this.percentage = percentage;
	}

	private double getDailyPercentage() {
		return dailyPercentage;
	}

	private void setDailyPercentage(double dailyPercentage) {
		this.dailyPercentage = dailyPercentage;
	}
	
	public double getResults()
	{
		double percentage = getPercentage();
		count = 0;
		setPercentage(100);
		return percentage;
	}
	
	public double getDailyResults()
	{
		double dailyPercentage = getDailyPercentage();
		dailyCount = 0;
		setDailyPercentage(100);
		return dailyPercentage;
	}
}
