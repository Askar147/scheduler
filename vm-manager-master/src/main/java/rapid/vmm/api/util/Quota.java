package rapid.vmm.api.util;

public class Quota {
    private Quota.QuotaLocalization localization;
    private double vCpu;
    private double memory;
    private double disk;

    public Quota(Quota.QuotaLocalization localization, double vCpu, double memory, double disk) {
        this.localization = localization;
        this.vCpu = vCpu;
        this.memory = memory;
        this.disk = disk;
    }

    public Quota.QuotaLocalization getLocalization() {
        return this.localization;
    }

    public double getvCpu() {
        return this.vCpu;
    }

    public void setvCpu(double vCpu) {
        this.vCpu = vCpu;
    }

    public double getMemory() {
        return this.memory;
    }

    public void setMemory(double memory) {
        this.memory = memory;
    }

    public double getDisk() {
        return this.disk;
    }

    public void setDisk(double disk) {
        this.disk = disk;
    }

    public void setLocalization(Quota.QuotaLocalization localization) {
        this.localization = localization;
    }

    public Quota clone() {
        return new Quota(this.localization, this.vCpu, this.memory, this.disk);
    }

    public static enum QuotaLocalization {
        LOCAL,
        REMOTE;
    }
}
