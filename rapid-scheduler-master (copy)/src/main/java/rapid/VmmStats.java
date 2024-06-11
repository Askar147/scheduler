package rapid;

public class VmmStats {
    private long statid;
    private long vmmid;
    private int cpuload;
    private float allocatedcpu;
    private long freemem;
    private long availmem;
    private int powerusage;

    public long getStatid() {
        return statid;
    }

    public void setStatid(long statid) {
        this.statid = statid;
    }

    /**
     * @return
     */
    public long getVmmid() {
        return vmmid;
    }

    /**
     * @param vmmid
     */
    public void setVmmid(long vmmid) {
        this.vmmid = vmmid;
    }

    /**
     * @return
     */
    public int getCpuload() {
        return cpuload;
    }

    /**
     * @param cpuload
     */
    public void setCpuload(int cpuload) {
        this.cpuload = cpuload;
    }

    /**
     * @return
     */
    public long getFreemem() {
        return freemem;
    }

    /**
     * @param freemem
     */
    public void setFreemem(long freemem) {
        this.freemem = freemem;
    }

    public int getPowerusage() {
        return powerusage;
    }

    public void setPowerusage(int powerusage) {
        this.powerusage = powerusage;
    }

    public float getAllocatedcpu() {
        return allocatedcpu;
    }

    public void setAllocatedcpu(float allocatedcpu) {
        this.allocatedcpu = allocatedcpu;
    }

    public long getAvailmem() {
        return availmem;
    }

    public void setAvailmem(long availmem) {
        this.availmem = availmem;
    }

}
