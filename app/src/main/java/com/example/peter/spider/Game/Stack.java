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
        if (stackId < 8 ) {
            stackHeight = ((cards.size()-1)*VERTICAL_CARD_SPACING)+cardHeight;
        } else {
            stackHeight = cardHeight;
        }
    }

    public void assignPosition(int left, int top, int cardWidth) {
        /**
         * The top and left of the stack are assigned from the Master
         *  constructor. Each card's width is set.
         */
        this.cardWidth = cardWidth;
        cardHeight = (int) (cardWidth * 1.5);
        for (Card card : cards) {
            card.setSize(cardWidth, cardHeight);
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

    public void flipTopCard() {
        if (cards.size() > 0) {
            cards.get(cards.size()-1).unhide();
        }
    }

    public void drawStack(Canvas canvas) {
        // Tells each card where to draw itself
        if (stackId < 8) {
            // Draw stack holder for playing stacks
            if (stackId >= 0) {
                canvas.drawRect(left, top, left+cardWidth, top+cardHeight, holderColor);
            }
            // Draw playing stacks and moving stack
            for (int i=0; i < cards.size(); i++) {
                int cardTop = top + i*VERTICAL_CARD_SPACING;
                cards.get(i).draw(canvas, left, cardTop);
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
        if (x > left && x < (left+cardWidth) && cards.size() > 0) {
            // X-coordinate of touch is within stack
            Log.e("STACK", "y:" + String.valueOf(y) + ", top:" + String.valueOf(top) + ", stackHeight:" + String.valueOf(stackHeight));
            if (y > top && y < (stackHeight+top)) {
                // TODO: use linked list instead?
                // The stack was touched, collect stack of cards touched
                ArrayList<Card> cardsTouched = new ArrayList<Card>();
                // Get next 8 cards if top
                if (stackId == 8) {
                    Log.e("STACK", "numCard:" + String.valueOf(cards.size()));
                    for (int i=0; i < 8; ++i) {
                        cardsTouched.add(cards.get(cards.size()-9));
                    }
                    Stack s = new Stack(-2, cardsTouched);
                    this.removeStack(s);
                    return s;
                }
                int topCardIdx =  (int) (((int)y-top) / VERTICAL_CARD_SPACING);
                Log.e("STACK", "Top card:" + String.valueOf(topCardIdx));
                boolean valid = true;
                if (topCardIdx >= cards.size()) {
                    cardsTouched.add(cards.get(cards.size()-1));
                } else {
                    for (int i=topCardIdx; i < cards.size(); ++i) {
                        cardsTouched.add(cards.get(i));
                    }
                    // Loop through cards in list and make sure they can be moved
                    Card topCard = cardsTouched.get(0);
                    for (int j=1; j<cardsTouched.size(); ++j) {
                        Card nextCard = cardsTouched.get(j);
                        if (!topCard.canHold(nextCard)) {
                            valid = false;
                            j = 53;  // break from loop
                        }
                        topCard = nextCard;
                    }
                }
                if (valid) {
                    // Remove cards from this stack and return
                    Stack stackTouched = new Stack(-1, cardsTouched);
                    this.removeStack(stackTouched);
                    stackTouched.moving = true;
                    return stackTouched;
                }
            }
        }
        return null;
    }

    private void removeStack(Stack s) {
        // Remove cards that were sent to moving stack
        // TODO: Could do this more easily with linked list
        int removeFrom = this.cards.size() - s.cards.size();
        for (int i=this.cards.size()-1; i >= removeFrom; --i) {
            this.cards.remove(i);
        }
        // Re-compute stack height
        this.computeHeight();
    }

    public Stack addStack(Stack s) {
        /**
         * Adds a stack (the moving stack) to the bottom of this stack
         * @return completed stack if full stack made, otherwise null
         */
        for (Card card : s.cards) {
            this.cards.add(card);
        }
        // Re-compute stack height
        this.computeHeight();
        // Check if full stack created
        if (stackId < 8 && cards.size() >= 13) {
            int lastCardIdx = cards.size()-1;
            Card lastCard = cards.get(lastCardIdx);
            if (lastCard.cardValue == 1) {
                boolean stackCreated = true;
                // Loop from 2nd to last card, backwards to 13th to last card
                for (int i=lastCardIdx-1; i>=(cards.size()-13); --i) {
                    Card c = cards.get(i);
                    if (c.canHold(lastCard)) {
                        lastCard = c;
                    }
                    else {
                        stackCreated = false;
                        i = -1;  // break loop
                    }
                }
                if (stackCreated) {
                    // return completed stack and remove from this stack
                    ArrayList<Card> compCards = new ArrayList<Card>();
                    for (int i=lastCardIdx; i>=(cards.size()-13); --i) {
                        compCards.add(cards.get(i));
                    }
                    Stack completedStack = new Stack(-2, compCards);
                    this.removeStack(completedStack);
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
        if (cards.size() == 0) {
            // Empty stack -> legal placement
            return 1;
        }
        Card bottomCard = cards.get(cards.size()-1);
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
        ArrayList<Card> newCard = new ArrayList<Card>();
        newCard.add(card);
        newCard.get(0).unhide();
        Stack newStack = new Stack(-3, newCard);
        this.addStack(newStack);
    }

}
