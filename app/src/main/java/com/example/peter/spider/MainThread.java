package com.example.peter.spider;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.example.peter.spider.Game.GameView;

public class MainThread extends Thread {
    // Thread allows multiple processes at the same time
    private SurfaceHolder surfaceHolder;  // SurfaceHolder contains the canvas
    private GameView gameView;
    private boolean running = false;
    public static Canvas canvas;  // Canvas is what gets drawn on

    public MainThread(SurfaceHolder surfaceHolder, GameView gameView) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
    }

    public void setRunning(boolean isRunning) {
        running = isRunning;
    }

    public boolean getRunning() {
        return running;
    }

    @Override
    public void run() {
        while(running) {
            canvas = null;
            try {
                canvas = this.surfaceHolder.lockCanvas();  // must lock (freeze) canvas before drawing
                synchronized (surfaceHolder) {
                    this.gameView.update();
                    this.gameView.draw(canvas);
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(100);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }  // end run()
}
