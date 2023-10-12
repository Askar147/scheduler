package rapid.vmm;

import java.io.Serializable;
import java.util.List;

public class VmmInfo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -1596628414770206114L;
    private long vmmId;
    private List<String> vmImageFreeList;
    private SlamInfo slamInfo;

    /**
     * @return
     */
    public long getVmmId() {
        return vmmId;
    }

    /**
     * @param vmmId
     */
    public void setVmmId(long vmmId) {
        this.vmmId = vmmId;
    }

    /**
     * @return
     */
    public List<String> getVmImageFreeList() {
        return vmImageFreeList;
    }

    /**
     * @param vmImageFreeList
     */
    public void setVmImageFreeList(List<String> vmImageFreeList) {
        this.vmImageFreeList = vmImageFreeList;
    }

    public SlamInfo getSlamInfo() {
        return slamInfo;
    }

    public void setSlamInfo(SlamInfo slamInfo) {
        this.slamInfo = slamInfo;
    }
}
