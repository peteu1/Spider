package com.example.peter.spider.Game;

import android.util.Log;

import com.example.peter.spider.Game.CardDeck.Card;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class HistoryObject {
    /**
     * This class stores a move so that it can be un-done.
     */

    private static final String TAG = "HistoryObject";
    boolean cardRevealed;
    int originalStack, newStack, numCards;
    // Store stack IDs that got completed
    ArrayList<Integer> completedStackIds;
    int pointsAwarded;

    public HistoryObject() {
        cardRevealed = false;
        numCards = 0;
        originalStack = -3;
        newStack = -3;
        completedStackIds = new ArrayList<Integer>();
        pointsAwarded = 0;
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

    public void computeScore(long millis) {
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(millis);
        int multiplier = ((1000 - seconds) / 100) + 1;
        if (cardRevealed) {
            pointsAwarded += multiplier;
        }
        // Add 50 * multiplier for completed stack
        pointsAwarded += completedStackIds.size() * multiplier * 50;
    }
}
