package teukka.Client;

import java.net.Socket;

public class SocketSingleton {

    private static Socket mySocket;

    public static void setSocket(Socket socket) {
        SocketSingleton.mySocket = socket;
    }

    public static Socket getSocket() {
        return SocketSingleton.mySocket;
    }
}