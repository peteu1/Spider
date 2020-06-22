package com.example.peter.spider.Game;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.example.peter.spider.Game.CardDeck.Card;
import com.example.peter.spider.Game.CardDeck.Const;
import com.example.peter.spider.Game.CardDeck.Deck;
import com.example.peter.spider.Game.CardDeck.Hint;
import com.example.peter.spider.Game.CardDeck.Stack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

public class Master {
    /**
     * This class contains the GameMaster and a list of the Stacks
     * It holds the list of Stacks, and passes stack objects to the game masters to
     *  be the main dictator between actions received and updates to the game
     */
    private static final String TAG = "Master";
    public int difficulty;
    private long seed;
    private Stack[] stacks;  // Holds 10 stacks: 8 in play, 1 unplayed, 1 complete
    HistoryTracker historyTracker;
    public int textPaintSize;

    int screenWidth, screenHeight, non_playing_stack_y, maxStackHeight;
    private int stackWidth, cardWidth, cardHeight, stackSpacing, edgeMargin;
    private float tappedX, tappedY;  // Store initial touch coords to see if screen was tapped

    // The following are used to track a moving stack
    private Stack movingStack = null;
    private int originalStack; // This is the stack where a moving stack was taken from

    // Refers to a stack where the arrow was touched to display cards too tall for screen
    public int displayFullStack;

    // Store and track hints list
    ArrayList<Hint> currentHints;
    int hintPosition;

    Master(int screenWidth, int screenHeight, int difficulty,
           HashMap<Integer, Drawable> mStore) {
        // Constructor for starting a new game
        this.difficulty = difficulty;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        generateSeed();
        initialize(mStore);
    }

    Master(int screenWidth, int screenHeight, HashMap<Integer, Drawable> mStore,
           ArrayList<String> savedData) {
        // Constructor for resuming a saved game
        long timeElapsed = 0;
        int numMoves = 0;
        int score = 0;
        boolean lastActionRemove = false;
        ArrayList<HistoryObject> history = new ArrayList<HistoryObject>();
        // Parse saved data
        for (String line : savedData) {
            String[] data = line.split(",");
            if (data[0].equals("difficulty")) {
                this.difficulty = Integer.parseInt(data[1]);
            } else if (data[0].equals("seed")) {
                this.seed = Long.parseLong(data[1]);
            } else if (data[0].equals("timeElapsed")) {
                timeElapsed = Long.parseLong(data[1]);
            } else if (data[0].equals("numMoves")) {
                numMoves = Integer.parseInt(data[1]);
            } else if (data[0].equals("lastActionRemove")) {
                lastActionRemove = Boolean.parseBoolean(data[1]);
            } else if (data[0].equals("score")) {
                score = Integer.parseInt(data[1]);
            }

            else {
                // Get history object
                HistoryObject ho = new HistoryObject();
                int numCards = Integer.parseInt(data[2]);
                int from = Integer.parseInt(data[0]);
                int to = Integer.parseInt(data[1]);
                ho.recordMove(numCards, from, to);
                history.add(ho);
            }
        }
        // Re-initialize stacks/etc to initial set-up
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        initialize(mStore);
        // Restore historyTracker and re-create all moves
        restoreHistory(history, timeElapsed, numMoves, score, lastActionRemove);
    }

    private void generateSeed() {
        // Generate random seed so exact shuffle can be restored
        Random rand = new Random();
        this.seed = rand.nextLong();
    }

    private void initialize(HashMap<Integer, Drawable> mStore) {
        // Helper for new -- and restored -- master constructors
        if (screenWidth > screenHeight) {
            // Landscape
            edgeMargin = (int) (screenWidth * Const.EDGE_MARGIN_PCT_LANDSCAPE);
        } else {
            // Portrait
            edgeMargin = (int) (screenWidth * Const.EDGE_MARGIN_PCT);
        }
        stackWidth = (int) ((screenWidth - edgeMargin*2) / 8);
        if (screenWidth > screenHeight) {
            // Landscape
            stackSpacing = (int) (screenWidth * Const.STACK_SPACING_PCT_LANDSCAPE);
        } else {
            // Portrait
            stackSpacing = (int) (screenWidth * Const.STACK_SPACING_PCT);
        }
        cardWidth = stackWidth - stackSpacing;
        cardHeight = (int) (cardWidth * Const.CARD_WH_RATIO);
        non_playing_stack_y = (int) (Const.NON_PLAYING_STACK_Y_PCT * screenHeight);
        Deck deck = new Deck(difficulty, seed, mStore);
        stacks = deck.dealStacks();
        historyTracker = new HistoryTracker();
        arrangeStacks();
        textPaintSize = (int) (non_playing_stack_y * Const.MENU_PAINT_PCT);
        displayFullStack = -1;
        currentHints = new ArrayList<>();
        hintPosition = -1;
    }

