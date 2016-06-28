package SLA;

public class SLAObject {
	private long count;
	private double percentage;
	private double dailyPercentage;
	private double dailyCount;
	
	public SLAObject(){
		count = 1;
		dailyCount = 1;
		setPercentage(0);
		setDailyPercentage(0);
	}
	
	public boolean addResult(Boolean iaActive)
	{
		if (iaActive == null)
			return false;
		
		if (iaActive)
		{
			setPercentage((getPercentage() + 100) /count);
			setDailyPercentage((getDailyPercentage() + 100) /count);
		}
		else
		{
			setPercentage(getPercentage() /count);
			setDailyPercentage(getDailyPercentage() /count);
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
		count = 1;
		setPercentage(100);
		return percentage;
	}
	
	public double getDailyResults()
	{
		double dailyPercentage = getDailyPercentage();
		dailyCount = 1;
		setDailyPercentage(100);
		return dailyPercentage;
	}
}
