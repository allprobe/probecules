package SLA;

public class SLAObject {
	private int count;
	private int sum;
	private int dailySum;
	private int dailyCount;
	
	public SLAObject(){
		count = 0;
		dailyCount = 0;
		setSum(0);
		setDailySum(0);
	}
	
	public boolean addResult(Boolean isActive)
	{
		if (isActive == null)
			return false;
		
		if (isActive)
		{
			setSum((getSum() + 100));
			setDailySum((getDailySum() + 100));
		}
		else
		{
			setSum(getSum());
			setDailySum(getDailySum());
		}
		
		count++;
		return true;
	}

	private int getSum() {
		return sum;
	}

	private void setSum(int percentage) {
		this.sum = percentage;
	}

	private int getDailySum() {
		return dailySum;
	}

	private void setDailySum(int dailyPercentage) {
		this.dailySum = dailyPercentage;
	}
	
	private double getDailyPercentage() {
		return dailySum / dailyCount;
	}
	
	private double getPecentage() {
		return sum / count;
	}

	public double getResults()
	{
		double percentage = getPecentage();
		count = 0;
		setSum(0);
		return percentage;
	}
	
	public double getDailyResults()
	{
		double dailyPercentage = getDailyPercentage();
		dailyCount = 0;
		setDailySum(0);
		return dailyPercentage;
	}
}
