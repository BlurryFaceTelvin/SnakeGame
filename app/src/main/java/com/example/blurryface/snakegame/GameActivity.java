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
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Switch;

import java.util.Random;

public class GameActivity extends Activity {
    Canvas canvas;
    SnakeView snakeView;
    Bitmap headBitmap,tailBitmap,bodyBitmap,appleBitmap,backBitMap,grass;

    //sound
    SoundPool soundPool;
    int sample1=-1,sample2=-1,sample3=-1,sample4=-1;

    //direction 0=up,1=right,2=down,3=left
    int directionOfTravel = 0;

    int screenWidth,screenHeight,topGap;
    int speed =100;

    long lastFrameTime;
    int fps,score,high;
    //coordinates of the snake x and y
    int[] snakeX,snakeY;
    int snakeLength,appleX,appleY;

    int blockSize,numBlockWide,numBlockHigh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        sample1 = soundPool.load(this,R.raw.sample1,0);
        sample2 = soundPool.load(this,R.raw.sample2,0);
        sample3 = soundPool.load(this,R.raw.sample3,0);
        sample4 = soundPool.load(this,R.raw.sample4,0);
        Display display = getWindowManager().getDefaultDisplay();
        Point size =new Point();
        display.getSize(size);
        screenHeight = size.y;
        screenWidth = size.x;
        topGap = screenHeight/14;
        blockSize = screenWidth/40;
        numBlockWide = 40;
        numBlockHigh = (screenHeight-topGap)/blockSize;

        headBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.head);
        tailBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.tail);
        bodyBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.body);
        appleBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.apple);
        grass = BitmapFactory.decodeResource(getResources(),R.drawable.grass);

        headBitmap = Bitmap.createScaledBitmap(headBitmap,blockSize,blockSize,false);
        bodyBitmap = Bitmap.createScaledBitmap(bodyBitmap,blockSize,blockSize,false);
        tailBitmap = Bitmap.createScaledBitmap(tailBitmap,blockSize,blockSize,false);
        appleBitmap = Bitmap.createScaledBitmap(appleBitmap,blockSize,blockSize,false);

        snakeView = new SnakeView(this);
        setContentView(snakeView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        snakeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        snakeView.pause();
        finish();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            snakeView.pause();
            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
            finish();
            return  true;
        }
        return false;
    }

    class SnakeView extends SurfaceView implements Runnable{
        Thread gameThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playGame;
        Paint paint;

        public SnakeView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();
            snakeX = new int[200];
            snakeY = new int[200];
            getSnake();
            getApple();
        }

        @Override
        public void run() {
            while (playGame){
            updateGame();
            drawGame();
            controlFPS();
            }

        }
        public void getSnake()
        {
            snakeLength =3;
            snakeX[0]= numBlockWide/2;
            snakeY[0] = numBlockHigh/2;

            snakeX[1] = snakeX[0]-1;
            snakeY[1] = snakeY[0];

            snakeX[1]= snakeX[1]-1;
            snakeY[1] = snakeY[0];
        }
        public void getApple()
        {
            Random random = new Random();
            appleX = 1+random.nextInt(numBlockWide-1);
            appleY = 1+random.nextInt(numBlockHigh-1);
        }
        long time = 0;
        public void updateGame()
        {
            if (time == 0) {
                time = System.currentTimeMillis();
            }

            if(System.currentTimeMillis() > (time + 10000)) {
                getApple();
                time = System.currentTimeMillis();
            }

            if(snakeX[0]==appleX&&snakeY[0]==appleY)
            {
                snakeLength++;
                getApple();
                score++;
                speed-=10;
                soundPool.play(sample1,1,1,0,0,1);
            }
            for (int i=snakeLength;i>0;i--)
            {
                snakeX[i] = snakeX[i-1];
                snakeY[i] = snakeY[i-1];
            }
            switch (directionOfTravel)
            {
                case 0:
                    snakeY[0]--;
                    break;
                case 1:
                    snakeX[0]++;
                    break;
                case 2:
                    snakeY[0]++;
                    break;
                case 3:
                    snakeX[0]--;
                    break;
            }
            //hitting wall
            boolean isDead = false;
            if(snakeX[0]<0)
                isDead=true;
            if (snakeX[0]>=numBlockWide)
                isDead=true;
            if(snakeY[0]<0)
                isDead=true;
            if(snakeY[0]>=numBlockHigh)
                isDead=true;
            //eating your self
            for (int i=snakeLength-1;i>0;i--)
                if(i<4&&(snakeX[0]==snakeX[i])&&(snakeY[0]==snakeY[i]))
                    isDead=true;

            if(isDead){
                soundPool.play(sample4,1,1,0,0,1);
                score=0;
                getSnake();
            }
        }
        public void drawGame(){
            if(ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();
                //canvas.drawBitmap(grass,screenWidth,screenHeight,paint);
                canvas.drawColor(Color.BLACK);
                paint.setColor(Color.WHITE);
                paint.setTextSize(topGap/2);
                canvas.drawText("Score "+score+ "High Score "+high,10,topGap-6,paint);
                paint.setStrokeWidth(3);
                canvas.drawLine(1,topGap,screenWidth-1,topGap,paint);
                canvas.drawLine(screenWidth-1,topGap,screenWidth-1,topGap+(numBlockHigh*blockSize),paint);
                canvas.drawLine(screenWidth-1,topGap+(numBlockHigh*blockSize),1,topGap+(numBlockHigh*blockSize),paint);
                canvas.drawLine(1,topGap+(numBlockHigh*blockSize),1,topGap,paint);

                //snake drawing
                canvas.drawBitmap(headBitmap,snakeX[0]*blockSize,(snakeY[0]*blockSize)+topGap,paint);
                //body
                for (int i=1;i<snakeLength-1;i++){
                    canvas.drawBitmap(bodyBitmap,snakeX[i]*blockSize,(snakeY[i]*blockSize)+topGap,paint);
                }
                //tail
                canvas.drawBitmap(tailBitmap,snakeX[snakeLength-1]*blockSize,(snakeY[snakeLength-1]*blockSize)+topGap,paint);
                //apple
                canvas.drawBitmap(appleBitmap,appleX*blockSize,(appleY*blockSize)+topGap,paint);
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }
        public void controlFPS()
        {
            long timeThisFrame = System.currentTimeMillis() - lastFrameTime;
            long timeToSleep = speed - timeThisFrame;
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
        public void pause()
        {
            playGame = false;
            try {
                gameThread.join();
            }catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        public void resume()
        {
            playGame = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch(event.getAction())
            {
                case MotionEvent.ACTION_UP:
                    if (event.getX()>=screenWidth/2) {
                        directionOfTravel++;
                        if (directionOfTravel==4)
                            directionOfTravel=0;
                    }
                    else{
                        directionOfTravel--;
                        if(directionOfTravel<0)
                            directionOfTravel=3;
                    }
                    break;

            }
            return true;
        }
    }
}
