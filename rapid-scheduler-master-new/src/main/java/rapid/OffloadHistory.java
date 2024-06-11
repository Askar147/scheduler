package rapid;

public class OffloadHistory {
    private long offloadid;
    private long vmmid;
    private long userid;
    //localdatetime format? LocalDateTime.parse("2015-02-20T06:30:00");
    private String deadline;
    private int vcpu;
    private long memory;
    private long cycles;

    public long getOffloadid() {
        return offloadid;
    }

    public void setOffloadid(long offloadid) {
        this.offloadid = offloadid;
    }

    public long getVmmid() {
        return vmmid;
    }

    public void setVmmid(long vmmid) {
        this.vmmid = vmmid;
    }

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
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

    public long getCycles() {
        return cycles;
    }

    public void setCycles(long cycles) {
        this.cycles = cycles;
    }
}
