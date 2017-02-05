package teukka.Client;

/**
 * Created by Teukka on 20.8.2016.
 */
public class Utilities {
    public static boolean isNumeric(String str)
    {
        if (str.isEmpty() || str.length() == 0)
            return false;
        for (char c : str.toCharArray())
        {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
}
