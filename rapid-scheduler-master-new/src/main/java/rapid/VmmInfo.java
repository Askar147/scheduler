package rapid;

import java.util.HashMap;

public class VmmInfo {
    private long vmmid;
    private String ipv4;
    private int mactype;

    private String macaddress;

    static final int DEFAULT = 1;
    static final int JETSON_NANO_WOL = 2;
    static final int JETSON_NANO_NO_WOL = 3;
    static final int JETSON_NANO_WOL_NEWJETPACK = 4;
    static final int XAVIER = 5;
    static final int ODROID = 6;

    static HashMap<Integer, String> macTypeMap = new HashMap<Integer, String>();
    static {
        macTypeMap.put(1, "DEFAULT");
        macTypeMap.put(2, "JETSON_NANO_WOL");
        macTypeMap.put(3, "JETSON_NANO_NO_WOL");
        macTypeMap.put(4, "JETSON_NANO_WOL_NEWJETPACK");
        macTypeMap.put(5, "XAVIER");
        macTypeMap.put(5, "ODROID");
    }

    private int suspended;

    private int cpuload;
    private float allocatedcpu;
    private int cpufrequency;
    private int cpunums;
    private long freemem;
    private long availmem;
    private int powerusage;
    private int freegpu;
    private int gpunums;
    private String availtypes;

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
    public String getIpv4() {
        return ipv4;
    }

    /**
     * @param ipv4
     */
    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    /**
     * @return
     */
    public int getMactype() {
        return mactype;
    }

    /**
     * @param mactype
     */
    public void setMactype(int mactype) {
        this.mactype = mactype;
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
    public int getCpunums() {
        return cpunums;
    }

    /**
     * @param cpunums
     */
    public void setCpunums(int cpunums) {
        this.cpunums = cpunums;
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

    /**
     * @return
     */
    public int getFreegpu() {
        return freegpu;
    }

    /**
     * @param freegpu
     */
    public void setFreegpu(int freegpu) {
        this.freegpu = freegpu;
    }

    /**
     * @return
     */
    public int getGpunums() {
        return gpunums;
    }

    /**
     * @param gpunums
     */
    public void setGpunums(int gpunums) {
        this.gpunums = gpunums;
    }

    /**
     * @return
     */
    public String getAvailtypes() {
        return availtypes;
    }

    /**
     * @param availtypes
     */
    public void setAvailtypes(String availtypes) {
        this.availtypes = availtypes;
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

    public int getCpufrequency() {
        return cpufrequency;
    }

    public void setCpufrequency(int cpufrequency) {
        this.cpufrequency = cpufrequency;
    }

    public long getAvailmem() {
        return availmem;
    }

    public void setAvailmem(long availmem) {
        this.availmem = availmem;
    }

    public int getSuspended() {
        return suspended;
    }

    public void setSuspended(int suspended) {
        this.suspended = suspended;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }
}
