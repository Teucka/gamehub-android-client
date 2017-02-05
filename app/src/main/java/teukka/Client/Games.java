package teukka.Client;

import java.util.ArrayList;

final class Games {
    public final static char GM_NONE = '\u0000';
    public final static char GM_TEXAS_HOLD_EM = '\u0001';

    public enum Status {
        ST_DISCONNECTED,
        ST_IDLE,
        ST_SEARCHING,
        ST_LOADED_GAME,
        ST_OPPONENT_FOUND,
        ST_IN_GAME
    }

    static final class Card implements Comparable<Card> {
        final int mySuit; // 0 - 3: hearts, diamonds, clubs, spades
        final int myRank; // 0 - 12: ace, 2, 3, 4, 5, 6, 7, 8, 9, 10, jack, queen, king

        Card(int suit, int rank) {
            mySuit = suit;
            myRank = rank;
        }

        public String getRankStringShort() {
            switch (myRank) {
                case 0:
                    return "2";
                case 1:
                    return "3";
                case 2:
                    return "4";
                case 3:
                    return "5";
                case 4:
                    return "6";
                case 5:
                    return "7";
                case 6:
                    return "8";
                case 7:
                    return "9";
                case 8:
                    return "10";
                case 9:
                    return "J";
                case 10:
                    return "Q";
                case 11:
                    return "K";
                case 12:
                    return "A";
            }
            return Integer.toString(myRank);
        }

        public String getRankStringPlural() {
            switch (myRank) {
                case 0:
                    return "Twos";
                case 1:
                    return "Threes";
                case 2:
                    return "Fours";
                case 3:
                    return "Fives";
                case 4:
                    return "Sixes";
                case 5:
                    return "Sevens";
                case 6:
                    return "Eights";
                case 7:
                    return "Nines";
                case 8:
                    return "Tens";
                case 9:
                    return "Jacks";
                case 10:
                    return "Queens";
                case 11:
                    return "Kings";
                case 12:
                    return "Aces";
            }
            return Integer.toString(myRank);
        }

        public String getRankString() {
            switch (myRank) {
                case 0:
                    return "Two";
                case 1:
                    return "Three";
                case 2:
                    return "Four";
                case 3:
                    return "Five";
                case 4:
                    return "Six";
                case 5:
                    return "Seven";
                case 6:
                    return "Eight";
                case 7:
                    return "Nine";
                case 8:
                    return "Ten";
                case 9:
                    return "Jack";
                case 10:
                    return "Queen";
                case 11:
                    return "King";
                case 12:
                    return "Ace";
            }
            return Integer.toString(myRank);
        }

        public String getSuitString() {
            switch (mySuit) {
                case 0:
                    return "Hearts";
                case 1:
                    return "Diamonds";
                case 2:
                    return "Clubs";
                case 3:
                    return "Spades";
            }
            return "Unknown";
        }

        public String toString() {
            return getRankString() + " of " + getSuitString();
        }

        @Override
        public int compareTo(Card c) {
            if (myRank > c.myRank)
                return -1;
            if (myRank < c.myRank)
                return 1;
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;

            if (!Card.class.isAssignableFrom(obj.getClass()))
                return false;

            final Card other = (Card) obj;

            if (this.mySuit != other.mySuit)
                return false;

            if (this.myRank != other.myRank)
                return false;

            return true;
        }
    }
}
