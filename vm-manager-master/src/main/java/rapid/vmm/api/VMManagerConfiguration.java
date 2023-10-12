package rapid.vmm.api;

import rapid.vmm.api.util.Flavor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

public class VMManagerConfiguration {
    private static Logger logger = Logger.getLogger("rapid");
    private static String gwId;
    private static final int CPUTINY = 1;
    private static final int CPUSMALL = 1;
    private static final int CPUSTANDARD = 2;
    private static final long MEMTINY = 384L;
    private static final long MEMSMALL = 512L;
    private static final long MEMSTANDARD = 1024L;
    private static final long DISKTINY = 0L;
    private static final long DISKSMALL = 0L;
    private static final long DISKSTANDARD = 0L;
    private static VMManagerConfiguration.SystemArchitecture systemArchitecture;
    private static String vmmPath = "/home/ubuntu/rapid-modules/VMM/vmmtest";
    private static String vmmArmPath;
    private static String customKernelPath;
    private static String customDtbPath;
    private static String baseImagesPath;
    private static String baseAppImagePath;
    private static String baseGatewayImagePath;
    private static String baseBigDataImagePath;
    private static String baseStorageImagePath;
    private static String instantiatedImagesPath;
    private static String baseImagesURL;
    private static String keystoneEndpoint;
    private static String novaEndpoint;
    private static String cinderEndpoint;
    private static String neutronEndpoint;
    private static String openStackTenant;
    private static String openStackUser;
    private static String openStackPass;
    private static final String AMD64 = "amd";
    private static final String X86 = "x";
    private static final String I386 = "i";
    private static HashMap clouds = null;

    public static void init() {
        logger.info("[VMManagerConfiguration] Starting configuration loading...");
        String archType = System.getProperty("os.arch");
        if (!archType.startsWith("x") && !archType.startsWith("i")) {
            if (archType.startsWith("amd")) {
                systemArchitecture = VMManagerConfiguration.SystemArchitecture.AMD;
            } else {
                systemArchitecture = VMManagerConfiguration.SystemArchitecture.ARM;
            }
        } else {
            systemArchitecture = VMManagerConfiguration.SystemArchitecture.INTEL;
        }

        logger.info("vmImagesPath    = " + vmmPath);
        logger.info("openStackHost   = " + keystoneEndpoint);
        logger.info("novaEndpoint    = " + novaEndpoint);
        logger.info("neutronEndpoint = " + neutronEndpoint);
        logger.info("openStackHost   = " + keystoneEndpoint);
        logger.info("openStackUser   = " + openStackUser);
        createPaths();
        logger.info("[VMManagerConfiguration] Configuration loaded...");
    }

    private static void createPaths() {
        if (systemArchitecture == VMManagerConfiguration.SystemArchitecture.ARM) {
            vmmArmPath = vmmPath + "/arm";
            customKernelPath = vmmArmPath + "/vexpress-zImage";
            customDtbPath = vmmArmPath + "/vexpress-v2p-ca15-tc1.dtb";
        } else {
            customKernelPath = "";
            customDtbPath = "";
        }

        baseImagesPath = vmmPath + "/base";
        baseAppImagePath = baseImagesPath + "/app.img";
        baseGatewayImagePath = baseImagesPath + "/gateway.img";
        baseBigDataImagePath = baseImagesPath + "/bigdata.img";
        baseStorageImagePath = baseImagesPath + "/storage.img";
        instantiatedImagesPath = vmmPath + "/instantiated";
        logger.info("Cheking configuration directory structure.");
        File f = new File(vmmPath);
        if (!f.isDirectory()) {
            logger.warn("BETaaS VMManager images directory does not exist. Creating...");
            if (f.mkdir()) {
                logger.info("BETaaS VMManager  image directory created successfully.");
            }
        } else {
            logger.info("BETaaS VMManager base image directory exists.");
        }

    }

    private static void downloadBaseImages() throws Exception {
        String arch = System.getProperty("os.arch");
        if (arch.equals("amd64") && arch.equals("arm") && arch.equals("x86")) {
            String finalImagesURL = baseImagesURL + "-" + arch;
            logger.info("Downloading default VM images.");

            try {
                URL website = new URL(finalImagesURL);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(baseImagesPath);
                logger.info("Downloading base image from " + baseImagesURL);
                fos.getChannel().transferFrom(rbc, 0L, Long.MAX_VALUE);
            } catch (MalformedURLException var6) {
                logger.error("Error downloading images: bad URL.");
                logger.error(var6.getMessage());
            } catch (IOException var7) {
                logger.error("Error downloading images: IO exception.");
                logger.error(var7.getMessage());
            }

            logger.info("Completed!");
        } else {
            throw new Exception("System architecture not supported for virtualization");
        }
    }

    private static void loadClouds() {
        if (clouds == null) {
            clouds = new HashMap();

            try {
                XMLConfiguration config = new XMLConfiguration("vmmanager.conf");
                int iter = 0;

                while(config.getProperty("clouds.cloud(" + iter + ")") != null) {
                    String type = config.getString("clouds.cloud(" + iter + ")[@type]");
                    String url = config.getString("clouds.cloud(" + iter + ").url");
                    String user = config.getString("clouds.cloud(" + iter + ").user");
                    String pass = config.getString("clouds.cloud(" + iter++ + ").password");
                    String[] cloudData = new String[]{type, user, pass};
                    clouds.put(url, cloudData);
                }
            } catch (ConfigurationException var7) {
                logger.error(var7.getMessage());
            }
        }

    }

