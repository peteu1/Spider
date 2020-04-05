package com.example.peter.spider.Game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.example.peter.spider.Game.CardDeck.Card;

import java.util.ArrayList;

class Stack {
    /**
     * There will be 10 stored instances of this class:
     *      1 for each of the 8 card slots (numbered 0 [left] to 7 [right])
     *      1 for un-played cards
     *      1 for solved cards
     * Then another temporary instance used for moving cards
     */
    private static final String TAG = "Stack";
    private static final int VERTICAL_CARD_SPACING = 40;
    // Game properties
    int stackId;
    //ArrayList<Card> cards;
    Card head; // This will be the first node in the stack, w/ pointer to next

    // Location/motion properties
    int left;  // The left (X) coordinate of the stack in the canvas
    int top;  // NOTE: These are constant (except for moving stack)
    int cardWidth;
    int cardHeight;
    int stackHeight = 0;
    int numCards = 0;
    boolean moving;  // true if the card is currently being moved
    Paint holderColor;

    public Stack(int stackId, Card head) {
        this.stackId = stackId;
        this.head = head;
        this.left = 0;
        this.top = 0;
        holderColor = new Paint();
        holderColor.setColor(Color.rgb(150,150,150));
    }

    public void computeHeight() {
        // Re-compute stack height
        if (head == null) {
            this.numCards = 0;
            stackHeight = cardHeight;
        } else {
            this.numCards = head.cardsBelow();
            if (stackId < 8) {
                stackHeight = ((this.numCards-1)*VERTICAL_CARD_SPACING)+cardHeight;
            } else {
                // Completed and un-played card stacks have cards on top of each other
                stackHeight = cardHeight;
            }
        }
    }

    public void assignPosition(int left, int top, int cardWidth) {
        /**
         * The top and left of the stack are assigned from the Master
         *  constructor. Each card's width is set.
         */
        this.cardWidth = cardWidth;
        cardHeight = (int) (cardWidth * 1.5);
        Card card = head;
        while (card != null) {
            Log.e(TAG, "Setting size for:" + card.cardValue);
            card.setSize(cardWidth, cardHeight);
            card = card.next;
        }
        this.computeHeight();
        if (!moving) {
            this.left = left;
            this.top = top;
        } else {
            // Adjust display for moving stack
            this.left = left - cardWidth;
            this.top = top - stackHeight;
        }
    }

    public void assignPosition(float x, float y) {
        /**
         * Update position of MOVING STACK (only called for stack in motion)
         */
        // Adjust display location so that finger is in bottom right corner
        left = (int) x - cardWidth;
        top = (int) y - stackHeight;
    }

    public void flipBottomCard() {
        /**
         * Turns over (un-hides) the bottom card in the stack
         */
        if (head != null) {
            head.bottomCard().unhide();
        }
    }

    public void drawStack(Canvas canvas) {
        /**
         * Tells each card where to draw itself
         */
        if (stackId < 8) {
            // Draw stack holder for playing stacks
            if (stackId >= 0) {
                canvas.drawRect(left, top, left+cardWidth, top+cardHeight, holderColor);
            }
            // Draw playing stacks (& moving stack if exists)
            int cardIdx = 0;
            Card card = head;
            while (card != null) {
                int cardTop = top + cardIdx*VERTICAL_CARD_SPACING;
                card.draw(canvas, left, cardTop);
                card = card.next;
                cardIdx++;
            }
        }
        else if (stackId == 8) {
            // Draw hidden cards
            // TODO: Draw sets of 8 side by side
            Card f = new Card(1, 1);
            f.setSize(cardWidth, cardHeight);
            f.draw(canvas, left, top);
        }
        else {
            // Draw completed stacks
            Card f = new Card(1, 1);
            f.setSize(cardWidth, cardHeight);
            f.draw(canvas, left, top);
            // TODO: Draw every 13th card in stack, then shift right
        }
    }

