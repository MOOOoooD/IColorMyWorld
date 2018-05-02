package com.apps.mooooood.functforapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * Created by denise on 1/28/18.
 */

public class PntCustView_using extends View {
    boolean colorToggle = false;
    boolean imgToggle = false;
    boolean save = false;
    boolean setCan = false;

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
    private int paintColor;
    private int lastPaintColor;
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
    int centerImg_W;
    int centerImg_H;

    int penWidth = 0;
    int penHeight = 0;
    int centerPen_W = 0;
    int centerPen_H = 0;
    Context needContext;


    boolean moveTo = true;


    // adding path list for drawing and undoing paths
    private ArrayList<Path> paths = new ArrayList<Path>();
    private ArrayList<Path> undonePaths = new ArrayList<Path>();
    private ArrayList<Paint> allPaints = new ArrayList<Paint>();
    private ArrayList<Paint> undonePaints = new ArrayList<Paint>();
    AttributeSet pAttr;

    public PntCustView_using(Context context, AttributeSet attr) {
        super(context, attr);

        pAttr = attr;
        // for pencil!!!
        RelativeLayout paintLayout = findViewById(R.id.paintScreen);
        //pencil = BitmapFactory.decodeResource(getResources(), R.drawable.test_pencil);
        pencil = BitmapFactory.decodeResource(getResources(), R.drawable.pencil_icon);
        penWidth = pencil.getWidth();
        penHeight = pencil.getHeight();
        //Log.d(PAINT_TAG, "penW: "+penWidth+", penH: "+penHeight);


        translate = new Matrix();
        gestures = new GestureDetector(context, new GestureListener(PntCustView_using.this));
        detector = new ScaleGestureDetector(context,new zScaleGestureListener());

        paintColor = getResources().getColor(R.color.red);
        lastPaintColor = paintColor;
        needContext = context;

        //Log.d(PAINT_TAG, "in Paint View constuctor");
        initialize();
    }



    // Initialize Variables
    private void initialize() {
        currentBrushSize = getResources().getInteger(R.integer.size_small);
        lastBrushSize = currentBrushSize;

        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);

        // attempting to resolve lines created in paint objects with antialiasing
        drawPaint.setAntiAlias(false);
        drawPaint.setFilterBitmap(true);

        drawPaint.setStrokeWidth(currentBrushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private float scaleFactor = 1.f;
    private static float MIN_ZOOM = .6f;
    private static float MAX_ZOOM = 6f;
    private static float SCALE_CHANGE = 0.4f;
    private class zScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        public zScaleGestureListener(){
            super();
        }

        @Override
        public boolean onScale(ScaleGestureDetector detect){

            Log.d("SCALE_C","In Scale Gesture Detector");
                scaleFactor *= detect.getScaleFactor();
                scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));

            return true;
        }

    }