    public int updateOrientation(int screenWidth, int screenHeight) {
        // Called when the screen is rotated, re-arrange stack spacing
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        if (screenWidth > screenHeight) {
            // Landscape
            edgeMargin = (int) (screenWidth * Const.EDGE_MARGIN_PCT_LANDSCAPE);
        } else {
            // Portrait
            edgeMargin = (int) (screenWidth * Const.EDGE_MARGIN_PCT);
        }
        stackWidth = (int) ((screenWidth - edgeMargin*2) / 8);
        if (screenWidth > screenHeight) {
            // Landscape
            stackSpacing = (int) (screenWidth * Const.STACK_SPACING_PCT_LANDSCAPE);
        } else {
            // Portrait
            stackSpacing = (int) (screenWidth * Const.STACK_SPACING_PCT);
        }
        cardWidth = stackWidth - stackSpacing;
        cardHeight = (int) (cardWidth * Const.CARD_WH_RATIO);
        non_playing_stack_y = (int) (Const.NON_PLAYING_STACK_Y_PCT * screenHeight);
        // Re-arrange stacks with new dimensions
        arrangeStacks();
        textPaintSize = (int) (non_playing_stack_y * Const.MENU_PAINT_PCT);
        return textPaintSize;
    }

    private void arrangeStacks() {
        // Sets the screen location for all the stacks based on screen size
        int stackTop = (non_playing_stack_y * 2) + cardHeight;
        // Assign location to each of the stacks
        double stackLeft = edgeMargin;
        for (int j=0; j<8; ++j) {
            Stack stack = stacks[j];
            stack.assignPosition((int) stackLeft, stackTop, cardWidth);
            stackLeft += stackWidth;
        }
        // Initiate un-played cards and stack locations
        int unplayedMargin = (int) (screenWidth * Const.NON_PLAYING_EDGE_MARGIN_PCT);
        stacks[8].assignPosition((screenWidth - unplayedMargin), non_playing_stack_y, cardWidth);
        stacks[9].assignPosition(unplayedMargin, non_playing_stack_y, cardWidth);
    }

    void setBottomMenuY(float bottomMenuY) {
        // Called from GameView.initPaints(), to know where max stack height is.
        maxStackHeight = ((int) bottomMenuY) - cardHeight - (2 * non_playing_stack_y);
        // Loop through stacks and update maxHeight
        for (Stack s : stacks) {
            s.setMaxHeight(maxStackHeight);
        }
    }

    void draw(Canvas canvas) {
        /**
         * This method is constantly being called
         * Every stack draws itself where its supposed to be
         */
        for (Stack stack : stacks) {
            stack.drawStack(canvas);
        }
        // Highlight hints
        if (hintPosition >= 0) {
            Hint hint = currentHints.get(hintPosition);
            Stack fromStack = stacks[hint.getFromStack()];
            fromStack.drawHint(canvas, hint.getCardsBelow(), false);
            Stack toStack = stacks[hint.getToStack()];
            toStack.drawHint(canvas, 1, true);
        }
        if (movingStack != null) {
            // Draw moving stack as finger drags
            movingStack.drawStack(canvas);
        }
    }

