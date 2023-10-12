package rapid.vmm;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class ThreadPooledServer implements Runnable {
    private Logger logger = Logger.getLogger(getClass());

    protected int serverPort;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected Thread runningThread = null;
    protected ExecutorService threadPool = null;

    public ThreadPooledServer(int port) {
        this.serverPort = port;
        VMManager vmManager = VMManager.getInstance();
        //threadPool = Executors.newFixedThreadPool(vmManager.getMaxConnection());
        threadPool = Executors.newCachedThreadPool();
    }

    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while (!isStopped()) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if (isStopped()) {
                    logger.info("Server Stopped.");
                    break;
                }
                logger.info("Error accepting client connection: " + e.getMessage());
                throw new RuntimeException("Error accepting client connection", e);
            }
            this.threadPool.execute(new WorkerRunnable(clientSocket, "Thread Pooled Server"));
        }
        this.threadPool.shutdown();
        logger.info("Server Stopped.");
    }

    /**
     * @return
     */
    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    /**
     *
     */
    public synchronized void stop() {
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    /**
     *
     */
    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port serverport", e);
        }
    }
}