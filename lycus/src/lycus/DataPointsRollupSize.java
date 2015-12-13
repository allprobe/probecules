package lycus;

public enum DataPointsRollupSize {
	_4minutes(240000),_20minutes(1200000),_1hour(3600000),_6hour(21600000),_36hour(129600000),_11day(950400000);
	
	private final long value;
	DataPointsRollupSize(long value)
	{
		this.value=value;
	}
	public long getValue()
	{
		return value;
	}
	public String getName()
	{
		return this.name();
	}
}