//    //Bitmap saveImage;
    public Bitmap getCanvasBitmap(){
        return canvasBitmap;
    }

    boolean paintChange = false;

    boolean erasing = false;
    /**
     * when color is changed, when color button is selected
     * store current drawpath and paint and create new ones so that
     * existing path does not change color
     * @param color
     */
    public void setPaint(int color){

        paintColor = color;
        paintChange=true;
        paths.add(drawPath);
        drawPath = new Path();
        allPaints.add(drawPaint);
        drawPaint = new Paint(drawPaint);
        drawPaint.setColor(paintColor);
    }
    public void setLastPaintColor() {
        lastPaintColor = paintColor;
    }
    public int getLastPaintColor(){
        return lastPaintColor;
    }

    private ScaleGestureDetector detector;

    public void setCanvas(){
        drawCanvas = new Canvas(canvasBitmap);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        invalidate();
    }


    Paint p = new Paint();
    boolean t_up = false;
    Rect s = new Rect();
    int pbarWidth;
    int pbarHeight;
    // stores drawing paths in ArrayList, draws the path on screen
    @Override
    protected void onDraw(Canvas canvas) {

        // to set the canvas and image
        if(imgToggle & !t_up) {
            if(setCan){
                //if()
                centerImg_W = (canvasWidth-imgWidth)/2;
                centerImg_H = (canvasHeight-imgHeight)/2;
                Log.d("TEST_37","why 37: " +centerImg_H+" result of "+canvasHeight+" - "+imgHeight+" / 2");
                setCanvas();
                setCan = false;
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.WHITE);
                canvas.drawRect(centerImg_W,centerImg_H,imgWidth+centerImg_W,imgHeight+centerImg_H,p);
                s.set(centerImg_W,centerImg_H,imgWidth+centerImg_W,imgHeight+centerImg_H);
            }


            canvas.drawRect(centerImg_W,centerImg_H,imgWidth+centerImg_W,imgHeight+centerImg_H,p);

            // stores drawpaths - color paths
            for (int i = 0; i < paths.size(); i++) {
                canvas.drawPath(paths.get(i), allPaints.get(i));
            }

            // draws path behind canvas bitmap, and under current drawpath
            if(erasing){
                Log.d("ERASE-", "Checking erase "+erasing);
                drawPaint.setStrokeWidth(getResources().getInteger(R.integer.medium_size));
            }else {
                drawPaint.setStrokeWidth(getResources().getInteger(R.integer.size_small));
            }
            //drawPaint.setStrokeWidth(strokWid);
            canvas.drawPath(drawPath, drawPaint);


            canvas.drawBitmap(canvasBitmap, centerImg_W, centerImg_H, null);


          //  Log.d(PAINT_TAG," in PCust W = "+imgWidth+"  H = "+imgHeight);
            if(!save) {
                canvas.drawBitmap(pencil, moveX, moveY, null);
            }
        }
        if(t_up){
            canvas.drawBitmap(canvasBitmap, s, s, null);
            t_up = !t_up;
        }
    }



    // creates canvas due to screen size
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        //create canvas of certain device size
        super.onSizeChanged(w, h, oldW, oldH);

        canvasWidth = w;
        canvasHeight = h-50;
        // create Bitmap of certain w, h
        //canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        //canvasBitmap = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.halloween_background);
        //canvasBitmap = canvasBitmap.copy(Bitmap.Config.ARGB_8888, true);

        //Log.d(PAINT_TAG, "losing my mind - "+canvasBitmap);
