package teukka.Client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SelectGame extends AppCompatActivity implements AsyncResponse {

    Button myTexasHoldEmButton;

    @Override
    public void onProcessFinish(ConnectionStatus status) {

    }


    private void switchView(int game) {
        Intent i = null;

        switch (game) {
            case Games.GM_TEXAS_HOLD_EM:
                i = new Intent(getApplicationContext(), TexasHoldEm.class);
                break;
        }

        if (i != null) {
            System.out.println("Switching view");
            startActivity(i);
        }
    }

    private void searchForOpponent(char gameType) {
        String data = Character.toString(gameType);
        SendPacket packet = new SendPacket(SocketSingleton.getSocket(), PacketSyntax.SX_SEARCH_OPPONENT, data);
        packet.delegate = this;
        packet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        switchView(gameType);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_game);

        myTexasHoldEmButton = (Button) findViewById(R.id.texasHoldEmButton);

        myTexasHoldEmButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                searchForOpponent(Games.GM_TEXAS_HOLD_EM);
            }
        });
    }
}
