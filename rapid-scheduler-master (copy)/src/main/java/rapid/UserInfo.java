package rapid;

public class UserInfo {
    private long userid;
    private String ipv4;
    private String qosparam;
    private String deadline;
    private long cycles;
    private int vcpu;
    private int memory;

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
    public String getQosparam() {
        return qosparam;
    }

    /**
     * @param qosparam
     */
    public void setQosparam(String qosparam) {
        this.qosparam = qosparam;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public long getCycles() {
        return cycles;
    }

    public void setCycles(long cycles) {
        this.cycles = cycles;
    }

    public int getVcpu() {
        return vcpu;
    }

    public void setVcpu(int vcpu) {
        this.vcpu = vcpu;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }
}
