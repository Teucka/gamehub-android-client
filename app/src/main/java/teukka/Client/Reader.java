package teukka.Client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

import android.os.AsyncTask;

public class Reader extends AsyncTask<Void, Void, Void> {
    public AsyncResponse delegate = null;
    private ConnectionStatus myStatus;

    private Socket mySocket;
    private volatile boolean newMessage = false;
    private volatile int lastHandledResponseID = -1;
    private Response currentResponse;

    Reader(Socket socket) {
        mySocket = socket;
        myStatus = new ConnectionStatus("", -1);
    }

    public Response getCurrentResponse() {
        newMessage = false;
        return currentResponse;
    }

    public boolean gotNewMessage() {
        return newMessage;
    }

    public void setLastHandledResponseID(int responseID) {
        lastHandledResponseID = responseID;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            currentResponse = new Response();
            InputStream in = mySocket.getInputStream();

            final int bufferSize = 256;
            byte[] buffer = new byte[bufferSize];
            char byteToChar;

            while (true) {
                if (in.read(buffer) > 0) {
                    for (byte b : buffer) {

                        if (b == '\0') {
                            Arrays.fill(buffer, (byte) '\0');
                            break;
                        }

                        byteToChar = (char) (b & 0xFF);

                        if (byteToChar == PacketSyntax.SX_EOR) { // End of a response
                            currentResponse.calculateDataString();
                            newMessage = true;

                            while (lastHandledResponseID != currentResponse.responseID) { // Waiting for ResponseHandler to handle the response
                                //System.out.println("Waiting for ResponseHandler to handle the response");
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                            continue;
                        }

                        if (currentResponse.responseType == 0) { // The first byte indicates the type of the response
                            if (b > 0 && b < 10 || b == -1) {
                                currentResponse.responseType = b;
                                currentResponse.responseID += 1;
                                continue;
                            }
                        }

                        if (currentResponse.responseType > 0) { // We have the type of the response: now store the data
                            currentResponse.addChar(byteToChar);
                            continue;
                        }
                    }

                    //String decoded = new String(buff, "UTF-8");
                    //System.out.println("read data: " + decoded);
                } else {
                    currentResponse.reset();
                    return null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        // Thread stopped: connection was closed
        // Enable Connect button
        myStatus.myStatusText = "Connection closed.";
        myStatus.myStatusID = -1;
        delegate.onProcessFinish(myStatus);
    }
}
