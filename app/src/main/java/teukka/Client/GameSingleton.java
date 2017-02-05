package teukka.Client;


public class GameSingleton {

    private volatile static Games.Status myGameStatus;
    private volatile static String myName;

    public static void setStatus(Games.Status gameStatus) {
        if (gameStatus == Games.Status.ST_OPPONENT_FOUND && myGameStatus != Games.Status.ST_SEARCHING)
            return;
        GameSingleton.myGameStatus = gameStatus;
    }

    public static Games.Status getStatus() {
        return GameSingleton.myGameStatus;
    }

    public static void setName(String name) {
        myName = name;
    }

    public static String getName() {
        return myName;
    }
}