    public void setGwId(String gwId) {
        VMManagerConfiguration.gwId = gwId;
    }

    public void setVmmPath(String vmImagesPath) {
        vmmPath = vmImagesPath;
    }

    public void setKeystoneEndpoint(String keystoneEndpoint) {
        VMManagerConfiguration.keystoneEndpoint = keystoneEndpoint;
    }

    public void setNovaEndpoint(String novaEndpoint) {
        VMManagerConfiguration.novaEndpoint = novaEndpoint;
    }

    public void setCinderEndpoint(String cinderEndpoint) {
        VMManagerConfiguration.cinderEndpoint = cinderEndpoint;
    }

    public void setNeutronEndpoint(String neutronEndpoint) {
        VMManagerConfiguration.neutronEndpoint = neutronEndpoint;
    }

    public void setOpenStackTenant(String openStackTenant) {
        VMManagerConfiguration.openStackTenant = openStackTenant;
    }

    public void setOpenStackUser(String openStackUser) {
        VMManagerConfiguration.openStackUser = openStackUser;
    }

    public void setOpenStackPass(String openStackPass) {
        VMManagerConfiguration.openStackPass = openStackPass;
    }

    public static String getGwId() {
        return gwId;
    }

    public static HashMap getClouds() {
        return clouds;
    }

    public static String getVmmPath() {
        return vmmPath;
    }

    public static String getBaseImagesPath() {
        return baseImagesPath;
    }

    public static String getBaseAppImagePath() {
        return baseAppImagePath;
    }

    public static String getBaseGatewayImagePath() {
        return baseGatewayImagePath;
    }

    public static String getBaseBigDataImagePath() {
        return baseBigDataImagePath;
    }

    public static String getBaseStorageImagePath() {
        return baseStorageImagePath;
    }

    public static String getInstantiatedImagesPath() {
        return instantiatedImagesPath;
    }

    public static String getBaseImagesURL() {
        return baseImagesURL;
    }

    public static String getKeystoneEndpoint() {
        return keystoneEndpoint;
    }

    public static String getCinderEndpoint() {
        return cinderEndpoint;
    }

    public static String getNovaEndpoint() {
        return novaEndpoint;
    }

    public static String getNeutronEndpoint() {
        return neutronEndpoint;
    }

    public static String getOpenStackTenant() {
        return openStackTenant;
    }

    public static String getOpenStackUser() {
        return openStackUser;
    }

    public static String getOpenStackPass() {
        return openStackPass;
    }

    public static VMManagerConfiguration.SystemArchitecture getSystemArchitecture() {
        return systemArchitecture;
    }

    public static String getCustomKernelPath() {
        return customKernelPath;
    }

    public static String getCustomDtbPath() {
        return customDtbPath;
    }

    public static Flavor getFlavor(Flavor.FlavorType type) {
        Flavor ret = null;
        switch(type) {
            case tiny:
                ret = new Flavor(type, 1, 384L, 0L);
                break;
            case small:
                ret = new Flavor(type, 1, 512L, 0L);
                break;
            case standard:
                ret = new Flavor(type, 2, 1024L, 0L);
        }

        return ret;
    }

    static {
        init();
    }

    private class DownloadUpdater extends Thread {
        private boolean completed = false;
        private long totalSize;
        private long actualSize;
        private String fileName;
        private String URL;
        private File file;

        public DownloadUpdater(String fileName, String uri) {
            this.fileName = fileName;
            VMManagerConfiguration.logger.debug(fileName);
            this.totalSize = -1L;

            try {
                URL url = new URL(uri);
                URLConnection conn = url.openConnection();
                this.totalSize = (long)conn.getContentLength();
                if (this.totalSize < 0L) {
                    VMManagerConfiguration.logger.warn("Could not determine file size.");
                }

                conn.getInputStream().close();
            } catch (Exception var7) {
                VMManagerConfiguration.logger.error("Error getting image size.");
                VMManagerConfiguration.logger.error(var7.getMessage());
            }

        }

        public synchronized void start() {
            super.start();
            if (this.totalSize > 0L) {
                try {
                    for(double progressPercentage = 0.0D; progressPercentage < 1.0D; progressPercentage += 0.01D) {
                        this.updateProgress();
                        Thread.sleep(20L);
                    }
                } catch (InterruptedException var3) {
                    VMManagerConfiguration.logger.error("Download interrupted!");
                }
            }

        }

        private void updateProgress() {
            this.file = new File(this.fileName);
            this.actualSize = this.file.getTotalSpace();
            this.completed = this.actualSize >= this.totalSize;
            int width = 1;
            float progress = (float)(this.actualSize / this.totalSize);
            VMManagerConfiguration.logger.debug("\r(" + (int)progress + "%) [");

            int i;
            for(i = 0; i <= (int)(progress * 50.0F); ++i) {
                VMManagerConfiguration.logger.debug("#");
            }

            while(i < 50) {
                VMManagerConfiguration.logger.debug(" ");
                ++i;
            }

            VMManagerConfiguration.logger.debug("](" + this.actualSize + "/" + this.totalSize + ")");
        }
    }

    public static enum SystemArchitecture {
        INTEL,
        ARM,
        AMD;
    }
}
