package com.apps.mooooood.functforapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * PntCustView_using.java - Custom view class that inflates a custom container
 *  holding multiple canvas objects to display converted images as binary lined
 *  image and uses animation and gesture detectors, Path objects, Paint objects
 *  to 'draw'/color/erase colors in the displayed lined image
 *
 *
 * @author Denise Fullerton
 * @since created 1/28/18
 * @since last updated - 5/12/18
 */

public class PntCustView_using extends View {

    // Constant for log tags
    private static final String PAINT_TAG = "PaintCustView.java";

    // boolean toggles for touch behaviors
    boolean colorToggle = false;
    boolean imgToggle = false;

    // boolean toggles for setting canvas and saving colored image
    boolean save = false;
    boolean setCan = false;

    // drawtool - holds pencil_icon and eraser_icon as toggled
    private Bitmap draw_tool;

    // translation matrix
    private Matrix translate;

    // holds coordinates drawn and paint object
    private Path drawPath;
    // holds color and 'brush' stroke style
    private Paint drawPaint;

    // used to create white canvas backdrop for coloring area
    private Paint canvasPaint;

    // initial color upon instantiation
    private int paintColor;

    // stores last paint color - used when toggleing eraser and pencil
    private int lastPaintColor;

    //canvas-holding pen, holds drawings, transfers to view
    private Canvas drawCanvas;

    // bitmap object used to save image
    Bitmap canvasBitmap;

    // brush size
    private float currentBrushSize;
    private float lastBrushSize;

    // gesture detector
    private GestureDetector gestures;

    // variables to hold x and y coordinates based on gesture listeners
    float moveX=0;
    float moveY=0;
    private float xDiff = 0;
    private float yDiff = 0;
    private float mX;
    private float mY;

    // touch tolerance - can be adjusted - used for movement
    private static final float TOUCH_TOLERANCE = 0;

    // variables to hold and use image coordinates related to view
    int canvasWidth = 0;
    int canvasHeight = 0;
    int imgWidth = 0;
    int imgHeight = 0;
    int centerImg_W;
    int centerImg_H;

    // variables to hold and use for animating pencil_icon and eraser_icon
    int drawToolWidth = 0;
    int drawToolHeight = 0;

    // adjust drawpath start point always be at the tip of the tool
    int centerPen_W = 0;
    int centerPen_H = 0;

    // used to toggle need for moveTo function for drawpath - making sure reset
    // does not start drawing at 0,0
    boolean moveTo = true;
    
    // ArrayLists to act as stacks for for drawing and undoing paths and paints
    private ArrayList<Path> paths = new ArrayList<Path>();
    private ArrayList<Path> undonePaths = new ArrayList<Path>();
    private ArrayList<Paint> allPaints = new ArrayList<Paint>();
    private ArrayList<Paint> undonePaints = new ArrayList<Paint>();

    /**
     * Constructor
     * @param context
     * @param attr
     */
    public PntCustView_using(Context context, AttributeSet attr) {
        super(context, attr);

        RelativeLayout paintLayout = findViewById(R.id.paintScreen);

        // instantiating draw_tool and storing initial height and width
        draw_tool = BitmapFactory.decodeResource(getResources(), R.drawable.pencil_icon);
        drawToolWidth = draw_tool.getWidth();
        drawToolHeight = draw_tool.getHeight();
        //Log.d(PAINT_TAG, "toolW: "+drawToolWidth+", toolH: "+drawToolHeight);

        translate = new Matrix();

        // gesture detector for gesture and scale
        gestures = new GestureDetector(context, new GestureListener(PntCustView_using.this));

        paintColor = getResources().getColor(R.color.red);
        lastPaintColor = paintColor;

        //Log.d(PAINT_TAG, "in Paint View constuctor");

        // calls function to initialize objects
        initialize();
    }


    /**
     * Method initializes brushes, paint objects, path objects for drawing
     *      functionality
     *      Paths store coordinates of movement
     *      Paints store color and style of strokes
     *      setting antialiasing to false so saved lines are clear
     *
     */
    private void initialize() {

        currentBrushSize = getResources().getInteger(R.integer.size_small);
        lastBrushSize = currentBrushSize;

        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);

        drawPaint.setAntiAlias(false);
        drawPaint.setFilterBitmap(true);

        drawPaint.setStrokeWidth(currentBrushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
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
        //Log.d("SET_CAN","checking heights "+centerImg_W+" , "+centerImg_H+" , "+imgHeight+" , "+ imgWidth);
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
                //Log.d("TEST_37","why 37: " +centerImg_H+" result of "+canvasHeight+" - "+imgHeight+" / 2");
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
                //Log.d("ERASE-", "Checking erase "+erasing);
                drawPaint.setStrokeWidth(getResources().getInteger(R.integer.medium_size));
            }else {
                drawPaint.setStrokeWidth(getResources().getInteger(R.integer.size_small));
            }
            //drawPaint.setStrokeWidth(strokWid);
            canvas.drawPath(drawPath, drawPaint);


            canvas.drawBitmap(canvasBitmap, centerImg_W, centerImg_H, null);


