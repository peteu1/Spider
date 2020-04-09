package com.example.peter.spider.Game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.peter.spider.Game.CardDeck.Card;
import com.example.peter.spider.Game.CardDeck.Deck;
import com.example.peter.spider.Game.CardDeck.Stack;
import com.example.peter.spider.R;

import java.util.ArrayList;
import java.util.HashMap;

public class Master {
    /**
     * This class contains the GameMaster and a list of the Stacks
     * It holds the list of Stacks, and passes stack objects to the game masters to
     *  be the main dictator between actions received and updates to the game
     */
    private static final String TAG = "Master";
    public int difficulty;
    private int seed;
    public Stack[] stacks;  // Holds 10 stacks: 8 in play, 1 unplayed, 1 complete
    public HistoryTracker historyTracker;
    private Paint textPaint;

    // Top of completed and un-played stacks
    private static final int NON_PLAYING_STACK_Y = 50;
    private static final int STACK_SPACING = 5;  // spacing between stacks
    // Left/right margins: 0.05 screen width
    private static final double EDGE_MARGIN = 0.05;
    public int screenWidth, screenHeight, stackWidth, cardWidth;
    private float tappedX, tappedY;  // Store initial touch coords to see if screen was tapped

    // The following are used to track a moving stack
    private Stack movingStack = null;
    private int originalStack; // This is the stack where a moving stack was taken from

    public Master(DisplayMetrics displayMetrics, int difficulty,
                  HashMap<Integer, Drawable> mStore) {
        /**
         * Constructor for starting a new game.
         */
        this.difficulty = difficulty;
        generateSeed();
        initialize(displayMetrics, mStore);
    }

    public Master(DisplayMetrics displayMetrics, HashMap<Integer, Drawable> mStore,
                  ArrayList<String> savedData) {
        /**
         * Constructor for resuming a saved game.
         */
        long timeElapsed = 0;
        int numMoves = 0;
        boolean lastActionRemove = false;
        ArrayList<HistoryObject> history = new ArrayList<HistoryObject>();
        // Parse saved data
        for (String line : savedData) {
            Log.e(TAG, "savedData:" + line);
            String[] data = line.split(",");
            Log.e(TAG, "data[0]:" + data[0]);
            if (data[0].equals("difficulty")) {
                this.difficulty = Integer.parseInt(data[1]);
                Log.e(TAG, "Integer.parseInt(data[1]):" + Integer.parseInt(data[1]));
            } else if (data[0].equals("seed")) {
                seed = Integer.parseInt(data[1]);
            } else if (data[0].equals("timeElapsed")) {
                timeElapsed = Long.parseLong(data[1]);
            } else if (data[0].equals("numMoves")) {
                numMoves = Integer.parseInt(data[1]);
            } else if (data[0].equals("lastActionRemove")) {
                lastActionRemove = Boolean.parseBoolean(data[1]);
            } else {
                // history object
                HistoryObject ho = new HistoryObject();
                int numCards = Integer.parseInt(data[2]);
                int from = Integer.parseInt(data[0]);
                int to = Integer.parseInt(data[1]);
                ho.recordMove(numCards, from, to);
                history.add(ho);
            }
        }
        // Re-initialize stacks/etc to initial set-up
        initialize(displayMetrics, mStore);
        // Restore historyTracker and re-create all moves
        restoreHistory(history, timeElapsed, numMoves, lastActionRemove);
    }

    private void generateSeed() {
        // TODO: Randomly generate number
        this.seed = 1;
    }

    private void initialize(DisplayMetrics displayMetrics, HashMap<Integer, Drawable> mStore) {
        /**
         * Constuctor helper for both new master and restore master constructors
         */
        Deck deck = new Deck(difficulty, seed, mStore);
        stacks = deck.dealStacks();
        historyTracker = new HistoryTracker();
        initPaints();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        stackWidth = (int) (((1-EDGE_MARGIN*2) * screenWidth) / 8);
        cardWidth = stackWidth - STACK_SPACING;
        arrangeStacks();
    }

    private void initPaints() {
        textPaint = new Paint();
        textPaint.setColor(Color.rgb(30,30,30));
        textPaint.setTextSize(30);
    }

    private void arrangeStacks() {
        /**
         * Sets the screen location for all the stacks based on screen size.
         */
        int cardHeight = (int) (cardWidth * 1.5);
        // Playing stacks start 30 pixels below non-playing stacks
        int stackTop = NON_PLAYING_STACK_Y + cardHeight + 30;
        // Assign location to each of the stacks
        double stackLeft = EDGE_MARGIN * screenWidth;
        for (int j=0; j<8; ++j) {
            Stack stack = stacks[j];
            stack.assignPosition((int) stackLeft, stackTop, cardWidth);
            stackLeft += stackWidth;
        }
        // Initiate un-played cards and stack locations
        stacks[8].assignPosition((int) (0.8*screenWidth), NON_PLAYING_STACK_Y, cardWidth);
        stacks[9].assignPosition((int) (0.1*screenWidth), NON_PLAYING_STACK_Y, cardWidth);
    }

