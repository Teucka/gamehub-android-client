package teukka.Client;


import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class Response {
    volatile int responseID = 0;
    volatile int responseType = 0;
    volatile int length = -1;
    volatile String rawLength = "";
    volatile private ByteArrayOutputStream data = new ByteArrayOutputStream();
    volatile String dataString = "";

    public void reset() {
        //responseID = 0;
        responseType = 0;
        length = -1;
        rawLength = "";
        data.reset();
    }

    public void addChar(char b) {
        data.write(b);
    }

    public void calculateDataString() {
        if (data.size() > 0)
            try {
                dataString = new String(data.toByteArray(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
    }
}
