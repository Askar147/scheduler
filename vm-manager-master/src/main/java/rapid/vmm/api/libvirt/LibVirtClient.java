package rapid.vmm.api.libvirt;

import rapid.vmm.api.util.Flavor;
import rapid.vmm.api.VMManagerConfiguration;
import rapid.vmm.api.util.Quota;
import org.apache.log4j.Logger;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.NodeInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class LibVirtClient {
   public static final String QEMU_TCP_127_0_0_1_SYSTEM = "qemu+tcp://127.0.0.1/system";
   public static final String LXC_CONNECT = "lxc+tcp://127.0.0.1/system";
   private Logger log = Logger.getLogger("rapid");
   private Quota quota;
   private String cpuArch;

   public LibVirtClient() throws Exception {
      this.log.info("[LibVirtClient] Initializing LibVirt client");

      try {
         this.log.info("[LibVirtClient] Connecting to local hypervisor");
         Connect conn = new Connect(LibVirtClient.LXC_CONNECT);
         NodeInfo ni = conn.nodeInfo();
         this.log.info("[LibVirtClient] model: " + ni.model + " mem(kb):" + ni.memory);
         this.log.info("[LibVirtClient] Shutting downs active VMs");
         String[] domains = conn.listDefinedDomains();
         ArrayList waitfor = new ArrayList();
         String[] var5 = domains;
         int var6 = domains.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String domainName = var5[var7];
            Domain vm = conn.domainLookupByName(domainName);
            if (vm.getName().contains("rapid-")) {
               this.log.info("[LibVirtClient] Shutting down " + vm.getName());
               vm.shutdown();
               waitfor.add(domainName);
            }
         }

         this.log.info("[LibVirtClient] Removing active VMs");
         Iterator var11 = waitfor.iterator();

         while(var11.hasNext()) {
            String domainName = (String)var11.next();
            Domain vm = conn.domainLookupByName(domainName);

            while(vm.isActive() == 1) {
               this.log.info("[LibVirtClient] Waiting for  " + vm.getName() + " to be halted");
               Thread.sleep(1000L);
            }

            this.log.info("[LibVirtClient] Deleting " + vm.getName());
            vm.destroy();
         }

         this.log.info("[LibVirtClient] Getting free memory");
         String archType = System.getProperty("os.arch");
         long memory;
         if (!"arm".equals(archType)) {
            memory = conn.getFreeMemory();
         } else {
            memory = this.getFreeMemoryFromOS();
         }

         this.log.info("[LibVirtClient] Getting capacity");
         long capacity = conn.storagePoolLookupByName("default").getInfo().available;
         conn.close();
         this.quota = new Quota(Quota.QuotaLocalization.LOCAL, (double)ni.cpus, (double)memory, (double)capacity);
         this.cpuArch = ni.model;
         this.log.info("[LibVirtClient] Available CPUs   : " + ni.cpus);
         this.log.info("[LibVirtClient] Available memory : " + memory / 1024L / 1024L + "MB");
         this.log.info("[LibVirtClient] Available disk   : " + capacity / 1024L / 1024L / 1024L + "GB");
      } catch (LibvirtException var10) {
         this.log.error(var10.getMessage());
      }

      this.log.info("[LibVirtClient] LibVirt client initialized");
   }

   private synchronized void updateQuota(double vcpu, double disk) {
      this.log.info("[LibVirtClient] Updating quota: ");
      this.log.info("[LibVirtClient]     cpu  : " + vcpu);
      this.log.info("[LibVirtClient]     disk : " + disk);
      this.quota.setvCpu(this.quota.getvCpu() + vcpu);
      this.quota.setDisk(this.quota.getDisk() + disk);
   }

   public String createVM(String vmName, UUID vmUuid, Flavor flavor, String vmImage) throws Exception {
      Domain domain;
      try {
         this.log.info("[LibVirtClient] Connecting to local hypervisor");
         Connect conn = new Connect(LibVirtClient.LXC_CONNECT);
         this.log.info("Creating template");
         String template = this.buildTemplate(vmName, vmUuid, flavor.getMemory(), flavor.getvCpu(), vmImage);
         this.log.info("[LibVirtClient] Resulting template: \n" + template);
         this.log.info("[LibVirtClient] Creating VM");
         domain = conn.domainCreateXML(template, 0);
         conn.close();
         Thread.sleep(30000);
         this.updateQuota((double)(-flavor.getvCpu()), 0.0D);
      } catch (LibvirtException var9) {
         var9.printStackTrace();
         return "";
      }

      return domain.getUUIDString();
   }

   public String getVmIp(String filePath, String vmName) {
      String[] command = new String[]{filePath, vmName};
      System.out.println("Path is" + filePath + " vmName is " + vmName);
      try {
         Process process = Runtime.getRuntime().exec(command);
         BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
         String s;
         if ((s = reader.readLine()) != null) {
         }
         System.out.println("retured string is " + s);
         return s;
      } catch (IOException var7) {
         return "";
      }
   }

   public boolean deleteVM(UUID vmId) {
      try {
         this.log.info("[LibVirtClient] Connecting to local hypervisor");
         Connect conn = new Connect(LibVirtClient.LXC_CONNECT);
         Domain domain = conn.domainLookupByUUID(vmId);
         domain.destroy();
         conn.close();
         Flavor flavor = VMManagerConfiguration.getFlavor(Flavor.FlavorType.valueOf(domain.getName().split("-")[1]));
         this.updateQuota((double)flavor.getvCpu(), (double)flavor.getDisk());
         return true;
      } catch (LibvirtException var5) {
         var5.printStackTrace();
         return false;
      }
   }

   public boolean resumeVM(UUID vmId) {
      try {
         this.log.info("[LibVirtClient] Connecting to local hypervisor");
         Connect conn = new Connect(LibVirtClient.LXC_CONNECT);
         Domain domain = conn.domainLookupByUUID(vmId);
         domain.resume();
         conn.close();
         return true;
      } catch (LibvirtException var5) {
         var5.printStackTrace();
         return false;
      }
   }

   public boolean suspendVM(UUID vmId) {
      try {
         this.log.info("[LibVirtClient] Connecting to local hypervisor");
         Connect conn = new Connect(LibVirtClient.LXC_CONNECT);
         Domain domain = conn.domainLookupByUUID(vmId);
         domain.suspend();
         conn.close();
         return true;
      } catch (LibvirtException var5) {
         var5.printStackTrace();
         return false;
      }
   }

   public Quota getQuota() {
      return this.quota.clone();
   }

   public String buildTemplate(String name, UUID uuid, long memory, int vCpu, String vmImage) {
      String archType = System.getProperty("os.arch");
      String arch;
      String machine;
      String kernel;
      String cmdline;
      String dtb;
      if (!"arm".equals(archType)) {
         arch = "x86_64";
         machine = "pc";
         cmdline = "";
         kernel = "";
         dtb = "";
      } else {
         arch = "arm";
         machine = "vexpress-a15";
         cmdline = "root=/dev/vda console=ttyAMA0 rootwait";
         kernel = VMManagerConfiguration.getCustomKernelPath();
         dtb = VMManagerConfiguration.getCustomDtbPath();
      }

      String template = LibVirtTemplates.getTemplate(name, uuid.toString(), String.valueOf(memory), String.valueOf(vCpu), arch, machine, kernel, cmdline, dtb, vmImage);
      return template;
   }

   private long getFreeMemoryFromOS() throws Exception {
      long memory = -1L;

      try {
         Process p = Runtime.getRuntime().exec("free");
         p.waitFor();
         BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
         String line = reader.readLine();
         Exception e;
         if (line != null) {
            line = reader.readLine();
            if (line != null) {
               memory = Long.valueOf(line.replaceAll(" +", " ").split(" ")[3]);
               memory /= 1024L;
               return memory;
            } else {
               e = new Exception("Error reading memory from OS");
               throw e;
            }
         } else {
            e = new Exception("Error reading memory from OS");
            throw e;
         }
      } catch (IOException var7) {
         Exception e = new Exception(var7.getMessage());
         throw e;
      }
   }
}
