package com.example.peter.spider.Game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.peter.spider.Game.CardDeck.Card;

import java.util.ArrayList;

public class Master {
    /**
     * This class contains the GameMaster and a list of the Stacks
     * It holds the list of Stacks, and passes stack objects to the game masters to
     *  be the main dictator between actions received and updates to the game
     */
    // TODO: class to store history stack movement so that undo can work

    private static final String TAG = "Master";
    public int difficulty;
    private Paint textPaint;
    private ArrayList<HistoryObject> history;

    private static final int DECK_SIZE = 52;
    private static final int NUM_STACKS = 10;
    public Card[] deck;
    // This is the top of completed and un-played stacks
    private static final int NON_PLAYING_STACK_Y = 50;
    public int screenWidth, screenHeight, stackWidth, cardWidth;
    public Stack[] stacks;  // Holds 10 stacks: 8 in play, 1 unplayed, 1 complete
    // The following are used to track a moving stack
    private Stack movingStack = null;
    private int originalStack; // This is the stack where a moving stack was taken from
    private float tappedX, tappedY;  // Store initial touch coords to see if screen was tapped

    public Master(DisplayMetrics displayMetrics, int difficulty) {
        this.difficulty = difficulty;
        textPaint = new Paint();
        textPaint.setColor(Color.rgb(30,30,30));
        textPaint.setTextSize(30);
        history = new ArrayList<HistoryObject>();
        deck = shuffleDeck();
        stacks = dealStacks();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        // Left/right margins: 0.05 screen width
        stackWidth = (int) ((0.9 * screenWidth) / 8);
        // spacing between stacks: 5 px
        cardWidth = stackWidth - 5;
        // Playing stacks start 30 pixels below non-playing stacks
        int cardHeight = (int) (cardWidth * 1.5);
        int stackTop = NON_PLAYING_STACK_Y + cardHeight + 30;
        // Assign location to each of the stacks
        double stackLeft = 0.05*screenWidth;
        for (int j=0; j<8; ++j) {
            Stack stack = stacks[j];
            stack.assignPosition((int) stackLeft, stackTop, cardWidth);
            stackLeft += stackWidth;
        }
        // Initiate un-played cards and stack locations
        stacks[8].assignPosition((int) (0.8*screenWidth), NON_PLAYING_STACK_Y, cardWidth);
        stacks[9].assignPosition((int) (0.1*screenWidth), NON_PLAYING_STACK_Y, cardWidth);
    }

    private Card[] shuffleDeck() {
        // Create ordered deck with every unique card
        Card[] rawDeck = new Card[DECK_SIZE];
        int cardNum = 0;
        for (int i=1; i<=4; ++i) {
            int suit = ((i-1)%difficulty)+1;
            Log.e(TAG, "i, suit: " + String.valueOf(i) + ", " + String.valueOf(suit));
            for (int cardVal=1; cardVal<=13; ++cardVal) {
                rawDeck[cardNum] = new Card(suit, cardVal);
                ++cardNum;
            }
        }
        Card[] shuffledDeck = rawDeck;
        // TODO: Shuffle the cards (wait until after test winning games, etc.)
        return shuffledDeck;
    }

