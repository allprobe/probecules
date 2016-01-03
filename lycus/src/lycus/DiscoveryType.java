package lycus;

public enum DiscoveryType {
	BandWidth ("bw"),
	Disk ("dsk");
    private final String name;       

    private DiscoveryType(String type) {
    	this.name=type;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
       return this.name;
    }

}
