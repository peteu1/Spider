package com.example.peter.spider.Game.CardDeck;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;

public class Hint {

    int fromStack;
    int toStack;
    Card head;
    int rating;

    public Hint(int fromStack, int toStack, Card head, int rating) {
        this.fromStack = fromStack;
        this.toStack = toStack;
        this.head = head;
        this.rating = rating;
    }

    public int getRating() {
        return rating;
    }

    public int getCardsBelow() {
        return head.cardsBelow();
    }

    public int getFromStack() {
        return fromStack;
    }

    public int getToStack() {
        return toStack;
    }

    @NonNull
    @Override
    public String toString() {
        String s = head.displayValue() + " from " + fromStack + " to " + toStack;
        return s + " (rating: " + rating + ")";
    }
}