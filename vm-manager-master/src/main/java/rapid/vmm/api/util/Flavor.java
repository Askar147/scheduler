package rapid.vmm.api.util;

public class Flavor {
    private Flavor.FlavorType type;
    private int vCpu;
    private long memory;
    private long disk;

    public Flavor() {
    }

    public Flavor(Flavor.FlavorType type, int vCpu, long memory, long disk) {
        this.type = type;
        this.vCpu = vCpu;
        this.memory = memory;
        this.disk = disk;
    }

    public Flavor.FlavorType getType() {
        return this.type;
    }

    public void setType(Flavor.FlavorType type) {
        this.type = type;
    }

    public int getvCpu() {
        return this.vCpu;
    }

    public void setvCpu(int vCpu) {
        this.vCpu = vCpu;
    }

    public long getMemory() {
        return this.memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public long getDisk() {
        return this.disk;
    }

    public void setDisk(long disk) {
        this.disk = disk;
    }

    public Flavor clone() {
        return new Flavor(this.type, this.vCpu, this.memory, this.disk);
    }

    public boolean equals(Object obj) {
        return obj != null && obj instanceof Flavor && ((Flavor)obj).getMemory() == this.memory && ((Flavor)obj).getvCpu() == this.vCpu;
    }

    public boolean modified(Flavor flavor) {
        return this.type == flavor.getType() && (this.vCpu != flavor.getvCpu() || this.memory != flavor.getMemory() || this.disk != flavor.getDisk());
    }

    public String toString() {
        return this.type.toString();
    }

    public static enum FlavorType {
        tiny,
        small,
        standard;
    }
}
