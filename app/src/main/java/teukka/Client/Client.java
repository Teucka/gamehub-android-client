package teukka.Client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.content.Context;
import android.os.AsyncTask;

public class Client extends AsyncTask<String, ConnectionStatus, ConnectionStatus> implements AsyncResponse {
    private String myUsername;
    private String myAddress;
    private int myPort;

    public AsyncResponse delegate = null;
    private ConnectionStatus myStatus;

    private Context myContext;

    private Socket mySocket = null;
    private Reader myReader = null;
    private ResponseHandler myResponseHandler = null;

    Client(String username, String address, int port, Context context) {
        if (username == null || username.length() == 0)
            myUsername = "Guest";
        else
            myUsername = username;
        myAddress = address;
        myPort = port;
        myContext = context;
        myStatus = new ConnectionStatus("", -1);
    }

    public void disconnect() throws IOException {
        if (mySocket != null) {
            try {
                mySocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    protected void startSession() throws IOException {
        myStatus.myStatusText = "Connected.";
        myStatus.myStatusID = 0;
        publishProgress(myStatus);

        myReader = new Reader(mySocket);
        myReader.delegate = this;
        myReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        myResponseHandler = new ResponseHandler(mySocket, myContext);
        myResponseHandler.delegate = this;

        SendPacket packet = new SendPacket(mySocket, PacketSyntax.SX_HELLO, myUsername);
        packet.delegate = this;
        packet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        int ping = -1;
        int lastPingID = -1;
        long timeLastPacket = System.currentTimeMillis();

        while (true) {
            if (System.currentTimeMillis() - timeLastPacket > 10000) {
                // No data has been received for 10 seconds
                myStatus.myStatusText = "Connection timed out.";
                myStatus.myStatusID = -1;
                //publishProgress(myStatus);
                return;
            }
            if (mySocket == null || mySocket.isClosed()) {
                myStatus.myStatusText = "Socket is closed or null.";
                myStatus.myStatusID = -1;
                //publishProgress(myStatus);
                return;
            }
            if (myReader.gotNewMessage()) {
                timeLastPacket = System.currentTimeMillis();
                myResponseHandler.handleResponse(myReader.getCurrentResponse());
                myReader.setLastHandledResponseID(myReader.getCurrentResponse().responseID);
            }
            ping = myResponseHandler.getPing();
            if (ping != -1 && myResponseHandler.getPingID() != lastPingID) {
                lastPingID = myResponseHandler.getPingID();
            }
            try {
                Thread.sleep(1);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    protected ConnectionStatus doInBackground(String... params) {
        myStatus.myStatusText = "Unable to connect.";

        try {
            SocketAddress socketAddress = new InetSocketAddress(myAddress, myPort);
            mySocket = new Socket();
            SocketSingleton.setSocket(mySocket);
            int timeout = 5000; // 5000 ms = 5 seconds
            mySocket.connect(socketAddress, timeout);
            if (mySocket != null && !mySocket.isClosed())
                startSession();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            myStatus.myStatusText = "UnknownHostException: " + e.toString();
        } catch (ConnectException e) {
            e.printStackTrace();
            myStatus.myStatusText  = "ConnectException: " + e.toString();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            myStatus.myStatusText  = "SocketTimeoutException: " + e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            myStatus.myStatusText  = "IOException: " + e.toString();
        } catch (Exception e) {
            e.printStackTrace();
            myStatus.myStatusText  = "Exception: " + e.toString();
        }

        myStatus.myStatusID = -1;
        return myStatus;
    }

    @Override
    protected void onProgressUpdate(ConnectionStatus... connectionStatus) {
        delegate.onProcessFinish(connectionStatus[0]);
    }

    @Override
    protected void onPostExecute(ConnectionStatus connectionStatus) {
        // Thread stopped: connection was closed
        // Enable Connect button
        try {
            mySocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectionStatus.myStatusID = -1;
        delegate.onProcessFinish(connectionStatus);
    }

    @Override
    public void onProcessFinish(ConnectionStatus connectionStatus) {
        //System.out.println("Client onProcessFinish: " + connectionStatus.myStatusText);
        //System.out.println("ID: " + connectionStatus.myStatusID);
        publishProgress(connectionStatus);
    }
}