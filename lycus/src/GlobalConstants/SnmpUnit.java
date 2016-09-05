package GlobalConstants;

public enum SnmpUnit {
	// b(1L), B(8L), kb(1024L), KB(8192L), mb(1048576L), MB(8388608L),
	// gb(1073741824L), GB(8589934592L), as_is(1);
	b, B, Kb, KB, Mb, MB, Gb, GB, as_is, s, ms;

	public static long getBasic(long value, SnmpUnit unit) {
		switch (unit) {
		case as_is:
			return value;
		case b:
			return value;
		case B:
			return value * 8;
		case Kb:
			return value * 1024;
		case KB:
			return value * 8192;
		case Mb:
			return value * 1048576;
		case MB:
			return value * 8388608;
		case Gb:
			return value * 1073741824;
		case GB:
			return value * 8589934592L;
		}

		return value;
	}

	// receiving bits
	public static long getBytes(long value) {
		return value / 8;
	}

	public static long getKBits(long value) {
		return value / 1024;
	}

	public static long getKBytes(long value) {
		return value / 8192;
	}

	public static long getMBits(long value) {
		return value / 1048576;
	}

	public static long getMBytes(long value) {
		return value / 8388608;
	}

	public static long getGBits(long value) {
		return value / 1073741824;
	}

	public static long getGBytes(long value) {
		return value / 8589934592L;
	}
}
