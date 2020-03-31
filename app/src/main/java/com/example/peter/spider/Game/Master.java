package com.example.peter.spider.Game;

import android.graphics.Canvas;
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
    private static final String TAG = "Master";
    private static final int DEFAULT_STACK_Y = 200;
    private int screenWidth, screenHeight, stackWidth, cardWidth;
    public int difficulty;
    private GameMaster gameMaster;
    public Stack[] stacks;  // Holds 10 stacks: 8 in play, 1 unplayed, 1 complete
    // The following are used to track a moving stack
    private Stack movingStack = null;
    private int originalStack; // This is the stack where a moving stack was taken from
    private float tappedX, tappedY;  // Store initial touch coords to see if screen was tapped

    public class GameMaster {
        /**
         * This class handles updates to the stacks
         * It also stores history stack movement so that undo can work
         */

        private static final int DECK_SIZE = 52;
        private static final int NUM_STACKS = 10;
        public Card[] deck;

        public GameMaster() {
            deck = shuffleDeck();
        }

        private Card[] shuffleDeck() {
            // Create ordered deck with every unique card
            Card[] rawDeck = new Card[DECK_SIZE];
            int cardNum = 0;
            for (int i=1; i<=4; ++i) {
                int suit = ((i-1)%difficulty)+1;
                Log.e("Game_Master", "i, suit: " + String.valueOf(i) + ", " + String.valueOf(suit));
                for (int cardVal=1; cardVal<=13; ++cardVal) {
                    rawDeck[cardNum] = new Card(suit, cardVal);
                    ++cardNum;
                }
            }
            Card[] shuffledDeck = rawDeck;
            // TODO: Shuffle the cards
            return shuffledDeck;
        }

        public Stack[] dealStacks() {
            // For cards 0 - 27, distribute to each stack (stack 0 gets 0, stack 1 gets 1... stack 7)
            //new Stack[NUM_STACKS]
            Stack[] stacks = new Stack[NUM_STACKS];
            // First, empty stack
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
//            ArrayList<Card> remaining_cards = new ArrayList<Card>();
            Card remainingHead = deck[cardNum];
            // TODO
            for (int i=cardNum; i < DECK_SIZE; ++i) {
                remaining_cards.add(deck[i]);
            }
            stacks[8] = new Stack(8, remainingHead);

            // Initialize complete stack
            stacks[9] = new Stack(9, null);
            return stacks;
        }
    }

    public Master(DisplayMetrics displayMetrics, int difficulty) {
        this.difficulty = difficulty;
        // TODO: Re-address how gameMaster is being used
        gameMaster = new GameMaster();
        stacks = gameMaster.dealStacks();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        // Margin = 0.05 screen width (for left & right)
        stackWidth = (int) ((0.9 * screenWidth) / 8);
        // spacing b/w stacks = 5 px
        cardWidth = stackWidth - 5;
        // Assign location to each of the stacks
        double stackLeft = 0.05*screenWidth;
        for (int j=0; j<8; ++j) {
            Stack stack = stacks[j];
            stack.assignPosition((int) stackLeft, DEFAULT_STACK_Y, cardWidth);
            stackLeft += stackWidth;
        }
        // Initiate un-played cards and stack locations
        stacks[8].assignPosition((int) (0.8*screenWidth), 50, cardWidth);
        stacks[9].assignPosition((int) (0.1*screenWidth), 50, cardWidth);
    }

    public void draw(Canvas canvas) {
        /**
         * This method is constantly being called
         * Every stack draws itself where its supposed to be
         */
        // Tell each stack to draw itself
        for (Stack stack : stacks) {
            stack.drawStack(canvas);
        }
        if (movingStack != null) {
            movingStack.drawStack(canvas);
        }
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
                    // New cards clicked
                    for (int i=0; i<8; ++i) {
                        stacks[i].addCard(movingStack.cards.get(i));
                    }
                    movingStack = null;
                    // NOTE: return false because cards are not in motion
                    return false;
                }
                movingStack.assignPosition((int) x, (int) y, cardWidth);
                originalStack = stack.stackId;
                tappedX = x;
                tappedY = y;
                return true;
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

    public void endStackMotion(float x, float y) {
        /**
         * This is only called when cards are in motion and touch released
         * Checks if valid drop
         * Updates moving cards (adds to new stack or reverts to original stack)
         */
        // Check if screen was tapped
        boolean tapped = false;
        if (x == tappedX && y == tappedY) {
            tapped = true;
        }
        int legal = -1;
        int addTo = originalStack;  // Indicates which stack id to add moving stack to
        x = movingStack.left + ((int) (0.25*stackWidth));
        y = movingStack.top;
        int topCardValue = movingStack.cards.get(0).cardValue;
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
        // add completed stacks if full stack made
        if (completedStack != null) {
            stacks[9].addStack(completedStack);
        }
        // Flip over newly revealed card
        if (addTo != originalStack) {
            stacks[originalStack].flipTopCard();
        }
        movingStack = null;
    }
}
