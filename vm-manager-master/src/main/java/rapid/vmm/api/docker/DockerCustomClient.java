package rapid.vmm.api.docker;

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerConfig;
import com.spotify.docker.client.messages.*;
import javafx.util.Pair;
import rapid.vmm.VmInfo;
import rapid.vmm.VmmInfo;
import rapid.vmm.api.util.Flavor;
import rapid.vmm.api.VMManagerConfiguration;
import rapid.vmm.api.util.Quota;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.*;

public class DockerCustomClient {
    private final Logger log = Logger.getLogger("rapid");
//    private final String dockerImageName = "redroid/redroid:11.0.0-latest";
    private final String dockerImageName = "rapid/as";
    private final String networkName = "my-dhcp-net";
    private ContainerConfig containerConfig;
    private Quota quota;

    private static DockerClient dockerClient = null;
    private final static HashSet<Integer> allocatedPorts = new HashSet<>();
    private final static HashMap<UUID, Pair<Integer, Integer>> portsOfVm = new HashMap<>();

    public DockerCustomClient() {
        final DockerClient dockerClient = getDockerClientInstance();
        try {
            List<Container> containers = dockerClient.listContainers();
            for(Container container: containers) {
                String containerName = container.names().get(0);
                if(containerName.startsWith("rapid")) {
                    dockerClient.killContainer(container.id());
                }
            }
            long memory = this.getFreeMemoryFromOS();
            Integer cpus = dockerClient.info().cpus();
//            Integer maxRam = dockerClient.info().memoryLimit();
            long capacity = 25 * 1024L * 1024L * 1024L;
            this.quota = new Quota(Quota.QuotaLocalization.LOCAL, (double)cpus, (double)memory, (double)capacity);
            this.log.info("[DockerClient] Available CPUs   : " + cpus);
            this.log.info("[DockerClient] Available memory : " + memory + "MB");
            this.log.info("[DockerClient] Available disk   : " + capacity / 1024L / 1024L / 1024L + "GB");

        } catch(Exception e) {
            e.printStackTrace();
        }

//        Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
//        ArrayList<PortBinding> ports = new ArrayList<>();
//        ports.add(new PortBinding() {
//            @Override
//            public String hostIp() {
//                return "0.0.0.0";
//            }
//
//            @Override
//            public String hostPort() {
//                return "5555";
//            }
//        });
//        portBindings.put("5555", ports);



    }

    private static DockerClient getDockerClientInstance() {
        if(DockerCustomClient.dockerClient == null) {
//            System.out.println("GETDOCKERCLIENTINSTANCE INVOKED");
            DockerCustomClient.dockerClient = new DefaultDockerClient("unix:///var/run/docker.sock");
            return DockerCustomClient.dockerClient;
        }
        return DockerCustomClient.dockerClient;
    }