    public Stack[] dealStacks() {
        /**
         * Initialize the 10 stacks with cards:
         *  - Distribute cards 0-27 to the in-play stacks (0 through 7)
         *      - Stack 0 gets 0 cards, stack 1 gets 1... stack 7 gets 7
         *  - Stack 8 (un-played cards) gets the remaining 24 cards
         *  - Stack 9 (completed cards) is initialized with 0 cards
         */
        Stack[] stacks = new Stack[NUM_STACKS];
        // Empty stack in-play
        stacks[0] = new Stack(0, null);
        int cardNum = 0;
        // Loop through stacks 2 -> 7
        for (int j=1; j<8; ++j) {
            // Get first card
            Card head = deck[cardNum];
            cardNum++;
            Card next = head;
            //ArrayList<Card> cards = new ArrayList<Card>();
            for (int i=1; i<j; ++i) {
                // Set pointer to next card in stack
                next.next = deck[cardNum];
                next = deck[cardNum];
                cardNum++;
            }
            // Unhide top card
            deck[cardNum-1].unhide();
            stacks[j] = new Stack(j, head);
        }
        // Add remaining cards to unplayed stack
        Card remainingHead = deck[cardNum];
        Card next = remainingHead;
        for (int i=cardNum; i < DECK_SIZE; ++i) {
            next.next = deck[i];
            next = deck[i];
        }
        stacks[8] = new Stack(8, remainingHead);
        // Initialize complete stack
        stacks[9] = new Stack(9, null);
        return stacks;
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
            movingStack.drawStack(canvas);
        }
        // TODO: Use undo icon instead
        canvas.drawText("UNDO", 15, screenHeight-40, textPaint);
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
        move.recordMove(movingStack.head, 8, -3);
        history.add(0, move);
        movingStack = null;
        // NOTE: return false because cards are not in motion
        return false;
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
                    movingStack.assignPosition(x, y);
                    //movingStack.assignPosition((int) x, (int) y, cardWidth);
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

    public boolean endStackMotion(float x, float y) {
        /**
         * This is only called when cards are in motion and touch released
         * Checks if valid drop
         * Updates moving cards (adds to new stack or reverts to original stack)
         * @return true if game was won; false otherwise
         */
        // Check if screen was tapped
        boolean tapped = false;
        if (x == tappedX && y == tappedY) {
            tapped = true;
        }
        // TODO: New logic for a tap, find best stack to move to (if any)
        int legal = -1;
        int addTo = originalStack;  // Indicates which stack id to add moving stack to
        x = movingStack.left + ((int) (0.25*stackWidth));
        y = movingStack.top;
        int topCardValue = movingStack.head.cardValue;
        // Find which stack it landed on, and whether move is valid
        for (int j=0; j<8; ++j) {
            Stack stack = stacks[j];
            if (tapped) {
                legal = stack.legalDrop(topCardValue);
            } else {
                legal = stack.legalDrop(x, y, topCardValue);
            }
            // Lock into place if legal move, else go back to initial location
            // -1: wrong stack; 0: right stack, illegal; 1: legal drop
            if (legal == 1) {
                if (!tapped) {
                    // Add moving stack to current stack
                    addTo = j;
                    j = 8;  // break loop
                }
                else {
                    // If tapped, only add tapped stack to different stack
                    if (j != originalStack) {
                        addTo = j;
                        j = 8;  // break loop
                    }
                }
            }
            else if (legal == 0) {
                // Illegal placement: restore moving stack to original stack
                j = 8;  // break loop
            }
            // else, didn't land here, keep looping
        }
        // Add moving stack to right stack
        Stack completedStack = stacks[addTo].addStack(movingStack);

        // Move completed stack to stacks[9] if full stack was made
        if (completedStack != null) {
            stacks[9].addStack(completedStack);
            Log.e(TAG, "Length completed stack:" + stacks[9].numCards);
            if (stacks[9].numCards == 52) {
                // TODO: Ensure below line fixes glitch of showing
                //  all the cards in the moving stack when game is won
                movingStack = null;
                return true;
            }
        }
        // Check if a legal move occurred
        if (addTo != originalStack) {
            // Record the move
            HistoryObject move = new HistoryObject();
            move.recordMove(movingStack.head, originalStack, addTo);
            if (completedStack != null) {
                move.completedStackIds.add(addTo);
            }
            // Flip over newly revealed card
            boolean cardFlipped = stacks[originalStack].flipBottomCard();
            move.cardRevealed = cardFlipped;
            history.add(0, move);
            Log.e(TAG, "Adding to history:" + move.originalStack + " > " + move.newStack + ",cards:" + move.head.cardsBelow());
        }
        movingStack = null;
        return false;
    }

    public boolean undo() {
        /**
         * Un-does a move when undo is clicked
         */
        Log.e(TAG, "Len history:" + history.size());
        if (history.size() == 0) {
            // TODO: Flash message ("Nothing to Undo")
            return false;
        }
        HistoryObject move = history.remove(0);
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
            int stackLength = move.head.cardsBelow();
            Stack origStack = stacks[move.newStack].removeStack(stackLength);
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

}
