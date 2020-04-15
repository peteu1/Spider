package com.example.peter.spider.Game.CardDeck;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.example.peter.spider.R;

import java.util.HashMap;

public class Card {
    /**
     * Cards hold all the info needed to draw itself. The stack object
     *  tells it where to draw itself.
     * The card also points to the card below it so that the stack only needs
     *  to keep track of the head (top) card.
     */

    public Card next = null;  // reference to card below this card
    private int width, height;
    public int cardSuit, cardValue;
    private String value;
    public boolean hidden = true;
    private Drawable cardBack, suitImage;
    private Paint blockColor, textPaint;


    public Card(int cardSuit, int cardValue, Drawable cardBack, Drawable suitImage) {
        this.cardSuit = cardSuit;
        this.cardValue = cardValue;
        this.cardBack = cardBack;
        this.suitImage = suitImage;
        value = displayValue();

        // TODO: Make front of cards look nicer
        blockColor = new Paint();
        blockColor.setColor(Color.rgb(70,70,70));
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

    public void setSize(int w, int h) {
        width = w;
        height = h;
    }

    public boolean unHide() {
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
            // Draw card back
            cardBack.setBounds(left, top, left+width, top+height);
            cardBack.draw(canvas);
        } else {
            canvas.drawRect(left, top, left+width, top+height, blockColor);
            canvas.drawText(value, left+5, top+11, textPaint);
            // Draw suit image
            suitImage.setBounds(left+width-35, top+5, left+width-5, top+35);
            suitImage.draw(canvas);
        }
        // Draw divider between cards
        canvas.drawRect(left, top, left+width, top+2, textPaint);
    }

    public Card bottomCard() {
        // Gets the last/bottom card that this card is connected to
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
        // Gets the number of attached cards (including this one)
        int numCards = 1;
        Card next = this.next;
        while (next != null) {
            next = next.next;
            numCards++;
        }
        return numCards;
    }

}
