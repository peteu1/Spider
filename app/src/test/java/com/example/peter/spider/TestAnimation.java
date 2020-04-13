package com.example.peter.spider;

import android.util.Log;

import com.example.peter.spider.Game.CardDeck.Card;
import com.example.peter.spider.Game.CardDeck.Stack;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestAnimation {

    private static final String TAG = "TestAnimation";

    @Test
    public void test_animation() {

        Card c = new Card(1, 1, null, null);
        Stack movingStack = new Stack(-1, c);
        movingStack.assignPosition(80, 80, 20);
//        movingStack.beginAnimation(220, 60);
        movingStack.beginAnimation(200, 100);
        System.out.println("position: " + movingStack.left + ", " + movingStack.top);

        while (!movingStack.incrementAnimation()) {
            System.out.println("position: " + movingStack.left + ", " + movingStack.top);
        }
        System.out.println("position: " + movingStack.left + ", " + movingStack.top);

        assertEquals(4, 2 + 2);
    }
}
