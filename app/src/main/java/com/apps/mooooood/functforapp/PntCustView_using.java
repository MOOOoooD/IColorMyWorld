package com.apps.mooooood.functforapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;

import java.util.ArrayList;


/**
 * Created by denise on 1/28/18.
 */

public class PntCustView_using extends View {
    boolean colorToggle = false;
    boolean fillToggle = false;
    boolean imgToggle = false;
    boolean save = false;

    private Bitmap pencil;
    private Matrix translate;
    private Canvas tool;

    //drawing path
    private Path drawPath;
    //defines what to draw
    private Paint canvasPaint;
    // defines how to draw
    private Paint drawPaint;
    // initial color
    private int paintColor = 0xFF00ABAD;
    //canvas-holding pen, holds drawings, transfers to view
    private Canvas drawCanvas;
    // canvas - bitmap
    Bitmap canvasBitmap;
    // brush size
    private float currentBrushSize;
    private float lastBrushSize;

    private float mX;
    private float mY;
    private static final float TOUCH_TOLERANCE = 8;
    private static final String PAINT_TAG = "PaintCustView.java";

    // added to drag pencil
    private GestureDetector gestures;
    float moveX=0;
    float moveY=0;

    private float mLastX;
    private float mLastY;
    private float touchedX;

    private float mStartX = 0;
    private float mStartY = 0;
    private float xDiff = 0;
    private float yDiff = 0;

    private float mTranslateX = 0;
    private float mTranslateY = 0;


    private float shiftLeft = 300;

    // center bitmap on canvas coord variables
    int canvasWidth = 0;
    int canvasHeight = 0;

    int imgWidth = 0;
    int imgHeight = 0;
    int centerImg_W = 0;
    int centerImg_H = 0;

    int penWidth = 0;
    int penHeight = 0;
    int centerPen_W = 0;
    int centerPen_H = 0;



    // adding path list for drawing and undoing paths
    private ArrayList<Path> paths = new ArrayList<Path>();
    private ArrayList<Path> undonePaths = new ArrayList<Path>();
    private ArrayList<Paint> allPaints = new ArrayList<Paint>();
    private ArrayList<Paint> undonePaints = new ArrayList<Paint>();

    public PntCustView_using(Context context, AttributeSet attr) {
        super(context, attr);

        // for pencil!!!
        RelativeLayout paintLayout = findViewById(R.id.paintScreen);
        pencil = BitmapFactory.decodeResource(getResources(), R.drawable.test_pencil);
        penWidth = pencil.getWidth();
        penHeight = pencil.getHeight();
        Log.d(PAINT_TAG, "penW: "+penWidth+", penH: "+penHeight);


        translate = new Matrix();
        gestures = new GestureDetector(context, new GestureListener(PntCustView_using.this));



        Log.d(PAINT_TAG, "in Paint View constuctor");
        initialize();
    }



    // Initialize Variables
    private void initialize() {
        currentBrushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = currentBrushSize;

        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setFilterBitmap(true);
        drawPaint.setStrokeWidth(currentBrushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);


        //canvasBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.halloween_background);
        //canvasBitmap = canvasBitmap.copy(Bitmap.Config.ARGB_8888, true);

        drawCanvas = new Canvas();


        canvasPaint = new Paint(Paint.DITHER_FLAG);

        // added to drag pencil

        post(new Runnable() {
            public void run(){
                Log.d("RUNTAG","Width "+ PntCustView_using.this.getMeasuredWidth());
                Log.d("RUNTAG","Height "+ PntCustView_using.this.getMeasuredHeight());
            }
        });


    }
    //Bitmap saveImage;
    public Bitmap getCanvasBitmap(){


        // adding to save current bitmap only
 //       save = true;
        //invalidate();

//        saveImage = Bitmap.createBitmap(imgWidth, imgHeight,Bitmap.Config.ARGB_8888 );
//        drawCanvas.drawBitmap(saveImage, new Rect(centerImg_W, centerImg_H, imgWidth, imgHeight), new Rect(0,0,imgWidth, imgHeight),null);


        //save = false;
        //return saveImage;

        return canvasBitmap;

    }

    public void setPaint(int color){

        Log.d("Color ", "C ="+color);

        paintColor = color;
        drawPaint.setColor(paintColor);
    }

    // stores drawing paths in ArrayList, draws the path on screen
    @Override
    protected void onDraw(Canvas canvas) {


        if(imgToggle) {
            centerImg_W = (canvasWidth-imgWidth)/2;
            centerImg_H = (canvasHeight-imgHeight)/2;

            // stores drawpaths - color paths
            for (int i = 0; i < paths.size(); i++) {
                canvas.drawPath(paths.get(i), allPaints.get(i));
            }
            //drawPaint.setColor(paintColor);

            canvas.drawPath(drawPath, drawPaint);


            canvas.drawBitmap(canvasBitmap, centerImg_W, centerImg_H, null);
            Log.d(PAINT_TAG," in PCust W = "+imgWidth+"  H = "+imgHeight);

        }


        if(!save) {
            canvas.drawBitmap(pencil, moveX, moveY, null);
        }
    }

