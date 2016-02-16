package lycus;

public class DataPointsRollup {
	private String runnableProbeId;
	private long startTime;
	private int resultsCounter;
	private DataPointsRollupSize timePeriod;
	private double min;
	private double max;
	private double avg;
	private long endTime;

	private DataPointsRollup lastFinishedRollup;
	
	public DataPointsRollup(String rpID,DataPointsRollupSize timePeriod) {
		this.setRunnableProbeId(rpID);
		this.startTime=0;
		this.endTime=0;
		this.timePeriod=timePeriod;
		this.resultsCounter=0;
		this.min=Double.MAX_VALUE;
		this.max=Double.MIN_VALUE;
		this.avg=0;
		this.lastFinishedRollup=null;
	}
	
	public String getRunnableProbeId() {
		return runnableProbeId;
	}

	public void setRunnableProbeId(String runnableProbeId) {
		this.runnableProbeId = runnableProbeId;
	}

	public DataPointsRollupSize getTimePeriod() {
		return timePeriod;
	}
	public void setTimePeriod(DataPointsRollupSize timePeriod) {
		this.timePeriod = timePeriod;
	}
	public double getMin() {
		return min;
	}
	public void setMin(double min) {
		this.min = min;
	}
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
	public double getAvg() {
		return avg;
	}
	public void setAvg(double avg) {
		this.avg = avg;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime=startTime;
	}
	
	public int getResultsCounter() {
		return resultsCounter;
	}
	public void setResultsCounter(int resultsCounter) {
		this.resultsCounter = resultsCounter;
	}
	


	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public DataPointsRollup getLastFinishedRollup() {
		return lastFinishedRollup;
	}

	public void setLastFinishedRollup(DataPointsRollup lastFinishedRollup) {
		this.lastFinishedRollup = lastFinishedRollup;
	}

	public void add(long lastTimestamp,double results)
	{
		if(this.isCompleted())
			this.saveFinishedRollup();
		if(this.getStartTime()==0)
		{
			this.setStartTime(lastTimestamp);
			this.setResultsCounter(1);
			this.setMin(results);
			this.setMax(results);
			this.setAvg(results);
		}
		else
		{
			this.setResultsCounter(this.getResultsCounter()+1);
			this.setMin(Math.min(results, this.getMin()));
			this.setMax(Math.max(results,this.getMax()));
			this.setAvg(this.cumulativeMovingAverage(results, this.getAvg(), this.getResultsCounter()));
		}
	}
	private void saveFinishedRollup() {
		DataPointsRollup finished=new DataPointsRollup(this.getRunnableProbeId(),this.getTimePeriod());
		finished.setStartTime(this.getStartTime());
		finished.setMin(this.getMin());
		finished.setMax(this.getMax());
		finished.setAvg(this.getAvg());
		finished.setResultsCounter(this.getResultsCounter());
		finished.setEndTime(System.currentTimeMillis());
		this.setLastFinishedRollup(finished);
		this.reset();
	}

	private double cumulativeMovingAverage(double lastValue,double lastAverage,int numberOfValues)
	{
		return lastAverage+((lastValue-lastAverage)/(numberOfValues));
	}
	
	
	public boolean isCompleted()
	{
		return ((System.currentTimeMillis()-this.getStartTime())>this.getTimePeriod().getValue())&&(this.getStartTime()!=0);
	}
	
	public void reset()
	{
		if(!this.isCompleted())
			return;
		this.setStartTime(0);
		this.resultsCounter=0;
		this.min=Double.MAX_VALUE;
		this.max=Double.MIN_VALUE;
		this.avg=0;
	}
	public void mergeRollup(DataPointsRollup existing)
	{
		long rollupEndTime=existing.getStartTime()+this.getTimePeriod().getValue();
		if(rollupEndTime<System.currentTimeMillis())
			return;
		this.setStartTime(existing.getStartTime());
		this.setResultsCounter(existing.getResultsCounter()+this.getResultsCounter());
		this.setMin(Math.min(existing.getMin(),this.getMin()));
		this.setMax(Math.max(existing.getMax(), this.getMax()));
		double newAvg=((existing.getAvg()*existing.getResultsCounter())+(this.getAvg()*this.getResultsCounter())/(existing.getResultsCounter()+this.getResultsCounter()));
		this.setAvg(newAvg);
	}
	
}
