package rapid;

public class VmType {

    private int osType;
    private int id;
    private int numCore;
    private long memory;
    private int disk;
    private int gpuCore;

    public int getOsType() {return osType; }

    public void setOsType(int osType) {this.osType = osType; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumCore() {
        return numCore;
    }

    public void setNumCore(int numCore) {
        this.numCore = numCore;
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public int getDisk() {
        return disk;
    }

    public void setDisk(int disk) {
        this.disk = disk;
    }

    public int getGpuCore() {
        return gpuCore;
    }

    public void setGpuCore(int gpuCore) {
        this.gpuCore = gpuCore;
    }
}
