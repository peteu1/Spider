package com.example.peter.spider.Game;

import android.util.Log;

import java.util.ArrayList;

public class HistoryTracker {

    private final String TAG = "HISTORY_TRACKER";
    private ArrayList<HistoryObject> history;

    private int numMoves = 0;
    private boolean lastActionRemove = false;

    public HistoryTracker() {
        history = new ArrayList<HistoryObject>();
    }

    public int getNumMoves() {
        return numMoves;
    }

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
