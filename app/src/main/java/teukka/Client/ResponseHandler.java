package teukka.Client;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import java.net.Socket;

public class ResponseHandler implements AsyncResponse {
    public AsyncResponse delegate = null;

    private Context myContext;

    private Socket mySocket;

    private volatile int ping = -1;

    private volatile int pingID = -1;

    ResponseHandler(Socket socket, Context context) {
        myContext = context;
        mySocket = socket;
    }

    public int getPing() {
        return ping;
    }

    public int getPingID() {
        return pingID;
    }

    public void handleResponse(Response response) {
        ConnectionStatus status = new ConnectionStatus("", -1);
        //System.out.println("Handling a response: " + response.responseType + " Data: " + response.dataString + " Length: " + response.length);
        switch (response.responseType) {
            case 1:
                switch (response.dataString.charAt(0)) {
                    case PacketSyntax.SX_ERROR_NAME_TAKEN:
                        System.out.println("Name already exists!");
                        status.myStatusText = "From server: Name already exists!";
                        status.myStatusID = -2;
                        break;
                    case PacketSyntax.SX_ERROR_ALREADY_CONNECTED:
                        System.out.println("You are already connected!");
                        status.myStatusText = "From server: You are already connected!";
                        status.myStatusID = 2;
                        break;
                    case PacketSyntax.SX_ERROR_INVALID_USERNAME:
                        System.out.println("Invalid username!");
                        status.myStatusText = "From server: Invalid username!";
                        status.myStatusID = -3;
                        break;
                    case PacketSyntax.SX_ERROR_USERNAME_TOO_SHORT:
                        System.out.println("Username is too short!");
                        status.myStatusText = "From server: Username is too short!";
                        status.myStatusID = -4;
                        break;
                    case PacketSyntax.SX_ERROR_USERNAME_TOO_LONG:
                        System.out.println("Username is too long!");
                        status.myStatusText = "From server: Username is too long!";
                        status.myStatusID = -5;
                        break;
                    default:
                        System.out.println("Received a Hello: " + response.dataString);
                        status.myStatusText = "From server: Hello, " + response.dataString+ ".";
                        GameSingleton.setName(response.dataString);
                        System.out.println("Name set: " + GameSingleton.getName());
                        status.myStatusID = 2;
                        break;
                    }
                break;
            case 2:
                status.myStatusText = "From server: Ping data " + response.dataString + ".";
                SendPacket packet = new SendPacket(mySocket, PacketSyntax.SX_PING, response.dataString);
                packet.delegate = this;
                packet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                status.myStatusID = 1;
                break;
            case 3:
                if (Utilities.isNumeric(response.dataString)) {
                    ping = Integer.parseInt(response.dataString);
                    pingID = response.responseID;
                    status.myStatusText = "From server: Ping " + ping + " ms.";
                }
                status.myStatusID = 1;
                break;
            case 4:
                switch (response.dataString.charAt(0)) {
                    case PacketSyntax.SX_NOW_SEARCHING:
                        status.myStatusText = "From server: Searching for an opponent";
                        GameSingleton.setStatus(Games.Status.ST_SEARCHING);
                        break;
                    case PacketSyntax.SX_OPPONENT_FOUND:
                        status.myStatusText = "From server: Opponent found";
                        GameSingleton.setStatus(Games.Status.ST_OPPONENT_FOUND);
                        System.out.println("Opponent found! Status: " + GameSingleton.getStatus());
                        break;
                }
                status.myStatusID = 1;
                break;
            case 5:
                //status.myStatusText = "From server: Game info: " + response.dataString;
                //System.out.println("Received game data: " + response.dataString);
                sendMessage("game", response.dataString);
                status.myStatusID = 1;
                break;
        }

        //System.out.println("Handled!");

        response.reset();
        onProcessFinish(status);
    }

    private void sendMessage(String action, String message) {
        Intent intent = new Intent(action);
        // You can also include some extra data.
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(myContext).sendBroadcast(intent);
    }

    @Override
    public void onProcessFinish(ConnectionStatus connectionStatus) {
        //System.out.println("ResponseHandler finished: " + connectionStatus.myStatusText);
        //System.out.println("ID: " + connectionStatus.myStatusID);
        delegate.onProcessFinish(connectionStatus);
    }
}