    public void updateOrientation(DisplayMetrics displayMetrics) {
        /**
         * This is called when the screen is rotated, re-arrange stack spacing
         */
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        stackWidth = (int) (((1-EDGE_MARGIN*2) * screenWidth) / 8);
        cardWidth = stackWidth - STACK_SPACING;
        // Re-arrange stacks with new dimensions
        arrangeStacks();
    }

    public void draw(Canvas canvas) {
        /**
         * This method is constantly being called
         * Every stack draws itself where its supposed to be
         */
        for (Stack stack : stacks) {
            stack.drawStack(canvas);
        }
        if (movingStack != null) {
            // NOTE: Draw moving stack last so that it's on top
            movingStack.drawStack(canvas);
        }
        // Draw move count
        String moves = String.valueOf(historyTracker.getNumMoves());
        canvas.drawText("Moves: " + moves, 15, textPaint.getTextSize()+5, textPaint);
        // Draw menu "button" (TODO: Properly center text)
        canvas.drawText("Menu", (int) (screenWidth/2) - 25, textPaint.getTextSize()+5, textPaint);
        // TODO: Use undo icon instead
        canvas.drawText("UNDO", 15, screenHeight-40, textPaint);
        // Draw time - bottom center
        String time = historyTracker.getTimeElapsed();
        canvas.drawText(time, (int) (screenWidth/2) - 25, screenHeight-40, textPaint);
    }

    public boolean legalTouch(float x, float y) {
        /**
         * - Gets called when the screen is touched
         * - Finds the card that was touched
         * - If the card was legal, movingStack becomes the cards from
         *  the card touched to the end of the stack. Otherwise movingStack
         *  remains null.
         * @return True if a legal card was touched, False otherwise
         */
        // Check if a legal touch was initiated and get card stack
        // NOTE: Moving stack has stackId -1
        // Loop through stacks to see if any cards were touched
        for (Stack stack : stacks) {
            movingStack = stack.touchContained(x, y);
            if (movingStack != null) {
                Log.e(TAG, "stack taken from" + String.valueOf(stack.stackId));
                if (movingStack.stackId == -2) {
                    // New cards clicked, add one to end of each stack
                    return distributeNewCards();
                } else {
                    // Legal move initiated, store coords to check for tap (no click & drag)
                    movingStack.assignPosition((int) x, (int) y, cardWidth);
                    originalStack = stack.stackId;
                    tappedX = x;
                    tappedY = y;
                    return true;
                }
            }
        }
        return false;
    }

    public void moveStack(float x, float y) {
        // Updates position of moving stack while dragging finger
        if (movingStack != null) {
            movingStack.assignPosition(x, y);
        }
    }

    private int processTap() {
        /**
         * Called when the screen is tapped. Finds the best stack
         *  for current movingStack to move to.
         * @return the ID of the stack to add current moving stack to
         */
        int bestStack = -1;
        int bestRating = 0;
        Card topCard = movingStack.head;
        for (int j=0; j<8; ++j) {
            if (j != originalStack) {
                Stack stack = stacks[j];
                int rating = stack.rateDrop(topCard);
                if (rating > bestRating) {
                    bestStack = j;
                    bestRating = rating;
                }
            }
        }
        if (bestStack >= 0) {
            return bestStack;
        } else {
            return originalStack;
        }
    }

    public boolean endStackMotion(float x, float y) {
        /**
         * This is only called when cards are in motion and touch released
         * Checks if valid drop
         * Updates moving cards (adds to new stack or reverts to original stack)
         * @return true if game was won; false otherwise
         */
        int addTo = originalStack;  // Indicates which stack id to add moving stack to
        // Check if screen was tapped
        if (x == tappedX && y == tappedY) {
            addTo = processTap();
        } else {
            // Screen was clicked and dragged
            int legal = -1;
            x = movingStack.left + ((int) (0.25*stackWidth));
            y = movingStack.top;
            int topCardValue = movingStack.head.cardValue;
            // Find which stack it landed on, and whether move is valid
            for (int j=0; j<8; ++j) {
                Stack stack = stacks[j];
                legal = stack.legalDrop(x, y, topCardValue);
                // Lock into place if legal move, else go back to initial location
                // -1: wrong stack; 0: right stack, illegal; 1: legal drop
                if (legal == 1) {
                    // Add moving stack to current stack
                    addTo = j;
                    j = 8;  // break loop
                }
                else if (legal == 0) {
                    // Illegal placement: restore moving stack to original stack
                    j = 8;  // break loop
                }
                // else, didn't land here, keep looping
            }
        }
        return updateStacks(addTo);
    }

