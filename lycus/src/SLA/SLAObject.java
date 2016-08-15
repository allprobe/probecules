package SLA;

public class SLAObject {
	private int count;
	private int sum;
	private int dailySum;
	private int dailyCount;

	public SLAObject() {
		count = 0;
		dailyCount = 0;
		dailySum = 0;
		addDailySum(0);
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

	private int getSum() {
		return sum;
	}

	private void setSum(int percentage) {
		this.sum = percentage;
	}

	private void addDailySum(int dailySum) {
		this.dailySum += dailySum;
	}

	private double getDailyPercentage() {
		return dailySum / dailyCount;
	}

	private double getPecentage() {
		return sum / count;
	}

	public double getResults() {
		addDailySum(getSum());
		dailyCount++;
		double percentage = getPecentage();
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
