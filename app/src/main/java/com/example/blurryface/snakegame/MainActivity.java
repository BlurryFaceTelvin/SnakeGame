package com.example.blurryface.snakegame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity {
    Canvas canvas;
    Bitmap headAnimBitmap;
    Rect rectToBeDrawn;
    int frameHeight = 64,frameWidth =64,frameNumber,numFrames = 6;


    SnakeAnimView snakeAnimView;

    int screenWidth,screenHeight;

    long lastFrameTime;
    int fps;
    int high;

    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        headAnimBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.head_sprite_sheet);
        snakeAnimView = new SnakeAnimView(this);
        setContentView(snakeAnimView);
        intent = new Intent(this,GameActivity.class);

    }

    @Override
    protected void onResume() {
        super.onResume();
        snakeAnimView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeAnimView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        snakeAnimView.pause();
        finish();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            snakeAnimView.pause();
            finish();
            return true;
        }
        return false;
    }

    public class SnakeAnimView extends SurfaceView implements Runnable{
        Thread ourThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playSnake;
        Paint paint;
        public SnakeAnimView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();
            frameWidth = headAnimBitmap.getWidth()/numFrames;
            frameHeight = headAnimBitmap.getHeight();
        }

        @Override
        public void run() {
            while (playSnake)
            {
                update();
                draw();
                controlFPS();
            }

        }
        public void update()
        {
            rectToBeDrawn = new Rect((frameNumber*frameWidth)-1,0,(frameNumber*frameWidth+frameWidth)-1,frameHeight);
            frameNumber++;
            if(frameNumber==numFrames)
                frameNumber=0;
        }


        public void draw() {
            if(ourHolder.getSurface().isValid())
            {
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.BLACK);
                paint.setColor(Color.BLUE);
                paint.setTextSize(125);

                canvas.drawText("Snake",10,150,paint);
                paint.setTextSize(25);
                canvas.drawText("High Score: "+high,10,screenHeight-50,paint);
                Rect desRect = new Rect(screenWidth/2-100,screenHeight/2-100,screenWidth/2+100,screenHeight/2+100);
                canvas.drawBitmap(headAnimBitmap,rectToBeDrawn,desRect,paint);
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }
        public void controlFPS(){
            long timeThisFrame = System.currentTimeMillis() - lastFrameTime;
            long timeToSleep = 15 - timeThisFrame;
            if(timeThisFrame>0)
            {
                fps = (int) (100/timeThisFrame);
            }
            if(timeToSleep>0)
            {
                try {
                    Thread.sleep(timeToSleep);
                }catch (InterruptedException e){}
            }
            lastFrameTime = System.currentTimeMillis();
        }
        public void pause() {
            playSnake = false;
            try {
                ourThread.join();
            }catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        public void resume()
        {
            playSnake = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            startActivity(intent);
            return true;
        }
    }
}
