package com.example.peter.spider.Game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "GameView";
    private static final String GAME_STATE_FILE_NAME = "game_state.txt";
    private MainThread thread = null;
    private Context context;
    public Master master;
    private boolean cardsInMotion;

    public GameView(Context context, int difficulty) {
        super(context);
        Log.e(TAG, "New constructor called.");
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

    public GameView(Context context) {
        /**
         * Constructor to restore game where it left off.
         */
        super(context);
        Log.e(TAG, "Restore constructor called.");
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
        ArrayList<String> savedData = readSavedData();
        master = new Master(displayMetrics, mStore, savedData);
    }

    public void updateOrientation(Context context, int orientation) {
        this.context = context;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        boolean portrait = (orientation == Configuration.ORIENTATION_PORTRAIT);
        master.updateOrientation(displayMetrics, portrait);
    }

    private ArrayList<String> readSavedData() {
        File filePath = context.getExternalFilesDir(null);
        File file = new File(filePath, GAME_STATE_FILE_NAME);
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        try {
            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        // separate into arraylist
        String rawData = new String(bytes);
        String[] items = rawData.split("\n");
        ArrayList<String> data = new ArrayList<String>(Arrays.asList(items));
        return data;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated()");
        cardsInMotion = false;
        if (thread == null) {
            Log.e(TAG, "Re-creating thread");
            getHolder().addCallback(this);
            thread = new MainThread(getHolder(), this);
        } else {
            Log.e(TAG, "Thread already exists!!");
        }
        if (!thread.getRunning()) {
            thread.setRunning(true);
            thread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format,
                               int width, int height) {
        Log.e(TAG, "surfaceChanged()");
    }

    public void killThread() {
        if (thread != null) {
            thread.setRunning(false);
            thread = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed()");
//        this.killThread();  // Stop the thread
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

    public void storeGameState() {
        /**
         * Called when surfaceDestroyed() is called after (1) screen
         *  rotation, or (2) back pressed.
         * - Store all game data
         */
        Log.e(TAG, "storeGameState()");
        ArrayList<String> gameData = master.getGameState();
        File filePath = context.getExternalFilesDir(null);
        try {
            File file = new File(filePath, GAME_STATE_FILE_NAME);
            FileWriter writer = new FileWriter(file);
            for (String line : gameData) {
                writer.append(line + "\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            // TODO: May need to check file.exists() and then file.delete()
            e.printStackTrace();
        }
    }
}