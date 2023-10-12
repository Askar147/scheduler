package rapid;

import java.util.HashMap;

public class VmInfo {

    static final int NORMAL_VM = 0;
    static final int HELPER_VM = 1;

    static final int HELPER_VM_USER_ID = 0;

    static final int OFFLOAD_REGISTERED = 1;
    static final int OFFLOAD_OCCUPIED = 2;
    static final int OFFLOAD_RELEASED = 3;
    static final int OFFLOAD_DEREGISTERED = 4;
    static final int OFFLOAD_RESERVED = 5;

    static HashMap<Integer, String> offloadstatusMap = new HashMap<Integer, String>();
    static {
        offloadstatusMap.put(1, "OFFLOAD_REGISTERED");
        offloadstatusMap.put(2, "OFFLOAD_OCCUPIED");
        offloadstatusMap.put(3, "OFFLOAD_RELEASED");
        offloadstatusMap.put(4, "OFFLOAD_DEREGISTERED");
        offloadstatusMap.put(5, "OFFLOAD_RESERVED");
    }

    static final int VM_STARTED = 1;
    static final int VM_SUSPENDED = 2;
    static final int VM_RESUMED = 3;
    static final int VM_STOPPED = 4;

    static HashMap<Integer, String> vmStatus = new HashMap<Integer, String>();
    static {
        vmStatus.put(1, "VM_STARTED");
        vmStatus.put(2, "VM_SUSPENDED");
        vmStatus.put(3, "VM_RESUMED");
        vmStatus.put(4, "VM_STOPPED");
    }

    private long vmid;
    private String ipv4;
    private int port;
    private long vmmid;
    private int category;
    private int type;
    private long userid;
    private int offloadstatus;
    private int vmstatus;
    private int vcpu;
    private long memory;

    /**
     * @return
     */
    public long getVmid() {
        return vmid;
    }

    /**
     * @param vmid
     */
    public void setVmid(long vmid) {
        this.vmid = vmid;
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
    public int getPort() {
        return port;
    }

    /**
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
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
    public int getCategory() {
        return category;
    }

    /**
     * @param category
     */
    public void setCategory(int category) {
        this.category = category;
    }

    /**
     * @return
     */
    public int getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return
     */
    public long getUserid() {
        return userid;
    }

    /**
     * @param userid
     */
    public void setUserid(long userid) {
        this.userid = userid;
    }

    /**
     * @return
     */
    public int getOffloadstatus() {
        return offloadstatus;
    }

    /**
     * @param offloadstatus
     */
    public void setOffloadstatus(int offloadstatus) {
        this.offloadstatus = offloadstatus;
    }

    /**
     * @return
     */
    public int getVmstatus() {
        return vmstatus;
    }

    /**
     * @param vmstatus
     */
    public void setVmstatus(int vmstatus) {
        this.vmstatus = vmstatus;
    }

    public int getVcpu() {
        return vcpu;
    }

    public void setVcpu(int vcpu) {
        this.vcpu = vcpu;
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

}
