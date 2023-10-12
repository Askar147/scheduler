package rapid.vmm.api;

import rapid.vmm.api.util.Flavor;
import rapid.vmm.api.util.VMRequest;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class VMManagerApiImpl implements VMManagerApi {
    private Logger logger = Logger.getLogger("rapid");

    public VMManagerApiImpl() {
        this.logger.info("[RapidVMManagerImpl] revoked");
        VMsAllocatorManager.init();
    }

    public String createVM(VMRequest request) throws Exception {
        this.logger.info("[RapidVMManagerImpl] Create VM method invoked!");
        return VMsAllocatorManager.createVM(request);
    }

    public boolean deleteVM(String idVM) {
        this.logger.info("[RapidVMManagerImpl] Delete VM method invoked!");
        return VMsAllocatorManager.deleteVM(idVM);
    }

    public boolean resumeVM(String idVM) {
        this.logger.info("[RapidVMManagerImpl] Resume VM method invoked!");
        return VMsAllocatorManager.resumeVM(idVM);
    }

    public String getVmIp(String filePath, String vmName) {
        this.logger.info("[RapidVMManagerImpl] getVmIp method invoked!");
        return VMsAllocatorManager.getVmIp(filePath, vmName);
    }

    public int getVmPort(String vmName) {
        this.logger.info("[RapidVMManagerImpl] getVmPort method invoked!");
        return VMsAllocatorManager.getVmPort(vmName);
    }

    public boolean suspendVM(String idVM) {
        this.logger.info("[RapidVMManagerImpl] Suspend VM method invoked!");
        return VMsAllocatorManager.suspendVM(idVM);
    }

    public List getFlavors() {
        this.logger.info("[RapidVMManagerImpl] Get flavors method invoked!");
        ArrayList flavors = new ArrayList();
        Flavor.FlavorType[] var2 = Flavor.FlavorType.values();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Flavor.FlavorType flavor = var2[var4];
            flavors.add(VMManagerConfiguration.getFlavor(flavor));
        }

        return flavors;
    }

}
