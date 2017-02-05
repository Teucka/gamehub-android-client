package teukka.Client;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends Activity implements AsyncResponse {
    Button myConnectButton, myGameSelectionButton, myDisconnectButton;
    ListView myMessageList;
    TextView myDebugTextView;
    ArrayList<Message> myMessageListItems = new ArrayList<>();
    ArrayAdapter<Message> myMessageListAdapter;

    Client myClient = null;

    String myUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myConnectButton = (Button) findViewById(R.id.connectButton);
        myGameSelectionButton = (Button) findViewById(R.id.gameSelectionButton);
        myDisconnectButton = (Button) findViewById(R.id.disconnectButton);
        myMessageList = (ListView) findViewById(R.id.messageListView);
        myDebugTextView = (TextView) findViewById(R.id.messageTextView);
        myMessageListAdapter = new CustomListAdapter(this, R.layout.text_view_messages, myMessageListItems);
        myMessageList.setAdapter(myMessageListAdapter);

        GameSingleton.setStatus(Games.Status.ST_IDLE);

        myConnectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showUserNameDialog(getResources().getString(R.string.username_info_text));
            }
        });

        myGameSelectionButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                switchView();
            }
        });

        myDisconnectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        myUsername = getUsernameFromFile();
        showUserNameDialog(getResources().getString(R.string.username_info_text));
    }

    private void switchView() {
        Intent i = new Intent(getApplicationContext(), SelectGame.class);
        if (i != null) {
            System.out.println("Switching view");
            startActivity(i);
        }
    }

    private void showUserNameDialog(String usernameInfo) {
        // Get prompt_user_name.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.prompt_user_name, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // Set prompt_user_name.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText usernameEditText = (EditText) promptsView.findViewById(R.id.usernameEditText);

        if (myUsername.length() > 0)
            usernameEditText.setText(myUsername);

        TextView usernameTextView = (TextView) promptsView.findViewById(R.id.usernameInfoTextView);
        usernameTextView.setText(usernameInfo);

        // Create dialog button
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Connect",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                myUsername = usernameEditText.getText().toString();
                                saveUsernameToFile(myUsername);
                                connect();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilder.create();

        // Handle enter key presses
        usernameEditText.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE
                                || event != null
                                && event.getAction() == KeyEvent.ACTION_DOWN
                                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                            return true;
                        }
                        return false;
                    }
                });

        alertDialog.show();
    }

    private void saveUsernameToFile(String username) {
        System.out.println("Saving: " + username);
        File file = new File(this.getFilesDir(), getResources().getString(R.string.username_file));
        try {
            if (!file.exists())
                file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(username.getBytes(Charset.forName("UTF-8")));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getUsernameFromFile() {
        System.out.println("Reading username.");
        FileInputStream fs = null;
        String username = "";

        try {
            File file = new File(this.getFilesDir(), getResources().getString(R.string.username_file));
            fs = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return username;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(fs));

        try {
            username = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return username;
        }

        System.out.println("Username read: " + username);

        return username;
    }

    private void updateButtons(int statusID) {
        if (statusID != 0) {
            if (statusID > 0) {
                myConnectButton.setEnabled(false);
                if (statusID == 2) {
                    switchView();
                }
            } else if (statusID < 0) {
                myConnectButton.setEnabled(true);
                if (statusID == -2) {
                    // Username is already in use
                    showUserNameDialog(getResources().getString(R.string.username_taken_text));
                } else if (statusID == -3) {
                    // Username is invalid
                    showUserNameDialog(getResources().getString(R.string.username_is_invalid_text));
                } else if (statusID == -4) {
                    // Username is too short
                    showUserNameDialog(getResources().getString(R.string.username_is_too_short_text));
                } else if (statusID == -5) {
                    // Username is too long
                    showUserNameDialog(getResources().getString(R.string.username_is_too_long_text));
                }
            }

            myDisconnectButton.setEnabled(!myConnectButton.isEnabled());
            myGameSelectionButton.setEnabled(!myConnectButton.isEnabled());
        }
    }

    protected void connect() {
        if (myClient == null || myClient.getStatus() == AsyncTask.Status.FINISHED) {
            onProcessFinish(new ConnectionStatus("Attempting to connect.", 0));
            myClient = new Client(myUsername,
                    getResources().getString(R.string.server_ip_text),
                    Integer.parseInt(getResources().getString(R.string.server_port_text)),
                    getApplicationContext());
            myClient.delegate = this;
            myClient.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            myConnectButton.setEnabled(false);
        } else {
            onProcessFinish(new ConnectionStatus("Already connected!", 1));
        }
    }

    protected void disconnect() {
        if (myClient != null) {
            try {
                myClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onProcessFinish(ConnectionStatus status) {
        if (status.myStatusID < 0) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            disconnect();
        }
        myMessageListItems.add(new Message(status.myStatusText, Message.MessageType.SYSTEM_MESSAGE));
        myMessageListAdapter.notifyDataSetChanged();
        updateButtons(status.myStatusID);
        //GameSingleton.setStatus(Games.Status.ST_DISCONNECTED);
    }
}