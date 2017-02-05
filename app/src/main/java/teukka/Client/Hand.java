package teukka.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Teukka on 16.9.2016.
 */
public class Hand {

    public enum HandRank {
        STRAIGHT_FLUSH,
        FOUR_OF_A_KIND,
        FULL_HOUSE,
        FLUSH,
        STRAIGHT,
        THREE_OF_A_KIND,
        TWO_PAIRS,
        PAIR,
        HIGH_CARD,
        NOT_KNOWN
    }

    private ArrayList<Games.Card> mCards = new ArrayList<>();
    private ArrayList<Games.Card> mBestHand = new ArrayList<>();
    private HandRank mBestHandRank = HandRank.NOT_KNOWN;

    private ArrayList<Games.Card> mStraightFlush = null;
    private ArrayList<Games.Card> mFourOfAKind = null;
    private ArrayList<Games.Card> mFullHouse = null;
    private ArrayList<Games.Card> mFlush = null; // Can contain more than fire cards
    private ArrayList<Games.Card> mStraight = null;
    private ArrayList<Games.Card> mThreeOfAKind = null;
    private ArrayList<Games.Card> mTwoPairs = new ArrayList<>();
    private ArrayList<Games.Card> mPairs = new ArrayList<>();

    public void addCards(ArrayList<Games.Card> cards) {
        /*
        if (cards.size() < 2 || cards.size() > 7) {
            throw new IllegalArgumentException("Hand() must consist of two to seven cards! Given: " + cards.size());
        }
        */
        for (Games.Card c : cards) {
            if (!mCards.contains(c))
                mCards.add(c);
        }
        sortCardsByRank();
    }

    public void addCard(Games.Card card) {
        if (!mCards.contains(card)) {
            mCards.add(card);
            sortCardsByRank();
        }
    }

    private void sortCardsByRank() {
        Collections.sort(mCards);
    }

    private Games.Card getCardByRank(ArrayList<Games.Card> cards, int rank) {
        for (Games.Card c : mCards) {
            if (c.myRank == rank)
                return c;
        }
        return null;
    }

    private ArrayList<Games.Card> getStraight(ArrayList<Games.Card> cards) {
        int cardsTotal = cards.size();
        if (cardsTotal < 5)
            return null;

        int maxRank = cards.get(0).myRank;
        int minRank = cards.get(cards.size()-1).myRank;

        if ((maxRank - minRank) < 4)
            return null;

        ArrayList<Games.Card> straightCards = new ArrayList<>();

        if (maxRank == 12) { // Handle ace as the lowest rank card
            Games.Card card = getCardByRank(cards, 12);
            if (card != null) {
                straightCards.add(card);
                for (int i = 0; i < cardsTotal -1; i++) {
                    card = getCardByRank(cards, i);
                    if (card != null)
                        straightCards.add(card);
                    else
                        break;
                }
            }

            if (straightCards.size() == 5)
                return straightCards;

            straightCards.clear();
        }

        for (int i = minRank; i < (maxRank - 3); i++) {
            for (int j = 0; j < cardsTotal; j++) {
                Games.Card card = getCardByRank(cards, (maxRank - j - (i - minRank)));
                if (card != null) {
                    straightCards.add(card);
                    if (straightCards.size() == 5)
                        return straightCards;
                } else
                    break;
            }

            if (straightCards.size() == 5)
                return straightCards;

            straightCards.clear();
        }

        return null;
    }

    private void calculateFlushes() {
        ArrayList<Games.Card> hearts = new ArrayList<>();
        ArrayList<Games.Card> diamonds = new ArrayList<>();
        ArrayList<Games.Card> clubs = new ArrayList<>();
        ArrayList<Games.Card> spades = new ArrayList<>();

        for (Games.Card c : mCards) {
            if (c.mySuit == 0)
                hearts.add(c);
            else if (c.mySuit == 1)
                diamonds.add(c);
            else if (c.mySuit == 2)
                clubs.add(c);
            else if (c.mySuit == 3)
                spades.add(c);
        }

        if (hearts.size() > 4)
            mFlush = hearts;
        else if (diamonds.size() > 4)
            mFlush = diamonds;
        else if (clubs.size() > 4)
            mFlush = clubs;
        else if (spades.size() > 4)
            mFlush = spades;
    }

