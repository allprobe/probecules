package SLA;

public class SLAObject {
	private int count;
	private double sum;
	private double dailySum;
	private int dailyCount;

	public SLAObject() {
		count = 0;
		dailyCount = 0;
		dailySum = 0;
		dailySum = 0;
	}

	public boolean addResult(Boolean isActive) {
		if (isActive == null)
			return false;

		if (isActive) {
			setSum((getSum() + 100));
		}

		count++;
		return true;
	}

	private double getSum() {
		return sum;
	}

	private void setSum(double percentage) {
		this.sum = percentage;
	}

	private void addDailySum(double dailySum) {
		this.dailySum += dailySum;
		dailyCount++;
	}

	private double getDailyPercentage() {
		return dailySum / dailyCount;
	}

	public Double getResults() {
		if (count == 0)
			return null;
		Double percentage = (double)(sum / count);
		addDailySum(percentage);
		
		count = 0;
		setSum(0);
		return percentage;
	}

	public Double getDailyResults() {
		if (dailyCount == 0)
			return null;
		double dailyPercentage = getDailyPercentage();
		dailyCount = 0;
		dailySum = 0;
		return dailyPercentage;
	}
}
