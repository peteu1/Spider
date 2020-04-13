package com.example.peter.spider.Game.CardDeck;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.HashMap;

public class Stack {
    /**
     * There will be 10 stored instances of this class:
     *      1 for each of the 8 card slots (numbered 0 [left] to 7 [right])
     *      1 for un-played cards
     *      1 for solved cards
     * Then another temporary instance used for moving cards
     */
    // Constants
    private static final String TAG = "Stack";
    private static final int VERTICAL_CARD_SPACING = 40;
    // Stack properties
    public int stackId;
    Paint holderColor;
    // Size/location properties
    public int left, top;  // X/Y canvas coordinates
    private int cardWidth, cardHeight, stackHeight, numCards;
    // Game properties
    public Card head; // This will be the first node in the stack, w/ pointer to next
    boolean moving;  // true if the stack is currently being moved
    int destX, destY;  // For animated moving stack, this is where it's heading

    public Stack(int stackId, Card head) {
        this.stackId = stackId;
        this.head = head;
        left = 0;
        top = 0;
        initPaints();
    }

    private void initPaints() {
        holderColor = new Paint();
        holderColor.setColor(Color.rgb(150,150,150));
    }

    public void assignPosition(int left, int top, int cardWidth) {
        /**
         * The top and left of the stack are assigned from the Master
         *  constructor. Each card's width is set.
         */
        this.cardWidth = cardWidth;
        cardHeight = (int) (cardWidth * 1.4);
        Card card = head;
        while (card != null) {
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
         * (For MOVING STACK only)
         * Update position as finger drags
         */
        // Adjust display location so that finger is in bottom right corner
        left = (int) x - cardWidth - 10;
        top = (int) y - stackHeight - 10;
        // NOTE: -10 to get a little buffer above the fatness of your finger
    }

    public int getNextCardY() {
        // When animating to this stack, get top of where next stack would go

        return top + stackHeight - cardHeight + VERTICAL_CARD_SPACING;
    }

    public void beginAnimation(int X, int Y) {
        // When a moving stack is set in motion for animation, record destination
        destX = X;
        destY = Y;
    }

    public boolean incrementAnimation() {
        /**
         * (For MOVING STACK only)
         * Returns true when the animation is finished, arrived destination
         */
        int frameRate = 30;
        // TODO: Trigonometry... :/
        // TODO: Set angle and increment X/Y based on angle stack needs to travel
        if (left > destX) {
            left-=frameRate;
        } else if (left < destX) {
            left+=frameRate;
        }
        if (top > destY) {
            top-=frameRate;
        } else if (top < destY) {
            top+=frameRate;
        }
        return (left >= (destX-frameRate) && left <= (destX+frameRate)) &&
                (top >= (destY-frameRate) && top <= (destY+frameRate));
    }

    public void drawStack(Canvas canvas) {
        /**
         * Tells each card where to draw itself
         */
        // Draw stack holder
        if (stackId >= 0) {
            canvas.drawRect(left, top, left+cardWidth, top+cardHeight, holderColor);
        }
        if (stackId < 8) {
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
            // Draw un-played cards (hidden) in sets of 8
            // Fake card since cards are hidden anyway
            Card f = head;
            // Draw sets of 8 side by side
            int numStacks = numCards / 8;
            for (int i=(numStacks-1); i>=0; --i) {
                int stackLeft = left - (i * VERTICAL_CARD_SPACING);
                f.draw(canvas, stackLeft, top);
            }
        }
        else {
            // Draw completed stacks (every 13th card, then shift right)
            int numStacks = numCards / 13;
            if (numStacks > 0) {
                Card c = head;
                for (int j=1; j<13; ++j) {
                    c = c.next;
                }
                for (int i=0; i<numStacks; ++i) {
                    int stackLeft = left + (i * VERTICAL_CARD_SPACING);
                    c.draw(canvas, stackLeft, top);
                    // Descend 13 cards for next card to draw
                    if (c.next != null) {
                        for (int j=0; j<13; ++j) {
                            c = c.next;
                        }
                    }
                }
            }
        }
    }

    /**************************************************
     * Various helper methods
     **************************************************/

    private void computeHeight() {
        // Re-compute stack height and number of cards
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

    public int getNumCards() {
        return this.numCards;
    }

    public boolean flipBottomCard() {
        /**
         * Turns over (un-hides) the bottom card in the stack
         */
        if (head != null) {
            return head.bottomCard().unhide();
        }
        return false;
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

    /**************************************************
     * Game logic methods
     **************************************************/

    public Stack touchContained(float x, float y) {
        /**
         * Check if the coordinates are in this stack
         * If they are, return the sub-stack from the clicked position
         *  to the end of this stack
         * Otherwise, return null
         */
        // Get real left (different for stack #8)
        int realLeft = left;
        if (stackId == 8) {
            realLeft = left - (((numCards/8) - 1) * VERTICAL_CARD_SPACING);
        }
        if (x > realLeft && x < (left+cardWidth) && numCards > 0) {
            // X-coordinate of touch is within stack
            Log.e("STACK", "y:" + y + ", top:" + top + ", stackHeight:" + stackHeight);
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
                Log.e("STACK", "Top card:" + topCardIdx);
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

    public int legalDrop(float x, float y, int topCardValue) {
        /**
         * Checks if a stack was dropped on this stack, and whether the move
         *  was legal
         * Only checks X-coordinates
         * @return -1: wrong stack; 0: right stack, illegal; 1: legal drop
         */
        if (x > left && x < (left + cardWidth)) {
            // Dropped on this stack
            if (head == null) {
                return 1;  // Empty stack -> legal placement
            }
            if (head.bottomCard().canPlace(topCardValue)) {
                return 1;  // Legal placement
            }
            return 0;  // Right stack, illegal placement
        }
        return -1;  // Did not land here
    }

    public int rateDrop(Card topCard) {
        /**
         * When screen tapped, checks if drop on this stack is legal and rates
         *  how good the move would be.
         * @param topCard is the top card in the moving stack.
         * @return rating, higher number means better move:
         *         0 ~ illegal placement
         *         1 ~ empty space
         *         2 ~ correct number
         *         3 ~ correct number & suit
         */
        if (head == null) {
            return 1;  // Empty space
        }
        Card bottomCard = head.bottomCard();
        if (bottomCard.canPlace(topCard.cardValue)) {
            // Legal placement
            if (bottomCard.cardSuit == topCard.cardSuit) {
                return 3;  // Correct number & suit
            }
            return 2;  // Correct number
        }
        return 0;  // Illegal placement
    }

    /**************************************************
     * Card adding/removing methods
     **************************************************/

    public Stack getFullStack() {
        /**
         * Checks if a full stack exists in this stack
         * @return the full stack if exists; otherwise null
         */
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
        return null;
    }

    public void replaceStack(Stack s) {
        /**
         * Adds the stack to the end of this stack, does not check
         *  if a complete stack is made.
         *  NOTE: This is called directly from master when undo-ing a completed stack
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
    }

    public Stack addStack(Stack s) {
        /**
         * Adds a stack (the moving stack) to the bottom of this stack
         * - NOTE: The add has already been deemed legal, so just add it
         * @return completed stack if full stack made, otherwise null
         */
        // Add cards to end of this stack
        replaceStack(s);
        // Check if a full stack was created
        if (stackId < 8 && numCards >= 13) {
            return getFullStack();
        }
        return null;
    }

    public Stack addCard(Card card) {
        // Adds a single card (when new cards are drawn) to end of stack
        card.unhide();
        Stack newStack = new Stack(-3, card);
        return this.addStack(newStack);
    }

    public Stack removeStack(int lenStack) {
        /**
         * When undo-ing a move, and cards need to be removed
         *  from this stack
         * @param lenStack: number of cards to remove from bottom
         * @return newStack: the stack that was removed
         */
        Stack newStack;
        if (lenStack == numCards) {
            newStack = new Stack(-3, head);
            head = null;
        } else {
            Card last = head;
            for (int i=0; i<(numCards-lenStack-1); ++i) {
                last = last.next;
            }
            newStack = new Stack(-3, last.next);
            last.next = null;
        }
        this.computeHeight();
        return newStack;
    }

    public Card removeBottomCard() {
        // When undo-ing a distribute cards move, grab last card
        Card prev = head;
        Card returnCard;
        if (prev.next == null) {
            returnCard = prev;
            head = null;
        } else {
            while (prev.next.next != null) {
                prev = prev.next;
            }
            returnCard = prev.next;
            prev.next = null;
        }
        this.computeHeight();
        returnCard.hidden = true;
        return returnCard;
    }
}