    public Stack touchContained(float x, float y) {
        /**
         * Check if the coordinates are in this stack
         * If they are, return the sub-stack from the clicked position
         *  to the end of this stack
         * Otherwise, return null
         */
        if (x > left && x < (left+cardWidth) && numCards > 0) {
            // X-coordinate of touch is within stack
            Log.e("STACK", "y:" + String.valueOf(y) + ", top:" + String.valueOf(top) + ", stackHeight:" + String.valueOf(stackHeight));
            if (y > top && y < (stackHeight+top)) {
                // The stack was touched, collect stack of cards touched
                if (stackId == 8) {
                    // Return next 8 cards if new cards (top-right) touched
                    // Get the card 8th from last (24 un-played cards initially)
                    Stack returnStack;
                    if (numCards == 8) {
                        // Last 8 cards
                        returnStack = new Stack(-2, head);
                        head = null;
                    }
                    else {
                        Card prev = head;
                        Card next = head.next;
                        for (int i=0; i<(numCards-9); ++i) {
                            prev = prev.next;
                            next = next.next;
                        }
                        returnStack = new Stack(-2, next);
                        prev.next = null;
                    }
                    this.computeHeight();
                    return returnStack;
                }
                // Normal playing stack touched, find which card was touched
                // Get index of card touched
                int topCardIdx =  (int) (((int)y-top) / VERTICAL_CARD_SPACING);
                Log.e("STACK", "Top card:" + String.valueOf(topCardIdx));
                Card cardTouched;
                boolean valid = true;  // valid until proven invalid
                if (topCardIdx >= numCards) {
                    // Take the bottom card (always valid)
                    cardTouched = head.bottomCard();
                    topCardIdx = numCards-1;
                } else {
                    // Get card at topCardIdx
                    cardTouched = head;
                    for (int i=0; i < topCardIdx; ++i) {
                        cardTouched = cardTouched.next;
                    }
                    // Loop through cards below and make sure they can be moved
                    Card nextCard = cardTouched;
                    while (nextCard.next != null && valid) {
                        if (nextCard.canHold()) {
                            nextCard = nextCard.next;
                        } else {
                            valid = false;  // invalid move, break loop
                        }
                    }
                }
                if (valid) {
                    // Get stack to return
                    Stack stackTouched = new Stack(-1, cardTouched);
                    stackTouched.moving = true;
                    // Remove cards from this stack
                    if (topCardIdx == 0) {
                        head = null;
                    } else {
                        Card endCard = head;
                        for (int i=0; i<(topCardIdx-1); ++i) {
                            endCard = endCard.next;
                        }
                        endCard.next = null;
                    }
                    this.computeHeight();
                    return stackTouched;
                }
            }
        }
        return null;
    }

    private Card getLastCard() {
        /**
         * Returns the bottom card in this stack, null if stack is empty
         */
        if (head == null) {
            return null;
        }
        return head.bottomCard();
    }

    public Stack addStack(Stack s) {
        /**
         * Adds a stack (the moving stack) to the bottom of this stack
         * - NOTE: The add has already been deemed legal, so just add it
         * @return completed stack if full stack made, otherwise null
         */
        Card last = getLastCard();
        if (last == null) {
            head = s.head;
        }
        else {
            last.next = s.head;
        }
        // Re-compute stack height
        this.computeHeight();
        // Check if full stack created
        if (stackId < 8 && numCards >= 13) {
            // Get the 13th card from bottom
            int cardIdx = 0;
            Card start = head;
            while (cardIdx < (numCards-13)) {
                start = start.next;
                cardIdx++;
            }
            Card check = start;
            if (check.bottomCard().cardValue == 1) {
                boolean stackCreated = true;
                // Loop through stack and verify suited/sequential
                while (check.next != null && stackCreated) {
                    if (check.canHold()) {
                        check = check.next;
                    } else {
                        stackCreated = false;  // no complete stack, break loop
                    }
                }
                if (stackCreated) {
                    // Get completed stack
                    Stack completedStack = new Stack(-2, start);
                    // Remove completed stack from this stack
                    if (numCards == 13) {
                        head = null;
                    }
                    else {
                        Card lastCard = head;
                        for (int i=0; i < (numCards-14); ++i) {
                            lastCard = lastCard.next;
                        }
                        lastCard.next = null;
                    }
                    this.computeHeight();
                    return completedStack;
                }
            }
        }
        return null;
    }

    public int legalDrop(float x, float y, int topCardValue) {
        /**
         * Checks if a stack was dropped on this stack, and whether the move
         *  was legal
         * Only checks X-coordinates
         * @return -1: wrong stack; 0: right stack, illegal; 1: legal drop
         */
        if (x > left && x < (left + cardWidth)) {
            // Dropped on this stack
            int canDrop = this.legalDrop(topCardValue);
            if (canDrop == 1) {
                return 1;
            }
            // Right stack, illegal placement
            return 0;
        }
        return -1;
    }

    public int legalDrop(int topCardValue) {
        /**
         * This is called directly from Master when the screen is tapped.
         * Returns 1 if legal, -1 if not legal
         */
        if (head == null) {
            // Empty stack -> legal placement
            return 1;
        }
        Card bottomCard = head.bottomCard();
        if (bottomCard.canPlace(topCardValue)) {
            // Legal placement
            return 1;
        }
        // Illegal placement
        return -1;
    }

    public void addCard(Card card) {
        /**
         * Adds a single card (when new cards are drawn)
         */
        card.unhide();
        Stack newStack = new Stack(-3, card);
        this.addStack(newStack);
    }

}