    private synchronized static Pair<Integer, Integer> getFreePorts() {
        while(true) {
            try {
                ServerSocket s1 = new ServerSocket(0);
                ServerSocket s2 = new ServerSocket(s1.getLocalPort() - 1);
                s1.close();
                s2.close();
                int port1 = s1.getLocalPort();
                int port2 = s2.getLocalPort();
                if (DockerCustomClient.allocatedPorts.contains(port1) || DockerCustomClient.allocatedPorts.contains(port2))
                    continue;
                DockerCustomClient.allocatedPorts.add(port1);
                DockerCustomClient.allocatedPorts.add(port2);
                return new Pair<Integer, Integer>(port1, port2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String createVM(String containerName, UUID vmUuid, Flavor flavor, String vmImage) throws Exception {

        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        List<PortBinding> randomPortFirst = new ArrayList<>();
        Pair<Integer, Integer> freePorts = getFreePorts();
//        System.out.println("RETRIEVED PORTS - " + freePorts.getKey() + ";   " + freePorts.getValue());
        randomPortFirst.add(PortBinding.create("0.0.0.0", freePorts.getKey().toString()));
        portBindings.put("4322", randomPortFirst);

        List<PortBinding> randomPortSecond = new ArrayList<>();
        randomPortSecond.add(PortBinding.create("0.0.0.0", freePorts.getValue().toString()));
        portBindings.put("4321", randomPortSecond);

        DockerCustomClient.portsOfVm.put(vmUuid, new Pair<Integer, Integer>(freePorts.getKey(), freePorts.getValue()));

        HostConfig hostConfig = HostConfig.builder()
                .privileged(true)
                .memorySwappiness(0)
//                .nanoCpus(10L * 1000L * 1000L)
                .cpuPeriod(100000L)
                .cpuQuota(flavor.getvCpu() * 1000L)
                .memory(flavor.getMemory() * 1024L * 1024L) //getMemory returns in megabytes
                .portBindings(portBindings)
                .build();

        containerConfig = ContainerConfig.builder()
                .image(dockerImageName)
                .hostConfig(hostConfig)
                .exposedPorts("4322", "4321", "5555")
                .build();
        final DockerClient dockerClient = getDockerClientInstance();
        try {
            this.log.info("[DockerClient] Pulling image " + dockerImageName);
//            dockerClient.pull(dockerImageName);
            this.log.info("Image has been pulled");
            this.log.info("[DockerClient] Creating container " + containerName);
            ContainerCreation container = dockerClient.createContainer(containerConfig, containerName);
            //dockerClient.connectToNetwork(container.id(), networkName);
            //dockerClient.disconnectFromNetwork(container.id(), "bridge");

            this.log.info("[DockerClient] Container successfully created");
            this.log.info("[DockerClient] Starting container " + containerName);
            dockerClient.startContainer(container.id());
            this.updateQuota((double)(-flavor.getvCpu()), 0.0D);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vmUuid.toString();
    }

    private Container getContainerByUUID(DockerClient dockerClient, UUID vmId) {
        try {
            for(Container container: dockerClient.listContainers()) {
                List<String> names = container.names();
                for(String name: names) {
                    if(name.contains(vmId.toString())) {
                        return container;
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean resumeVM(UUID vmId) {
        DockerClient dockerClient = getDockerClientInstance();
        try {
            this.log.info("[DockerClient] Connecting to local hypervisor");
            Container container = getContainerByUUID(dockerClient, vmId);
            dockerClient.unpauseContainer(container.id());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean suspendVM(UUID vmId) {
        DockerClient dockerClient = getDockerClientInstance();
        try {
            this.log.info("[DockerClient] Connecting to local hypervisor");
            Container container = getContainerByUUID(dockerClient, vmId);
            dockerClient.pauseContainer(container.id());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteVM(UUID vmId) {
        DockerClient dockerClient = getDockerClientInstance();
        try {
            this.log.info("[DockerClient] Connecting to local hypervisor");
            Container container = getContainerByUUID(dockerClient, vmId);
            dockerClient.killContainer(container.id());
            String flavorType = container.names().get(0).split("-")[1];
            Flavor flavor = VMManagerConfiguration.getFlavor(Flavor.FlavorType.valueOf(flavorType));
            this.updateQuota((double)flavor.getvCpu(), (double)flavor.getDisk());

            Pair<Integer, Integer> ports = DockerCustomClient.portsOfVm.get(vmId);
            DockerCustomClient.allocatedPorts.remove(ports.getKey());
            DockerCustomClient.allocatedPorts.remove(ports.getValue());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private synchronized void updateQuota(double vcpu, double disk) {
        this.log.info("[DockerClient] Updating quota: ");
        this.log.info("[DockerClient] cpu: " + vcpu);
        this.log.info("[DockerClient] disk: " + disk);
        this.quota.setvCpu(this.quota.getvCpu() + vcpu);
        this.quota.setDisk(this.quota.getDisk() + disk);
    }

    public String getVmIp(String filePath, String vmName) {
        DockerClient dockerClient = getDockerClientInstance();
        try {
            List<Container> containers = dockerClient.listContainers();
            for(Container container: containers) {
                for(String name: container.names()) {
                    if(name.contains(vmName)) {
                        ContainerInfo containerInfo = dockerClient.inspectContainer(container.id());
                        //return containerInfo.networkSettings().networks().get(networkName).ipAddress();
                        return containerInfo.networkSettings().ipAddress();
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public int getVmPort(String vmName) {
        DockerClient dockerClient = getDockerClientInstance();
        try {
            List<Container> containers = dockerClient.listContainers();
            for(Container container: containers) {
                for(String name: container.names()) {
                    if(name.contains(vmName)) {
                        ContainerInfo containerInfo = dockerClient.inspectContainer(container.id());
//                        ImmutableMap<String, List<PortBinding>> ports = containerInfo.networkSettings().ports();
                        ImmutableMap<String, List<PortBinding>> ports = containerInfo.hostConfig().portBindings();
                        return Integer.parseInt(ports.get("4322").get(0).hostPort());
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return -1;
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
