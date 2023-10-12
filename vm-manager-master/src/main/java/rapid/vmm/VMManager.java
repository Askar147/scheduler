package rapid.vmm;

import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import eu.project.rapid.common.RapidMessages;

public class VMManager {
    private static VMManager vmManager = new VMManager();
    private Logger logger = Logger.getLogger(getClass());

    private int vmmPort;
    private int dsPort;
    private int maxConnections;
    private String vmmAddress;
    private String dsAddress;
    private String dataPath;
    private int deviceType;

    private VMManager() {
        try {
            readConfiguration();
        } catch (Exception e) {
            logger.info("VMM Initialization is failed");
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            logger.error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            e.printStackTrace();
            // System.exit(-1);
        }
    }

    public static VMManager getInstance() {
        return vmManager;
    }

    public static void main(String[] args) throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        VMManager vmManager = VMManager.getInstance();
        VMMEngine vmmEngine = VMMEngine.getInstance();
        LifetimeManager lifetimeManager = LifetimeManager.getInstance();

        vmManager.getLogger().info("Started VMManager Initialization");

        // initialize vmTypes. now hard coding, but later will use XML style
        // assignment.
        vmmEngine.initializeVmTypes();

        // restore VMM and VM info from local files;
        vmmEngine.restoreVmmInfo();
        lifetimeManager.restoreVmList();

        VmmInfo vmmInfo = vmmEngine.getVmmInfo();

        int errorCode = RapidMessages.ERROR;
        if (vmmInfo.getVmmId() == 0) {
            if ((errorCode = vmmEngine.vmmRegisterDs()) != RapidMessages.OK) {
                vmManager.getLogger().info("Failed DS registation. errorCode: " + errorCode);
                System.exit(-1);
            } else {
                vmManager.getLogger().info("Finished DS registation. New VMM ID: " + vmmInfo.getVmmId());
            }
        } else {
            vmManager.getLogger().info("Finished DS registation. Existing VMM ID: " + vmmInfo.getVmmId());
        }

		if ((errorCode = vmmEngine.vmmRegisterSlam()) != RapidMessages.OK) {
			vmManager.getLogger().info("Failed SLAM registation. errorCode: " + errorCode);
			System.exit(-1);
		}
        vmManager.getLogger().info("Finished SLAM registation");

        // start vmmDsTimer Timer
        VmmDsTimerTask vmmDsTimerTask = new VmmDsTimerTask();
        Timer vmmDsTimer = new Timer();
        vmmDsTimer.schedule(vmmDsTimerTask, 5000, 5000);

        // vmmEngine.startHelperVms();

        vmManager.getLogger().info("Finished VMManager Initialization");

        try {
            ThreadPooledServer server = new ThreadPooledServer(vmManager.getVmmPort());
            new Thread(server).start();
        } catch (Exception e) {
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            vmManager.getLogger().error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            vmManager.getLogger().info("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);

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
            setVmmAddress(props.getProperty("vmmAddress"));
            setMaxConnections(Integer.parseInt(props.getProperty("maxConnections")));

            VMMEngine vmmEngine = VMMEngine.getInstance();
            int gpuCores = Integer.parseInt(props.getProperty("gpuCores"));
            vmmEngine.setGpuCores(gpuCores);

            String availableType = props.getProperty("availableType");
            vmmEngine.setAvailableType(availableType);

            ResourceMonitor resourceMonitor = ResourceMonitor.getInstance();
            String nvidiaSmiPath = props.getProperty("nvidiaSmiPath");
            resourceMonitor.setNvidiaSmiPath(nvidiaSmiPath);
            ;
            String gpuDeviceNum = props.getProperty("gpuDeviceNum");
            resourceMonitor.setGpuDeviceNum(Integer.parseInt(gpuDeviceNum));

            String dataPath = props.getProperty("dataPath");
            setDataPath(dataPath);

            setDsPort(Integer.parseInt(props.getProperty("dsPort")));
            setDsAddress(props.getProperty("dsAddress"));

            setDeviceType(Integer.parseInt(props.getProperty("deviceType")));

            reader.close();
        } catch (Exception e) {
            String message = "Unable to read configs";
            vmManager.getLogger().error("Caught Exception: " + System.lineSeparator() + message);
            e.printStackTrace();
        }
    }

    /**
     * @return
     */
    public int getVmmPort() {
        return vmmPort;
    }

    /**
     * @return
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * @param logger
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * @param vmmPort
     */
    public void setVmmPort(int vmmPort) {
        this.vmmPort = vmmPort;
    }

    /**
     * @return
     */
    public int getDsPort() {
        return dsPort;
    }

    /**
     * @param dsPort
     */
    public void setDsPort(int dsPort) {
        this.dsPort = dsPort;
    }

    /**
     * @return
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * @param maxConnections
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * @return
     */
    public String getVmmAddress() {
        return vmmAddress;
    }

    /**
     * @param vmmAddress
     */
    public void setVmmAddress(String vmmAddress) {
        this.vmmAddress = vmmAddress;
    }

    /**
     * @return
     */
    public String getDsAddress() {
        return dsAddress;
    }

    /**
     * @param dsAddress
     */
    public void setDsAddress(String dsAddress) {
        this.dsAddress = dsAddress;
    }

    /**
     * @return
     */
    public String getDataPath() {
        return dataPath;
    }

    /**
     * @param dataPath
     */
    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    /**
     * @return
     */

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
        VMMEngine.setMacType(this.deviceType);
    }
}

class VmmDsTimerTask extends TimerTask {
    public void run() {
        VMMEngine vmmEngine = VMMEngine.getInstance();
        vmmEngine.vmmNotifyDs();
    }
}
