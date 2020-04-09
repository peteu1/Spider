package com.example.peter.spider.Game;

import com.example.peter.spider.Game.CardDeck.Card;

import java.util.ArrayList;

public class HistoryObject {
    /**
     * This class stores a move so that it can be un-done.
     */

    boolean cardRevealed;
    int originalStack, newStack, numCards;
    // Store stack IDs that got completed
    ArrayList<Integer> completedStackIds;

    public HistoryObject() {
        cardRevealed = false;
        numCards = 0;
        originalStack = -3;
        newStack = -3;
        completedStackIds = new ArrayList<Integer>();
    }

    public void recordMove(int numCards, int from, int to) {
        /**
         * Record the move that occurred
         * @param head: the top card that was moved
         * @param from: the stack it moved from
         * @param to: the stack it moved to
         */
        this.numCards = numCards;
        this.originalStack = from;
        this.newStack = to;
    }
}
