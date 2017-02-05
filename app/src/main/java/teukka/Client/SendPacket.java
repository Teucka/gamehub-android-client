package teukka.Client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import android.os.AsyncTask;

public class SendPacket extends AsyncTask<Void, String, ConnectionStatus> {
    public AsyncResponse delegate = null;

    private Socket mySocket = null;
    private char myHeader = '\0';
    private String myData = "";

    SendPacket(Socket socket, char header, String data) {
        mySocket = socket;
        myHeader = header;
        myData = data;
    }

    @Override
    protected ConnectionStatus doInBackground(Void... voids) {
        ConnectionStatus status = new ConnectionStatus("", -1);
        try {
            DataOutputStream outToServer  = new DataOutputStream(mySocket.getOutputStream());
            String dataToSend;

            if (myData.length() > 0)
                dataToSend = String.format("%c%s%c", myHeader, myData, PacketSyntax.SX_EOR);
            else
                dataToSend = String.format("%c%c", myHeader, PacketSyntax.SX_EOR);

            byte[] buf = dataToSend.getBytes(Charset.forName("UTF-8"));
            outToServer.write(buf, 0, buf.length);
            outToServer.flush();

            System.out.println("Sending: " + dataToSend);
            status.myStatusText = "Sending: " + dataToSend;
            status.myStatusID = 0;
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            status.myStatusText = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println();
            status.myStatusText = "IOException: " + e.toString();
        }

        return status;
    }

    @Override
    protected void onPostExecute(ConnectionStatus connectionStatus) {
        // Finished sending a packet
        //System.out.println("SendPacket finished: " + connectionStatus.myStatusText);
        //System.out.println("ID: " + connectionStatus.myStatusID);
        delegate.onProcessFinish(connectionStatus);
    }
}