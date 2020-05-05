package com.example.peter.spider.Game.CardDeck;

import android.graphics.drawable.Drawable;

import com.example.peter.spider.R;

import java.util.HashMap;
import java.util.Random;

public class Deck {

    //private static final String TAG = "DECK";
    private static final int DECK_SIZE = 52, NUM_STACKS = 10;
    public long seed;
    private Card[] deck;
    private HashMap<Integer, Drawable> mStore;

    public Deck(int difficulty, long seed, HashMap<Integer, Drawable> mStore) {
        this.mStore = mStore;
        this.seed = seed;
        deck = createDeck(difficulty, seed);
    }

    private Card[] createDeck(int difficulty, long seed) {
        // Create ordered deck with every unique card
        Card[] rawDeck = new Card[DECK_SIZE];
        Drawable cardBack = mStore.get(R.id.card_back);
        int cardNum = 0;
        for (int i=1; i<=4; ++i) {
            int suit = ((i-1)%difficulty)+1;
            Drawable suitImage = getSuitDrawable(suit);
            for (int cardVal=1; cardVal<=13; ++cardVal) {
                rawDeck[cardNum] = new Card(suit, cardVal, cardBack, suitImage);
                ++cardNum;
            }
        }
        return shuffleDeck(rawDeck, seed);
    }

    private Card[] shuffleDeck(Card[] rawDeck, long seed) {
        Card[] shuffledDeck = rawDeck;
        //
        //int n = cards.Count;
        int n = DECK_SIZE;
        Random rand = new Random(seed);
        //while (n > 1) {
        while (n > 0) {
            n--;
            int k = rand.nextInt(DECK_SIZE);
            // Swap cards at indexes k and n
            Card temp = shuffledDeck[k];
            shuffledDeck[k] = shuffledDeck[n];
            shuffledDeck[n] = temp;
        }
        //    n--;
        //    int k = Random.Range(0, n + 1);
        //    // Swap cards at indexes k and n
        //    int temp = cards[k];
        //    cards[k] = cards[n];
        //    cards[n] = temp;
        //}


        return shuffledDeck;
    }

    private Drawable getSuitDrawable(int suit) {
        switch (suit) {
            case 1:
                return mStore.get(R.id.suit_spades);
            case 2:
                return mStore.get(R.id.suit_hearts);
            case 3:
                return mStore.get(R.id.suit_diamonds);
            case 4:
                return mStore.get(R.id.suit_clubs);
        }
        return mStore.get(R.id.suit_spades);
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
            deck[cardNum-1].unHide();
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
