package com.example.simplegameengine;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity {

    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameView = new GameView(this);
        setContentView(gameView);
    }

    class GameView extends SurfaceView implements Runnable {
        Thread gameThread = null;

        SurfaceHolder outHolder;

        volatile boolean playing;

        Canvas canvas;
        Paint paint;

        long fps;
        private long timeThisFrame;

        Bitmap bitmapBob;
        float bobSize;

        boolean isMoving = false;

        float walkSpeedPerSecond = 150;
        float bobXPosition = 10;

        int xMax;

        int width, height;

        public GameView(Context context) {
            super(context);

            outHolder = getHolder();
            paint = new Paint();
            bobSize = getBobSize();
            getScreenSize();

            bitmapBob = BitmapFactory.decodeResource(this.getResources(), R.drawable.bob);
        }

        public float getBobSize()
        {
            BitmapFactory.Options dimensions = new BitmapFactory.Options();
            dimensions.inJustDecodeBounds = true;
            Bitmap bob = BitmapFactory.decodeResource(getResources(), R.drawable.bob, dimensions);
            float width = dimensions.outWidth;
            return width;
        }

        public void getScreenSize()
        {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            width = size.x;
            height = size.y;
            xMax = getResources().getDisplayMetrics().widthPixels;
        }

        @Override
        public void run() {
            while (playing) {
                long startFrameTime = System.currentTimeMillis();
                update();

                draw();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame > 0) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void update() {
            if (isMoving & bobXPosition < xMax - bobSize*2)
            {
                bobXPosition = bobXPosition + (walkSpeedPerSecond / fps);
            }
        }

        public void draw() {
            if (outHolder.getSurface().isValid()) {
                canvas = outHolder.lockCanvas();
                canvas.drawColor(Color.argb(255, 26, 128, 182));
                paint.setColor(Color.argb(255, 249, 129, 0));
                paint.setTextSize(45);
                canvas.drawText("FPS: " + fps, 20, 40, paint);
                canvas.drawText("xMax=" + xMax, 20, 100, paint);
                canvas.drawText("bobXPos=" + bobXPosition, 20, 160, paint);
                canvas.drawBitmap(bitmapBob, bobXPosition, 200, paint);
                outHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void pause() {
            playing = false;
            try
            {
                gameThread.join();
            }
            catch(InterruptedException e)
            {
                Log.e("Error:!", "Joining Thread");
            }
        }

        public void resume ()
        {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent)
        {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK)
            {
                case MotionEvent.ACTION_DOWN:
                    isMoving = true;
                    break;
                case MotionEvent.ACTION_UP:
                    isMoving = false;
                    break;
            }
            return true;
        }
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        gameView.pause();
    }
}

