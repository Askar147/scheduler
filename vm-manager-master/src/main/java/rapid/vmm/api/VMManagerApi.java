package rapid.vmm.api;

import rapid.vmm.api.util.VMRequest;
import java.util.List;

public interface VMManagerApi {
    List getFlavors();

    String createVM(VMRequest var1) throws Exception;

    boolean deleteVM(String var1);

    boolean resumeVM(String var1);

    String getVmIp(String var1, String var2);

    int getVmPort(String var1);

    boolean suspendVM(String var1);

}
