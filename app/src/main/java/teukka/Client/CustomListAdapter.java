package teukka.Client;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;

public class CustomListAdapter extends ArrayAdapter<Message> {

    private Context myContext;
    private TextView myMessageTextView;
    private List <Message>myItems;
    private ActionMode myActionMode;
    private Message selectedMessage;

    private final Typeface myTypeface;

    public static final int colorBG1 = 0xFFFFFFFF;
    public static final int colorBG2 = 0xFFF0F0FF;
    public static final int colorBGSelected1 = 0xFFF0F0F0;
    public static final int colorBGSelected2 = 0xFFE0E0FF;
    public static final int colorTextChatMessage = 0xFF116622;
    public static final int colorTextChatMessageOwn = Color.BLUE;
    public static final int colorTextSystemMessage = Color.BLACK;


    public CustomListAdapter(Context context, int textViewResourceId, List<Message> itemList)
    {
        super(context, textViewResourceId, itemList);
        myContext = context;
        myItems = itemList;
        myTypeface = Typeface.createFromAsset(myContext.getAssets(), "dejavusans.ttf");
    }


    @Override
    public void add(Message message) {
        myItems.add(message);
    }

    private ActionMode.Callback myActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();

            switch (selectedMessage.getMessageType()) {
                case CHAT_MESSAGE:
                    inflater.inflate(R.menu.menu_list_chat_message, menu);
                    if (selectedMessage.getParsedData().length() > 0) {
                        if (GameSingleton.getName().equals(selectedMessage.getParsedData())) {
                            menu.findItem(R.id.menuAddFriend).setEnabled(false);
                            menu.findItem(R.id.menuMute).setEnabled(false);
                        }
                    }
                    break;
                default:
                    inflater.inflate(R.menu.menu_list_system_message, menu);
                    break;
            }

            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menuCopy:
                    ClipboardManager clipboard = (ClipboardManager) myContext.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(null, selectedMessage.toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(myContext, "Text copied to clipboard.", Toast.LENGTH_SHORT).show();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menuAddFriend:
                    if (selectedMessage.getParsedData().length() == 0)
                        return false;
                    Toast.makeText(myContext, "Adding " + selectedMessage.getParsedData() + " to friends.", Toast.LENGTH_SHORT).show();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menuMute:
                    if (selectedMessage.getParsedData().length() == 0)
                        return false;
                    Toast.makeText(myContext, "Muting " + selectedMessage.getParsedData() + ".", Toast.LENGTH_SHORT).show();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            myActionMode = null;
            selectedMessage = null;
        }
    };

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View textView = super.getView(position, convertView, parent);

        myMessageTextView = (TextView) textView;
        myMessageTextView.setTypeface(myTypeface);

        final Message message = myItems.get(position);

        if (message != null)
        {
            myMessageTextView.setText(message.toString());

            myMessageTextView.setOnLongClickListener(new View.OnLongClickListener() {
                // Called when the user long-clicks on someView
                public boolean onLongClick(View view) {
                    if (myActionMode != null) {
                        return false;
                    }

                    selectedMessage = message;

                    // Start the CAB using the ActionMode.Callback defined above
                    myActionMode = ((Activity) myContext).startActionMode(myActionModeCallback);
                    view.setSelected(true);
                    return true;
                }
            });

            if (message.getMessageType() == Message.MessageType.CHAT_MESSAGE) {
                if (GameSingleton.getName().equals(message.getParsedData()))
                    myMessageTextView.setTextColor(colorTextChatMessageOwn);
                else
                    myMessageTextView.setTextColor(colorTextChatMessage);
            } else
                myMessageTextView.setTextColor(colorTextSystemMessage);


            if (textView.isSelected()) {
                if (position % 2 == 0) {
                    myMessageTextView.setBackgroundColor(colorBGSelected1);
                } else {
                    myMessageTextView.setBackgroundColor(colorBGSelected2);
                }
            } else {
                if (position % 2 == 0) {
                    myMessageTextView.setBackgroundColor(colorBG1);
                } else {
                    myMessageTextView.setBackgroundColor(colorBG2);
                }
            }
        }

        return textView;
    }
}