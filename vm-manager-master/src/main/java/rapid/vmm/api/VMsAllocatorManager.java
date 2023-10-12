package rapid.vmm.api;

import rapid.vmm.api.util.Flavor;
import rapid.vmm.api.util.VMRequest;
import rapid.vmm.api.docker.DockerCustomClient;
import rapid.vmm.api.libvirt.LibVirtClient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;

public class VMsAllocatorManager {
    private static HashMap internalVMs = null;
    private static HashMap externalVMs = null;
    private static final String TARGET = "/dev/sda";
    private static LibVirtClient libVirtClient = null;
    private static DockerCustomClient dockerCustomClient = null;
    private static Logger log = Logger.getLogger("rapid");


    public static void init() {
        try {
            if (internalVMs == null && externalVMs == null && libVirtClient == null) {
                internalVMs = new HashMap();
                //libVirtClient = new LibVirtClient();
                dockerCustomClient = new DockerCustomClient();
            }
        } catch (Exception var1) {
            log.error("[VMsAllocatorManager] Local hypervisor not available, please check it is running and you have enough permissions. The error is: \n" + var1.getMessage());
        }

    }

    public static String createVM(VMRequest request) throws Exception {
        UUID uuid = UUID.randomUUID();
        int cores = request.getCores();
        long memory = request.getMemory();
        Flavor flavour = new Flavor(request.getFlavorType(), cores, memory, request.getDisk());
        String vmId = "rapid-" + flavour.getType().toString() + "-" + uuid;
//      libVirtClient.createVM(vmId, uuid, flavour, request.getImagePath());
        dockerCustomClient.createVM(vmId, uuid, flavour, request.getImagePath());
        log.info("Vm created vmId: " + vmId + " uuid: " + uuid);
        internalVMs.put(vmId, String.valueOf(uuid));
        return vmId;
    }

    public static boolean resumeVM(String idVM) {
        try {
//         libVirtClient.resumeVM(UUID.fromString(idVM));
            dockerCustomClient.resumeVM(UUID.fromString(idVM));
            log.info("VM's been resumed: " + idVM);
            return true;
        } catch (Exception var2) {
            return false;
        }
    }

    public static boolean suspendVM(String idVM) {
        try {
//         libVirtClient.suspendVM(UUID.fromString(idVM));
            dockerCustomClient.suspendVM(UUID.fromString(idVM));
            log.info("VM's been suspended: " + idVM);
            return true;
        } catch (Exception var2) {
            return false;
        }
    }

    public static String getVmIp(String filePath, String vmName) {
//      return libVirtClient.getVmIp(filePath, vmName);
        return dockerCustomClient.getVmIp(filePath, vmName);

    }

    public static int getVmPort(String vmName) {
        return dockerCustomClient.getVmPort(vmName);
    }

    public static boolean deleteVM(String idVM) {
//      return libVirtClient.deleteVM(UUID.fromString(idVM));
        return dockerCustomClient.deleteVM(UUID.fromString(idVM));
    }

    public static List deleteAllVMs() {
        ArrayList pendingVMs = new ArrayList();
        List ids = new ArrayList(internalVMs.keySet());
        Iterator var3 = ids.iterator();

        String idVM;
        while(var3.hasNext()) {
            idVM = (String)var3.next();
            if (!deleteVM(idVM)) {
                pendingVMs.add(idVM);
            }
        }

        var3 = externalVMs.keySet().iterator();

        while(var3.hasNext()) {
            idVM = (String)var3.next();
            String id = (String)externalVMs.get(idVM);
            if (!deleteVM(id)) {
                pendingVMs.add(id);
            }
        }

        if (pendingVMs.isEmpty()) {
            log.info("All virtual machines successfully deleted...");
        } else {
            log.info("Unable to delete all virtual machines. Pending virtual machines: " + pendingVMs.toString());
        }

        return pendingVMs;
    }

    private static class FlavorComp implements Comparator<Flavor> {
        private boolean compareByCPU;

        public FlavorComp(boolean compareByCPU) {
            this.compareByCPU = compareByCPU;
        }

        public int compare(Flavor o1, Flavor o2) {
            return this.compareByCPU ? (new Integer(o1.getvCpu())).compareTo(o2.getvCpu()) : (new Long(o1.getMemory())).compareTo(o2.getMemory());
        }
    }
}
