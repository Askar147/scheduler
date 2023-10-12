package rapid.vmm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import eu.project.rapid.common.RapidMessages;

public class WorkerRunnable implements Runnable {
    private Logger logger = Logger.getLogger(getClass());

    protected Socket clientSocket = null;
    protected String serverText = null;

    public WorkerRunnable(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText = serverText;
    }

    public void run() {
        try {
            VMMEngine vmmEngine = VMMEngine.getInstance();

            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            int command = in.readByte();

            //logger.info("Started processing request: " + command);

            switch (command) {
                case RapidMessages.SLAM_START_VM_VMM:
                    vmmEngine.slamStartVmVMM(in, out);
                    break;
                case RapidMessages.AS_RM_NOTIFY_VMM:
                    vmmEngine.asRmNotifyVmm(in, out);
                    break;
                case RapidMessages.DS_VM_DEREGISTER_VMM:
                    vmmEngine.dsVmDeregisterVmm(in, out);
                    break;
                case RapidMessages.DS_VMM_SUSPEND:
                    vmmEngine.dsVmmSuspend(in, out);
                    break;
                case RapidMessages.AS_RM_REGISTER_VMM:
                    vmmEngine.asRmRegisterVmm(in, out, clientSocket);
                    break;
                case RapidMessages.SLAM_GET_VMCPU_VMM:
                    //vmmEngine.slamGetVmResourceVmm(in, out, vmmEngine.cpuType);
                    break;
                case RapidMessages.SLAM_GET_VMINFO_VMM:
                    //vmmEngine.slamGetVminfoVmm(in, out);
                    break;
                case RapidMessages.SLAM_CHANGE_VMFLV_VMM:
                    //vmmEngine.slamChangeVmflvVmm(in, out);
                    break;
                case RapidMessages.SLAM_CONFIRM_VMFLV_VMM:
                    //vmmEngine.slamConfirmVmflvVmm(in, out);
                    break;
                case RapidMessages.SLAM_GET_VMMEM_VMM:
                    //vmmEngine.slamGetVmResourceVmm(in, out, vmmEngine.memType);
                    break;
                case RapidMessages.SLAM_GET_VMDISK_VMM:
                    //vmmEngine.slamGetVmResourceVmm(in, out, vmmEngine.diskType);
                    break;
            }

            logger.info("Finished processing request: " + command);

            in.close();
            out.close();
            clientSocket.close();

        } catch (IOException e) {
            String message = "";
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                message = message + System.lineSeparator() + stackTraceElement.toString();
            }
            logger.error("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);
            logger.info("Caught Exception: " + e.getMessage() + System.lineSeparator() + message);

            e.printStackTrace();
        }
    }
}