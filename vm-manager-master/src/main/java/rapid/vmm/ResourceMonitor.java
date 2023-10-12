package rapid.vmm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.sun.management.OperatingSystemMXBean;

public class ResourceMonitor {
    private Logger logger = Logger.getLogger(getClass());

    private static ResourceMonitor resourceMonitor = new ResourceMonitor();
    private OperatingSystemMXBean osBean = null;
    private String nvidiaSmiPath = "/usr/bin/nvidia-smi";
    private int gpuDeviceNum;
    private int allocatedCpu;

    private ResourceMonitor() {
        osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        osBean.getSystemCpuLoad();
    }

    public static ResourceMonitor getInstance() {
        return resourceMonitor;
    }

    /**
     * The function reports the current total CPU usage.
     *
     * @return current total CPU load. The value is between 0 and 100. 0 means
     *         that the system is completely idle whereas 100 indicates that the
     *         system is busy.
     *
     */
    public double getSystemCpuLoad() {
        return osBean.getSystemCpuLoad() * 100;
    }

    /**
     * The function reports the free amount of physical memory in MB.
     *
     * @return free amount of physical memory in MB.
     */
    public long getFreePhysicalMemorySize() {
        //return osBean.getFreePhysicalMemorySize() / (1024 * 1024);
        String osName = System.getProperty("os.name");
        if (osName.equals("Linux"))
        {
            try {
                BufferedReader memInfo = new BufferedReader(new FileReader("/proc/meminfo"));
                String line;
                while ((line = memInfo.readLine()) != null)
                {
                    if (line.startsWith("MemFree: "))
                    {
                        // Output is in KB which is close enough.
                        return java.lang.Long.parseLong(line.split("[^0-9]+")[1]) / 1024;
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            // We can also add checks for freebsd and sunos which have different ways of getting available memory
        } else
        {
            return osBean.getFreePhysicalMemorySize() / (1024 * 1024);
        }
        return -1;
    }

    /**
     * The function reports the free amount of physical memory in MB.
     *
     * @return availible amount of physical memory in MB.
     */
    public long getAvailablePhysicalMemorySize() {
        //return osBean.getFreePhysicalMemorySize() / (1024 * 1024);
        String osName = System.getProperty("os.name");
        if (osName.equals("Linux"))
        {
            try {
                BufferedReader memInfo = new BufferedReader(new FileReader("/proc/meminfo"));
                String line;
                while ((line = memInfo.readLine()) != null)
                {
                    if (line.startsWith("MemAvailable: "))
                    {
                        // Output is in KB which is close enough.
                        return java.lang.Long.parseLong(line.split("[^0-9]+")[1]) / 1024;
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            // We can also add checks for freebsd and sunos which have different ways of getting available memory
        } else
        {
            return osBean.getFreePhysicalMemorySize() / (1024 * 1024);
        }
        return -1;
    }

    /**
     * The function reports the total number of cores in the system.
     *
     * @return total number of cores in the system.
     */
    public int getNumberOfCPUCores() {
        String command = "";
        if (OSValidator.isUnix()) {
            command = "lscpu";
        } else if (OSValidator.isWindows()) {
            command = "cmd /C WMIC CPU Get /Format:List";
        } else if (OSValidator.isMac()) {
            command = "sysctl -n hw.ncpu";
        }

        Process process = null;
        int numberOfCores = 0;
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            logger.error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (OSValidator.isUnix()) {
                    if (line.contains("On-line CPU(s)")) {
                        numberOfCores = Integer.parseInt(line.split("-")[line.split("-").length - 1]) + 1;
                        break;
                    }
                } else if (OSValidator.isWindows()) {
                    if (line.contains("NumberOfCores")) {
                        numberOfCores = Integer.parseInt(line.split("=")[1]);
                    }
                } else if (OSValidator.isMac()) {
                    numberOfCores = Integer.parseInt(line);
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
        return numberOfCores;
    }

    /**
     * The function reports the current total GPU usage.
     *
     * @return current total GPU load. The value is between 0 and 100. 0 means
     *         that the GPU is completely idle whereas 100 indicates that the
     *         GPU is busy.
     */
    public int getSystemGpuLoad() {
        String command = nvidiaSmiPath + " stats -i " + gpuDeviceNum + " -d gpuUtil -c 1";

        Process process = null;
        int gpuUtil = 0;
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            //logger.error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            return gpuUtil;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String gpuUtilStr = line.split(",")[3].trim();
                gpuUtil = Integer.parseInt(gpuUtilStr);

            }
        } catch (Exception e) {
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            //logger.error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            //e.printStackTrace();
            return gpuUtil;
        }
        return gpuUtil;
    }

    /**
     * @return
     */
    public String getNvidiaSmiPath() {
        return nvidiaSmiPath;
    }

    /**
     * @param nvidiaSmiPath
     */
    public void setNvidiaSmiPath(String nvidiaSmiPath) {
        this.nvidiaSmiPath = nvidiaSmiPath;
    }

    /**
     * @return
     */
    public int getGpuDeviceNum() {
        return gpuDeviceNum;
    }

    /**
     * @param gpuDeviceNum
     */
    public void setGpuDeviceNum(int gpuDeviceNum) {
        this.gpuDeviceNum = gpuDeviceNum;
    }

    public Integer getPowerUsage(int macType, int cpuLoad) {
        int powerUsage = 0;
        try {
            if (macType == VMMEngine.JETSON_NANO_WOL || macType == VMMEngine.JETSON_NANO_NO_WOL
                    || macType == VMMEngine.JETSON_NANO_WOL_NEWJETPACK)
                powerUsage = Integer.parseInt(execCmd("sudo tegrastats --interval 1000 | head -1 | awk -F ' ' '{print $27}' | awk -F '/' '{print $1}'"));
            else if (macType == VMMEngine.XAVIER)
                powerUsage = 4904 + Integer.parseInt(execCmd("sudo tegrastats --interval 1000 | head -1 | awk -F ' ' '{print $33}' | awk -F '/' '{print $1}'"));
            else {
                powerUsage = 1000 + (int) (4000.0 * cpuLoad / 100.0);
            }
        }
        catch (Exception e) {
            return 1000 + (int) (4000.0 * getSystemCpuLoad() / 100.0);
            // estimated power usage based on cpu usage
        }
        return powerUsage;
    }

    private static String execCmd(String cmd) throws java.io.IOException {
        String[] cmds = {
                "/bin/sh",
                "-c",
                cmd
        };

        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmds).getInputStream()).useDelimiter("\\n");
        return s.hasNext() ? s.next() : "";
    }

    public Integer getCpufrequency() {
        int frequency = 0;
        try {
            frequency = (int) Float.parseFloat(execCmd("lscpu | grep 'max MHz' | awk '{print $4}'").replace(',', '.'));
        }
        catch (IOException e) {
            String message = "Unable to get cpu frequency";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
        }
        return frequency;
    }

    public int getAllocatedCpu() {
        return allocatedCpu;
    }

    public void setAllocatedCpu(int allocatedCpu) {
        this.allocatedCpu = allocatedCpu;
    }

    public void updateAllocatedCpu(int vcpuNum) {
        int allocatedCpu = getAllocatedCpu();
        setAllocatedCpu(allocatedCpu + (int) Math.ceil(((double) vcpuNum / getNumberOfCPUCores())));
    }

    public String getMacAddress(int macType) throws SocketException {
        if (macType == VMMEngine.JETSON_NANO_WOL || macType == VMMEngine.JETSON_NANO_WOL_NEWJETPACK
            || macType == VMMEngine.XAVIER) {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface key = null;
            while (networkInterfaces.hasMoreElements()) {
                key = networkInterfaces.nextElement();
                if (key.getDisplayName().equals("eth0"))
                    break;
            }
            byte[] hardwareAddress = key.getHardwareAddress();
            String[] hexadecimal = new String[hardwareAddress.length];
            for (int i = 0; i < hardwareAddress.length; i++) {
                hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
            }
            String macAddress = String.join(":", hexadecimal);
            return macAddress;
        }
        return "";
    }
}

class OSValidator {

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }

    /**
     * The function will report the type of the system OS.
     *
     * @return type of the system OS
     */
    public static String getOS() {
        if (isWindows()) {
            return "win";
        } else if (isMac()) {
            return "osx";
        } else if (isUnix()) {
            return "uni";
        } else if (isSolaris()) {
            return "sol";
        } else {
            return "err";
        }
    }
}
