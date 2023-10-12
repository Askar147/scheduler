package rapid.vmm.api.libvirt;

public class LibVirtTemplates {
   private static String KERNEL_ENTRY = "<kernel>%s</kernel>";
   private static String CMDLINE_ENTRY = "<cmdline>%s</cmdline>";
   private static String DTB_ENTRY = "<dtb>%s</dtb> ";
   private static String TEMPLATE = "<domain type='kvm'><name>%s</name><uuid>%s</uuid><memory>%s</memory><vcpu>%s</vcpu><os><type arch='%s' machine='%s'>hvm</type><boot dev='hd'/>%s%s%s</os><clock offset='utc'/><on_poweroff>destroy</on_poweroff><on_reboot>restart</on_reboot><on_crash>destroy</on_crash><devices><emulator>/usr/bin/qemu-system-x86_64</emulator><disk type='file' device='disk'><source file='%s'/><driver name='qemu' type='raw'/><target dev='vda' bus='virtio'/><alias name='ide0-0-0'/><address type='pci' domain='0x0000' bus='0x00' slot='0x05' function='0x0'/></disk><interface type='bridge'><source bridge='virbr0'/></interface><input type='mouse' bus='ps2'/><input type='keyboard' bus='ps2'/><graphics type='vnc' port='5900' autoport='yes'/><video><model type='cirrus' vram='9216' heads='1'/><alias name='video0'/><address type='pci' domain='0x0000' bus='0x00' slot='0x02' function='0x0'/></video><serial type='pty'><target port='0'/></serial><console type='pty'><target type='serial' port='0'/></console></devices></domain>";
   private static String LXC_TEMPLATE = "<domain type='lxc'><name>%s</name><uuid>%s</uuid><memory>%s</memory><os><type>exe</type><init>/bin/lxc_startup.sh</init></os><vcpu>%s</vcpu><clock offset='utc'/><on_poweroff>destroy</on_poweroff><on_reboot>restart</on_reboot><on_crash>destroy</on_crash><devices><interface type='bridge'><source bridge='br0'/><target dev='vnet0'/><model type='virtio'/><alias name='net0'/><address type='pci' domain='0x0000' bus='0x01' slot='0x00' function='0x0'/></interface><console type='pty' /></devices></domain>";

   public static String getTemplate(String vmName, String vmUuid, String vmMemory, String vmCpu, String arch, String machine, String kernel, String cmdline, String dtb, String vmImage) {
      String kernelEntry = kernel != null && !kernel.equals("") ? String.format(KERNEL_ENTRY, kernel) : "";
      String cmdlineEntry = cmdline != null && !cmdline.equals("") ? String.format(CMDLINE_ENTRY, kernel) : "";
      String dtbEntry = dtb != null && !dtb.equals("") ? String.format(DTB_ENTRY, kernel) : "";
      return String.format(LXC_TEMPLATE, vmName, vmUuid, vmMemory, vmCpu);
      //return String.format(TEMPLATE, vmName, vmUuid, vmMemory, vmCpu, arch, machine, kernelEntry, cmdlineEntry, dtbEntry, vmImage);
   }
}
