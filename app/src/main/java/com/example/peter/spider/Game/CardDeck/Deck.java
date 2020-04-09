package com.example.peter.spider.Game.CardDeck;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.example.peter.spider.R;

import java.util.HashMap;

public class Deck {

    private static final String TAG = "DECK";
    private static final int DECK_SIZE = 52, NUM_STACKS = 10;
    public int seed;
    private Card[] deck;
    private HashMap<Integer, Drawable> mStore;

    public Deck(int difficulty, int seed, HashMap<Integer, Drawable> mStore) {
        this.mStore = mStore;
        this.seed = seed;
        deck = createDeck(difficulty);
    }

    private Card[] createDeck(int difficulty) {
        // Create ordered deck with every unique card
        Card[] rawDeck = new Card[DECK_SIZE];
        Drawable cardBack = mStore.get(R.id.card_back);
        int cardNum = 0;
        for (int i=1; i<=4; ++i) {
            int suit = ((i-1)%difficulty)+1;
            // TODO: Get current suit from mStore
            for (int cardVal=1; cardVal<=13; ++cardVal) {
                rawDeck[cardNum] = new Card(suit, cardVal, cardBack);
                ++cardNum;
            }
        }
        // TODO: Shuffle the cards with seed (wait until after test winning games, etc.)
        Card[] shuffledDeck = rawDeck;
        return shuffledDeck;
    }

    public Stack[] dealStacks() {
        /**
         * Initialize the 10 stacks with cards:
         *  - Distribute cards 0-27 to the in-play stacks (0 through 7)
         *      - Stack 0 gets 0 cards, stack 1 gets 1... stack 7 gets 7
         *  - Stack 8 (un-played cards) gets the remaining 24 cards
         *  - Stack 9 (completed cards) is initialized with 0 cards
         */
        Stack[] stacks = new Stack[NUM_STACKS];
        // Empty stack in-play
        stacks[0] = new Stack(0, null);
        int cardNum = 0;
        // Loop through stacks 2 -> 7
        for (int j=1; j<8; ++j) {
            // Get first card
            Card head = deck[cardNum];
            cardNum++;
            Card next = head;
            //ArrayList<Card> cards = new ArrayList<Card>();
            for (int i=1; i<j; ++i) {
                // Set pointer to next card in stack
                next.next = deck[cardNum];
                next = deck[cardNum];
                cardNum++;
            }
            // Unhide top card
            deck[cardNum-1].unhide();
            stacks[j] = new Stack(j, head);
        }
        // Add remaining cards to unplayed stack
        Card remainingHead = deck[cardNum];
        Card next = remainingHead;
        for (int i=cardNum; i < DECK_SIZE; ++i) {
            next.next = deck[i];
            next = deck[i];
        }
        stacks[8] = new Stack(8, remainingHead);
        // Initialize complete stack
        stacks[9] = new Stack(9, null);
        return stacks;
    }

}
