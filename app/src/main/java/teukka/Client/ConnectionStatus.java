package teukka.Client;

/**
 * Created by Teukka on 20.8.2016.
 */
public class ConnectionStatus {
    public String myStatusText = "";
    public int myStatusID = -1;

    ConnectionStatus(String statusText, int statusID) {
        myStatusText = statusText;
        myStatusID = statusID;
    }
}
