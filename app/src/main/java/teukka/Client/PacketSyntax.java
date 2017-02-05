package teukka.Client;

final class PacketSyntax {
    public final static char SX_EOR = '\u001F'; // End of Response/Request
    public final static char SX_EOO = '\u001E'; // End of Object

    public final static char SX_HELLO = '\u0001';
    public final static char SX_ERROR_NAME_TAKEN = '\u0001';
    public final static char SX_ERROR_ALREADY_CONNECTED = '\u0002';
    public final static char SX_ERROR_INVALID_USERNAME = '\u0003';
    public final static char SX_ERROR_USERNAME_TOO_SHORT = '\u0004';
    public final static char SX_ERROR_USERNAME_TOO_LONG = '\u0005';

    public final static char SX_PING = '\u0002';

    public final static char SX_SEARCH_OPPONENT = '\u0004';
    public final static char SX_NOW_SEARCHING = '\u0001';
    public final static char SX_OPPONENT_FOUND = '\u0002';

    public final static char SX_GAME_INFO = '\u0005';
    public final static char SX_GAME_PLAYER_CHIPS = '\u0001';
    public final static char SX_GAME_READY_TO_START = '\u0002';
    public final static char SX_GAME_DEAL_HAND = '\u0003';
    public final static char SX_GAME_DEAL_TABLE = '\u0004';
    public final static char SX_GAME_BLINDS = '\u0005';
    public final static char SX_GAME_BET = '\u0006';
    public final static char SX_GAME_POT = '\u0007';
    public final static char SX_GAME_DISCONNECT = '\u0008';
    public final static char SX_GAME_NOT_ENOUGH_PLAYERS = '\u000B';
    public final static char SX_GAME_BUTTONS_CHAIRS = '\u000C';
    public final static char SX_GAME_PLAYER_CHIPS_IN_POT = '\u0010';
    public final static char SX_GAME_PLAYER_CHAIR = '\u0011';
    public final static char SX_GAME_TABLE_FULL = '\u0012';
    public final static char SX_GAME_PLAYER_TURN = '\u0013';
    public final static char SX_GAME_FOLD = '\u0014';
    public final static char SX_GAME_PLAYER_HAND = '\u0015';
    public final static char SX_GAME_HAND_ENDED = '\u0016';
    public final static char SX_GAME_PLAYER_SIT_OUT = '\u0017';
    public final static char SX_GAME_CHAT_MESSAGE = '\u0018';
    public final static char SX_GAME_PLAYER_CARD_COUNT = '\u0019';
}
