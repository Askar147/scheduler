package rapid;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;  

import org.apache.log4j.Logger;

public class MainScheduler {

    private static MainScheduler mainScheduler = new MainScheduler();
    private Logger logger = Logger.getLogger(getClass());

    private int port = 9001;
    private int maxConnections = 100;
    private int vmmPort = 9000;
    static final int maxAvailableVmType = 10;
    private String ipv4;
    private VmType[] vmTypes;
    private SlamInfo slamInfo = new SlamInfo();

    private MainScheduler() {
        readConfiguration();

        vmTypes = new VmType[MainScheduler.maxAvailableVmType];

//        try {
//           InetAddress addr = InetAddress.getLocalHost();
//            ipv4 = addr.getHostAddress();
//            logger.info("Ipv4 Address is " + ipv4);
//        } catch (Exception e) {
//          String message = "Unable to get inet ipv4 address";
//          getLogger().error("Caught Exception: " + System.lineSeparator() + message);
//          e.printStackTrace();
//      }
    }

    public static MainScheduler getInstance() {
        return mainScheduler;
    }

    public static void main(String[] args) throws IOException {

        MainScheduler mainScheduler = MainScheduler.getInstance();
/*
        //start suspendIdleVmms Timer
	mainScheduler.getLogger().info("BEFORE CREATING ADJUSTIDLE VMMS"); 
        AdjustIdleVmms adjustIdleVmms = new AdjustIdleVmms();
        Timer suspendVmmsTimer = new Timer();
//        // TODO: RETURN AUTO SLEEP
        suspendVmmsTimer.schedule(adjustIdleVmms, 30000, 2000);
	mainScheduler.getLogger().info("AFTER CREATING ADJUSTIDLE VMMS"); 
*/
        CalculateAverageAllocatedCpu calculateAverageAllocatedCpu = new CalculateAverageAllocatedCpu();
        Timer queryAllocatedCpuTimer = new Timer();
        queryAllocatedCpuTimer.schedule(calculateAverageAllocatedCpu, 15000, 5000);

        mainScheduler.getLogger().info("Scheduler Started YES");
	mainScheduler.getLogger().info("AFter CREATING ADJUSTIDLE VMMSa");
        mainScheduler.initializeVmTypes();
        try  {
            ThreadPooledServer server = new ThreadPooledServer(mainScheduler.getPort());
            new Thread(server).start();

            // Start the queue processor
            QueueProcessor queueProcessor = new QueueProcessor(WorkerRunnable.getRequestQueue());
            new Thread(queueProcessor).start();
        } catch(Exception e) {
            StringBuilder message = new StringBuilder("Failed to start Scheduler");
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message.append(System.lineSeparator()).append(stackTraceElement.toString());
            }
            mainScheduler.getLogger().error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            e.printStackTrace();
        }
    }

    private void readConfiguration() {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream reader = loader.getResourceAsStream("config.properties");
            Properties props = new Properties();
            props.load(reader);

            setVmmPort(Integer.parseInt(props.getProperty("vmmPort")));
            setPort(Integer.parseInt(props.getProperty("schedulerPort")));
            setMaxConnections(Integer.parseInt(props.getProperty("maxConnections")));
            setIpv4(props.getProperty("schedulerIp"));

            reader.close();
        } catch (Exception e) {
            String message = "Unable to read configs";
            mainScheduler.getLogger().error("Caught Exception: " + System.lineSeparator() + message);
            e.printStackTrace();
        }
    }

    public void initializeVmTypes() {
        for (int i = 0; i < MainScheduler.maxAvailableVmType; i++)
            vmTypes[i] = new VmType();

        vmTypes[0].setId(0);
        vmTypes[0].setNumCore(1);
        vmTypes[0].setMemory(1024000); // memory in KB
        vmTypes[0].setDisk(5000); // disk in MB
        vmTypes[0].setGpuCore(512);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public int getVmmPort() {
        return vmmPort;
    }

    public void setVmmPort(int vmmPort) {
        this.vmmPort = vmmPort;
    }

    public SlamInfo getSlamInfo() {
        return slamInfo;
    }

    public void setSlamInfo(SlamInfo slamInfo) {
        this.slamInfo = slamInfo;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

}

class CalculateAverageAllocatedCpu extends TimerTask {
    private Logger logger = Logger.getLogger(getClass());
    private static int count = 0;

    public void run() {
        List<VmmInfo> vmmInfoList = DSManager.vmmInfoList();
        int activeVmms = 0;
        float totalAllocatedCpu = 0;
        float averageAllocatedCpu = 0;
        int totalPowerUsage = 0;
        float averagePowerUsage = 0;
        if (vmmInfoList.size() != 0) {
            for (VmmInfo vmmInfo: vmmInfoList) {
                if (vmmInfo.getSuspended() == 0) {
                    activeVmms++;
                    totalAllocatedCpu += vmmInfo.getAllocatedcpu();
                    totalPowerUsage += vmmInfo.getPowerusage();
                }
            }
            averageAllocatedCpu = totalAllocatedCpu / activeVmms;
            averagePowerUsage = (float) totalPowerUsage / activeVmms;
        }

        GlobalReadings globalReadings = new GlobalReadings();
        globalReadings.setActivevmms(activeVmms);
        globalReadings.setAllocatedcpu(averageAllocatedCpu);
        globalReadings.setPowerusagesum(totalPowerUsage);
        globalReadings.setPowerusageavg(averagePowerUsage);
        DSManager.insertGlobalReading(globalReadings);

        logger.info("Inserted current global readings into the database.AND SEQUENTIONAL");
        if (count != 0)
            logger.info("Predicted workload in the next minute - " + DSManager.predictWorkload());
        count++;
    }
}

class AdjustIdleVmms extends TimerTask {
    private Logger logger = Logger.getLogger(getClass());

    public void run() {
	logger.info("ADJUSTIDLEVMMS DOES WORK WITH XAVIER AND SEQUENTIONAL");
        List<VmmInfo> vmmInfoList = DSManager.vmmInfoList();
        int activeVmms = 0;
        float totalAllocatedCpu = 0;
        float averageAllocatedCpu = 0;
        if (vmmInfoList.size() != 0) {
            for (VmmInfo vmmInfo: vmmInfoList) {
                if (vmmInfo.getSuspended() == 0) {
                    activeVmms++;
                    totalAllocatedCpu += vmmInfo.getAllocatedcpu();
                }
            }
            averageAllocatedCpu = totalAllocatedCpu / activeVmms;
        }

        float predictedCpuLoad = DSManager.predictWorkload();
        if (predictedCpuLoad < 50 && averageAllocatedCpu < 70) {
            for (VmmInfo vmmInfo : vmmInfoList) {
                if ((int) vmmInfo.getAllocatedcpu() == 0 && vmmInfo.getSuspended() == 0
                        && (vmmInfo.getMactype() == VmmInfo.JETSON_NANO_WOL ||
                        vmmInfo.getMactype() == VmmInfo.XAVIER)) {
//                if ((int) vmmInfo.getAllocatedcpu() == 0 && vmmInfo.getSuspended() == 0
//                        && (vmmInfo.getMactype() == VmmInfo.JETSON_NANO_WOL)) {
                    DSEngine.getInstance().suspendVmm(vmmInfo.getVmmid());
                    logger.info("Suspending vmmid=" + vmmInfo.getVmmid());
                }
            }
        } else {
            for (VmmInfo vmmInfo: vmmInfoList) {
                if (vmmInfo.getSuspended() == 1) {
                    try {
                        DSEngine.runWithPrivileges("sudo etherwake -i eth0 " + vmmInfo.getMacaddress());
                        for (int i = 0; i < 10; i++) {
                            DSEngine.wakeOnLan("192.168.0.255", vmmInfo.getMacaddress());
                        }
                        Socket wakeOnLanSocket = new Socket(MainScheduler.getInstance().getIpv4(), 9876);
                        ObjectOutputStream dsOut = new ObjectOutputStream(wakeOnLanSocket.getOutputStream());
                        dsOut.flush();

                        dsOut.writeByte(1);

                        dsOut.writeUTF(vmmInfo.getMacaddress());
                        dsOut.flush();

                        dsOut.close();
                        wakeOnLanSocket.close();
                        logger.info("Waking vmmid=" + vmmInfo.getVmmid());
                        logger.info(vmmInfo.getMacaddress());
                        break;
                    } catch (Exception e) {
                        logger.info(e.getMessage());
                    }
                }
            }
        }
    }


}
class QueueProcessor implements Runnable {
    private static Logger logger = Logger.getLogger(QueueProcessor.class);
    private BlockingQueue<Connection> requestQueue;

    public QueueProcessor(BlockingQueue<Connection> requestQueue) {
        this.requestQueue = requestQueue;
    }

    public void run() {
        while (true) {
            try {
                int queueSize = requestQueue.size();
                logger.info("Queue Size = " + queueSize);
                if (queueSize > 0) {
                    processBatchRequests(queueSize);
                }
                
                long sleepTime = Math.max(MIN_SLEEP_TIME, MAX_SLEEP_TIME - queueSize * 10);
                logger.info("Queue Sleep For = " + sleepTime);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("QueueProcessor interrupted", e);
                break;
            }
        }
    }

    private void processBatchRequests(int queueSize) {
        List<Connection> batch = new ArrayList<>();
        requestQueue.drainTo(batch, Math.min(MAX_BATCH_SIZE, queueSize));

        for (Connection connection : batch) {
            boolean success = processRequest(connection);

            if (!success) {
                // If the VM was not found, requeue the connection for another attempt
                requestQueue.put(connection);
            } else {
                // Close the connection only if processing is complete
                connection.close();
            }
        }
    }

    private boolean processRequest(Connection connection) {
        try {

            ObjectInputStream in = connection.getInputStream();
            ObjectOutputStream out = connection.getOutputStream();

            if (connection.getRequestid() == -1){
                long userid = in.readLong();
                int vcpuNum = in.readInt();
                int memSize = in.readInt();
                int gpuCores = in.readInt();
                String deadline = in.readUTF();
                long cycles = in.readLong();
                logger.info("Processing Request - userId: " + userid + ", vcpuNum: " + vcpuNum + ", memSize: " + memSize);

                RequestInfo requestInfo = new RequestInfo();
                requestInfo.setAccepted(0);
                requestInfo.setUserid(userid);
                requestInfo.setDeadline(deadline);
                requestInfo.setVcpu(vcpuNum);
                requestInfo.setMemory(memSize);
                requestInfo.setCycles(cycles);

                LocalDateTime currentDateTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                requestInfo.setStartQueueTime(currentDateTime.format(formatter).toString());

                long requestId = DSManager.insertRequestInfo(requestInfo);

                connection.setRequestid(requestId);
            }

            RequestInfo addedRequestInfo = DSManager.getRequestInfo(connection.getRequestid());

            LocalDateTime deadlineTime = LocalDateTime.parse(addedRequestInfo.getDeadline(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Compare with the current time
            if (deadlineTime.isBefore(LocalDateTime.now())) {
                logger.error("Request rejected due to expired deadline: " + deadline);
                out.writeByte(RapidMessages.ERROR);
                out.flush();
                return true;
            }

            DSEngine dsEngine = DSEngine.getInstance();
            return dsEngine.acRegisterNewDs(connection.getInputStream(), connection.getOutputStream(), connection.getSocket(), connection.getRequestid());
        } catch (Exception e) {
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            logger.error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            e.printStackTrace();
            return false;
        }
    }
}