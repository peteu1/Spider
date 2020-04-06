package com.example.peter.spider.Game.CardDeck;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.example.peter.spider.R;

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
        // TODO: get symbols
        switch (cardSuit) {
            case 1:
                return "S";
            case 2:
                return "H";
            case 3:
                return "D";
            case 4:
                return "C";
            default:
                return "X";
        }
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
    }

    public boolean unhide() {
        /**
         * Makes cards value and suit visible
         * @return true if card had been hidden; false if already visible
         */
        if (!hidden) {
            return false;
        }
        hidden = false;
        return true;
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
            // TODO: Draw card back (mipmap.card_back [png], better way to do this?)
//            Drawable d = getResources().getDrawable(R.mipmap.card_back, null);
//            d.setBounds(left, top, left+width, top+height);
//            d.draw(canvas);
            canvas.drawRect(left, top, left+width, top+height, hiddenColor);
            // draw card left border (TODO: Remove after implement card back)
            canvas.drawRect(left, top, left+2, top+height, blockColor);
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
