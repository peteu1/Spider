package com.example.peter.spider.Game.CardDeck;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Card {
    /**
     * Cards just hold their value and suit
     * The stack tells it where to draw itself
     */
    private int width;
    private int height;
    public boolean hidden;
    public int cardSuit, cardValue;
    public String suit, value;
    public Card next;  // reference to card below this card
    private Paint blockColor, hiddenColor, textPaint;

    public Card(int cardSuit, int cardValue) {
        this.cardSuit = cardSuit;
        this.cardValue = cardValue;
        suit = displaySuit();
        value = displayValue();
        hidden = true;
        next = null;

        // TODO: Make cards look nicer
        blockColor = new Paint();
        blockColor.setColor(Color.rgb(70,70,70));
        hiddenColor = new Paint();
        hiddenColor.setColor(Color.rgb(110,110,110));
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(12);
    }

    public String displayValue() {
        switch (this.cardValue) {
            case 1:
                return "A";
            case 11:
                return "J";
            case 12:
                return "Q";
            case 13:
                return "K";
            default:
                return String.valueOf(this.cardValue);
        }
    }

    private String displaySuit() {
        // TODO: Suits are all appearing as "C"... 4?
        // TODO: get symbols
        String suit = "X";
        switch (cardSuit) {
            case 1:
                suit = "S";
            case 2:
                suit = "H";
            case 3:
                suit = "D";
            case 4:
                suit = "C";
        }
        return suit;
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
    }

    public void unhide() {
        // Makes cards value and suit visible
        hidden = false;
    }

    public boolean canPlace(int otherVal) {
        // Returns true if card with value otherVal can be placed on top of this card
        return (this.cardValue-1) == otherVal;
    }

    public boolean canHold() {
        /**
         * Checks if this card can be moved based on next card value/suit
         */
        // Checks if this card can be moved while holding other on top of it
        if (!hidden) {
            if (next == null) {
                // Nothing after this card, so it can be moved regardless
                return true;
            }
            if (cardSuit == next.cardSuit) {
                if ((this.cardValue-1) == next.cardValue) {
                    return true;
                }
            }
        }
        return false;
    }

    public void draw(Canvas canvas, int left, int top) {
        // Draws the card on the canvas
        if (this.hidden) {
            canvas.drawRect(left, top, left+width, top+height, hiddenColor);
        } else {
            canvas.drawRect(left, top, left+width, top+height, blockColor);
            canvas.drawText(value, left+5, top+11, textPaint);
            canvas.drawText(suit, left+width-10, top+11, textPaint);
        }
        // Draw divider between cards
        canvas.drawRect(left, top, left+width, top+2, textPaint);
    }

    public Card bottomCard() {
        /**
         * Iterates the cards below and returns the last (bottom) card.
         */
        if (next == null) {
            return this;
        }
        Card last = this;
        while (last.next != null) {
            last = last.next;
        }
        return last;
    }

    public int cardsBelow() {
        /**
         * Returns the number of cards attached to this
         *  card (including this one)
         */
        int numCards = 1;
        Card next = this.next;
        while (next != null) {
            next = next.next;
            numCards++;
        }
        return numCards;
    }

}
