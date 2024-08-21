package rapid;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class DSManager {
    private static SqlSessionFactory sqlMapper;
    static {
        try {
            Reader reader = Resources.getResourceAsReader("myBatisConfig.xml");
            sqlMapper = new SqlSessionFactoryBuilder().build(reader);
        } catch (IOException err) {
            throw new RuntimeException("Fail to create an SQL Session Factory instance " + err, err);
        }
    }

    /**
     * This function returns the VMM information list.
     *
     * @return VMM information list.
     */
    public static List<VmmInfo> vmmInfoList() {
        List<VmmInfo> list = null;
        SqlSession session = sqlMapper.openSession();
        list = session.selectList("DS.vmmInfoList");
        session.close();
        return list;
    }

    /**
     * This function returns the VMM information list ordered by the CPU
     * utilization.
     *
     * @return VMM information list.
     */
    public static List<VmmInfo> vmmInfoListByLowUtil() {
        List<VmmInfo> list = null;
        SqlSession session = sqlMapper.openSession();
        list = session.selectList("DS.vmmInfoListByLowUtil");
        session.close();
        return list;
    }

    public static List<VmmInfo> vmmInfoListByHighAllocatedCpu() {
        List<VmmInfo> list = null;
        SqlSession session = sqlMapper.openSession();
        list = session.selectList("DS.vmmInfoListByHighAllocatedCpu");
        session.close();
        return list;
    }

    /**
     * This function returns the VMM information structure.
     *
     * @param vmmid
     *            VMM ID.
     * @return VMM information structure.
     */
    public static VmmInfo getVmmInfo(long vmmid) {
        VmmInfo vmmInfo = null;
        SqlSession session = sqlMapper.openSession();
        vmmInfo = session.selectOne("DS.getVmmInfo", vmmid);
        session.close();
        return vmmInfo;
    }

    /**
     * This function returns the VMM information structure based on the IP
     * address.
     *
     * @param ipv4
     *            IP address of the VMM.
     * @return VMM information structure.
     */
    public static VmmInfo getVmmInfoByIp(String ipv4) {
        VmmInfo vmmInfo = null;
        SqlSession session = sqlMapper.openSession();
        vmmInfo = session.selectOne("DS.getVmmInfoByIp", ipv4);
        session.close();
        return vmmInfo;
    }

    /**
     * This function inserts the VMM information structure.
     *
     * @param vmmInfo
     *            VMM information structure.
     * @return VMM ID.
     */
    public static long insertVmmInfo(VmmInfo vmmInfo) {
        SqlSession session = sqlMapper.openSession();
        session.insert("DS.insertVmmInfo", vmmInfo);
        session.commit();
        session.close();

        return vmmInfo.getVmmid();
    }

    /**
     * This function updates the VMM information structure.
     *
     * @param vmmInfo
     *            VMM information structure.
     * @return result
     */
    public static int updateVmmInfo(VmmInfo vmmInfo) {
        SqlSession session = sqlMapper.openSession();
        int result = session.update("DS.updateVmmInfo", vmmInfo);
        session.commit();
        session.close();
        return result;
    }

    public static int changeAllocatedcpuVmmInfo(VmmInfo vmmInfo) {
        SqlSession session = sqlMapper.openSession();
        int result = session.update("DS.changeAllocatedcpuVmmInfo", vmmInfo);
        session.commit();
        session.close();
        return result;
    }

    public static int insertVmmStats(VmmStats vmmStats) {
        SqlSession session = sqlMapper.openSession();
        int result = session.update("DS.insertVmmStats", vmmStats);
        session.commit();
        session.close();
        return result;
    }

    public static int insertOffloadHistory(OffloadHistory offloadHistory) {
        SqlSession session = sqlMapper.openSession();
        int result = session.update("DS.insertOffloadHistory", offloadHistory);
        session.commit();
        session.close();
        return result;
    }

    public static int updateOffloadHistory(OffloadHistory offloadHistory) {
        SqlSession session = sqlMapper.openSession();
        int result = session.update("DS.updateOffloadHistory", offloadHistory);
        session.commit();
        session.close();
        return result;
    }

    public static OffloadHistory getOffloadHistoryByuservmmid(long vmmid, long userid) {
        OffloadHistory offloadHistory = new OffloadHistory();
        offloadHistory.setVmmid(vmmid);
        offloadHistory.setUserid(userid);
        SqlSession session = sqlMapper.openSession();
        OffloadHistory offloadHistory1 = session.selectOne("DS.getOffloadHistoryByuservmmid", offloadHistory);
        session.close();
        return offloadHistory1;
    }

    public static long insertRequestInfo(RequestInfo requestInfo) {
        SqlSession session = sqlMapper.openSession();
        int result = session.insert("DS.insertRequestInfo", requestInfo);
        session.commit();
        session.close();
        return requestInfo.getRequestid();
    }

    public static int updateRequestInfo(RequestInfo requestInfo){
        SqlSession session = sqlMapper.openSession();
        int result = session.update("DS.updateRequestInfo", requestInfo);
        session.commit();
        session.close();
        return result;
    }

    public static RequestInfo getRequestInfo(long requestid) {
        SqlSession session = sqlMapper.openSession();
        try {
            return session.selectOne("DS.getRequestInfo", requestid);
        } finally {
            session.close();
        }
    }

    public statis List<RequestInfo> getAllRequestInfos(){
        SqlSession session = sqlMapper.openSession();
        try {
            return session.selectList("DS.getAllRequestInfos");
        } finally {
            session.close();
        }
    }

    public static int insertWolHistory(WolHistory wolHistory) {
        SqlSession session = sqlMapper.openSession();
        int result = session.update("DS.insertWolHistory", wolHistory);
        session.commit();
        session.close();
        return result;
    }

    public static int wolLast6Sec(VmmInfo vmmInfo) {
        SqlSession session = sqlMapper.openSession();
        int result = session.selectOne("DS.wolLast6Sec", vmmInfo);
        session.commit();
        session.close();
        return result;
    }

    public static int insertGlobalReading(GlobalReadings globalReadings) {
        SqlSession session = sqlMapper.openSession();
        session.update("DS.insertGlobalReading", globalReadings);
        session.commit();
        session.close();
        return 1;
    }

    public static float predictWorkload() {
        SqlSession session = sqlMapper.openSession();
        float result = session.selectOne("DS.predictWorkload");
        session.commit();
        session.close();
        return result;
    }

    /**
     * This function deletes the VMM information structure.
     *
     * @param vmmid
     *            VMM ID.
     * @return result.
     */
    public static int deleteVmmInfo(long vmmid) {
        SqlSession session = sqlMapper.openSession();
        int result = session.delete("DS.deleteVmmInfo", vmmid);
        session.commit();
        session.close();
        return result;
    }

    /**
     * This function returns the user information list.
     *
     * @return User information list.
     */
    public static List<UserInfo> userInfoList() {
        List<UserInfo> list = null;
        SqlSession session = sqlMapper.openSession();
        list = session.selectList("DS.userInfoList");
        session.close();
        return list;
    }

    /**
     * This function returns the user information structure.
     *
     * @param userid
     *            User ID.
     * @return User information structure.
     */
    public static UserInfo getUserInfo(long userid) {
        UserInfo userInfo = null;
        SqlSession session = sqlMapper.openSession();
        userInfo = session.selectOne("DS.getUserInfo", userid);
        session.close();
        return userInfo;
    }

    /**
     * This function inserts the user information structure.
     *
     * @param userInfo
     *            User information structure.
     * @return User ID.
     */
    public static long insertUserInfo(UserInfo userInfo) {
        SqlSession session = sqlMapper.openSession();
        session.insert("DS.insertUserInfo", userInfo);
        session.commit();
        session.close();

        return userInfo.getUserid();
    }

    /**
     * This function updates the user information structure.
     *
     * @param userInfo
     *            User information structure.
     * @return result.
     */
    public static int updateUserInfo(UserInfo userInfo) {
        SqlSession session = sqlMapper.openSession();
        int result = session.update("DS.updateUserInfo", userInfo);
        session.commit();
        session.close();
        return result;
    }

    /**
     * This function deletes the user information structure.
     *
     * @param userid
     *            User ID.
     * @return result.
     */
    public static int deleteUserInfo(long userid) {
        SqlSession session = sqlMapper.openSession();
        int result = session.delete("DS.deleteUserInfo", userid);
        session.commit();
        session.close();
        return result;
    }

    /**
     * This function returns the VM information list.
     *
     * @return VM information list.
     */
    public static List<VmInfo> vmInfoList() {
        List<VmInfo> list = null;
        SqlSession session = sqlMapper.openSession();
        list = session.selectList("DS.vmInfoList");
        session.close();
        return list;
    }

    /**
     * This function returns the helper VM information list.
     *
     * @param vmmid
     *            VMM ID.
     * @return VM information list.
     */
    public static List<VmInfo> helperVmInfoListByVmmid(long vmmid) {
        List<VmInfo> list = null;
        SqlSession session = sqlMapper.openSession();
        list = session.selectList("DS.helperVmInfoListByVmmid", vmmid);
        session.close();
        return list;
    }

    /**
     * This function returns the VM information structure.
     *
     * @param vmid
     *            VM ID.
     * @return VM information structure.
     */
    public static VmInfo getVmInfo(long vmid) {
        VmInfo vmInfo = null;
        SqlSession session = sqlMapper.openSession();
        vmInfo = session.selectOne("DS.getVmInfo", vmid);
        session.close();
        return vmInfo;
    }

    /**
     * This function returns the VM information structure searched by the user
     * ID.
     *
     * @param userid
     *            User ID.
     * @return VM information structure.
     */
    public static VmInfo getVmInfoByUserid(long userid) {
        VmInfo vmInfo = null;
        SqlSession session = sqlMapper.openSession();
        vmInfo = session.selectOne("DS.getVmInfoByuserid", userid);
        session.close();
        return vmInfo;
    }

    /**
     * This function inserts the VM information structure.
     *
     * @param vmInfo
     *            VM information structure.
     * @return VM ID.
     */
    public static long insertVmInfo(VmInfo vmInfo) {
        SqlSession session = sqlMapper.openSession();
        session.insert("DS.insertVmInfo", vmInfo);
        session.commit();
        session.close();

        return vmInfo.getVmid();
    }

    /**
     * This function updates the VM information structure.
     *
     * @param vmInfo
     *            VM information structure.
     * @return result.
     */
    public static int updateVmInfo(VmInfo vmInfo) {
        SqlSession session = sqlMapper.openSession();
        int result = session.update("DS.updateVmInfo", vmInfo);
        session.commit();
        session.close();
        return result;
    }

    /**
     * This function deletes the VM information structure.
     *
     * @param vmid
     *            VM ID.
     * @return result.
     */
    public static int deleteVmInfo(long vmid) {
        SqlSession session = sqlMapper.openSession();
        int result = session.delete("DS.deleteVmInfo", vmid);
        session.commit();
        session.close();
        return result;
    }

    /**
     * This function returns the slam information structure.
     *
     * @return Slam information structure.
     */
    public static SlamInfo getSlamInfo() {
        SlamInfo slamInfo = null;
        SqlSession session = sqlMapper.openSession();
        slamInfo = session.selectOne("DS.getSlamInfo");
        session.close();
        return slamInfo;
    }

    /**
     * This function inserts the slam information structure.
     *
     * @param slamInfo
     *            Slam information structure.
     */
    public static void insertSlamInfo(SlamInfo slamInfo) {
        SqlSession session = sqlMapper.openSession();
        session.insert("DS.insertSlamInfo", slamInfo);
        session.commit();
        session.close();
    }

    /**
     * This function updates the slam information structure.
     *
     * @param slamInfo
     *            Slam information structure.
     * @return result.
     */
    public static int updateSlamInfo(SlamInfo slamInfo) {
        SqlSession session = sqlMapper.openSession();
        int result = session.update("DS.updateSlamInfo", slamInfo);
        session.commit();
        session.close();
        return result;
    }

    /**
     * This function deletes the slam information structure.
     *
     * @return result.
     */
    public static int deleteSlamInfo() {
        SqlSession session = sqlMapper.openSession();
        int result = session.delete("DS.deleteSlamInfo");
        session.commit();
        session.close();
        return result;
    }
}
