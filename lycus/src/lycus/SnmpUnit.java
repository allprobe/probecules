package lycus;

public enum SnmpUnit 
{
  bits(1L),
  bytes(8L),
  kbits(1024L),
  kbytes(8192L),
  mbits(1048576L),
  mbytes(8388608L),
  gbits(1073741824L),
  gbytes(8589934592L),
  none(1);

  private final long multiplieBy; 
  private SnmpUnit(final long multiplier) { this.multiplieBy = multiplier; }
  public long getMultiplier() { return this.multiplieBy; }
  public long getBasic( long value) { return this.getMultiplier()*value; }
  public long getBytes( long value) { return this.getBasic(value)/8; }
  public long getKBits( long value) { return this.getBasic(value)/1024; }
  public long getKBytes( long value) { return this.getBasic(value)/8192; }
  public long getMBits( long value) { return this.getBasic(value)/1048576; }
  public long getMBytes( long value) { return this.getBasic(value)/8388608; }
  public long getGBits( long value) { return this.getBasic(value)/1073741824; }
  public long getGBytes( long value) { return this.getBasic(value)/8589934592L; }
}