    boolean legalTouch(float x, float y) {
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
        if (displayFullStack >= 0) {
            // Stop displaying full stack when screen is touched again
            stacks[displayFullStack].showingFullStack = false;
            displayFullStack = -1;
        }
        // Loop through stacks to see if any cards were touched
        for (Stack stack : stacks) {
            movingStack = stack.touchContained(x, y);
            if (movingStack != null) {
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

    void moveStack(float x, float y) {
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

    boolean endStackMotion(float x, float y) {
        /**
         * This is only called when cards are in motion and touch released
         * Checks if valid drop
         * Updates moving cards (adds to new stack or reverts to original stack)
         * @return true if game was won; false otherwise
         */
        int addTo = originalStack;  // Indicates which stack id to add moving stack to
        // Check if screen was tapped
        boolean tapped = false;
        if (x == tappedX && y == tappedY) {
            addTo = processTap();
            tapped = true;
        } else {
            // Screen was clicked and dragged
            int legal = -1;
            // TODO: Move 0.25 to Const
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
        return updateStacks(addTo, tapped);
    }

    private boolean updateStacks(int newStackIdx, boolean showAnimation) {
        /**
         * - Called after finger is released (endStackMotion).
         * - Current moving stack is added to the specified stack.
         * - Logic for the move has already been checked, so just update
         *   the stacks and record the move if new stack is different from
         *   original.
         * @param showAnimation (boolean): when stack is tapped, animate stack motion
         * @return true if game is over; false otherwise.
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
        if (newStackIdx != originalStack) {
            // Add move to history if legal
            HistoryObject move = new HistoryObject();
            move.recordMove(movingStack.head.cardsBelow(), originalStack, newStackIdx);
            if (completedStack != null) {
                move.completedStackIds.add(newStackIdx);
            }
            // Flip over newly revealed card
            boolean cardFlipped = stacks[originalStack].flipBottomCard();
            move.cardRevealed = cardFlipped;
            move.computeScore(historyTracker.getMillisElapsed());
            historyTracker.record(move);
        }
        hintPosition = -1;
        movingStack = null;
        return false;
    }

    public boolean arrowTouched(float x, float y) {
        // Checks if the arrow above any stacks that are too tall was pressed
        for (Stack stack : stacks) {
            if (stack.arrowTouched(x, y)) {
                displayFullStack = stack.stackId;
                return true;
            }
        }
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
            // Move stack if a complete stack was made
            if (completedStack != null) {
                // Completed stack was created
                stacks[9].addStack(completedStack);
                // Record which stack was completed in history
                move.completedStackIds.add(i);
            }
            currentCard = nextCard;
        }
        move.recordMove(8, 8, -3);
        historyTracker.record(move);
        movingStack = null;
        hintPosition = -1;
        return false;  // NOTE: return false because cards aren't in motion
    }

    private boolean getHints() {
        /**
         * Create list of hints
         * @return emptySpace: whether there are any empty spaces available
         */
        currentHints = new ArrayList<>();
        boolean emptySpace = false;
        for (int i=0; i<8; ++i) {
            Stack fromStack = stacks[i];
            // For each stack, get highest card that can be moved
            Card card = fromStack.getTopMovableCard();
            if (card == null) {
                continue;
            }
            // Loop through other stacks to see where card can go
            for (int j=0; j<8; ++j) {
                if (i == j) {
                    continue;
                }
                int rating = stacks[j].rateDrop(card);
                if (rating > 2) {
                    Hint h = new Hint(i, j, card, rating);
                    currentHints.add(h);
                } else if (rating == 1) {
                    // Empty space available
                    Log.e(TAG, "Rating 1");
                    emptySpace = true;
                }
            }
        }
        return emptySpace;
    }

    String showHint() {
        /**
         * Called from GameView when hint button is clicked
         * @ return gets Toasted by GameView; null if hints are available
         */
        Log.e(TAG, "Number of hints: " + currentHints.size());
        Log.e(TAG, "Hint pos: " + hintPosition);
        if (hintPosition < 0) {
            // Create/update list of hints
            boolean emptySpace = getHints();
            if (currentHints.size() == 0) {
                // Use empty space
                if (emptySpace) {
                    Log.e(TAG, "Empty Space");
                    // TODO: Highlight empty space
                    return "Empty Space Available";
                }
                // Check if any un-played cards
                if (stacks[8].head != null) {
                    // TODO: Highlight stacks
                    return "Draw new stack";
                }
                return "No Suggestions";
            }
            // Sort on rating, then number of cards being moved
            Collections.sort(currentHints, (h1, h2) -> {
                if (h1.getRating() != h2.getRating()) {
                    return h2.getRating() - h1.getRating();
                }
                return h2.getCardsBelow() - h1.getCardsBelow();
            });
            hintPosition = 0;
        } else {
            // increment hint position
            hintPosition++;
            if (hintPosition >= currentHints.size()) {
                hintPosition = 0;
            }
        }
        return null;
    }

    boolean undo() {
        // Un-does a move when undo is clicked
        hintPosition = -1;
        if (historyTracker.isEmpty()) {
            return false;
        }
        HistoryObject move = historyTracker.pop();
        // First, revert stacks that were completed (if any)
        for (int i=0; i<move.completedStackIds.size(); ++i) {
            int stackId = move.completedStackIds.get(i);
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

    ArrayList<String> getGameState() {
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

    private void restoreHistory(ArrayList<HistoryObject> history, long timeElapsed,
                                int numMoves, int score, boolean lastActionRemove) {
        /**
         * Restores game to saved game state by looping through all moves in history
         *  and re-doing them. Also restores historyTracker to previous state.
         */
        // Loop backwards because most recent move first
        for (int i=history.size()-1; i>=0; --i) {
            HistoryObject move = history.get(i);
            originalStack = move.originalStack;
            movingStack = stacks[originalStack].removeStack(move.numCards);
            if (move.originalStack == 8) {
                distributeNewCards();
            } else {
                updateStacks(move.newStack, false);
            }
        }
        // Restores time elapsed, numMoves in historyTracker
        Log.e(TAG, "Restoring history, numMoves = " + numMoves);
        historyTracker.restoreProperties(timeElapsed, numMoves, score, lastActionRemove);
    }

}
