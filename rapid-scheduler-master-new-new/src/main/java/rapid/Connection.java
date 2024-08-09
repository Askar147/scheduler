package rapid;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Connection {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Connection(Socket socket, ObjectInputStream in, ObjectOutputStream out) throws IOException {
        this.socket = socket;
        this.out = out;
        this.out.flush(); // Ensure that the stream is ready for writing
        this.in = in;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectInputStream getInputStream() {
        return in;
    }

    public ObjectOutputStream getOutputStream() {
        return out;
    }

    public void close() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