          //  Log.d(PAINT_TAG," in PCust W = "+imgWidth+"  H = "+imgHeight);
            if(!save) {
                canvas.drawBitmap(draw_tool, moveX, moveY, null);
            }
        }
        if(t_up){
            canvas.drawBitmap(canvasBitmap, s, s, null);
            t_up = !t_up;
        }
    }


    /**
     * Methods creates canvas based on screen size an
     *      takes in width and height
     *
     * @param w
     * @param h
     * @param oldW
     * @param oldH
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        //create canvas of certain device size
        super.onSizeChanged(w, h, oldW, oldH);

        canvasWidth = w;
        canvasHeight = h-50;
        centerPen_W = canvasWidth-drawToolWidth;
        centerPen_H = (canvasHeight+50-drawToolHeight)/2;
        moveX = centerPen_W;
        moveY = centerPen_H;
    }


    /**
     * Method skews drawpath and drawpaint points on screen to fit 'point' of drawTool
     *      object
     *      takes in touch coordinates and sets movement coordinates at a shifted value
     * @param x
     * @param y
     */
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

    /**
     * onTouchListener for moving drawtool and creating drawpaths
     *      When user holds color button and drags drawTool, drawPaths are created
     *      and stored in and ArrayList with paint objects to display on canvas
     *      responds to down touch, move gesture, and touch up
     *      invalidates as changes occurs - which calls onDraw() to redraw
     *      view with changes
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();


        switch (event.getAction()) {

            // gets coordinate of initial touch
            // shifts drawpath coordinates to edge of drawtool
            case MotionEvent.ACTION_DOWN:
                    xDiff = Math.abs(moveX - touchX);
                    yDiff = Math.abs(moveY - touchY);
                    skew(touchX, touchY);
                    if(!erasing) {
                        touch_start(moveX + xAdj, moveY + draw_tool.getHeight() - yAdj);
                        //Log.d("ACTDOWN", " checking x and y : " + moveX + ", " + moveY);
                        translate.postTranslate(moveX + xAdj, moveY - yAdj);

                    }else{
                        touch_start(moveX + xAdj + 20, moveY + draw_tool.getHeight() - yAdj);
                        //Log.d("ACTDOWN", " checking x and y : " + moveX + ", " + moveY);
                        //  Log.d("CKWD", " Width "+canvasBitmap.getWidth());
                        translate.postTranslate(moveX + xAdj + 20, moveY - yAdj);
                    }
                    invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                skew(touchX, touchY);
                if(!erasing) {
                    touch_move(moveX + xAdj, moveY + draw_tool.getHeight() - yAdj);
                    translate.postTranslate(moveX + xAdj, moveY - yAdj);
                }else{
                    touch_move(moveX + xAdj + 20, moveY + draw_tool.getHeight() - yAdj);
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

    /**
     * Method called when touch listener detects the touch down of the pointer/finger
     *      starts drawpath and paint to new path and paint object
     *      makes new drawpath and paint object for next
     *      takes in x and y coordinate of touch start point
     * @param x
     * @param y
     */
    private void touch_start(float x, float y) {
        //Log.d("T_START", "Checking X and Y : "+x+", "+y);

        if(colorToggle) {
            //Log.d("T_START", "In if statement -> X, Y "+ x +", "+y+" colorToggle : "+colorToggle);
            undonePaths.clear();
            undonePaints.clear();
            drawPath.reset();
            drawPath.moveTo(x, y);
        }
        mX = x;
        mY = y;
    }

    /**
     * Method called when touch listener detects move of pointer/touch to display and add
     *      coordinates to drawpath and store paint object with brush style
     *      in drawpath object
     *      takes in x and y coordinate of move points
     * @param x
     * @param y
     */
    private void touch_move(float x, float y) {

        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
           // Log.d("T_MOVE", "Checking X, Y and colortoggle : "+ x +", "+y+"  colorToggle-"+colorToggle);
            if(colorToggle) {
                //Log.d("T_MOVE", "In if colorToggle dX, dY "+ dx +", "+dy+"   colorTog - : "+colorToggle+" mx, my "+ mX+", "+mY+"  x, y "+x+", "+y);
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

    /**
     * Method called when touch listener detects the lift of the pointer/finger
     *      adds drawpath and paint to active arraylist
     *      makes new drawpath and paint object for next
     *      gesture, onscreen draw
     *      calls invalidate to redraw canvas
     *      sets moveTo to true so next starting point starts
     *      based on touch coordinates and not 0,0
     */
    private void touch_up() {

        paths.add(drawPath);
        drawPath = new Path();
        allPaints.add(drawPaint);
        drawPaint = new Paint(drawPaint);
        invalidate();
        //Log.d("TOUCH_UP", "Color Toggle Value In:"+ colorToggle);
        //Log.d("TOUCH_UP", "Path Size Value In:"+ paths.size());
        moveTo = true;
    }

    /**
     * Method to remove drawpaths that are stored in active drawpath lists and moves
     *      drawpath to inactive list of drawpaths - acts as stack
     *      calls invalidate() to redraw view
     */
    public void onClickUndo() {
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            undonePaints.add(allPaints.remove(allPaints.size() - 1));
            invalidate();
        }
    }

    /**
     * onClick Method to switch onscreen onscreen pencil tool with eraser tool
     *      switches drawable pencil_icon and eraser_icon in custom view area
     *      and the draw_tool image button
     *      sets erasing boolean to true or false
     *      calls invalidate() to activate onDraw() method to update screen
     * @param eraserPencil
     */
    public void onClickEraseDraw(boolean eraserPencil){
        if(!eraserPencil){
            erasing = true;
            draw_tool = BitmapFactory.decodeResource(getResources(), R.drawable.eraser_icon);
        }else{
            erasing = false;
            draw_tool = BitmapFactory.decodeResource(getResources(), R.drawable.pencil_icon);
        }
        invalidate();
    }

    /**
     * Method to add drawpaths that are stored in inactive drawpath due to undo
     *      list back to the list of active drawpaths displayed on the canvas
     *      calls invalidate() to redraw view acts as a stack
     */
    public void onClickRedo() {
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            allPaints.add(undonePaints.remove(undonePaints.size() - 1));
            invalidate();
        }
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