    private ArrayList<Games.Card> getFourOfAKind() {
        if (mCards.size() < 4)
            return null;

        ArrayList<Games.Card> cards = new ArrayList<>();

        for (int i = 0; i < mCards.size() - 3; i++) {
            if (mCards.get(i).myRank == mCards.get(i + 1).myRank && mCards.get(i).myRank == mCards.get(i + 2).myRank && mCards.get(i).myRank == mCards.get(i + 3).myRank) {
                cards.add(mCards.get(i));
                cards.add(mCards.get(i + 1));
                cards.add(mCards.get(i + 2));
                cards.add(mCards.get(i + 3));
                Games.Card card = highestCardNotInCards(cards);
                if (card != null)
                    cards.add(card);
                return cards;
            }
        }

        return null;
    }

    // Must have called getThreeOfAKind() and calculatePairs() before calling this!
    private ArrayList<Games.Card> getFullHouse() {
        if (mCards.size() < 5)
            return null;

        if (mThreeOfAKind != null && mPairs.size() > 1) {
            ArrayList<Games.Card> cards = new ArrayList<>();
            cards.addAll(mThreeOfAKind);
            cards.add(mPairs.get(0));
            cards.add(mPairs.get(1));
            return cards;
        }

        return null;
    }

    private ArrayList<Games.Card> getThreeOfAKind() {
        if (mCards.size() < 3)
            return null;

        ArrayList<Games.Card> cards = new ArrayList<>();

        for (int i = 0; i < mCards.size() - 2; i++) {
            if (mCards.get(i).myRank == mCards.get(i + 1).myRank && mCards.get(i).myRank == mCards.get(i + 2).myRank) {
                cards.add(mCards.get(i));
                cards.add(mCards.get(i + 1));
                cards.add(mCards.get(i + 2));
                return cards;
            }
        }

        return null;
    }

    private void calculatePairs() {
        for (int i = 0; i < mCards.size() - 1; i++) {
            if (mCards.get(i).myRank == mCards.get(i + 1).myRank) {
                // Do not count cards of which there are three and do not allow three pairs
                // if (self.threeOfAKind is None or self.cards[i] not in self.threeOfAKind) and len(self.pairs) < 4:
                if ((mThreeOfAKind == null || !mThreeOfAKind.contains(mCards.get(i))) && mPairs.size() < 4) {
                    mPairs.add(mCards.get(i));
                    mPairs.add(mCards.get(i + 1));
                    i++;
                }
            }
        }
    }

    private Games.Card highestCardNotInCards(ArrayList<Games.Card> cards) {
        for (Games.Card c : mCards) {
            if (!cards.contains(c))
                return c;
        }
        return null;
    }

    private ArrayList<Games.Card> highestCardsNotInCards(ArrayList<Games.Card> cards) {
        ArrayList<Games.Card> highestCards = new ArrayList<>();

        int cardsSize = 0;
        if (cards != null)
            cardsSize = cards.size();

        for (Games.Card c : mCards) {
            if (cardsSize == 0 || !cards.contains(c)) {
                highestCards.add(c);
                if ((cardsSize + highestCards.size()) == 5)
                    return highestCards;
            }
        }

        return highestCards;
    }

