package teukka.Client;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class TexasHoldEm extends Activity implements AsyncResponse {
    final AsyncResponse delegate = this;

    TextView[] myCardsText = new TextView[17];
    ImageView[] myCardsSuitsImages = new ImageView[17];
    TextView myHandText, myChipsText, myPotText, mySmallBlindText, myBigBlindText, myDealerButtonText, mySmallBlindButtonText, myBigBlindButtonText;
    TextView[] myPlayersText = new TextView[6];
    EditText mySendMessageEditText;
    ImageView mySendMessageImageView;
    Button mySitButton, mySitOutButton, myCheckCallButton, myBetRaiseButton, myFoldButton;
    SeekBar myBetRaiseAmountSeekBar;
    ProgressBar myTurnTimerProgressBar;
    ListView myMessageList;
    ArrayList<Message> myMessageListItems = new ArrayList<>();
    ArrayAdapter<Message> myMessageListAdapter;

    Drawable[] mySuitsDrawables = new Drawable[5];

    private ArrayList<Player> myPlayers = new ArrayList<>();
    private Player myPlayer;

    private int myMinimumBet = 0;
    private int myCallAmount = 0;
    private int myBetRaiseAmount = 0;
    private int myBetRaiseMinAmount = 0;
    private int myBetRaiseMaxAmount = 0;

    private Player myCurrentPlayerTurn;

    private int myPot;

    private int myDealerPlayerChair;
    private int mySmallBlindPlayerChair;
    private int myBigBlindPlayerChair;
    private int mySmallBlindAmount;
    private int myBigBlindAmount;

    private ArrayList<Games.Card> myCardsOnTable = new ArrayList<>();

    private int myTurnTimerRemainingTime;
    private long myTurnTimerStartTime;

    private Handler myHandler = new Handler();
    private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            int delay = 1000; // 1000 ms = 1 second
            switch (GameSingleton.getStatus()) {
                case ST_OPPONENT_FOUND:
                    myMessageListItems.add(new Message("Press Take A Seat to sit at the table.", Message.MessageType.INFO_MESSAGE));
                    myMessageListAdapter.notifyDataSetChanged();
                    GameSingleton.setStatus(Games.Status.ST_IN_GAME);
                    mySendMessageImageView.setEnabled(true);
                    mySendMessageEditText.setEnabled(true);
                    mySitButton.setEnabled(true);
                    mySitButton.setVisibility(View.VISIBLE);
                    myHandler.postDelayed(myRunnable, delay);
                    break;
                case ST_SEARCHING:
                    myMessageListItems.add(new Message("Searching for a table...", Message.MessageType.INFO_MESSAGE));
                    myMessageListAdapter.notifyDataSetChanged();
                    myHandler.postDelayed(myRunnable, delay);
                    break;
                case ST_IN_GAME:
                    if (myTurnTimerStartTime > 0) {
                        myTurnTimerRemainingTime = (int)Math.floor((System.currentTimeMillis() - myTurnTimerStartTime) / 1000L);
                    } else {
                        myTurnTimerStartTime = 0;
                        myTurnTimerRemainingTime = 0;
                    }
                    myTurnTimerProgressBar.setProgress(myTurnTimerProgressBar.getMax() - myTurnTimerRemainingTime);
                    myHandler.postDelayed(myRunnable, delay);
                    break;
                default:
                    //TODO: Handle this (close the activity and go back to menu?)
                    myHandler.postDelayed(myRunnable, delay);
                    break;
            }
        }
    };

    enum IntegerMeaning {
        CHIPS,
        CHIPS_IN_POT,
        CARD_COUNT,
        CHAIR,
        BLIND,
        BET,
        FOLD,
        TURN,
        WINNER
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texas_hold_em);

        myHandText = (TextView) findViewById(R.id.myHandText);
        myCardsText[0] = (TextView) findViewById(R.id.card1Text);
        myCardsText[1] = (TextView) findViewById(R.id.card2Text);
        myCardsText[2] = (TextView) findViewById(R.id.card3Text);
        myCardsText[3] = (TextView) findViewById(R.id.card4Text);
        myCardsText[4] = (TextView) findViewById(R.id.card5Text);
        myCardsText[5] = (TextView) findViewById(R.id.player1Card1Text);
        myCardsText[6] = (TextView) findViewById(R.id.player1Card2Text);
        myCardsText[7] = (TextView) findViewById(R.id.player2Card1Text);
        myCardsText[8] = (TextView) findViewById(R.id.player2Card2Text);
        myCardsText[9] = (TextView) findViewById(R.id.player3Card1Text);
        myCardsText[10] = (TextView) findViewById(R.id.player3Card2Text);
        myCardsText[11] = (TextView) findViewById(R.id.player4Card1Text);
        myCardsText[12] = (TextView) findViewById(R.id.player4Card2Text);
        myCardsText[13] = (TextView) findViewById(R.id.player5Card1Text);
        myCardsText[14] = (TextView) findViewById(R.id.player5Card2Text);
        myCardsText[15] = (TextView) findViewById(R.id.player6Card1Text);
        myCardsText[16] = (TextView) findViewById(R.id.player6Card2Text);
        myCardsSuitsImages[0] = (ImageView) findViewById(R.id.card1SuitImage);
        myCardsSuitsImages[1] = (ImageView) findViewById(R.id.card2SuitImage);
        myCardsSuitsImages[2] = (ImageView) findViewById(R.id.card3SuitImage);
        myCardsSuitsImages[3] = (ImageView) findViewById(R.id.card4SuitImage);
        myCardsSuitsImages[4] = (ImageView) findViewById(R.id.card5SuitImage);
        myCardsSuitsImages[5] = (ImageView) findViewById(R.id.player1Card1SuitImage);
        myCardsSuitsImages[6] = (ImageView) findViewById(R.id.player1Card2SuitImage);
        myCardsSuitsImages[7] = (ImageView) findViewById(R.id.player2Card1SuitImage);
        myCardsSuitsImages[8] = (ImageView) findViewById(R.id.player2Card2SuitImage);
        myCardsSuitsImages[9] = (ImageView) findViewById(R.id.player3Card1SuitImage);
        myCardsSuitsImages[10] = (ImageView) findViewById(R.id.player3Card2SuitImage);
        myCardsSuitsImages[11] = (ImageView) findViewById(R.id.player4Card1SuitImage);
        myCardsSuitsImages[12] = (ImageView) findViewById(R.id.player4Card2SuitImage);
        myCardsSuitsImages[13] = (ImageView) findViewById(R.id.player5Card1SuitImage);
        myCardsSuitsImages[14] = (ImageView) findViewById(R.id.player5Card2SuitImage);
        myCardsSuitsImages[15] = (ImageView) findViewById(R.id.player6Card1SuitImage);
        myCardsSuitsImages[16] = (ImageView) findViewById(R.id.player6Card2SuitImage);
        myPlayersText[0] = (TextView) findViewById(R.id.player1Text);
        myPlayersText[1] = (TextView) findViewById(R.id.player2Text);
        myPlayersText[2] = (TextView) findViewById(R.id.player3Text);
        myPlayersText[3] = (TextView) findViewById(R.id.player4Text);
        myPlayersText[4] = (TextView) findViewById(R.id.player5Text);
        myPlayersText[5] = (TextView) findViewById(R.id.player6Text);
        myChipsText = (TextView) findViewById(R.id.chipsText);
        myPotText = (TextView) findViewById(R.id.potText);
        mySmallBlindText = (TextView) findViewById(R.id.smallBlindText);
        myBigBlindText = (TextView) findViewById(R.id.bigBlindText);
        myDealerButtonText = (TextView) findViewById(R.id.dealerButtonText);
        mySmallBlindButtonText = (TextView) findViewById(R.id.smallBlindButtonText);
        myBigBlindButtonText = (TextView) findViewById(R.id.bigBlindButtonText);
        mySendMessageEditText = (EditText) findViewById(R.id.sendMessageEditText);
        mySendMessageImageView = (ImageView) findViewById(R.id.sendMessageImageView);
        mySitButton = (Button) findViewById(R.id.sitButton);
        mySitOutButton = (Button) findViewById(R.id.sitOutButton);
        myCheckCallButton = (Button) findViewById(R.id.checkCallButton);
        myBetRaiseButton = (Button) findViewById(R.id.betRaiseButton);
        myFoldButton = (Button) findViewById(R.id.foldButton);
        myBetRaiseAmountSeekBar = (SeekBar) findViewById(R.id.betRaiseAmountSeekBar);
        myTurnTimerProgressBar = (ProgressBar) findViewById(R.id.turnTimerProgressBar);
        myMessageList = (ListView) findViewById(R.id.messageListView);
        myMessageListAdapter = new CustomListAdapter(this, R.layout.text_view_messages, myMessageListItems);
        myMessageList.setAdapter(myMessageListAdapter);

        try
        {
            InputStream is = getAssets().open("hearts.png");
            mySuitsDrawables[0] = Drawable.createFromStream(is, null);
            is = getAssets().open("diamonds.png");
            mySuitsDrawables[1] = Drawable.createFromStream(is, null);
            is = getAssets().open("clubs.png");
            mySuitsDrawables[2] = Drawable.createFromStream(is, null);
            is = getAssets().open("spades.png");
            mySuitsDrawables[3] = Drawable.createFromStream(is, null);
            is = getAssets().open("cardback.png");
            mySuitsDrawables[4] = Drawable.createFromStream(is, null);
            is.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return;
        }

        mySendMessageEditText.setOnEditorActionListener(
            new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                    if (event != null) {
                        if (actionId == EditorInfo.IME_ACTION_SEND
                                || event.getAction() == KeyEvent.ACTION_DOWN
                                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            // TODO: Handle shift adding a new line
                            sendChatMessage(mySendMessageEditText.getText().toString());
                        }
                        return true;
                    }
                    return false;
                }
            });

        mySendMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChatMessage(mySendMessageEditText.getText().toString());
            }
        });

        mySitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sit();
            }
        });

        mySitOutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sitOut();
            }
        });

        myCheckCallButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkCall();
            }
        });

        myBetRaiseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                betRaise();
            }
        });

        myFoldButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                fold();
            }
        });

        myBetRaiseAmountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                myBetRaiseAmount = ((int)Math.floor(((progressValue * (myBetRaiseMaxAmount - myBetRaiseMinAmount)) / 100)) + myBetRaiseMinAmount);

                if (progressValue == 100)
                    myBetRaiseButton.setText(getResources().getString(R.string.all_in_button) + " [" + myBetRaiseAmount + "]");
                else {
                    if (myCallAmount == 0)
                        myBetRaiseButton.setText(getResources().getString(R.string.bet_button) + " [" + myBetRaiseAmount + "]");
                    else
                        myBetRaiseButton.setText(getResources().getString(R.string.raise_button) + " [" + myBetRaiseAmount + "]");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        myPlayer = createPlayer(GameSingleton.getName(), -1, 0);

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("game"));

        searchForTable();
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        myHandler.removeCallbacks(myRunnable);
        super.onDestroy();
    }

    private void sendChatMessage(String message) {
        if (message.length() > 0) {
            SendPacket packet = new SendPacket(SocketSingleton.getSocket(), PacketSyntax.SX_GAME_INFO, PacketSyntax.SX_GAME_CHAT_MESSAGE + message);
            packet.delegate = delegate;
            packet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mySendMessageEditText.setText("");
        }
    }

    private void receiveChatMessage(String sendingPlayer, String message) {
        myMessageListAdapter.add(new Message(sendingPlayer + ": " + message, Message.MessageType.CHAT_MESSAGE));
        myMessageListAdapter.notifyDataSetChanged();
    }

    private void sit() {
        SendPacket packet = new SendPacket(SocketSingleton.getSocket(), PacketSyntax.SX_GAME_INFO, Character.toString(PacketSyntax.SX_GAME_READY_TO_START));
        packet.delegate = delegate;
        packet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mySitButton.setEnabled(false);
        mySitButton.setVisibility(View.INVISIBLE);
    }

    private void sitOut() {
        SendPacket packet = new SendPacket(SocketSingleton.getSocket(), PacketSyntax.SX_GAME_INFO, Character.toString(PacketSyntax.SX_GAME_PLAYER_SIT_OUT));
        packet.delegate = delegate;
        packet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mySitOutButton.setEnabled(false);
        mySitOutButton.setVisibility(View.INVISIBLE);
    }

    private void checkCall() {
        System.out.println("Calling/Checking!");
        SendPacket packet = new SendPacket(SocketSingleton.getSocket(), PacketSyntax.SX_GAME_INFO, PacketSyntax.SX_GAME_BET + "0");
        packet.delegate = delegate;
        packet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        resetButtons();
    }

    private void betRaise() {
        if (myBetRaiseAmount > 0) {
            System.out.println("Betting/Raising!");
            SendPacket packet = new SendPacket(SocketSingleton.getSocket(), PacketSyntax.SX_GAME_INFO, PacketSyntax.SX_GAME_BET + Integer.toString(myBetRaiseAmount));
            packet.delegate = delegate;
            packet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            resetButtons();
        } else {
            myBetRaiseAmountSeekBar.setProgress(100);
            myBetRaiseAmountSeekBar.setProgress(0);
            myBetRaiseAmountSeekBar.setVisibility(View.VISIBLE);
            mySendMessageEditText.setVisibility(View.INVISIBLE);
            mySendMessageImageView.setVisibility(View.INVISIBLE);
        }
    }

    private void fold() {
        System.out.println("Folding!");
        SendPacket packet = new SendPacket(SocketSingleton.getSocket(), PacketSyntax.SX_GAME_INFO, Character.toString(PacketSyntax.SX_GAME_FOLD));
        packet.delegate = delegate;
        packet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        resetButtons();
    }

    private void searchForTable() {
        myHandler.post(myRunnable);
    }

    private Player createPlayer(String name, int chair, int chips) {
        Player player = new Player(name, chair, chips);
        myPlayers.add(player);
        if (chair != -1) {
            changePlayerNameVisibility(chair, View.VISIBLE);
        }
        return player;
    }

    private void removePlayer(Player player) {
        if (player != null) {
            myPlayers.remove(player);
        }
    }

    private void changePlayerCardsVisibility(int chair, int visibility) {
        if (chair == -1)
            return;

        for (int i = 0; i < 2; i++) {
            int index = ((chair * 2) + i + 5);
            myCardsText[index].setVisibility(visibility);
            myCardsSuitsImages[index].setVisibility(visibility);
        }
    }

    private void changePlayerNameVisibility(int chair, int visibility) {
        if (chair == -1)
            return;

        myPlayersText[chair].setVisibility(visibility);
    }

    private ArrayList<Integer> parseIntsFromData(String data) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        String numberString = "";

        for (int i = 0; i < data.length(); i++) {
            if (data.charAt(i) != PacketSyntax.SX_EOO) {
                numberString += data.charAt(i);
            } else {
                arrayList.add(parseIntFromData(numberString));
                numberString = "";
                continue;
            }
        }

        if (numberString.length() > 0)
            arrayList.add(parseIntFromData(numberString));

        System.out.println("List: ");
        for (int integer : arrayList) {
            System.out.println(integer);
        }

        return arrayList;
    }

    private int parseIntFromData(String data) {
        String numberString = "";
        int parsedInteger;

        for (int i = 0; i < data.length(); i++) {
            if (data.charAt(i) != PacketSyntax.SX_EOO) {
                numberString += data.charAt(i);
            } else
                break;
        }

        try {
            parsedInteger = Integer.parseInt(numberString);
        } catch(NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Failed to parse integer from data: " + numberString);
            parsedInteger = -1;
        }

        return parsedInteger;
    }

    private String parseStringFromData(String data) {
        String parsedString = "";

        for (int i = 0; i < data.length(); i++) {
            if (data.charAt(i) != PacketSyntax.SX_EOO) {
                parsedString += data.charAt(i);
            } else
                break;
        }

        return parsedString;
    }

    private void updatePlayerChips(String name, int chips) {
        Player player = getPlayer(name);
        if (player != null) {
            player.setChips(chips);
        } else {
            createPlayer(name, -1, chips);
        }
    }

    private void updatePlayerChipsInPot(String name, int chips) {
        Player player = getPlayer(name);
        if (player != null) {
            player.setChipsInPot(chips);
        } else {
            player = createPlayer(name, -1, 0);
            player.setChipsInPot(chips);
        }
    }

    private void updatePlayerCardCount(String name, int count) {
        Player player = getPlayer(name);
        if (player != null) {
            if (count > 0)
                changePlayerCardsVisibility(player.getChair(), View.VISIBLE);
            else
                changePlayerCardsVisibility(player.getChair(), View.INVISIBLE);
        }
    }

    private void updateButtons() {
        int buttonChairs[] = {myDealerPlayerChair, mySmallBlindPlayerChair, myBigBlindPlayerChair};
        TextView textViews[] = {myDealerButtonText, mySmallBlindButtonText, myBigBlindButtonText};

        for (int i = 0; i < buttonChairs.length; i++) {
            int chair = buttonChairs[i];
            if (chair != -1) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textViews[i].getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_TOP, myPlayersText[chair].getId());
                params.addRule(RelativeLayout.ALIGN_BOTTOM, myPlayersText[chair].getId());
                params.addRule(RelativeLayout.RIGHT_OF, myPlayersText[chair].getId());
                textViews[i].setLayoutParams(params);
                textViews[i].setVisibility(View.VISIBLE);
            } else
                textViews[i].setVisibility(View.INVISIBLE);
        }
    }

    private void updateMyChips() {
        if (myPlayer != null)
            myChipsText.setText(Integer.toString(myPlayer.getChips()));
    }

    private void addToPot(int amount) {
        myPot += amount;
        myPotText.setText(Integer.toString(myPot));
    }

    private void updatePlayerChair(String name, int chair) {
        Player player = getPlayer(name);
        if (player != null) {
            player.setChair(chair);
        } else {
            player = createPlayer(name, chair, 0);
        }

        changePlayerNameVisibility(player.getChair(), View.VISIBLE);
        changePlayerCardsVisibility(player.getChair(), View.INVISIBLE);
    }

    private void updateBlinds() {
        mySmallBlindText.setText(Integer.toString(mySmallBlindAmount));
        myBigBlindText.setText(Integer.toString(myBigBlindAmount));
    }

    private void updateCards() {
        for (Player p: myPlayers) {
            if (p == null)
                continue;
            int chair = p.getChair();
            if (chair == -1)
                continue;

            for (int i = 0; i < 2; i++) {
                int index = ((chair * 2) + i + 5);
                if (p.getCards().size() == 2) {
                    myCardsText[index].setText(p.getCards().get(i).getRankStringShort());
                    myCardsSuitsImages[index].setImageDrawable(mySuitsDrawables[p.getCards().get(i).mySuit]);
                    myCardsText[index].setTypeface(null, Typeface.BOLD);
                } else {
                    myCardsText[index].setText("");
                    myCardsSuitsImages[index].setImageDrawable(mySuitsDrawables[4]);
                }
            }
        }

        if (myPlayer != null && myPlayer.getCards().size() == 2) {
            myHandText.setText(myPlayer.getHand().getBestHandRank());
        }

        for (int i = 0; i < 5; i++) {
            if (myCardsOnTable.size() > i) {
                myCardsText[i].setText(myCardsOnTable.get(i).getRankStringShort());
                myCardsSuitsImages[i].setImageDrawable(mySuitsDrawables[myCardsOnTable.get(i).mySuit]);
            } else {
                myCardsText[i].setText("");
                myCardsSuitsImages[i].setImageDrawable(null);
            }
        }
    }

    private void myTurn() {
        System.out.println("Buttons enabled.");
        myCheckCallButton.setEnabled(true);
        myBetRaiseButton.setEnabled(true);
        myFoldButton.setEnabled(true);

        myCallAmount = 0;
        myBetRaiseAmount = 0;

        if (myPlayer.getChipsInPot() < myMinimumBet) {
            // Player can call or raise
            if (myPlayer.getChips() <= (myMinimumBet - myPlayer.getChipsInPot())) {
                myCallAmount = myPlayer.getChips();
                myCheckCallButton.setText("All In [" + myCallAmount + "]");
                myBetRaiseButton.setEnabled(false);
            } else {
                myCallAmount = myMinimumBet - myPlayer.getChipsInPot();
                myCheckCallButton.setText(getResources().getString(R.string.call_button) + " [" + myCallAmount + "]");
                myBetRaiseMinAmount = Math.min(myPlayer.getChips(), (myCallAmount + myBigBlindAmount));
            }
            myBetRaiseButton.setText(getResources().getString(R.string.raise_button));
        } else {
            // Player can check or bet
            myBetRaiseMinAmount = Math.min(myPlayer.getChips(), myBigBlindAmount);
            myCheckCallButton.setText(getResources().getString(R.string.check_button));
            myBetRaiseButton.setText(getResources().getString(R.string.bet_button));
        }
        myBetRaiseMaxAmount = myPlayer.getChips();

        if (myBetRaiseButton.isEnabled() && myBetRaiseMinAmount == myBetRaiseMaxAmount)
            myBetRaiseAmountSeekBar.setProgress(100);
    }

    private void resetButtons() {
        System.out.println("Buttons reseted.");
        myCallAmount = 0;
        myBetRaiseAmount = 0;
        myCheckCallButton.setText(getResources().getString(R.string.check_button));
        myBetRaiseButton.setText(getResources().getString(R.string.bet_button));
        myCheckCallButton.setEnabled(false);
        myBetRaiseButton.setEnabled(false);
        myFoldButton.setEnabled(false);
        myBetRaiseAmountSeekBar.setVisibility(View.INVISIBLE);
        mySendMessageEditText.setVisibility(View.VISIBLE);
        mySendMessageImageView.setVisibility(View.VISIBLE);
        myBetRaiseAmountSeekBar.setProgress(0);
    }

    private void updateProgressBar(Boolean show) {
        if (show) {
            int chair = myCurrentPlayerTurn.getChair();
            int index = ((chair * 2) + 6);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) myTurnTimerProgressBar.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, myPlayersText[chair].getId());
            params.addRule(RelativeLayout.ALIGN_TOP, myCardsSuitsImages[index].getId());
            params.addRule(RelativeLayout.ALIGN_BOTTOM, myCardsSuitsImages[index].getId());
            params.addRule(RelativeLayout.RIGHT_OF, myCardsSuitsImages[index].getId());
            myTurnTimerProgressBar.setLayoutParams(params);
            myTurnTimerProgressBar.setVisibility(View.VISIBLE);
        } else
            myTurnTimerProgressBar.setVisibility(View.INVISIBLE);

        myTurnTimerProgressBar.setProgress(myTurnTimerProgressBar.getMax());
    }

    private void updatePlayers() {
        for (TextView t: myPlayersText) {
            t.setText("");
        }
        for (Player p: myPlayers) {
            if (p == null)
                continue;
            int chair = p.getChair();
            if (chair == -1)
                continue;

            if (p.getName().equals(GameSingleton.getName())) {
                myPlayersText[chair].setTextColor(Color.RED);
            } else
                myPlayersText[chair].setTextColor(Color.BLACK);

            myPlayersText[chair].setText(p.getName());
            myPlayersText[chair].append(" [" + p.getChipsInPot() + "/" + p.getChips() + "]");
        }

        if (myPlayer != null && myPlayer.getChair() != -1) {
            myCheckCallButton.setVisibility(View.VISIBLE);
            myBetRaiseButton.setVisibility(View.VISIBLE);
            myFoldButton.setVisibility(View.VISIBLE);
            mySitButton.setEnabled(false);
            mySitButton.setVisibility(View.INVISIBLE);
            mySitOutButton.setEnabled(true);
            mySitOutButton.setVisibility(View.VISIBLE);
        } else {
            resetButtons();
            myCheckCallButton.setVisibility(View.INVISIBLE);
            myBetRaiseButton.setVisibility(View.INVISIBLE);
            myFoldButton.setVisibility(View.INVISIBLE);
            mySitButton.setEnabled(true);
            mySitButton.setVisibility(View.VISIBLE);
            mySitOutButton.setEnabled(false);
            mySitOutButton.setVisibility(View.INVISIBLE);
        }
    }

    private void resetPlayers() {
        for (Player p: myPlayers) {
            p.reset();
        }
    }

    private void resetGame() {
        resetPlayers();
        myCardsOnTable.clear();
        myPot = 0;
        mySmallBlindAmount = 0;
        myBigBlindAmount = 0;
        myDealerPlayerChair = -1;
        mySmallBlindPlayerChair = -1;
        myBigBlindPlayerChair = -1;
        myCurrentPlayerTurn = null;
        myHandText.setText("");
        myPotText.setText("0");
        for (int i = 0; i < myPlayers.size(); i++) {
            changePlayerCardsVisibility(i, View.INVISIBLE);
        }
        updateMyChips();
        updateBlinds();
        updateCards();
        resetButtons();
    }

    private void playerFold(String name) {
        Player player = getPlayer(name);
        if (player != null) {
            changePlayerCardsVisibility(player.getChair(), View.INVISIBLE);
            System.out.println("Folded: " + Integer.toString(player.getChair()));
            myMessageListItems.add(new Message(name + " folded.", Message.MessageType.INFO_MESSAGE));
            myMessageListAdapter.notifyDataSetChanged();
        }
    }

    private void playerBet(String name, int amount, boolean blind) {
        Player player = getPlayer(name);

        if (player != null) {
            boolean check = false, call = false, bet = false, raise = false;
            if (amount == 0)
                check = true;
            else if (amount + player.getChipsInPot() <= myMinimumBet)
                call = true;
            else if (myMinimumBet == player.getChipsInPot())
                bet = true;
            else
                raise = true;
            
            int realAmount = player.addChipsInPot(amount);
            addToPot(realAmount);

            if (blind) {
                if (mySmallBlindAmount == 0) {
                    mySmallBlindAmount = amount;

                    if (player.getAllIn())
                        myMessageListItems.add(new Message(name + " posts a Small Blind of " + realAmount + " and goes ALL IN.", Message.MessageType.INFO_MESSAGE));
                    else
                        myMessageListItems.add(new Message(name + " posts a Small Blind of " + realAmount + ".", Message.MessageType.INFO_MESSAGE));
                } else {
                    myBigBlindAmount = amount;

                    if (player.getAllIn())
                        myMessageListItems.add(new Message(name + " posts a Big Blind of " + realAmount + " and goes ALL IN.", Message.MessageType.INFO_MESSAGE));
                    else
                        myMessageListItems.add(new Message(name + " posts a Big Blind of " + realAmount + ".", Message.MessageType.INFO_MESSAGE));

                    myMinimumBet = myBigBlindAmount;
                }
            } else {
                if (raise) {
                    if (player.getAllIn())
                        myMessageListItems.add(new Message(name + " raises " + realAmount + " and goes ALL IN.", Message.MessageType.INFO_MESSAGE));
                    else
                        myMessageListItems.add(new Message(name + " raises " + realAmount + ".", Message.MessageType.INFO_MESSAGE));
                } else if (bet) {
                    if (player.getAllIn())
                        myMessageListItems.add(new Message(name + " bets " + realAmount + " and goes ALL IN.", Message.MessageType.INFO_MESSAGE));
                    else
                    myMessageListItems.add(new Message(name + " bets " + realAmount + ".", Message.MessageType.INFO_MESSAGE));
                } else if (call) {
                    if (player.getAllIn())
                        myMessageListItems.add(new Message(name + " calls " + realAmount + " and goes ALL IN.", Message.MessageType.INFO_MESSAGE));
                    else
                        myMessageListItems.add(new Message(name + " calls " + realAmount + ".", Message.MessageType.INFO_MESSAGE));
                } else if (check) {
                    myMessageListItems.add(new Message(name + " checks.", Message.MessageType.INFO_MESSAGE));
                }

                if (myMinimumBet < player.getChipsInPot())
                    myMinimumBet = player.getChipsInPot();
            }

            myMessageListAdapter.notifyDataSetChanged();
        }
    }

    private void playerWins(String name, int amount) {
        myMessageListItems.add(new Message(name + " wins a pot of " + amount + ".", Message.MessageType.INFO_MESSAGE));
        myMessageListAdapter.notifyDataSetChanged();
    }

    private void playerTurn(String name, int timeLeftOnTurn) {
        if (timeLeftOnTurn > 0) {
            myCurrentPlayerTurn = getPlayer(name);

            if (myCurrentPlayerTurn == null || myCurrentPlayerTurn.getChair() == -1)
                return;

            myTurnTimerProgressBar.setMax(timeLeftOnTurn);
            myTurnTimerStartTime = System.currentTimeMillis();

            for (TextView t : myPlayersText) {
                t.setTypeface(null, Typeface.NORMAL);
            }

            myPlayersText[myCurrentPlayerTurn.getChair()].setTypeface(null, Typeface.BOLD);
            if (myCurrentPlayerTurn == myPlayer) {
                myTurn();
            }
        } else {
            myTurnTimerStartTime = 0;
            if (myCurrentPlayerTurn == myPlayer)
                resetButtons();
        }
    }

    private void parsePlayerCardsFromData(String data) {
        Player player = getPlayer(parseStringFromData(data));
        if (player != null) {
            player.addCardsToCards(parseCardsFromData(data.substring(player.getName().length() + 1)));
        }
    }

    private void parseButtonsData(String data) {
        ArrayList<Integer> buttonsChairs = parseIntsFromData(data);
        if (buttonsChairs.size() > 0)
            myDealerPlayerChair = buttonsChairs.get(0);
        if (buttonsChairs.size() > 1)
            mySmallBlindPlayerChair = buttonsChairs.get(1);
        if (buttonsChairs.size() > 2)
            myBigBlindPlayerChair = buttonsChairs.get(2);
    }

    private void parsePlayerData(String data, IntegerMeaning integerMeaning) {
        String name = "";
        int integer = 0;
        boolean firstEOO = true;

        for (int i = 0; i < data.length(); i++) {
            if (firstEOO) {
                name = parseStringFromData(data.substring(i));
                i += name.length();
                firstEOO = false;
            } else {
                integer = parseIntFromData(data.substring(i));
                i += String.valueOf(integer).length();

                switch (integerMeaning) {
                    case CHIPS:
                        updatePlayerChips(name, integer);
                        break;
                    case CHIPS_IN_POT:
                        updatePlayerChipsInPot(name, integer);
                        break;
                    case CARD_COUNT:
                        updatePlayerCardCount(name, integer);
                        break;
                    case CHAIR:
                        updatePlayerChair(name, integer);
                        break;
                    case BET:
                        playerBet(name, integer, false);
                        break;
                    case FOLD:
                        playerFold(name);
                        break;
                    case BLIND:
                        playerBet(name, integer, true);
                        break;
                    case TURN:
                        playerTurn(name, integer);
                        break;
                    case WINNER:
                        playerWins(name, integer);
                        break;
                }

                updatePlayers();
                updateMyChips();
                firstEOO = true;
            }
        }
    }


    private ArrayList<Games.Card> parseCardsFromData(String data) {
        ArrayList<Games.Card> cards = new ArrayList<>();

        int suit, rank, cardNumber;

        for (int i = 0; i < data.length(); i++) {
            cardNumber = parseIntFromData(data.substring(i));
            suit = (int) Math.floor((cardNumber / 13));
            rank = (cardNumber % 13);
            Games.Card card = new Games.Card(suit, rank);
            cards.add(card);
            i += 2;
        }

        return cards;
    }

    private void announceHands() {
        for (Player p : myPlayers) {
            if (p.getCards() != null && p.getCards().size() == 2 && myCardsOnTable.size() == 5) {
                if (!p.equals(myPlayer)) {
                    p.addCardsToHand(myCardsOnTable);
                    p.addCardsToHand(p.getCards());
                }
                myMessageListItems.add(new Message(p.getName() + " has " + p.getHand().getBestHandRank() + ".", Message.MessageType.INFO_MESSAGE));
            }
        }
        myMessageListAdapter.notifyDataSetChanged();
    }

    private void disconnected(String name) {
        Player player = getPlayer(name);
        if (player != null) {
            removePlayer(player);
            myMessageListItems.add(new Message(name + " disconnected.", Message.MessageType.SYSTEM_MESSAGE));
            myMessageListAdapter.notifyDataSetChanged();
        }
    }

    private Player getPlayer(String name) {
        for (Player p: myPlayers) {
            if (p.getName().equals(name))
                return p;
        }
        return null;
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String data = intent.getStringExtra("message");

            System.out.println("Got data: " + data);

            switch (data.charAt(0)) {
                case PacketSyntax.SX_GAME_PLAYER_CHAIR:
                    System.out.println("Received SX_GAME_PLAYER_CHAIR");
                    parsePlayerData(data.substring(1), IntegerMeaning.CHAIR);
                    break;
                case PacketSyntax.SX_GAME_TABLE_FULL:
                    System.out.println("Received SX_GAME_TABLE_FULL");
                    myMessageListItems.add(new Message("Table is full.", Message.MessageType.INFO_MESSAGE));
                    mySitButton.setEnabled(true);
                    mySitButton.setVisibility(View.VISIBLE);
                    break;
                case PacketSyntax.SX_GAME_PLAYER_CHIPS:
                    System.out.println("Received SX_GAME_PLAYER_CHIPS");
                    parsePlayerData(data.substring(1), IntegerMeaning.CHIPS);
                    break;
                case PacketSyntax.SX_GAME_PLAYER_CHIPS_IN_POT:
                    System.out.println("Received SX_GAME_PLAYER_CHIPS_IN_POT");
                    parsePlayerData(data.substring(1), IntegerMeaning.CHIPS_IN_POT);
                    break;
                case PacketSyntax.SX_GAME_DISCONNECT:
                    System.out.println("Received SX_GAME_DISCONNECT");
                    disconnected(parseStringFromData(data.substring(1)));
                    updatePlayers();
                    break;
                case PacketSyntax.SX_GAME_DEAL_HAND:
                    System.out.println("Received SX_GAME_DEAL_HAND");
                    myPlayer.getCards().clear();
                    myPlayer.getHand().clear(true);
                    myPlayer.addCardsToCards(parseCardsFromData(data.substring(1)));
                    myPlayer.addCardsToHand(myPlayer.getCards());
                    updateCards();
                    break;
                case PacketSyntax.SX_GAME_DEAL_TABLE:
                    System.out.println("Received SX_GAME_DEAL_TABLE");
                    myCardsOnTable = parseCardsFromData(data.substring(1));
                    myPlayer.addCardsToHand(myCardsOnTable);
                    updateCards();
                    break;
                case PacketSyntax.SX_GAME_PLAYER_HAND:
                    System.out.println("Received SX_GAME_PLAYER_HAND");
                    parsePlayerCardsFromData(data.substring(1));
                    updateCards();
                    updateProgressBar(false);
                    break;
                case PacketSyntax.SX_GAME_PLAYER_CARD_COUNT:
                    System.out.println("Received SX_GAME_PLAYER_CARD_COUNT");
                    parsePlayerData(data.substring(1), IntegerMeaning.CARD_COUNT);
                    updateCards();
                    break;
                case PacketSyntax.SX_GAME_BLINDS:
                    System.out.println("Received SX_GAME_BLINDS");
                    resetGame();
                    parsePlayerData(data.substring(1), IntegerMeaning.BLIND);
                    updateBlinds();
                    break;
                case PacketSyntax.SX_GAME_BUTTONS_CHAIRS:
                    System.out.println("Received SX_GAME_BUTTONS_CHAIRS");
                    parseButtonsData(data.substring(1));
                    updateButtons();
                    break;
                case PacketSyntax.SX_GAME_BET:
                    System.out.println("Received SX_GAME_BET");
                    parsePlayerData(data.substring(1), IntegerMeaning.BET);
                    break;
                case PacketSyntax.SX_GAME_FOLD:
                    System.out.println("Received SX_GAME_FOLD");
                    parsePlayerData(data.substring(1), IntegerMeaning.FOLD);
                    break;
                case PacketSyntax.SX_GAME_PLAYER_TURN:
                    System.out.println("Received SX_GAME_PLAYER_TURN");
                    parsePlayerData(data.substring(1), IntegerMeaning.TURN);
                    updateProgressBar(true);
                    break;
                case PacketSyntax.SX_GAME_POT:
                    System.out.println("Received SX_GAME_POT");
                    parsePlayerData(data.substring(1), IntegerMeaning.WINNER);
                    break;
                case PacketSyntax.SX_GAME_HAND_ENDED:
                    System.out.println("Received SX_GAME_HAND_ENDED");
                    announceHands();
                    updateProgressBar(false);
                    break;
                case PacketSyntax.SX_GAME_PLAYER_SIT_OUT:
                    System.out.println("Received SX_GAME_PLAYER_SIT_OUT");
                    Player sittingOutPlayer = getPlayer(parseStringFromData(data.substring(1)));
                    if (sittingOutPlayer != null) {
                        changePlayerNameVisibility(sittingOutPlayer.getChair(), View.INVISIBLE);
                        changePlayerCardsVisibility(sittingOutPlayer.getChair(), View.INVISIBLE);
                        sittingOutPlayer.setChair(-1);
                        updatePlayers();
                    }
                    break;
                case PacketSyntax.SX_GAME_CHAT_MESSAGE:
                    System.out.println("Received SX_GAME_CHAT_MESSAGE");
                    // TODO: Make this non-crashable
                    if (data.length() > 3) {
                        String sendingPlayer = parseStringFromData(data.substring(1));
                        if (sendingPlayer.length() > 0) {
                            String message = parseStringFromData(data.substring(2 + sendingPlayer.length()));
                            System.out.println("Received a message from player " + sendingPlayer + ": " + message);
                            receiveChatMessage(sendingPlayer, message);
                        }
                    }
                    break;
                case PacketSyntax.SX_GAME_NOT_ENOUGH_PLAYERS:
                    System.out.println("Received SX_GAME_NOT_ENOUGH_PLAYERS");
                    myMessageListItems.add(new Message("Not enough players. Waiting for players to sit.", Message.MessageType.INFO_MESSAGE));
                    resetGame();
                    break;
            }
            myMessageListAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onProcessFinish(ConnectionStatus status) {

    }
}
