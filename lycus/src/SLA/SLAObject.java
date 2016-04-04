package SLA;

public class SLAObject {
	private long count;
	private double percentage;
	
	public SLAObject(){
		count = 0;
		setPercentage(100);
	}
	
	public boolean addResult(Boolean iaActive)
	{
		if (iaActive == null)
			return false;
		if (iaActive && getPercentage() != 100)
		{
			setPercentage(getPercentage() + 1/count);
		}
		else if (!iaActive)
		{
			setPercentage(getPercentage() + 1/count);
		}
		count++;
		return true;
	}

	public double getPercentage() {
		return percentage;
	}

	private void setPercentage(double percentage) {
		this.percentage = percentage;
	}
}
