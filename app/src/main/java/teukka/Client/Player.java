package teukka.Client;

import java.util.ArrayList;

public class Player {
    private String myName;
    private int myChips;
    private ArrayList<Games.Card> myCards = new ArrayList();
    private Hand myHand = new Hand();
    private int myChair = -1;
    private int myChipsInPot = 0;

    Player(String name, int chair, int chips) {
        reset();
        myName = name;
        myChips = chips;
        myChair = chair;
    }

    public void reset() {
        myCards.clear();
        myHand.clear(true);
        myChipsInPot = 0;
    }

    public String getName() {
        return myName;
    }

    public void setChips(int chips) {
        myChips = chips;
    }
    public int getChips() {
        return myChips;
    }

    public void addCardsToCards(ArrayList<Games.Card> cards) {
        myCards.addAll(cards);
    }
    public ArrayList<Games.Card> getCards() {
        return myCards;
    }

    public void addCardsToHand(ArrayList<Games.Card> cards) {
        myHand.addCards(cards);
    }
    public void addCardToHand(Games.Card card) {
        myHand.addCard(card);
    }
    public Hand getHand() { return myHand; }

    public void setChair(int chair) { myChair = chair; }
    public int getChair() { return myChair; }

    public int getChipsInPot() { return myChipsInPot; }
    public int addChipsInPot(int chips) {
        int addedChips = chips;
        if (myChips < chips)
            addedChips = myChips;

        myChips -= addedChips;
        myChipsInPot += addedChips;
        return addedChips;
    }
    public void setChipsInPot(int chips) {
        myChipsInPot = chips;
    }

    public boolean getAllIn() {
        return (myChips == 0);
    }
}