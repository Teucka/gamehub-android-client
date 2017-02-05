package teukka.Client;

public interface AsyncResponse {
    void onProcessFinish(ConnectionStatus status);
}