package rapid;

import eu.project.rapid.common.RapidMessages;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Random;
import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DSEngine {
    private static DSEngine dsEngine = new DSEngine();
    private Logger logger = Logger.getLogger(getClass());

    private final static Map<Long, Float> allocatedCpu = Collections.synchronizedMap(new HashMap<Long, Float>());

//    private final HashMap<Long, Float> lastAllocatedCpu = new HashMap<Long, Float>();
    private static BlockingQueue<RequestInfo> requestQueue = new LinkedBlockingQueue<>();

    private DSEngine() { }

    public static DSEngine getInstance() {
        return dsEngine;
    }

    /**
     * The function deals with the VMM_REGISTER_DS message. It registers the VMM
     * to the DS.
     *
     * @param in
     *            ObjectInputStream instance retrieved by the socket.
     * @param out
     *            ObjectOutputStream instance retrieved by the socket.
     */
    public void vmmRegisterDs(ObjectInputStream in, ObjectOutputStream out) {
        try {

            MainScheduler scheduler = MainScheduler.getInstance();
//            SlamInfo slamInfo = scheduler.getSlamInfo();
//
//            if (slamInfo == null) {
//                out.writeByte(RapidMessages.ERROR); // errorCode
//                out.flush();
//                return;
//            }

            String ipv4 = in.readUTF();
            int mactype = in.readInt();
            String macAddress = in.readUTF();
            int cpuload = in.readInt();
            int allocatedcpu = in.readInt();
            int cpufrequency = in.readInt();
            int cpunums = in.readInt();
            long freemem = in.readLong();
            long availmem = in.readLong();
            int powerusage = in.readInt();
            int freegpu = in.readInt();
            int gpunums = in.readInt();
            String availtypes = in.readUTF();

            VmmInfo vmmInfo = DSManager.getVmmInfoByIp(ipv4);

            if (vmmInfo != null) {
                out.writeByte(RapidMessages.ERROR); // errorCode
                out.flush();
                return;
            }

            vmmInfo = new VmmInfo();

            vmmInfo.setIpv4(ipv4);
            vmmInfo.setMactype(mactype);
            vmmInfo.setMacaddress(macAddress);

//            if (mactype == VmmInfo.JETSON_NANO_WOL || mactype == VmmInfo.JETSON_NANO_WOL_NEWJETPACK
//            || mactype == VmmInfo.XAVIER) {
//                vmmInfo.setSuspended(1);
//            }

            vmmInfo.setCpuload(cpuload);
            vmmInfo.setAllocatedcpu(allocatedcpu);
            vmmInfo.setCpufrequency(cpufrequency);
            vmmInfo.setCpunums(cpunums);
            vmmInfo.setFreemem(freemem);
            vmmInfo.setAvailmem(availmem);
            vmmInfo.setPowerusage(powerusage);
            vmmInfo.setFreegpu(freegpu);
            vmmInfo.setGpunums(gpunums);
            vmmInfo.setAvailtypes(availtypes);

            long vmmId = DSManager.insertVmmInfo(vmmInfo);

            allocatedCpu.put(vmmId, (float) 0.0);

            logger.info(
                    "VMM_REGISTER_DS, returned SLAM IP: " + scheduler.getIpv4() + " SLAM Port: " + scheduler.getPort());

            out.writeByte(RapidMessages.OK); // errorCode
            out.writeLong(vmmId); // vmmId
//            out.writeUTF(scheduler.getSlamInfo().getIpv4());
            out.writeUTF(scheduler.getIpv4());
//            out.writeInt(scheduler.getSlamInfo().getPort());
            out.writeInt(scheduler.getPort());
            out.flush();

        } catch (Exception e) {
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            logger.error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            e.printStackTrace();
        }
    }

    /**
     * The function deals with the VMM_NOTIFY_DS message. It notifies the DS
     * about free resource information.
     *
     * @param in
     *            ObjectInputStream instance retrieved by the socket.
     * @param out
     *            ObjectOutputStream instance retrieved by the socket.
     */
    public void vmmNotifyDs(ObjectInputStream in, ObjectOutputStream out) {
        try {
            long vmmid = in.readLong();
            VmmInfo vmmInfo = DSManager.getVmmInfo(vmmid);
            VmmStats vmmStats = new VmmStats();
            logger.info("VMM_NOTIFY_DS VMM_ID = " + vmmid);
            if (vmmInfo != null) {
                int cpuload = in.readInt();
                vmmInfo.setCpuload(cpuload);
                int allocatedcpu = in.readInt();
//                vmmInfo.setAllocatedcpu(allocatedcpu);
                long freemem = in.readLong();
                vmmInfo.setFreemem(freemem);
                long availmem = in.readLong();
                vmmInfo.setAvailmem(availmem);
                int powerusage = in.readInt();
                vmmInfo.setPowerusage(powerusage);
                vmmInfo.setFreegpu(in.readInt());

                if (vmmInfo.getSuspended() == 1 && DSManager.wolLast6Sec(vmmInfo) == 0) {
                    vmmInfo.setSuspended(0);

                    WolHistory wolHistory = new WolHistory();
                    wolHistory.setVmmid(vmmInfo.getVmmid());
                    wolHistory.setIswol(1);
                    DSManager.insertWolHistory(wolHistory);
                }

                DSManager.updateVmmInfo(vmmInfo);

                vmmStats.setVmmid(vmmid);
                vmmStats.setCpuload(cpuload);
                vmmStats.setAllocatedcpu(vmmInfo.getAllocatedcpu());
//                vmmStats.setAllocatedcpu(allocatedcpu);
                vmmStats.setFreemem(freemem);
                vmmStats.setAvailmem(availmem);
                vmmStats.setPowerusage(powerusage);

                DSManager.insertVmmStats(vmmStats);
            }

        } catch (Exception e) {
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            logger.error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            e.printStackTrace();
        }
    }

    private synchronized void incrementAllocatedcpu(VmmConfig vmmConfig) {
        VmmInfo vmmInfo = DSManager.getVmmInfoByIp(vmmConfig.getVmmIP());
        float allocatedcpuDelta = (float) vmmConfig.getVcpu() / vmmInfo.getCpunums();
        allocatedCpu.put(vmmInfo.getVmmid(), allocatedCpu.get(vmmInfo.getVmmid()) + allocatedcpuDelta);
//        VmmInfo vmmInfoDelta = new VmmInfo();
//        vmmInfoDelta.setVmmid(vmmInfo.getVmmid());
//        vmmInfoDelta.setAllocatedcpu(allocatedcpuDelta);
//        DSManager.changeAllocatedcpuVmmInfo(vmmInfoDelta);
        vmmInfo.setAllocatedcpu(allocatedCpu.get(vmmInfo.getVmmid()));
//            lastAllocatedCpu.put(userid, (float) vmmConfig.getVcpu() / vmmInfo.getCpunums());
        DSManager.updateVmmInfo(vmmInfo);
    }

    private synchronized void decrementAllocatedcpu(String vmmIp, long userID) {
        VmmInfo vmmInfo = DSManager.getVmmInfoByIp(vmmIp);
        UserInfo userInfo = DSManager.getUserInfo(userID);
        float allocatedcpuDelta = - (float) userInfo.getVcpu() / vmmInfo.getCpunums();
        allocatedCpu.put(vmmInfo.getVmmid(), allocatedCpu.get(vmmInfo.getVmmid()) + allocatedcpuDelta);
        vmmInfo.setAllocatedcpu(allocatedCpu.get(vmmInfo.getVmmid()));
        DSManager.updateVmmInfo(vmmInfo);
    }

    /**
     * The function deals with the AC_REGISTER_NEW_DS message. It receives the
     * message from a new AC and sends a proper VMM IP list.
     *
     * @param in
     *            ObjectInputStream instance retrieved by the socket.
     * @param out
     *            ObjectOutputStream instance retrieved by the socket.
     * @param socket
     */
    public void acRegisterNewDs(ObjectInputStream in, ObjectOutputStream out, Socket socket) {
        try {
            long userid = in.readLong();
            int vcpuNum = in.readInt();
            int memSize = in.readInt();
            int gpuCores = in.readInt();
            String deadline = in.readUTF();
            long cycles = in.readLong();
	    String clientIp = socket.getInetAddress().getHostAddress();
	    int clientPort = socket.getPort();

            logger.info("AC_REGISTER_NEW_DS, userId: " + userid + " vcpuNum: " + vcpuNum + " memSize: " + memSize
                    + " gpuCores: " + gpuCores + " deadline: " + deadline + " cycles: " + cycles);


            RequestInfo requestInfo = new RequestInfo();
            requestInfo.setAccepted(0);
            requestInfo.setVcpu(vcpuNum);
            requestInfo.setMemory(memSize);
            requestInfo.setCycles(cycles);
            requestInfo.setUserid(userid);
            requestInfo.setDeadline(deadline);
	    requestInfo.setClientIp(clientIp);
            requestInfo.setClientPort(clientPort);
	    requestInfo.setQueueStartTime(new Timestamp(System.currentTimeMillis()).toString());
	    requestInfo.setStatus("PENDING");
	
            DSManager.insertRequestInfo(requestInfo);

	    logger.info("Beofore Calling THE METHOD FINDVAILMAHCINES");
            VmmConfig vmmConfig = dsEngine.findAvailMachines(userid, vcpuNum, memSize, gpuCores, deadline, cycles);

            ArrayList<String> ipList = new ArrayList<>();
            if (vmmConfig != null) {
                ipList.add(vmmConfig.getVmmIP());
                incrementAllocatedcpu(vmmConfig);
            }else {
	    
		// Queue the request
		 requestQueue.add(requestInfo);
		 out.writeInt(RapidMessages.PING);
	    }
            

            if (userid > 0) {
                VmInfo vmInfo = DSManager.getVmInfoByUserid(userid);

                UserInfo userInfo = DSManager.getUserInfo(userid);
                userInfo.setDeadline(deadline);
                userInfo.setCycles(cycles);
                userInfo.setVcpu(vmmConfig.getVcpu());
                userInfo.setMemory(vmmConfig.getMemory());
                DSManager.updateUserInfo(userInfo);

                if (vmInfo != null && vmInfo.getOffloadstatus() != VmInfo.OFFLOAD_DEREGISTERED) {
                    vmInfo.setOffloadstatus(VmInfo.OFFLOAD_DEREGISTERED);
                    vmInfo.setVmstatus(VmInfo.VM_STOPPED);
                    DSManager.updateVmInfo(vmInfo);

                    // need to send DEREGISTERED message to VMM or AS.
                    MainScheduler scheduler = MainScheduler.getInstance();
                    VmmInfo vmmInfo = DSManager.getVmmInfo(vmInfo.getVmmid());

                    Socket vmmSocket = new Socket(vmmInfo.getIpv4(), scheduler.getVmmPort());
                    ObjectOutputStream vmmOut = new ObjectOutputStream(vmmSocket.getOutputStream());
                    vmmOut.flush();

                    vmmOut.writeByte(RapidMessages.DS_VM_DEREGISTER_VMM);

                    vmmOut.writeLong(userid); // userid
                    vmmOut.flush();

                    vmmOut.close();
                    vmmSocket.close();
                }
            }

            // find available machines
            DSEngine dsEngine = DSEngine.getInstance();

            MainScheduler scheduler = MainScheduler.getInstance();

            if (ipList.size() == 0) {
                out.writeByte(RapidMessages.ERROR);
                out.flush();
                return;
            }

            long newUserid = userid;

            if (userid < 0) {
                UserInfo userInfo = new UserInfo();

                InetSocketAddress addr = (InetSocketAddress) socket.getRemoteSocketAddress();
                userInfo.setIpv4(getIpAddress(addr.getAddress().getAddress()));
                userInfo.setDeadline(deadline);
                userInfo.setCycles(cycles);
                userInfo.setVcpu(vmmConfig.getVcpu());
                userInfo.setMemory(vmmConfig.getMemory());

                newUserid = DSManager.insertUserInfo(userInfo);
            }

            // OK <ip List>

            out.writeByte(RapidMessages.OK);
            out.writeLong(newUserid);
            out.writeObject(ipList);
//            out.writeUTF(scheduler.getSlamInfo().getIpv4());
            out.writeUTF(scheduler.getIpv4());
//            out.writeInt(scheduler.getSlamInfo().getPort());
            out.writeInt(scheduler.getPort());
            out.flush();

        } catch (Exception e) {
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            logger.error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            e.printStackTrace();
        }
    }


private void processQueue() {
    while (true) {
        RequestInfo requestInfo = requestQueue.poll();
        if (requestInfo != null) {
            VmmConfig vmmConfig = findAvailMachines(
                requestInfo.getUserid(),
                requestInfo.getVcpu(),
                (int) requestInfo.getMemory(),
                0, // Assuming gpuCores is 0
                requestInfo.getDeadline(),
                requestInfo.getCycles()
            );
            if (vmmConfig != null) {
                DSManager.updateRequestInfoWithVmm(requestInfo.getRequestid(), vmmConfig);
                notifyClient(requestInfo, vmmConfig);
            } else {
                // Requeue the request if no available machines found
                requestQueue.add(requestInfo);
            }
        }
        // Sleep or wait before processing the next item
        try {
            Thread.sleep(1000); // Adjust as needed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

private void notifyClient(RequestInfo requestInfo, VmmConfig vmmConfig) {
    try (Socket clientSocket = new Socket(requestInfo.getClientIp(), requestInfo.getClientPort());
         ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
        out.writeInt(RapidMessages.OK);
        out.writeLong(requestInfo.getRequestid());
        out.writeUTF(vmmConfig.getVmmIP());
        out.writeInt(vmmConfig.getVmmPort());
        out.flush();
    } catch (IOException e) {
        e.printStackTrace();
    }
}



    /**
     * The function deals with the AC_REGISTER_PREV_DS message. It receives the
     * message from a existing AC and sends its VMM IP address.
     *
     * @param in
     *            ObjectInputStream instance retrieved by the socket.
     * @param out
     *            ObjectOutputStream instance retrieved by the socket.
     * @param socket
     */
    public void acRegisterPrevDs(ObjectInputStream in, ObjectOutputStream out, Socket socket) {
        try {
            long userid = in.readLong();
            String deadline = in.readUTF();
            long cycles = in.readLong();
//            String deadline = "2022-08-05 18:19:03";
//            int cycles=1;

            /*
             * int qosFlag = in.readInt(); String qosParam = ""; if (qosFlag ==
             * 1) qosParam = in.readUTF();
             */

            logger.info("AC_REGISTER_PREV_DS, userId: " + userid);

            UserInfo userInfo = null;

            if ((userInfo = DSManager.getUserInfo(userid)) == null) {
                out.writeByte(RapidMessages.ERROR);
                out.flush();
                return;
            }

            userInfo.setDeadline(deadline);
            userInfo.setCycles(cycles);

            VmInfo vmInfo = DSManager.getVmInfoByUserid(userid);

            if (vmInfo == null || vmInfo.getOffloadstatus() == VmInfo.OFFLOAD_DEREGISTERED) {
                out.writeByte(RapidMessages.ERROR);
                out.flush();
                return;
            }

            // Find the previous machine
            MainScheduler scheduler = MainScheduler.getInstance();
            // RapidUtils.sendAnimationMsg(scheduler.getAnimationAddress(),
            // scheduler.getAnimationServerPort(),
            // RapidMessages.AnimationMsg.DS_PREV_FIND_MACHINE.toString());

            VmmInfo vmmInfo = DSManager.getVmmInfo(vmInfo.getVmmid());

            if (vmmInfo == null) {
                out.writeByte(RapidMessages.ERROR);
                out.flush();
                return;
            }

            // QoS metric check is required later. TBD.
            InetSocketAddress addr = (InetSocketAddress) socket.getRemoteSocketAddress();
            userInfo.setIpv4(getIpAddress(addr.getAddress().getAddress()));

            /*
             * if (qosFlag == 1) userInfo.setQosparam(qosParam);
             */

            DSManager.updateUserInfo(userInfo);

            // OK <IP>
            // RapidUtils.sendAnimationMsg(scheduler.getAnimationAddress(),
            // scheduler.getAnimationServerPort(),
            // RapidMessages.AnimationMsg.DS_PREV_IP_AC.toString());

            ArrayList<String> ipList = new ArrayList<String>();
            ipList.add(vmmInfo.getIpv4());

            out.writeByte(RapidMessages.OK);
            out.writeLong(userid);
            out.writeObject(ipList);
//            out.writeUTF(scheduler.getSlamInfo().getIpv4());
            addr = (InetSocketAddress) socket.getLocalSocketAddress();
            out.writeUTF(getIpAddress(addr.getAddress().getAddress()));
//            out.writeInt(scheduler.getSlamInfo().getPort());
            out.writeInt(scheduler.getPort());
            out.flush();

        } catch (Exception e) {
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            logger.error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            e.printStackTrace();
        }
    }

    /**
     * The function deals with the VM_REGISTER_DS message. It creates a new VM
     * information structure.
     *
     * @param in
     *            ObjectInputStream instance retrieved by the socket.
     * @param out
     *            ObjectOutputStream instance retrieved by the socket.
     */
    public void vmRegisterDs(ObjectInputStream in, ObjectOutputStream out) {
        try {
            long vmmid = in.readLong();
            int category = in.readInt();
            int type = in.readInt();
            long userid = in.readLong();
            int vmstatus = in.readInt();
            int vcpu = in.readInt();
            long memory = in.readInt();

            String ipv4 = in.readUTF();
            int port = in.readInt();

            logger.info("VM_REGISTER_DS, vmmid: " + vmmid + " type: " + type + " vmstatus: " +
                    VmInfo.vmStatus.get(vmstatus));

            VmInfo vmInfo = new VmInfo();

            vmInfo.setVmmid(vmmid);
            vmInfo.setCategory(category);
            vmInfo.setType(type);
            vmInfo.setUserid(userid);
            vmInfo.setVmstatus(vmstatus);
            vmInfo.setVcpu(vcpu);
            vmInfo.setMemory(memory);

            vmInfo.setIpv4(ipv4);
            vmInfo.setPort(port);

            long newVmid = DSManager.insertVmInfo(vmInfo);

            out.writeLong(newVmid);
            out.flush();
        } catch (Exception e) {
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            logger.error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            e.printStackTrace();
        }
    }

    /**
     * The function deals with the VM_NOTIFY_DS message. It updates an existing
     * VM information structure.
     *
     * @param in
     *            ObjectInputStream instance retrieved by the socket.
     * @param out
     *            ObjectOutputStream instance retrieved by the socket.
     */
    public void vmNotifyDs(ObjectInputStream in, ObjectOutputStream out) {
        try {
            long vmid = in.readLong();
            int vmstatus = in.readInt();
            int offloadstatus = in.readInt();

            VmInfo vmInfo = DSManager.getVmInfo(vmid);

            logger.info("VM_NOTIFY_DS, vmid: " + vmid + ", vmstatus:" + VmInfo.vmStatus.get(vmstatus) + "offloadstatus:" + VmInfo.offloadstatusMap.get(offloadstatus));

            if (vmInfo != null) {
                vmInfo.setVmstatus(vmstatus);
                vmInfo.setOffloadstatus(offloadstatus);
                DSManager.updateVmInfo(vmInfo);

                if (offloadstatus == VmInfo.OFFLOAD_DEREGISTERED) {
                    OffloadHistory offloadHistory = DSManager.getOffloadHistoryByuservmmid(vmInfo.getVmmid(), vmInfo.getUserid());
                    DSManager.updateOffloadHistory(offloadHistory);
                    decrementAllocatedcpu(vmInfo.getIpv4(), vmInfo.getUserid());
//                    VmmInfo vmmInfo = DSManager.getVmmInfo(vmInfo.getVmmid());
//                    UserInfo userInfo = DSManager.getUserInfo(vmInfo.getUserid());
//                    float allocatedcpuDelta = - (float) userInfo.getVcpu() / vmmInfo.getCpunums();
////                    VmmInfo vmmInfoDelta = new VmmInfo();
////                    vmmInfoDelta.setVmmid(vmmInfo.getVmmid());
////                    vmmInfoDelta.setAllocatedcpu(allocatedcpuDelta);
////                    DSManager.changeAllocatedcpuVmmInfo(vmmInfoDelta);
//////                    vmmInfo.setAllocatedcpu(vmmInfo.getAllocatedcpu() - (float) userInfo.getVcpu() / vmmInfo.getCpunums());
//////                    DSManager.updateVmmInfo(vmmInfo);
//
//                    allocatedCpu.put(vmmInfo.getVmmid(), allocatedCpu.get(vmmInfo.getVmmid()) + allocatedcpuDelta);
////        VmmInfo vmmInfoDelta = new VmmInfo();
////        vmmInfoDelta.setVmmid(vmmInfo.getVmmid());
////        vmmInfoDelta.setAllocatedcpu(allocatedcpuDelta);
////        DSManager.changeAllocatedcpuVmmInfo(vmmInfoDelta);
//                    vmmInfo.setAllocatedcpu(allocatedCpu.get(vmmInfo.getVmmid()));
////            lastAllocatedCpu.put(userid, (float) vmmConfig.getVcpu() / vmmInfo.getCpunums());
//                    DSManager.updateVmmInfo(vmmInfo);
                }
            }
        } catch (Exception e) {
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            logger.error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            e.printStackTrace();
        }
    }

    /**
     * The function deals with the HELPER_NOTIFY_DS message. It receives the
     * message from the VMM when helper VMs launch.
     *
     * @param in
     *            ObjectInputStream instance retrieved by the socket.
     * @param out
     *            ObjectOutputStream instance retrieved by the socket.
     */
    public void helperNotifyDs(ObjectInputStream in, ObjectOutputStream out) {
        try {
            long vmid = in.readLong();
            String ipv4 = in.readUTF();

            VmInfo vmInfo = DSManager.getVmInfo(vmid);

            if (vmInfo != null) {
                vmInfo.setIpv4(ipv4);
                logger.info("helperNotifyDs: ipAddress:" + ipv4);
                DSManager.updateVmInfo(vmInfo);
            }
        } catch (Exception e) {
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            logger.error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            e.printStackTrace();
        }
    }

    /**
     * The function deals with the AS_RM_REGISTER_DS message. It receives the
     * message from the AS when the AS starts.
     *
     * @param in
     *            ObjectInputStream instance retrieved by the socket.
     * @param out
     *            ObjectOutputStream instance retrieved by the socket.
     * @param socket
     */
    public void asRmRegisterDs(ObjectInputStream in, ObjectOutputStream out, Socket socket) {
        try {
            long userid = in.readLong();

            VmInfo vmInfo = DSManager.getVmInfoByUserid(userid);

            logger.info("AS_RM_REGISTER_DS, userid: " + userid);

            if (vmInfo == null) {
                out.writeByte(RapidMessages.ERROR);
                out.flush();
                return;
            }

            logger.info("AS_RM_REGISTER_DS, VM found: vmid: " + vmInfo.getVmid());

            InetSocketAddress addr = (InetSocketAddress) socket.getRemoteSocketAddress();
            // logger.info("asRmRegisterDs: ipAddress:" +
            // addr.getAddress().getAddress());
            // vmInfo.setIpv4(getIpAddress(addr.getAddress().getAddress()));
            vmInfo.setOffloadstatus(VmInfo.OFFLOAD_REGISTERED);

            DSManager.updateVmInfo(vmInfo);

            out.writeByte(RapidMessages.OK);
            out.writeLong(vmInfo.getVmid());
            out.flush();
        } catch (Exception e) {
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            logger.error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            e.printStackTrace();
        }
    }

    /**
     * Convert raw IP address to string.
     *
     * @param rawBytes
     *            raw IP address.
     * @return a string representation of the raw ip address.
     */
    private String getIpAddress(byte[] rawBytes) {
        int i = 4;
        String ipAddress = "";
        for (byte raw : rawBytes) {
            ipAddress += (raw & 0xFF);
            if (--i > 0) {
                ipAddress += ".";
            }
        }

        return ipAddress;
    }

    private synchronized VmmConfig findAvailMachines(long userid, int vcpuNum, int memSize, int gpuCores, String deadline, long cycles) throws IOException, InterruptedException {
        //TODO add scheduling decision here
        ArrayList<String> ipList = new ArrayList<String>();
        int maxCount = 5;

        List<VmmInfo> vmmInfoList = DSManager.vmmInfoListByHighAllocatedCpu();

        Collections.shuffle(vmmInfoList);

        boolean allSuspended = true;

        String selectedVmmIp = "";
        int selectedVcpu;
        int selectedMemory;
        int minvcpu = 40;
	

      for (VmmInfo vmmInfo: vmmInfoList) {
            if (vmmInfo.getSuspended() == 0) {

		Random rand = new Random();
		int rand_int1 = rand.nextInt(vmmInfoList.size());
		VmmInfo vmm1 = vmmInfoList.get(rand_int1);
		if (vmm1.getAllocatedcpu() >= 80){
		    logger.info("machine vmmid=" + vmm1.getVmmid() +" is NOT suitable!");
                    continue;
		}
                allSuspended = false;
                long millionCycles = cycles / 1000000;
                logger.info("CURRENT ALLOC CPU: " + vmm1.getAllocatedcpu());

                int minExecTime = (int) Math.ceil(( (double) millionCycles / (vmm1.getCpufrequency() *
                        Double.min((double) vmm1.getCpunums() * ((double) (100 - vmm1.getAllocatedcpu()) / 100), 1) ) ));

                allSuspended = false;
                long millionCycles = cycles / 1000000;
                logger.info("CURRENT ALLOC CPU: " + vmm1.getAllocatedcpu());
                int minExecTime = (int) Math.ceil(( (double) millionCycles / (vmm1.getCpufrequency() *
                        Double.min((double) vmm1.getCpunums() * ((double) (100 - vmm1.getAllocatedcpu()) / 100), 1) ) ));
                logger.info("DECIDING vmmid: " + vmm1.getVmmid());
                logger.info("minExecTime: " + minExecTime);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime deadlineLDT = LocalDateTime.parse(deadline, formatter);
                long allowedTime = ChronoUnit.SECONDS.between(LocalDateTime.now(), deadlineLDT);
                logger.info("allowedTime: " + allowedTime);


                logger.info("machine vmmid=" + vmm1.getVmmid() +" is suitable!");
                int slackTime = 15;
                long requiredTime = allowedTime - slackTime;
                requiredTime = Long.max(requiredTime, 15);
                int requiredCpuFrequency = (int) Math.ceil((double) millionCycles / requiredTime);
                logger.info("requiredCpuFrequency: " + requiredCpuFrequency);
                int vcpu = (int) Math.ceil(Double.min(( (double)requiredCpuFrequency / vmm1.getCpufrequency()) * 100, 100));
                vcpu = Integer.max(vcpu, minvcpu);
                logger.info("vcpu: " + vcpu);

                //UserInfo userInfo = DSManager.getUserInfo(userid);
                //userInfo.setVcpu(vcpu);
                //userInfo.setMemory(128);
                //DSManager.updateUserInfo(userInfo);

                //ipList.add(vmmInfo.getIpv4());
                //vmmConfig.setVmmIP(vmmInfo.getIpv4());
                //vmmConfig.setVcpu(vcpu);
                //vmmConfig.setMemory(128);
                selectedVmmIp = vmm1.getIpv4();
                selectedVcpu = vcpu;
                selectedVcpu = 100;
                selectedMemory = 128;


                return new VmmConfig(selectedVmmIp, selectedVcpu, selectedMemory);
			
		}
	}



logger.info("Before Entering the loop in THE METHOD FINDVAILMAHCINES");
/*        for (VmmInfo vmmInfo: vmmInfoList) {
        for (VmmInfo vmmInfo: vmmInfoList) {
            if (vmmInfo.getSuspended() == 0) {
		//TODO:PYTHON CONNECT

                allSuspended = false;
                long millionCycles = cycles / 1000000;
                logger.info("CURRENT ALLOC CPU: " + vmmInfo.getAllocatedcpu());
                int minExecTime = (int) Math.ceil(( (double) millionCycles / (vmmInfo.getCpufrequency() *
                        Double.min((double) vmmInfo.getCpunums() * ((double) (100 - vmmInfo.getAllocatedcpu()) / 100), 1) ) ));
                logger.info("DECIDING vmmid: " + vmmInfo.getVmmid());
                logger.info("minExecTime: " + minExecTime);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime deadlineLDT = LocalDateTime.parse(deadline, formatter);
                long allowedTime = ChronoUnit.SECONDS.between(LocalDateTime.now(), deadlineLDT);
                logger.info("allowedTime: " + allowedTime);

                if (minExecTime < 1 || minExecTime > allowedTime || vmmInfo.getAllocatedcpu() > (100 - (double) minvcpu / vmmInfo.getCpunums())) {
                    logger.info("machine vmmid=" + vmmInfo.getVmmid() +" is NOT suitable!");
                    continue;
                }

                logger.info("machine vmmid=" + vmmInfo.getVmmid() +" is suitable!");
                int slackTime = 15;
                long requiredTime = allowedTime - slackTime;
                requiredTime = Long.max(requiredTime, 15);
                int requiredCpuFrequency = (int) Math.ceil((double) millionCycles / requiredTime);
                logger.info("requiredCpuFrequency: " + requiredCpuFrequency);
                int vcpu = (int) Math.ceil(Double.min(( (double)requiredCpuFrequency / vmmInfo.getCpufrequency()) * 100, 100));
                vcpu = Integer.max(vcpu, minvcpu);
                logger.info("vcpu: " + vcpu);
                selectedVcpu = vcpu;
//                UserInfo userInfo = DSManager.getUserInfo(userid);
//                userInfo.setVcpu(vcpu);
//                userInfo.setMemory(128);
//                DSManager.updateUserInfo(userInfo);

//                ipList.add(vmmInfo.getIpv4());
//                vmmConfig.setVmmIP(vmmInfo.getIpv4());
//                vmmConfig.setVcpu(vcpu);
//                vmmConfig.setMemory(128);
                selectedVmmIp = vmmInfo.getIpv4();
                selectedVcpu = vcpu;
//                selectedVcpu = 100;
                selectedMemory = 128;

                RequestInfo requestInfo = new RequestInfo();
                requestInfo.setAccepted(1);
                requestInfo.setVmmid(vmmInfo.getVmmid());
                requestInfo.setUserid(userid);
                requestInfo.setDeadline(deadline);
                requestInfo.setVcpu(selectedVcpu);
                requestInfo.setMemory(selectedMemory);
                requestInfo.setCycles(cycles);
                DSManager.insertRequestInfo(requestInfo);
                return new VmmConfig(selectedVmmIp, selectedVcpu, selectedMemory);
            }
        }
*/
//        if (!allSuspended) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setAccepted(0);
        requestInfo.setUserid(userid);
        requestInfo.setDeadline(deadline);
        requestInfo.setCycles(cycles);
        DSManager.insertRequestInfo(requestInfo);
        return null;
    }

    public void suspendVmm(long vmmid) {
        try {

        VmmInfo vmmInfo = DSManager.getVmmInfo(vmmid);
        if (vmmInfo == null)
            logger.info("VmmInfo not found when trying to suspend a VMM");
        else {
            vmmInfo.setSuspended(1);
            DSManager.updateVmmInfo(vmmInfo);

            WolHistory wolHistory = new WolHistory();
            wolHistory.setVmmid(vmmInfo.getVmmid());
            wolHistory.setIswol(0);
            DSManager.insertWolHistory(wolHistory);

            // sending Suspend message to VMM
            MainScheduler scheduler = MainScheduler.getInstance();

            Socket vmmSocket = new Socket(vmmInfo.getIpv4(), scheduler.getVmmPort());
            ObjectOutputStream vmmOut = new ObjectOutputStream(vmmSocket.getOutputStream());
            vmmOut.flush();

            vmmOut.writeByte(RapidMessages.DS_VMM_SUSPEND);

            vmmOut.flush();

            vmmOut.close();
            vmmSocket.close();
        }
        } catch (Exception e) {
            logger.error("Exception", e);
        }
    }

    public static void wakeOnLan(String ipStr, String macStr) {
        final int PORT = 9;
        try {
            byte[] macBytes = getMacBytes(macStr);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(ipStr);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();

//            System.out.println("Wake-on-LAN packet sent.");
        }
        catch (Exception e) {
            System.out.println("Failed to send Wake-on-LAN packet: + e");
        }
    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }

    public static String runWithPrivileges(String cmd) throws IOException {
        String[] cmds = {
                "/bin/sh",
                "-c",
                cmd
        };

        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmds).getInputStream()).useDelimiter("\\n");
        return s.hasNext() ? s.next() : "";
//        InputStreamReader input;
//        OutputStreamWriter output;
//
//        try {
//            //Create the process and start it.
//            Process pb = new ProcessBuilder("/bin/bash", "-c", "sudo etherwake -i eno1" + cmd).start();
//            output = new OutputStreamWriter(pb.getOutputStream());
//            input = new InputStreamReader(pb.getInputStream());
//
//            int bytes, tryies = 0;
//            char buffer[] = new char[1024];
//            while ((bytes = input.read(buffer, 0, 1024)) != -1) {
//                if(bytes == 0)
//                    continue;
//                //Output the data to console, for debug purposes
//                String data = String.valueOf(buffer, 0, bytes);
//                System.out.println(data);
//                // Check for password request
//                if (data.contains("[sudo] password")) {
//                    // Here you can request the password to user using JOPtionPane or System.console().readPassword();
//                    // I'm just hard coding the password, but in real it's not good.
//                    output.write(password);
//                    output.write('\n');
//                    output.flush();
//                    // erase password data, to avoid security issues.
//                    Arrays.fill(password, '\0');
//                    tryies++;
//                }
//            }
//
//            return tryies < 3;
//        } catch (IOException ex) {
//        }

//        return false;
    }

    public void vmmRegisterSlam(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        long threadId = Thread.currentThread().getId();
        try {
            logger.debug("VMM_REGISTER_SLAM() start" + "Thread Id: " + threadId);
            String vmmIP = in.readUTF();
            int vmmPort = in.readInt();
            logger.debug("A vmm has been registered with ip: " + vmmIP + " port: " + vmmPort);
            //ThreadPoolServer.vmmIPList.add(vmmIP + "|" + String.valueOf(vmmPort));
            out.writeByte(RapidMessages.OK);
        } catch (Exception e) {
            logger.error("Exception", e);
            out.writeByte(RapidMessages.ERROR);
        }
        logger.debug("Thread Id: " + threadId + " | VMM_REGISTER_SLAM() end");
        out.flush();
    }

    public void acRegisterSlam(ObjectInputStream in, long threadId, ObjectOutputStream out) throws IOException {
        logger.debug("Thread Id: " + threadId + " | AC_REGISTER_SLAM()");
        long userID = 0;
        try {
            //Thread.sleep(2000);
            userID = in.readLong();
            int osType = in.readInt();
            String vmmIP = in.readUTF();
            int vmmPort = in.readInt();
            int vcpuNum = in.readInt();
            int memSize = in.readInt();
            UserInfo userInfo = DSManager.getUserInfo(userID);
            vcpuNum = userInfo.getVcpu();
            memSize = userInfo.getMemory();
            int gpuCores = in.readInt();
            String qosinjson = in.readUTF();

            logger.debug("[Flow-"+userID+"] ************ PARAMETERS RECEIVED ************");
            logger.debug("[Flow-"+userID+"] userID:[" + userID + "],"+
                    "osType:[" + osType + "]," +
                    "vmmIP:[" + vmmIP + "],"+
                    "vmmPort:[" + vmmPort + "],"+
                    "vcpuNum:[" + vcpuNum + "],"+
                    "memSize:[" + memSize + "],"+
                    "gpuCores:[" + gpuCores + "],"+
                    "qosinjson:[" + qosinjson + "]");
            logger.debug("[Flow-"+userID+"] ********************************************");


//            QoSItemList qosItemList = null;
//            try{
//                ObjectMapper mapper = new ObjectMapper();
//                qosItemList = mapper.readValue(qosinjson, QoSItemList.class);
//            } catch (Throwable e) {
//                logger.error("[Flow] exception parsing qos json", e);
//            }

            VmInfo vmInfo = slamStartVmVmm(userID, osType, vmmIP, vmmPort,
                    vcpuNum, memSize, gpuCores);
            String vmIp = vmInfo.getIpv4();
            int vmPort = vmInfo.getPort();

            if (!"".equals(vmIp)) {
                out.writeByte(RapidMessages.OK);
                out.writeUTF(vmIp);
                out.writeInt(vmPort);
            } else {
                out.writeByte(RapidMessages.ERROR);
            }

            //SLAHandler.receivedRegisterRequest(userID, osType, vmmIP, vmmPort, vcpuNum, memSize, gpuCores, qosItemList, vmIp);

            logger.debug("[Flow] User id: " + userID + "vmmPort: " + vmmPort +
                    " osType: " + osType + " vmmIP: " + vmmIP +
                    " vcpuNum: " + vcpuNum + " memSize: " + memSize +
                    " gpuCores: " + gpuCores + " vmIp: " + vmIp + " vmPort: " + vmPort
            );

            logger.debug("[Flow] Thread Id: " + threadId + " | Finished processing AC_REGISTER_SLAM()");
        } catch (Exception e) {
            logger.error("[Flow-"+userID+"] VMM not respond. socket worker runnable Exception", e);
        }
        out.flush();
    }

    private VmInfo slamStartVmVmm(long userID, int osType, String vmmIp, int vmmPort, int vcpuNum, int memSize, int gpuCores ) throws IOException {
        logger.debug("slamStartVmVmm() start userID: " + userID + " osType: " + osType);
        logger.debug("[Flow-"+userID+"] Calling VMM manager socket server running at: " + vmmIp + ":" + Integer.toString(vmmPort));

        //logger.info("SLAM_START_VM_VMM invoked! vmmIp: " + vmmIp);

        String ip=null;
        int port = -1;

        Socket vmmSocket = new Socket(vmmIp, vmmPort);
        vmmSocket.setSoTimeout(120000);
        ObjectOutputStream vmmOut = new ObjectOutputStream(vmmSocket.getOutputStream());
        vmmOut.flush();
        ObjectInputStream vmmIn = new ObjectInputStream(vmmSocket.getInputStream());

        logger.debug("[Flow-"+userID+"] RapidMessages.SLAM_START_VM_VMM params: userID="+userID+", osType="+osType+", vcpuNum="+vcpuNum+", memSize="+memSize+", gpuCores="+gpuCores);
        vmmOut.writeByte(RapidMessages.SLAM_START_VM_VMM);
        vmmOut.writeLong(userID); // userId
        vmmOut.writeInt(osType);
        vmmOut.writeInt(vcpuNum);
        vmmOut.writeInt(memSize);
        vmmOut.writeInt(gpuCores);
        vmmOut.flush();

        byte status = vmmIn.readByte();
        logger.debug("[Flow-"+userID+"] RapidMessages.SLAM_START_VM_VMM status: " + status);
        logger.debug("Return Status: " + (status == RapidMessages.OK ? "OK" : "ERROR"));


        if (status == RapidMessages.OK) {
            long user_id = vmmIn.readLong();//not used
            ip = vmmIn.readUTF();
            port = vmmIn.readInt();
            logger.debug("Successfully retrieved VM ip: " + ip);
            OffloadHistory offloadHistory = new OffloadHistory();
            long vmmid = DSManager.getVmmInfoByIp(vmmIp).getVmmid();
            offloadHistory.setVmmid(vmmid);
            offloadHistory.setUserid(userID);
            offloadHistory.setVcpu(vcpuNum);
            offloadHistory.setMemory(memSize);
            UserInfo userInfo = DSManager.getUserInfo(user_id);
            offloadHistory.setDeadline(userInfo.getDeadline());
            offloadHistory.setCycles(userInfo.getCycles());
            DSManager.insertOffloadHistory(offloadHistory);

            VmmInfo vmmInfo = DSManager.getVmmInfo(vmmid);
            int cpuLoad = 100 / vmmInfo.getCpunums();
            vmmInfo.setCpuload(vmmInfo.getCpuload() + cpuLoad);
            DSManager.updateVmmInfo(vmmInfo);
        } else {
            logger.error("Error! returning null..");
            decrementAllocatedcpu(vmmIp, userID);
//            VmmInfo vmmInfo = DSManager.getVmmInfoByIp(vmmIp);
//            UserInfo userInfo = DSManager.getUserInfo(userID);
//            float allocatedcpuDelta = - (float) userInfo.getVcpu() / vmmInfo.getCpunums();
//            VmmInfo vmmInfoDelta = new VmmInfo();
//            vmmInfoDelta.setVmmid(vmmInfo.getVmmid());
//            vmmInfoDelta.setAllocatedcpu(allocatedcpuDelta);
//            DSManager.changeAllocatedcpuVmmInfo(vmmInfoDelta);
            ip = "";
        }
        logger.debug("[Flow-"+userID+"] RapidMessages.SLAM_START_VM_VMM ip: " + ip);

        vmmOut.close();
        vmmIn.close();
        vmmSocket.close();
        logger.debug("SlamStartVmVmm() end");

        VmInfo vmInfo = new VmInfo();
        vmInfo.setIpv4(ip);
        vmInfo.setPort(port);
        return vmInfo;
    }

}

class VmmConfig {
    private String vmmIP;
    private int vcpu;
    private int memory;

    public VmmConfig(String vmmIP, int vcpu, int memory) {
        this.vmmIP = vmmIP;
        this.vcpu = vcpu;
        this.memory = memory;
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

    public String getVmmIP() {
        return vmmIP;
    }

    public void setVmmIP(String vmmIP) {
        this.vmmIP = vmmIP;
    }
}
