package rapid.vmm.api.util;

public class VMRequest {
    private String architecture;
    private long memory;
    private int cores;
    private long disk;
    private Flavor.FlavorType flavorType;
    private String imagePath;
    private int instances;
    public static final String OCCI_COMPUTE_ARCHITECTURE = "occi.compute.architecture";
    public static final String OCCI_COMPUTE_SPEED = "occi.compute.speed";
    public static final String OCCI_COMPUTE_MEMORY = "occi.compute.memory";
    public static final String OCCI_COMPUTE_CORES = "occi.compute.cores";
    public static final String OCCI_COMPUTE_STATUS = "occi.compute.state";
    public static final String OCCI_COMPUTE_HOSTNAME = "occi.compute.hostname";
    public static final String OPTIMIS_VM_IMAGE = "optimis.occi.optimis_compute.imagePath";
    public static final String OPTIMIS_SERVICE_ID = "optimis.occi.optimis_compute.service_id";

    public String getArchitecture() {
        return this.architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public long getMemory() {
        return this.memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public int getCores() {
        return this.cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public String getImagePath() {
        return this.imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getInstances() {
        return this.instances;
    }

    public void setInstances(int instances) {
        this.instances = instances;
    }

    public String toString() {
        return this.cores + " " + this.memory;
    }

    public static VMRequest valueOf(String value) {
        String[] values = value.split(" ");
        Integer parsedCores = Integer.valueOf(values[0]);
        Long parsedMemory = Long.valueOf(values[1]);
        VMRequest ret = new VMRequest();
        ret.setCores(parsedCores);
        ret.setMemory(parsedMemory);
        return ret;
    }

    public Flavor.FlavorType getFlavorType() {
        return this.flavorType;
    }

    public void setFlavorType(Flavor.FlavorType flavorType) {
        this.flavorType = flavorType;
    }

    public long getDisk() {
        return this.disk;
    }

    public void setDisk(long disk) {
        this.disk = disk;
    }
}
