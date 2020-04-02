package com.example.peter.spider;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.peter.spider.Game.Master;

class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "GameView";
    private MainThread thread;
    private boolean cardsInMotion;
    private Master master;

    public GameView(Context context, int difficulty) {
        super(context);
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        setFocusable(true);

        // Initialize Master (holds GameMaster and stacks)
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        master = new Master(displayMetrics, difficulty);
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
                // Do nothing if initial touch was not active/ cardsInMotion is false
                break;
            case MotionEvent.ACTION_UP:
                // touch was released
                Log.e("action", "Touch released, x:" + String.valueOf(x) + ", y:" + String.valueOf(y));
                if (cardsInMotion) {
                    // Lock into place if legal move, else go back to initial location
                    boolean gameOver = master.endStackMotion(x, y);
                    cardsInMotion = false;
                    if (gameOver) {
                        // TODO: Stop clock, go to game won screen, show stats
                        Log.e(TAG, "You win!");
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

}