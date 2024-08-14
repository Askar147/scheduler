package rapid;

public class RequestInfo {
    private long requestid;
    private int accepted;
    private long vmmid;
    private long userid;
    //localdatetime format? LocalDateTime.parse("2015-02-20T06:30:00");
    private String deadline;
    private int vcpu;
    private long memory;
    private long cycles;
    private String startQueueTime;
    private String endQueueTime;

    public long getRequestid() {
        return requestid;
    }

    public void setRequestid(long requestid) {
        this.requestid = requestid;
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

    public int getAccepted() {
        return accepted;
    }

    public void setAccepted(int accepted) {
        this.accepted = accepted;
    }

    public String getStartQueueTime(){
        return startQueueTime;
    }

    public void setStartQueueTime(String startTime){
        this.startQueueTime = startTime;
    }

    public String getEndQueueTime(){
        return endQueueTime;
    }

    public void setStartQueueTime(String endTime){
        this.endQueueTime = endTime;
    }
}