    private ArrayList<Games.Card> getBestHand() {
        calculateFlushes();
        mStraight = getStraight(mCards);

        if (mFlush != null) {
            mStraightFlush = getStraight(mFlush);

            if (mStraightFlush != null) {
                mBestHand = mStraightFlush;
                mBestHandRank = HandRank.STRAIGHT_FLUSH;
                return mBestHand;
            }
        }

        mFourOfAKind = getFourOfAKind();
        if (mFourOfAKind != null) {
            mBestHand = mFourOfAKind;
            mBestHandRank = HandRank.FOUR_OF_A_KIND;
            return mBestHand;
        }

        mThreeOfAKind = getThreeOfAKind();
        calculatePairs();
        mFullHouse = getFullHouse();
        if (mFullHouse != null) {
            mBestHand = mFullHouse;
            mBestHandRank = HandRank.FULL_HOUSE;
            return mBestHand;
        }

        if (mFlush != null) {
            mBestHand = new ArrayList<Games.Card>(mFlush.subList(0, 5));
            mBestHandRank = HandRank.FLUSH;
            return mBestHand;
        }

        if (mStraight != null) {
            mBestHand = mStraight;
            mBestHandRank = HandRank.STRAIGHT;
            return mBestHand;
        }

        if (mThreeOfAKind != null) {
            ArrayList<Games.Card> highestCards = highestCardsNotInCards(mThreeOfAKind);
            if (highestCards != null)
                mThreeOfAKind.addAll(highestCards);
            mBestHand = mThreeOfAKind;
            mBestHandRank = HandRank.THREE_OF_A_KIND;
            return mBestHand;
        }

        if (mPairs.size() > 3) {
            mTwoPairs.add(mPairs.get(0));
            mTwoPairs.add(mPairs.get(1));
            mTwoPairs.add(mPairs.get(2));
            mTwoPairs.add(mPairs.get(3));
            Games.Card highestCard = highestCardNotInCards(mTwoPairs);
            if (highestCard != null)
                mTwoPairs.add(highestCard);
            mBestHand = mTwoPairs;
            mBestHandRank = HandRank.TWO_PAIRS;
            return mBestHand;
        }

        if (mPairs.size() == 2) {
            ArrayList<Games.Card> highestCards = highestCardsNotInCards(mPairs);
            if (highestCards != null)
                mPairs.addAll(highestCards);
            mBestHand = mPairs;
            mBestHandRank = HandRank.PAIR;
            return mBestHand;
        }

        mBestHand = highestCardsNotInCards(null);
        mBestHandRank = HandRank.HIGH_CARD;
        return mBestHand;
    }

    /*
    public int compareToHand(Hand hand) {
        if (hand.mBestHand == null || hand.mBestHandRank == Rank.NOT_KNOWN)
            hand.getBestHand();
        if (mBestHand == null || mBestHandRank == Rank.NOT_KNOWN)
            getBestHand();


    }
    */

    public ArrayList<Games.Card> getCards() {
        return mCards;
    }

    public String getBestHandRank() {
        clear(false);
        getBestHand();

        if (mBestHand != null && mCards != null) {
            System.out.println("mBestHand: ");
            for (Games.Card c : mBestHand)
                System.out.println(c.myRank + " of " + c.mySuit);

            System.out.println("mCards: ");
            for (Games.Card c : mCards)
                System.out.println(c.myRank + " of " + c.mySuit);
        }

        switch (mBestHandRank) {
            case STRAIGHT_FLUSH:
                return "Straight Flush (" + mBestHand.get(0).getRankString() + " High)";
            case FOUR_OF_A_KIND:
                return "Four Of A Kind (" + mBestHand.get(0).getRankStringPlural() + ")";
            case FULL_HOUSE:
                return mBestHand.get(0).getRankStringPlural() + " Full Of " + mBestHand.get(3).getRankStringPlural();
            case FLUSH:
                return "Flush (" + mBestHand.get(0).getRankString() + " High)";
            case STRAIGHT:
                return "Straight (" + mBestHand.get(0).getRankString() + " High)";
            case THREE_OF_A_KIND:
                return "Three " + mBestHand.get(0).getRankStringPlural();
            case TWO_PAIRS:
                return "Two Pairs (" + mBestHand.get(0).getRankStringPlural() + " and " + mBestHand.get(2).getRankStringPlural() + ")";
            case PAIR:
                return "A Pair Of " + mBestHand.get(0).getRankStringPlural();
            case HIGH_CARD:
                return mBestHand.get(0).getRankString() + " High";
        }

        return "";
    }

    public void clear(boolean clearCards) {
        if (clearCards)
            mCards.clear();
        mBestHand = null;
        mBestHandRank = HandRank.NOT_KNOWN;

        mStraightFlush = null;
        mFourOfAKind = null;
        mFullHouse = null;
        mFlush = null;
        mStraight = null;
        mThreeOfAKind = null;
        mTwoPairs.clear();
        mPairs.clear();
    }
}
