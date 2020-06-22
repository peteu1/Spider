package com.example.peter.spider.Game;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

class HistoryTracker {

    private final String TAG = "HISTORY_TRACKER";
    private ArrayList<HistoryObject> history;
    private int numMoves, score;
    private boolean lastActionRemove = false;
    private long startMillis, elapsedMillis;
    private boolean clockRunning;

    HistoryTracker() {
        history = new ArrayList<HistoryObject>();
        startMillis = System.currentTimeMillis();
        elapsedMillis = 0;
        numMoves = 0;
        score = 0;
        clockRunning = false;
    }

    boolean isEmpty() {
        return history.size() == 0;
    }

    int getNumMoves() {
        return numMoves;
    }

    int getScore() {
        return score;
    }

    long getMillisElapsed() {
        long totalMillis = elapsedMillis;
        if (clockRunning) {
            totalMillis += (System.currentTimeMillis() - startMillis);
        }
        return totalMillis;
    }

    String getTimeElapsed() {
        // Shows current time elapsed to display on screen
        long totalMillis = getMillisElapsed();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalMillis) % 60;
        String strSeconds = ((seconds<10) ? "0" : "") + seconds;
        return minutes + ":" + strSeconds;
    }

    ArrayList<String> storeState(ArrayList<String> data) {
        /**
         * When game is saved, store everything necessary to
         *  re-create the current game state and history
         */
        Log.e(TAG, "storeState()");
        if (clockRunning) {
            elapsedMillis += (System.currentTimeMillis() - startMillis);
        }
        clockRunning = false;
        data.add("timeElapsed," + elapsedMillis);
        data.add("numMoves," + numMoves);
        data.add("score," + score);
        data.add("lastActionRemove," + lastActionRemove);
        // Add each HistoryObject
        for (HistoryObject ho : history) {
            String item = ho.originalStack + ",";
            item += ho.newStack + ",";
            item +=  ho.numCards;
            data.add(item);
        }
        return data;
    }

    void restoreProperties(long elapsedMillis, int numMoves, int score,
                           boolean lastActionRemove) {
        /**
         * This is called to restore game properties after the
         *  saved history has been re-created.
         */
        Log.e(TAG, "restoreProperties()");
        this.elapsedMillis = elapsedMillis;
        this.numMoves = numMoves;
        this.score = score;
        this.lastActionRemove = lastActionRemove;
        startMillis = System.currentTimeMillis();
        clockRunning = false;
    }

    void record(HistoryObject move) {
        // Prepend move to beginning of history list
        numMoves++;
        score += move.pointsAwarded;
        lastActionRemove = false;
        history.add(0, move);
    }

    HistoryObject pop() {
        /**
         * Removes the most recent HistoryObject.
         * RULES: You can only reduce move count by 1, consecutive undo's
         *  in a row will not reduce move count.
         */
        if (!lastActionRemove) {
            numMoves--;
        }
        lastActionRemove = true;
        HistoryObject ho = history.remove(0);
        score -= ho.pointsAwarded;
        return ho;
    }

    boolean isClockRunning() {
        return clockRunning;
    }

    void startClock() {
        clockRunning = true;
        startMillis = System.currentTimeMillis();
    }
}
