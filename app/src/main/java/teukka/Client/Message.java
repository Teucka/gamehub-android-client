package teukka.Client;

/**
 * Created by Teukka on 29.1.2017.
 */

public class Message {
    private String data;
    private MessageType type;
    private String parsedData;

    enum MessageType {
        SYSTEM_MESSAGE,
        INFO_MESSAGE,
        CHAT_MESSAGE
    }

    public Message(String data, MessageType type) {
        this.data = data;
        this.type = type;
        this.parsedData = null;
    }

    public String getParsedData() {
        if (parsedData == null) {
            switch (type) {
                case CHAT_MESSAGE:
                    parsedData = data.substring(0, data.indexOf(':'));
                    break;
                default:
                    return "";
            }
        }
        return parsedData;
    }

    public MessageType getMessageType() {
        return type;
    }

    @Override
    public String toString() {
        return data;
    }
}
