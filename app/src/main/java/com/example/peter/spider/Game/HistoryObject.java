package com.example.peter.spider.Game;

import com.example.peter.spider.Game.CardDeck.Card;

import java.util.ArrayList;

public class HistoryObject {
    /**
     * This class stores a move so that it can be un-done.
     */

    boolean cardRevealed;
    int originalStack, newStack;
    Card head;
    ArrayList<Integer> completedStackIds;  // Store stack IDs that got completed

    public HistoryObject() {
        cardRevealed = false;
        head = null;
        originalStack = -3;
        newStack = -3;
        completedStackIds = new ArrayList<Integer>();
    }

    public void recordMove(Card head, int from, int to) {
        /**
         * Record the move that occurred
         * @param head: the top card that was moved
         * @param from: the stack it moved from
         * @param to: the stack it moved to
         */
        this.head = head;
        this.originalStack = from;
        this.newStack = to;
    }
}
