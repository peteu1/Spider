package com.example.peter.spider.Game;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

class HistoryTracker {

    //private final String TAG = "HISTORY_TRACKER";
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

    boolean isEmpty() {
        return history.size() == 0;
    }

    int getNumMoves() {
        return numMoves;
    }

    String getTimeElapsed() {
        // Shows current time elapsed to display on screen
        long curMillis = System.currentTimeMillis();
        long totalMillis = elapsedMillis + (curMillis-startMillis);
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
        long timeElapsed = elapsedMillis + (System.currentTimeMillis()-startMillis);
        data.add("timeElapsed," + timeElapsed);
        data.add("numMoves," + numMoves);
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

    void restoreProperties(long elapsedMillis, int numMoves,
                           boolean lastActionRemove) {
        /**
         * This is called to restore game properties after the
         *  saved history has been re-created.
         */
        this.elapsedMillis = elapsedMillis;
        this.numMoves = numMoves;
        this.lastActionRemove = lastActionRemove;
        startMillis = System.currentTimeMillis();
    }

    void record(HistoryObject move) {
        // Prepend move to beginning of history list
        numMoves++;
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
        return history.remove(0);
    }
}