    private boolean updateStacks(int newStackIdx) {
        /**
         * Called after finger is released (endStackMotion). \
         * The current moving stack is added to the specified stack and
         *  history is recorded.
         */
        Stack completedStack = stacks[newStackIdx].addStack(movingStack);
        // Move completed stack to stacks[9] if full stack was made
        if (completedStack != null) {
            stacks[9].addStack(completedStack);
            if (stacks[9].getNumCards() == 52) {
                movingStack = null;
                return true;
            }
        }
        // Check if a legal move occurred
        if (newStackIdx != originalStack) {
            // Record the move
            HistoryObject move = new HistoryObject();
            move.recordMove(movingStack.head.cardsBelow(), originalStack, newStackIdx);
            if (completedStack != null) {
                move.completedStackIds.add(newStackIdx);
            }
            // Flip over newly revealed card
            boolean cardFlipped = stacks[originalStack].flipBottomCard();
            move.cardRevealed = cardFlipped;
            historyTracker.record(move);
            Log.e(TAG, "Adding to history:" + move.originalStack + " > " + move.newStack + ",cards:" + move.numCards);
        }
        movingStack = null;
        return false;
    }

    private boolean distributeNewCards() {
        /**
         * When a new set of cards is clicked in the top-right, 8 un-played
         *  cards are taken and distributed: 1 to each of the in-play stacks.
         * @return true if cards were distributed;
         *      false if no un-played cards left.
         */
        HistoryObject move = new HistoryObject();
        Card currentCard = movingStack.head;
        Card nextCard;
        for (int i=0; i<8; ++i) {
            nextCard = currentCard.next;  // Save next card
            currentCard.next = null;
            Stack completedStack = stacks[i].addCard(currentCard);
            if (completedStack != null) {
                // Completed stack was created
                stacks[9].addStack(completedStack);
                // Record which stack was completed in history
                move.completedStackIds.add(i);
            }
            currentCard = nextCard;
        }
//        move.recordMove(movingStack.head, 8, -3);
        // TODO: Verify this works
        move.recordMove(8, 8, -3);
        historyTracker.record(move);
        movingStack = null;
        // NOTE: return false because cards are not in motion
        return false;
    }

    public boolean undo() {
        /**
         * Un-does a move when undo is clicked
         */
        if (historyTracker.isEmpty()) {
            return false;
        }
        HistoryObject move = historyTracker.pop();
        // First, revert stacks that were completed (if any)
        for (int i=0; i<move.completedStackIds.size(); ++i) {
            int stackId = move.completedStackIds.get(i);
            Log.e(TAG, "restoring stack:" + stackId);
            // remove bottom 13 cards from completed stack
            Stack newStack = stacks[9].removeStack(13);
            // add back to original stack
            stacks[stackId].replaceStack(newStack);
        }
        if (move.originalStack != 8) {
            // Remove moved stack from the destination
            Stack origStack = stacks[move.newStack].removeStack(move.numCards);
            // Re-hide revealed card on original stack
            if (move.cardRevealed) {
                stacks[move.originalStack].head.bottomCard().hidden = true;
            }
            // Add back to original stack
            stacks[move.originalStack].addStack(origStack);
        }
        else {
            // Undo distribute cards
            Card head = stacks[0].removeBottomCard();
            // Remove bottom card from all playing stacks
            Card next = head;
            for (int i=1; i<8; ++i) {
                next.next = stacks[i].removeBottomCard();
                next = next.next;
            }
            // Add cards back to un-played stack
            Stack replaceStack = new Stack(-3, head);
            stacks[8].addStack(replaceStack);
        }
        return true;
    }

    // Methods for saving data and restoring saved game state

    public ArrayList<String> getGameState() {
        /**
         * Compiles all game information necessary to re-create current game state.
         * - Information is stored in the file: GameView.GAME_STATE_FILE_NAME
         * - Game state will be restored when app is quit and user pressed "Resume"
         *   from the main menu.
         *  Fields:
         *      (1 value): difficulty, seed, timeElapsed, numMoves, lastActionRemove
         *      history fields: from,to,numCards
         */
        // Stop any cards in motion
        if (movingStack != null) {
            endStackMotion(-9999, -9999);
        }
        ArrayList<String> data = new ArrayList<String>();
        data.add("difficulty," + difficulty);
        data.add("seed," + seed);
        data = historyTracker.storeState(data);
        return data;
    }

    public void restoreHistory(ArrayList<HistoryObject> history, long timeElapsed,
                               int numMoves, boolean lastActionRemove) {
        /**
         * Restores game to saved game state.
         */
        // Create new history tracker
        historyTracker = new HistoryTracker();
        // TODO: Redo all moves in history (opposite of undo)
        // Loop backwards because most recent move first
        for (int i=history.size()-1; i>=0; --i) {
            HistoryObject move = history.get(i);
            originalStack = move.originalStack;
            movingStack = stacks[originalStack].removeStack(move.numCards);
            if (move.originalStack == 8) {
                distributeNewCards();
            } else {
                updateStacks(move.newStack);
            }
        }
        // Restores time elapsed, numMoves in historyTracker
        historyTracker.restoreProperties(timeElapsed, numMoves, lastActionRemove);
    }

}
