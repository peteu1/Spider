package com.example.peter.spider.Game;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

class HistoryTracker {

    private final String TAG = "HISTORY_TRACKER";
    private ArrayList<HistoryObject> history;

    private int numMoves;
    private boolean lastActionRemove = false;
    private long startMillis, elapsedMillis;

    HistoryTracker() {
        history = new ArrayList<HistoryObject>();
        startMillis = System.currentTimeMillis();
        elapsedMillis = 0;
        numMoves = 0;
    }

    HistoryTracker(ArrayList<HistoryObject> history, long elapsedMillis,
                   int numMoves) {
        /**
         * This constructor is called to restore retained fragment, i.e.
         *  - when the screen is rotated
         *  - when the app is left and returned to (onResume)
         */
        this.history = history;
        this.elapsedMillis = elapsedMillis;
        this.numMoves = numMoves;
        // OnResume, restart startMillis
        startMillis = System.currentTimeMillis();
    }

    int getNumMoves() {
        return numMoves;
    }

    String getTimeElapsed() {
        long curMillis = System.currentTimeMillis();
        long totalMillis = elapsedMillis + (curMillis-startMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalMillis) % 60;
        String strSeconds = ((seconds<10) ? "0" : "") + seconds;
        return minutes + ":" + strSeconds;
    }

    long stopClock() {
        // Called when game is paused
        // append current time to elapsedMillis & return
        long curMillis = System.currentTimeMillis();
        return elapsedMillis + (curMillis-startMillis);
    }

    void record(HistoryObject move) {
        // Prepend move to beginning of history list
        numMoves++;
        lastActionRemove = false;
        history.add(0, move);
    }

    boolean isEmpty() {
        return history.size() == 0;
    }

    HistoryObject pop() {
        /**
         * Removes the most recent HistoryObject.
         * RULES: You can only reduce move count by 1, consecutive undo's
         *  in a row will not reduce move count.
         */
        Log.e(TAG, "Len history:" + history.size());
        if (!lastActionRemove) {
            numMoves--;
        }
        lastActionRemove = true;
        return history.remove(0);
    }
}
