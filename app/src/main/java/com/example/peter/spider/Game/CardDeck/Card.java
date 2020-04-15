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
     * Cards just hold their value and suit
     * The stack tells it where to draw itself
     */
    private int width;
    private int height;
    public boolean hidden;
    public boolean arrived;  // To check if animation is occurring
    public int cardSuit, cardValue;
    public String value;
    public Card next;  // reference to card below this card
    private Paint blockColor, hiddenColor, textPaint;
    Drawable cardBack, suitImage;

    public Card(int cardSuit, int cardValue, Drawable cardBack, Drawable suitImage) {
        this.cardSuit = cardSuit;
        this.cardValue = cardValue;
        this.cardBack = cardBack;
        this.suitImage = suitImage;
        value = displayValue();
        hidden = true;
        arrived = true;
        next = null;

        // TODO: Make front of cards look nicer
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

    void setArrived(boolean hasArrived) {
        this.arrived = hasArrived;
        if (next != null) {
            next.setArrived(hasArrived);
        }
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