//        Log.d(PAINT_TAG, "--- new group ---");
//        Log.d(PAINT_TAG, "---V-> V-Width: "+canvasWidth+", V-Height: "+canvasHeight);
//        Log.d(PAINT_TAG, "---I-> I-Width: "+imgWidth+", I-Height: "+imgHeight);
//        centerImg_W = (canvasWidth-imgWidth)/2;
//        centerImg_H = (canvasHeight-imgHeight)/2;
//        Log.d(PAINT_TAG,"---C-> CenterW: "+centerImg_W+", CenterH: "+centerImg_H);
        centerPen_W = canvasWidth-penWidth;
        centerPen_H = (canvasHeight+50-penHeight)/2;
        moveX = centerPen_W;
        moveY = centerPen_H;

        //apply bitmap to graphic to start drawing
        //drawCanvas = new Canvas(canvasBitmap);
    }



    public void skew(float x, float y){
        if(x > moveX ){
            moveX = x-xDiff;
           // Log.d("X > MOVE", "X, MoveX: "+x+", "+moveX+"  xD:"+xDiff);
        }
        if(y > moveY){
            moveY = y-yDiff;
          //  Log.d("Y > MOVE", "Y, MoveY: "+y+", "+moveY+"  yD:"+yDiff);

        }
        if(x < moveX ){
            moveX = x+xDiff;
        //    Log.d("X < MOVE", "X, MoveX: "+x+", "+moveX);

        }
        if(y < moveY){
            moveY = y+yDiff;
          //  Log.d("Y > MOVE", "Y, MoveY: "+y+", "+moveY);

        }
    }

    int xAdj = 0;//8;
    int yAdj = 0;//6;;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        //xDiff = Math.abs(moveX-touchX);
        //yDiff = Math.abs(moveY-touchY);
        // respond to down, move and up events
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                    xDiff = Math.abs(moveX - touchX);
                    yDiff = Math.abs(moveY - touchY);



                    skew(touchX, touchY);
                    if(!erasing) {
                        touch_start(moveX + xAdj, moveY + pencil.getHeight() - yAdj);
                        Log.d("ACTDOWN", " checking x and y : " + moveX + ", " + moveY);
                        //  Log.d("CKWD", " Width "+canvasBitmap.getWidth());
                        translate.postTranslate(moveX + xAdj, moveY - yAdj);
                        // need to be able to access the colorImage function in OpenCV_Paint_Image and set the
                        // color - probably in the touch_move
                    }else{
                        touch_start(moveX + xAdj + 20, moveY + pencil.getHeight() - yAdj);
                        Log.d("ACTDOWN", " checking x and y : " + moveX + ", " + moveY);
                        //  Log.d("CKWD", " Width "+canvasBitmap.getWidth());
                        translate.postTranslate(moveX + xAdj + 20, moveY - yAdj);
                    }
                    invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                skew(touchX, touchY);
                if(!erasing) {
                    touch_move(moveX + xAdj, moveY + pencil.getHeight() - yAdj);
                    translate.postTranslate(moveX + xAdj, moveY - yAdj);
                }else{
                    touch_move(moveX + xAdj + 20, moveY + pencil.getHeight() - yAdj);
                    translate.postTranslate(moveX + xAdj + 20, moveY - yAdj);
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                touch_up();
                break;

            default:
                return false;
        }

        // return true;
        return gestures.onTouchEvent(event);
    }

    /**************************************************/

    // Start new Drawing - work on side effects that are cool
    public void eraseAll() {

        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        drawPaint.reset();
        drawPath.reset();
        paths.clear();
        allPaints.clear();
        invalidate();
    }

    // called when finger touches screen
    private void touch_start(float x, float y) {
        Log.d("T_START", "Checking X and Y : "+x+", "+y);

        if(colorToggle) {
            Log.d("T_START", "In if statement -> X, Y "+ x +", "+y+" colorToggle : "+colorToggle);
            undonePaths.clear();
            undonePaints.clear();
            drawPath.reset();
            drawPath.moveTo(x, y);
        }
        mX = x;
        mY = y;
    }

    // evaluating move of user
    private void touch_move(float x, float y) {

        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
           // Log.d("T_MOVE", "Checking X, Y and colortoggle : "+ x +", "+y+"  colorToggle-"+colorToggle);

            if(colorToggle) {
                Log.d("T_MOVE", "In if colorToggle dX, dY "+ dx +", "+dy+"   colorTog - : "+colorToggle+" mx, my "+ mX+", "+mY+"  x, y "+x+", "+y);

                if(moveTo) {
                    drawPath.moveTo(mX, mY);
                    moveTo=false;
                }
                drawPath.quadTo(mX, mY, ((x + mX) / 2), ((y + mY) / 2));
            }
            mX = x;
            mY = y;
            if(!colorToggle){
                moveTo=true;
            }
        }
    }

    // called when user lifts finger
    private void touch_up() {
        //fillToggle = !fillToggle;

        Log.d("TOUCH_UP", "Color Toggle Value B4:"+ colorToggle);
        Log.d("TOUCH_UP", "Path Size Value B4:"+ paths.size());

        if(colorToggle) {
//            drawPath.lineTo(mX, mY);
//            drawCanvas.drawPath(drawPath, drawPaint);
            paths.add(drawPath);
            drawPath = new Path();
            allPaints.add(drawPaint);
            drawPaint = new Paint(drawPaint);
            invalidate();
            Log.d("TOUCH_UP", "Color Toggle Value In:"+ colorToggle);
            Log.d("TOUCH_UP", "Path Size Value In:"+ paths.size());
            //colorToggle = false;
        }
        moveTo = true;
        Log.d("TOUCH_UP", "Color Toggle Value AFT:"+ colorToggle);
        Log.d("TOUCH_UP", "Path Size Value AFT:"+ paths.size());

        //drawPath.reset();


    }

    // methods to trigger the undo and redo
    public void onClickUndo() {
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            undonePaints.add(allPaints.remove(allPaints.size() - 1));
            invalidate();
        }
    }

    public void onClickEraseDraw(boolean eraserPencil){
        if(!eraserPencil){
            erasing = true;
            pencil = BitmapFactory.decodeResource(getResources(), R.drawable.eraserd);
        }else{
            erasing = false;
            pencil = BitmapFactory.decodeResource(getResources(), R.drawable.pencil_icon);
        }
        invalidate();

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

    /**
     * sets canvas with bitmap - toggles boolean values to allow coloring
     * @param bitmap
     * @param loaded
     */
    public void setBitmap(Bitmap bitmap, boolean loaded){
        canvasBitmap = bitmap;
        imgToggle = loaded;
        setCan = loaded;
    }

    /**
     * Gesture detector for pencil and drawpaths
     */
    private class GestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{

        public static final String G_TAG = "Gesture tag ";
        PntCustView_using view;
        public GestureListener(PntCustView_using view){this.view = view;}
        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {return true;}
        @Override
        public boolean onDown(MotionEvent motionEvent) {return true;}
        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float velX,
                               float velY) {return true;}
        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1,
                                float distanceX, float distanceY) {return true;}
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
