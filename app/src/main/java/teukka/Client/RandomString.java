package teukka.Client;

import java.security.SecureRandom;
import java.math.BigInteger;

public final class RandomString {
    private SecureRandom random = new SecureRandom();

    public String nextString() {
        return new BigInteger(25, random).toString(32);
    }
}