    // creates canvas due to screen size
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        //create canvas of certain device size
        super.onSizeChanged(w, h, oldW, oldH);

        canvasWidth = w;
        canvasHeight = h;
        // create Bitmap of certain w, h
        //canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        //canvasBitmap = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.halloween_background);
        //canvasBitmap = canvasBitmap.copy(Bitmap.Config.ARGB_8888, true);

        //Log.d(PAINT_TAG, "losing my mind - "+canvasBitmap);
        Log.d(PAINT_TAG, "--- new group ---");
        Log.d(PAINT_TAG, "---V-> V-Width: "+canvasWidth+", V-Height: "+canvasHeight);
        Log.d(PAINT_TAG, "---I-> I-Width: "+imgWidth+", I-Height: "+imgHeight);
//        centerImg_W = (canvasWidth-imgWidth)/2;
//        centerImg_H = (canvasHeight-imgHeight)/2;
        Log.d(PAINT_TAG,"---C-> CenterW: "+centerImg_W+", CenterH: "+centerImg_H);
        centerPen_W = canvasWidth-penWidth;
        centerPen_H = (canvasHeight-penHeight)/2;
        moveX = centerPen_W;
        moveY = centerPen_H;

        //apply bitmap to graphic to start drawing
        //drawCanvas = new Canvas(canvasBitmap);
    }



    public void skew(float x, float y){
        if(x > moveX ){
            moveX = x-xDiff;
            //Log.d("X > MOVE", "X, MoveX: "+x+", "+moveX+"  xD:"+xDiff);
        }
        if(y > moveY){
            moveY = y-yDiff;
            //Log.d("Y > MOVE", "Y, MoveY: "+y+", "+moveY+"  yD:"+yDiff);

        }
        if(x < moveX ){
            moveX = x+xDiff;
            //Log.d("X < MOVE", "X, MoveX: "+x+", "+moveX);

        }
        if(y < moveY){
            moveY = y+yDiff;
            //Log.d("Y > MOVE", "Y, MoveY: "+y+", "+moveY);

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        //xDiff = Math.abs(moveX-touchX);
        //yDiff = Math.abs(moveY-touchY);
        // respond to down, move and up events
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDiff = Math.abs(moveX-touchX);
                yDiff = Math.abs(moveY-touchY);


                skew(touchX, touchY);
                touch_start(moveX, moveY+pencil.getHeight());
                Log.d("CKX", " X is "+moveX);
                Log.d("CKWD", " Width "+canvasBitmap.getWidth());
                translate.postTranslate(moveX, moveY);
                // need to be able to access the colorImage function in OpenCV_Paint_Image and set the
                // color - probably in the touch_move

                /*****
                 if(touchX > mLastX) {
                 touch_start(touchX - shiftLeft, touchY);
                 moveX = touchX - shiftLeft;
                 moveY = touchY - pencil.getHeight();
                 translate.postTranslate(moveX, moveY-pencil.getHeight());
                 }else if(touchX < mLastX){
                 touch_start(touchX + shiftLeft, touchY);
                 moveX = touchX + shiftLeft;
                 moveY = touchY - pencil.getHeight();
                 translate.postTranslate(moveX, moveY-pencil.getHeight());
                 }
                 mLastX = moveX;
                 ****/

//                touch_start(touchX - shiftLeft, touchY);
//                moveX = touchX - shiftLeft;
//                moveY = touchY - pencil.getHeight();

                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                //xDiff = Math.abs(moveX-touchX);
                //yDiff = Math.abs(moveY-touchY);
                skew(touchX, touchY);
                touch_move(moveX,moveY+pencil.getHeight());
                translate.postTranslate(moveX, moveY);


                /****
                 if(touchX > mLastX) {
                 touch_move(touchX - shiftLeft, touchY);
                 moveX = touchX - shiftLeft;
                 moveY = touchY - pencil.getHeight();
                 translate.postTranslate(moveX, moveY-pencil.getHeight());
                 }else if( touchX< mLastX){
                 touch_move(touchX + shiftLeft, touchY);
                 moveX = touchX + shiftLeft;
                 moveY = touchY - pencil.getHeight();
                 translate.postTranslate(moveX, moveY-pencil.getHeight());
                 }
                 mLastX = moveX;
                 ****/
//                touch_move(touchX-shiftLeft, touchY);
//                moveX = touchX-shiftLeft;
//                moveY = touchY-pencil.getHeight();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
            default:
                return false;
        }

        // return true;
        return gestures.onTouchEvent(event);
    }






    /*************************************************************/
    public void onMove(float distX, float distY){
//        if(touchedX > mLastX) {
//
//        }else {
//            touch_move(touchX + shiftLeft, touchY);
//            moveX = touchX + shiftLeft;
//            moveY = touchY + pencil.getHeight();
//        }


//        invalidate();
    }

    private Matrix animateStart;
    private OvershootInterpolator animateInterpolator;
    private long startTime;
    private long endTime;
    private float totalAnimDistX;
    private float totalAnimDistY;
    public void onAnimateMove(float distX, float distY, long duration){
        animateStart = new Matrix(translate);
        animateInterpolator = new OvershootInterpolator();
        startTime = System.currentTimeMillis();
        endTime = startTime + duration;
        totalAnimDistX = distX;
        totalAnimDistY = distY;
        post(new Runnable() {
            @Override
            public void run() {
                onAnimateStep();
            }
        });

    }
    private void onAnimateStep(){
        long currentTime = System.currentTimeMillis();
        float percentTime = (float)(currentTime - startTime)/(float)(endTime - startTime);
        float percentDistance = animateInterpolator.getInterpolation(percentTime);
        float currentDistX = percentDistance * totalAnimDistX;
        float currentDistY = percentDistance * totalAnimDistY;
        translate.set(animateStart);
        onMove(currentDistX, currentDistY);
        Log.d("ANIMATE ", "Percent Dist: "+percentDistance);
    }

    /**************************************************/

    // Start new Drawing
    public void eraseAll() {

        //drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        paths.clear();
        Log.d(PAINT_TAG, " in Erase All - new drawing??");
        invalidate();
    }

    // called when finger touches screen
    private void touch_start(float x, float y) {
        //tool.drawBitmap(pencil, 500,0, null);

        if(fillToggle){

            int pixel = canvasBitmap.getPixel((int)x,(int)y);
            Point point = new Point((int)x,(int)y);
            fill(point,pixel);

        }else if(colorToggle) {
            undonePaths.clear();
            undonePaints.clear();
            drawPath.reset();
            drawPath.moveTo(x, y);
        }
//        Log.d(PAINT_TAG, " mX, mY "+ mX +", "+mY);
//        Log.d(PAINT_TAG, " X, Y "+ x +", "+y);
        mX = x;
        mY = y;
    }

    private void fill(Point pt, int pix){
        //FloodFillThread flood = new FloodFillThread();
        QueueLinearFloodFiller qFill = new QueueLinearFloodFiller(canvasBitmap,pix,100);
        Log.d("FILL","IN FILL");
        qFill.setTolerance(10);
        qFill.floodFill(pt.x, pt.y);

    }

    // evaluating move of user
    private void touch_move(float x, float y) {

        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            if(colorToggle) {
                drawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            }
            mX = x;
            mY = y;
        }
    }

    // called when user lifts finger
    private void touch_up() {
        //fillToggle = !fillToggle;
        if(colorToggle) {
            drawPath.lineTo(mX, mY);
            drawCanvas.drawPath(drawPath, drawPaint);
            paths.add(drawPath);
            drawPath = new Path();
            allPaints.add(drawPaint);
            drawPaint = new Paint(drawPaint);
        }

    }

    // methods to trigger the undo and redo
    public void onClickUndo() {
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            undonePaints.add(allPaints.remove(allPaints.size() - 1));

            invalidate();
        }
    }

    public void onClickRedo() {
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            allPaints.add(undonePaints.remove(undonePaints.size() - 1));
            invalidate();
        }
    }

    public void setBrushSize(float newSize){
        float pixelAmt = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
        currentBrushSize = pixelAmt;
        canvasPaint.setStrokeWidth(newSize);

    }
    public void setLastBrushSize(float lastSize){
        lastBrushSize = lastSize;
    }
    public float getLastBrushSize(){
        return lastBrushSize;
    }
    public interface OnNewBrushSizeSelectedListener{
        void onNewBrushSizeSelected(float newBrushSize);
    }


    public void setBitmap(Bitmap bitmap, boolean loaded){
        canvasBitmap = bitmap;
        imgToggle = loaded;

    }


    /*****************************************************************/
    // For PENCIL!!

    private class GestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{

        public static final String G_TAG = "Gesture tag ";

        PntCustView_using view;
        public GestureListener(PntCustView_using view){this.view = view;}

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            Log.v(G_TAG, "onDoubleTap");
            return true;
        }

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            //           Log.v(G_TAG, "onDown");
            return true;
        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float velX, float velY) {
//            Log.v(G_TAG, "onFling");
            final float distanceTimeFactor = 0.4f;
            final float totalDistX = (distanceTimeFactor * velX/2);
            final float totalDistY = (distanceTimeFactor * velY/2);
            view.onAnimateMove(totalDistX, totalDistY, (long)(1000 * distanceTimeFactor));
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float distanceX, float distanceY) {
////            Log.v(G_TAG, "onScroll");
//            view.onMove(-distanceX, -distanceY);
            return true;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {}
        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {return false;}
        @Override
        public void onLongPress(MotionEvent motionEvent) {}
        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {return false;}
        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {return false;}

    }


}
