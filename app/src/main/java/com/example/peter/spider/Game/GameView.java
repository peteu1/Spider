package com.example.peter.spider.Game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.peter.spider.MainActivity;
import com.example.peter.spider.MainThread;
import com.example.peter.spider.R;

import java.util.HashMap;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "GameView";
    private MainThread thread;
    private Context context;
    private Master master;
    private boolean cardsInMotion;

    public GameView(Context context, int difficulty) {
        super(context);
        this.context = context;
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        setFocusable(true);

        // Initialize Image HashMap
        HashMap<Integer, Drawable> mStore = new HashMap<Integer, Drawable>();
        Drawable cardBack = getResources().getDrawable(R.drawable.card_back, null);
        mStore.put(R.id.card_back, cardBack);

        // Initialize Master (holds GameMaster and stacks)
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        master = new Master(displayMetrics, difficulty, mStore);
    }

    public GameView(Context context, Bundle savedInstanceState) {
        /**
         * Constructor to restore game where it left off.
         */
        super(context);
        this.context = context;
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        setFocusable(true);

        // Initialize Image HashMap
        HashMap<Integer, Drawable> mStore = new HashMap<Integer, Drawable>();
        Drawable cardBack = getResources().getDrawable(R.drawable.card_back, null);
        mStore.put(R.id.card_back, cardBack);

        // Initialize Master (holds GameMaster and stacks)
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // Re-create master
        int difficulty = savedInstanceState.getInt("difficulty");
        master = new Master(displayMetrics, difficulty, mStore);
        // Restore history
        long timeElapsed = savedInstanceState.getLong("TimeElapsed");
        int numMoves = savedInstanceState.getInt("NumMoves");
        master.restoreClock(timeElapsed, numMoves);
        // TODO: Restore the rest of history
        // TODO: Restore card stack positions (from history?)
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        cardsInMotion = false;
        // Start the Thread, which loops while(running) making repeated calls to update() and start()
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Initial touch of screen
                Log.e("action", "Initial Touch");
                // Check if a legal touch was initiated
                cardsInMotion = master.legalTouch(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                // Touch is dragging
                if (cardsInMotion) {
                    master.moveStack(x, y);
                }
                // Do nothing when initial touch not valid (no cards in motion)
                break;
            case MotionEvent.ACTION_UP:
                // touch was released
                Log.e("action", "Touch released, x:" + String.valueOf(x) + ", y:" + String.valueOf(y));
                if (cardsInMotion) {
                    // Lock into place if legal move, else go back to initial location
                    boolean gameOver = master.endStackMotion(x, y);
                    cardsInMotion = false;
                    if (gameOver) {
                        // TODO: Stop clock
                        Log.e(TAG, "You win!");
                        Toast.makeText(context, "You Win!", Toast.LENGTH_SHORT).show();
                        // TODO:  go to game won screen, show stats

                        // Return to home screen
                        Intent i = new Intent(context, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(i);
                    }
                } else {
                    // TODO: Better approach than hard-coding some of these?
                    int centerX = master.screenWidth / 2;
                    // Check if undo "button" was touched
                    if ((x < 120) && (y > (master.screenHeight-100))) {
                        Log.e(TAG, "Undo clicked!");
                        if (!master.undo()) {
                            Toast.makeText(context, "Nothing to undo...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    // Check if menu "button" was touched
                    else if ((y < 50) && (x>(centerX-50)) && (x<(centerX+50))) {
                        // Return to home screen
                        Intent i = new Intent(context, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(i);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.e("action", "Cancelled");
                if (cardsInMotion) {
                    master.endStackMotion(-9999, -9999);
                }
                cardsInMotion = false;
                break;
            default:
                break;
        }
        return true; //super.onTouchEvent(event) || handled;
    }

    public void update() {
        // This is called continuously right before draw() is called
    }

    @Override
    public void draw(Canvas canvas) {
        // This is called continuously right after update() is called
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.GREEN);  // fill background
            master.draw(canvas);
        }
    }

    public Bundle saveInstance(Bundle savedInstanceState) {
        /**
         * Called by MainActivity.onSaveInstanceState() to save all data
         *  needed to set game back to where it left off.
         */
        savedInstanceState.putInt("difficulty", master.difficulty);
        // TODO: Store random seed when implement shuffle
        // Stop any cards in motion
        if (cardsInMotion) {
            master.endStackMotion(-9999, -9999);
        }
        long timeElapsed = master.stopClock();
        savedInstanceState.putLong("TimeElapsed", timeElapsed);
        int numMoves = master.historyTracker.getNumMoves();
        savedInstanceState.putInt("NumMoves", numMoves);
        // putString, putBoolean, putDouble
        return savedInstanceState;
    }
}