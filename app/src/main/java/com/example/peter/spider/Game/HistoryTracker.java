package com.example.peter.spider.Game;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class HistoryTracker {

    private final String TAG = "HISTORY_TRACKER";
    private ArrayList<HistoryObject> history;

    private int numMoves = 0;
    private boolean lastActionRemove = false;
    private long startMillis;
    private long elapsedMillis;

    public HistoryTracker() {
        history = new ArrayList<HistoryObject>();
        long startMillis = System.currentTimeMillis();
        elapsedMillis = 0;
        // TODO: Fix time so that there's start and end to get current.
        // Add current to elapsed, onPause and such, store elapsed
        // OnResume, restart startMillis
        // OnPause, update elapsedMillis
    }

    public int getNumMoves() {
        return numMoves;
    }

    // TODO: Get time elapsed
//    public String timeElapsed() {
//        long millis = System.currentTimeMillis();
//        long elapsedMillis = millis - startMillis;
//        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
//        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
//    }

    public void record(HistoryObject move) {
        // Prepend move to beginning of history list
        numMoves++;
        lastActionRemove = false;
        history.add(0, move);
    }

    public boolean isEmpty() {
        return history.size() == 0;
    }

    public HistoryObject pop() {
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
