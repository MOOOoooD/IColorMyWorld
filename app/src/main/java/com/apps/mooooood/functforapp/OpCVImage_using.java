package com.apps.mooooood.functforapp;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
//import android.support.v4.view.MotionEventCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Point;


import org.opencv.imgproc.Imgproc;

/**
 * OpCVImage_using.java - this class takes in images seleced by users from
 *  their device gallery and converts the image to a binary lined image
 *  as an example of how the openCV functions can work on an image
 *  and to provide an example of zoom and drag functionality on an
 *  Image View object
 *
 * @author Denise Fullerton
 * @since created 1/28/18
 * @since last updated - 5/12/18
 */

public class OpCVImage_using extends AppCompatActivity{


    // zoom/drag buttons
    Button dragModeBtn;
    ImageButton zoomIn;
    ImageButton zoomOut;
    ImageButton dragUp;
    ImageButton dragDown;
    ImageButton dragLeft;
    ImageButton dragRight;

    // booleans to determine which method to use to process image
    boolean lap = false;
    boolean can = false;

    // Log tag consants
    private static final String SOBELTAG = "SOBEL";
    private static final String CANNYTAG = "CANNY";
    private static final String LAPTAG = "LAPLACE";
    private static final String MERGEDTAG = "SOB-CAN";
    private static final String  TAG = "CheckOpCVImage_using";
    private static final String CHECK_TAG = "Check-OpCVImage_using ";

    // Activity result intent result code
    private static int RESULT_LOAD_IMG = 1;

    // zoom/scale float variables
    private static float MIN_ZOOM = .6f;
    private static float MAX_ZOOM = 6f;
    private static float SCALE_CHANGE = 0.4f;
    private static float SHIFT_UD = 30F;
    private static float SHIFT_LR = 40F;
    private float scaleFactor = 1.f;

    // Gesture detector variable
    private ScaleGestureDetector detector;

    // Event listener state variable
    private int mEventState;

    boolean DRAG_MODE = true;
    private final static int NONE = 0;
    private final static int PAN = 1;
    private final static int ZOOM = 2;

    // screen coordinate values for move and scale
    private float mStartX = 0;
    private float mStartY = 0;
    private float prevTranslateX = 0;
    private float prevTranslateY = 0;
    private float mTranslateX = 0;
    private float mTranslateY = 0;


    // initialize the pointers for onTouch events
    final int IsREMOVED = -1;
    int primaryPtr = IsREMOVED;
    int primaryPtrIndex = IsREMOVED;
    int secondPtr = IsREMOVED;
    int secondPtrIndex = IsREMOVED;

    // Object to store converted image for screen display
    ImageView loaded_img;

    /**
     * Callbach method to load and validate OpenCV library has loaded successfully
     *      to/for this activity
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            Log.d(CHECK_TAG, "in BLC onManConn");
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    Log.d(CHECK_TAG, "Switch Default: "+status);

                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    /**
     *
     *
     */
    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(CHECK_TAG, "in onResume");

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        Log.d(CHECK_TAG, "onResume, after if ");
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_op_cvimage);

        // loades OpenCV library
        System.loadLibrary("opencv_java3");

        // assigns ids to button view objects
        Button lap_Btn = findViewById(R.id.lap_load_btn);
        Button can_Btn = findViewById(R.id.can_load_btn);
        Button sob_Btn = findViewById(R.id.sobel_load_btn);
        dragModeBtn = findViewById(R.id.drag_mode_switch_btn);
        zoomIn = findViewById(R.id.zoom_in_btn);
        zoomOut = findViewById(R.id.zoom_out_btn);
        dragUp = findViewById(R.id.img_up_btn);
        dragDown = findViewById(R.id.img_down_btn);
        dragLeft = findViewById(R.id.img_left_btn);
        dragRight = findViewById(R.id.img_right_btn);

        // setting onclick listener for lap_load_btn - sets lap=true and can=false, so
        // laplacian method would be called on image result
        lap_Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                lap = true;
                can = false;
                Intent image = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(image, RESULT_LOAD_IMG);
            }
        });

        // setting onclick listener for can_load_btn - sets lap=false and can=true, so
        // canny method would be called on image result
        can_Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                lap = false;
                can = true;
                Intent image = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(image, RESULT_LOAD_IMG);
            }
        });

        // setting onclick listener for sobel_load_btn - sets lap and can boolean false, so
        // sobel method would be called on image result
        sob_Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                lap = false;
                can = false;
                Intent image = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(image, RESULT_LOAD_IMG);
            }
        });

        // image view object to hold loaded image
        loaded_img = (ImageView) findViewById(R.id.loaded_image);

        // sets scalegesture detector to listen to gesture events on screen
        detector = new ScaleGestureDetector(this, new MySimpleOnScaleGestureListener(loaded_img));
    }

    /**
     * onToucheEvent takes in motionEvent 'heard' and passes event to switch case to process
     *      course of action based on event
     *      always returns true
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e){

        switch (e.getActionMasked()) {

            // switch case to register primary pointer to move image, if single touchevent
            // point is on imageView
            // gets location of touch point, and calculates the difference between last
            // point touched and current point so that image does not jump when
            // new point is touched on screen
            case MotionEvent.ACTION_DOWN:
                DRAG_MODE = true;
                // Primary pointer is registered
                primaryPtrIndex = e.getActionIndex();
                primaryPtr = e.getPointerId(primaryPtrIndex);
                if (primaryPtrIndex != IsREMOVED && DRAG_MODE) {
                    mStartX = e.getX() - prevTranslateX;
                    mStartY = e.getY() - prevTranslateY;
                }
                break;

            // switch case to register secondary pointer for multi-touch, if more than
            // one touchevent point is on imageView
            case MotionEvent.ACTION_POINTER_DOWN:
                mEventState = ZOOM;
                // Secondary pointer is registered
                secondPtrIndex = e.getActionIndex();
                secondPtr = e.getPointerId(secondPtrIndex);
                break;

            // switch case to shift image based on movement of single touchevent
            // on imageView - used for image dragging
            case MotionEvent.ACTION_MOVE:
                mEventState = PAN;
                if (primaryPtrIndex != IsREMOVED) {
                    mTranslateX = e.getX() - mStartX;
                    mTranslateY = e.getY() - mStartY;
                }
                break;

            // switch case clear registered touchevents for multi-touch detected
            case MotionEvent.ACTION_POINTER_UP:
                primaryPtrIndex = IsREMOVED;
                secondPtrIndex = IsREMOVED;
                break;

            // switch case clear registered touchevents for single touch removed from
            // screen - stores current coordinate where single touch is lifted from screen
            // as previous translation X and Y values so image will not skip across
            // screen when user lifts finger and moves to another point
            case MotionEvent.ACTION_UP:
                mEventState = NONE;
                prevTranslateX = mTranslateX;
                prevTranslateY = mTranslateY;
                primaryPtrIndex = IsREMOVED;
                secondPtrIndex = IsREMOVED;
        }

        detector.onTouchEvent(e);

        // sets the image to locations shifted based on user touch inputs
        if(mEventState == PAN && DRAG_MODE){
            loaded_img.setTranslationX(mTranslateX);
            loaded_img.setTranslationY(mTranslateY);
        }
        return true;
    }


    //  Use for pinch Zoom
    /**
     * Scalegesture detector that is used with user motionevents to drag and zoom image
     *      view to resize and move image based on constant values for MIN_ZOOM and
     *      MAX_ZOOM and boolean DRAG_MODE
     */
    private class MySimpleOnScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{

        public MySimpleOnScaleGestureListener(ImageView imgView){
            super();
            if(DRAG_MODE) {
                loaded_img = imgView;
            }
        }
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detect){return true;}

        @Override
        public boolean onScale(ScaleGestureDetector detect){

            if(DRAG_MODE) {
                scaleFactor *= detect.getScaleFactor();

                // minZoom before was 0.1f
                scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
                loaded_img.setScaleX(scaleFactor);
                loaded_img.setScaleY(scaleFactor);
            }
            return true;
        }
    }

    /**
     * re-inflates activity_op_cvimage.xml after image is selected in from the device
     *      image gallery by the user, and displays resulting image based after
     *      image filters are applied
     *      method takes in request code to identify which intent the callback is using,
     *      resultCode from URI to ensure image will be returned, and intent including description
     *      of action to be performed and passed in activity
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data){
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            assert selectedImage != null;
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            if(lap){
                // Laplacian Filter
                lapFilter(picturePath);
            }else if(can){
                Log.d("IF_CAN"," go to canny filter_can-"+can+", lap-"+lap);
                // Canny Filter
                canFilter(picturePath);
                //  Merged can and sobel
                //sobANDcan(picturePath);
                Log.d("IF_CAN"," go to canny filter");
            }else{
                sobFilter(picturePath);
            }
        }
    }

    /***  Sobel Filter  ***/
    /**
     *  Sobel filter method using OpenCV functions
     *      method applies a sobel filter to image
     *      in attempt enhance image feature edges as the image is converted to a binary image
     *      method takes in the picture path to the selected image from
     *      device image gallery and sets converted image to image view object
     *      on screen
     * @param pictPath
     */
    public void sobFilter(String pictPath){
        Log.d(SOBELTAG," --> ** START sobFilter(pp) ** <--");

        Bitmap bitmap = BitmapFactory.decodeFile(pictPath);
        Mat loadedImg = new Mat();
        Utils.bitmapToMat(bitmap, loadedImg );

        //Sobel
        Imgproc.GaussianBlur(loadedImg, loadedImg, new Size(3,3),0,0,Core.BORDER_DEFAULT);
        Imgproc.GaussianBlur(loadedImg, loadedImg, new Size(3,3),0,0,Core.BORDER_DEFAULT);
        Imgproc.cvtColor(loadedImg, loadedImg, Imgproc.COLOR_RGB2GRAY);

        // Varibles needed
        int scale = 1, delta = 0, xDx = 1, xDy = 0, yDy = 1, yDx = 0, kernalSz = 3;
        double alph = 0.5, bet = 0.5, gamm = 1;
        Mat gradXY = new Mat(), absGradX = new Mat(), absGradY = new Mat();

        // Gradient X
        Imgproc.Sobel(loadedImg, gradXY, CvType.CV_16S,xDx,xDy,kernalSz,scale,delta,Core.BORDER_DEFAULT);
        Core.convertScaleAbs(gradXY, absGradX);
        gradXY = new Mat();

        // Gradient Y
        Imgproc.Sobel(loadedImg, gradXY, CvType.CV_16S,yDx,yDy,kernalSz,scale,delta,Core.BORDER_DEFAULT);
        loadedImg.release();
        Core.convertScaleAbs(gradXY, absGradY);

        loadedImg = new Mat();
        gradXY.release();
        Core.addWeighted(absGradX,alph,absGradY,bet,gamm,loadedImg);
        absGradX.release();
        absGradY.release();

        Imgproc.threshold(loadedImg, loadedImg, 17.0, 255, Imgproc.THRESH_BINARY_INV);

        // set Mat to Bitmap
        bitmap = Bitmap.createBitmap(loadedImg.width(), loadedImg.height(),Bitmap.Config.ARGB_8888 );
        Utils.matToBitmap(loadedImg, bitmap);
        loadedImg.release();
        Log.d(SOBELTAG," --> ** END sobFilter(pp) ** <--");

        loaded_img = findViewById(R.id.loaded_image);
        loaded_img.setImageBitmap(bitmap);
    }

    /***  Canny Filter  ***/
    /**
     *  Canny filter method using OpenCV functions
     *      method applies a canny filter to image
     *      in attempt enhance image edges as it is converted to a binary image
     *      method takes in the picture path to the selected image from
     *      device image gallery and sets converted image to image view object
     *      on screen
     * @param pictPath
     */
    public void canFilter(String pictPath){
        Log.d(CANNYTAG," --> ** START canFilter(pp) ** <--");
        Bitmap bitmap = BitmapFactory.decodeFile(pictPath);
        Mat loadedImg = new Mat();
        Utils.bitmapToMat(bitmap, loadedImg );
        // Canny filter
        Imgproc.cvtColor(loadedImg, loadedImg, Imgproc.COLOR_RGB2GRAY);
        Mat canImg = new Mat();

        // Normalized Block Blur
        // Imgproc.blur(loadedImg, loadedImg, new Size(3,3), new Point(-1,-1));

        // Gaus Blur Filter
        Imgproc.GaussianBlur(loadedImg, canImg, new Size(3,5),0,0,Core.BORDER_DEFAULT);

        // Median Filter
        //Imgproc.medianBlur(loadedImg,canImg,7);
        loadedImg = new Mat();

        int lowThresh = 10;
        int ratio = 3;
        int kernalSize = 3;

        Imgproc.Canny(canImg, loadedImg, lowThresh, lowThresh * ratio, kernalSize, false );
        canImg = new Mat();

        // invert colors
        Imgproc.threshold(loadedImg, canImg, 25.0, 255, Imgproc.THRESH_BINARY_INV);

        loadedImg.release();
        bitmap = Bitmap.createBitmap(canImg.width(), canImg.height(),Bitmap.Config.ARGB_8888 );
        Utils.matToBitmap(canImg, bitmap);
        canImg.release();
        can = false;

        Log.d(CANNYTAG," --> ** END canFilter(pp) ** <--");

        loaded_img = findViewById(R.id.loaded_image);
        loaded_img.setImageBitmap(bitmap);
    }

    /*** Laplacian Filter ***/
    /**
     *  Laplacian filter method using OpenCV functions
     *      method applies a laplacian filter to image
     *      in attempt enhance image feature edges as binary image
     *      method takes in the picture path to the selected image from
     *      device image gallery and sets converted image to image view object
     *      on screen
     * @param pictPath
     */
    public void lapFilter(String pictPath){
        Log.d(LAPTAG," --> ** START lapFilter(pp) ** <--");

        Bitmap bitmap = BitmapFactory.decodeFile(pictPath);
        Mat loadedImg = new Mat();
        Utils.bitmapToMat(bitmap, loadedImg );

        //Laplacian of gausian
        Imgproc.GaussianBlur(loadedImg, loadedImg, new Size(3,3),0,0,Core.BORDER_DEFAULT);
        Imgproc.cvtColor(loadedImg, loadedImg, Imgproc.COLOR_RGB2GRAY);

        Mat bLap = new Mat();
        Imgproc.Laplacian(loadedImg, bLap, CvType.CV_16S,3,1,0,Core.BORDER_DEFAULT);
        loadedImg.release();

        Mat lapImg = new Mat();
        bLap.convertTo(lapImg,CvType.CV_8UC4);
        bLap.release();

        // invert colors
        Imgproc.threshold(lapImg, lapImg, 15.0, 255, Imgproc.THRESH_BINARY_INV);
        //Log.d(LAPTAG,lapImg.channels()+" ");

        bitmap = Bitmap.createBitmap(lapImg.width(), lapImg.height(),Bitmap.Config.ARGB_8888 );
        Utils.matToBitmap(lapImg, bitmap);
        lapImg.release();
        lap = false;

        Log.d(LAPTAG," --> ** END lapFilter(pp) ** <--");

        loaded_img = findViewById(R.id.loaded_image);
        loaded_img.setImageBitmap(bitmap);
    }

    /***  Sobel AND Canny   currently not connected to button ****/
    /**
     *  Combination Sobel and Canny filter method using OpenCV functions
     *      method applies a combination of a sobel filter and canny filter to image
     *      in attempt enhance image feature edges as binary image
     *      method takes in the picture path to the selected image from
     *      device image gallery and sets converted image to image view object
     *      on screen
     * @param pictPath
     */
    public void sobANDcan(String pictPath){
        Log.d(MERGEDTAG," --> ** START sobANDcan(pp) ** <--");

        Bitmap bitmap = BitmapFactory.decodeFile(pictPath);
        Mat loadedImg = new Mat();
        Utils.bitmapToMat(bitmap, loadedImg );

/**   Canny Part  ******/
        Log.d(MERGEDTAG,"--> START Canny section <--");

        Mat canImg = new Mat();
        Imgproc.cvtColor(loadedImg, canImg, Imgproc.COLOR_RGB2GRAY);
        // Normalized Block Blur
        // Imgproc.blur(loadedImg, loadedImg, new Size(3,3), new Point(-1,-1));
        // Gaus Blur Filter
        // Imgproc.GaussianBlur(loadedImg, loadedImg, new Size(5,5),0,0,Core.BORDER_DEFAULT);
        // Median Filter
        Imgproc.medianBlur(canImg,canImg,7);
        Mat canImgB = new Mat();
        int lowThresh = 20;
        int ratio = 3;
        int kernalSize = 3;
        Imgproc.Canny(canImg, canImgB, lowThresh, lowThresh * ratio, kernalSize, false );
        canImg = new Mat();
        // invert colors
        Core.bitwise_not(canImgB, canImg);
        canImgB.release();
        Log.d(MERGEDTAG,"--> END Canny section <--");


/**    Sobel Part  ******/
        Log.d(MERGEDTAG,"--> START Sobel section <--");
        Imgproc.GaussianBlur(loadedImg, loadedImg, new Size(5,5),0,0,Core.BORDER_DEFAULT);
        Imgproc.cvtColor(loadedImg, loadedImg, Imgproc.COLOR_RGB2GRAY);
        int scale = 1, delta = 0, xDx = 1, xDy = 0, yDy = 1, yDx = 0, kernalSz = 3;
        double alph = 1.0, bet = 1.0, gamm = 0;
        Mat gradXY = new Mat(), absGradX = new Mat(), absGradY = new Mat();

        // Gradient X
        Imgproc.Sobel(loadedImg, gradXY, CvType.CV_16S,xDx,xDy,kernalSz,scale,delta,Core.BORDER_DEFAULT);
        Core.convertScaleAbs(gradXY, absGradX);
        gradXY = new Mat();

        // Gradient Y
        Imgproc.Sobel(loadedImg, gradXY, CvType.CV_16S,yDx,yDy,kernalSz,scale,delta,Core.BORDER_DEFAULT);
        loadedImg.release();
        Core.convertScaleAbs(gradXY, absGradY);

        loadedImg = new Mat();
        gradXY.release();
        Core.addWeighted(absGradX,alph,absGradY,bet,gamm,loadedImg);
        absGradX.release();
        absGradY.release();

        // invert colors
        Core.bitwise_not(loadedImg, loadedImg);
        Log.d(MERGEDTAG,"-->  END Sobel section <--");

        Mat mergedImg = new Mat();
        Core.bitwise_and(loadedImg,canImg,mergedImg);
        loadedImg.release();
        canImg.release();

        bitmap = Bitmap.createBitmap(mergedImg.width(), mergedImg.height(),Bitmap.Config.ARGB_8888 );
        Utils.matToBitmap(mergedImg, bitmap);
        mergedImg.release();
        can = false;

        Log.d(MERGEDTAG," --> ** END sobANDcan(pp) ** <--");

        loaded_img = findViewById(R.id.loaded_image);
        loaded_img.setImageBitmap(bitmap);
    }

    /**
     * onClick methods that takes in button view objects that changes
     *      the scale factor of the image using constant
     *      SCALE_CHANGE applied when zoom_in_btn or zoom_out_btn
     *      is clicked
     * @param view
     */
    public void zoomIn(View view){
        scaleFactor+= SCALE_CHANGE;
        // minZoom before was 0.1f
        scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
        loaded_img.setScaleX(scaleFactor);
        loaded_img.setScaleY(scaleFactor);
    }
    public void zoomOut(View view){
        scaleFactor-= SCALE_CHANGE;
        // minZoom before was 0.1f
        scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
        loaded_img.setScaleX(scaleFactor);
        loaded_img.setScaleY(scaleFactor);
    }

    /**
     * onClick methods that takes in button view objects that allows
     *      the image on screen to be shifted based on directional button selected
     *      img_up_btn, img_down_btn, img_left_btn, and img_right_btn
     *      adding a constant value to the current x and y values
     *      of where the image is located on screen, which will then
     *      translate/shift where the image is located on screen
     *      using setTranslationX and setTranslationY
     * @param view
     */
    public void imageUp(View view){
        Log.d(TAG, "img up");
        mTranslateY -= SHIFT_UD;
        loaded_img.setTranslationY(mTranslateY);
    }
    public void imageDown(View view){
        Log.d(TAG, "img dwn");
        mTranslateY += SHIFT_UD;
        loaded_img.setTranslationY(mTranslateY);
    }
    public void imageLeft(View view){
        Log.d(TAG, "img LEFT");
        mTranslateX -= SHIFT_LR;
        loaded_img.setTranslationX(mTranslateX);
    }
    public void imageRight(View view){
        Log.d(TAG, "img RIGHT");
        mTranslateX += SHIFT_LR;
        loaded_img.setTranslationX(mTranslateX);
    }

    /**
     * onClick method that takes in button view object
     *      When drag_mode_switch_btn is clicked, changes widget/button
     *      views visible on screen to display or remove zoom and pan buttons
     *      This allows user to use multitouch functionality to zoom and pan
     *      image, or use buttons to zoom and pan image
     * @param view
     */
    public void dragModeViewSwitch(View view){
        DRAG_MODE = !DRAG_MODE;
        if(DRAG_MODE){
            dragModeBtn.setText(R.string.drag_mode_btn_true);
            zoomIn.setVisibility(View.GONE);
            zoomOut.setVisibility(View.GONE);
            dragUp.setVisibility(View.GONE);
            dragDown.setVisibility(View.GONE);
            dragLeft.setVisibility(View.GONE);
            dragRight.setVisibility(View.GONE);
        }else{
            dragModeBtn.setText(R.string.drag_mode_btn_false);
            zoomIn.setVisibility(View.VISIBLE);
            zoomOut.setVisibility(View.VISIBLE);
            dragUp.setVisibility(View.VISIBLE);
            dragDown.setVisibility(View.VISIBLE);
            dragLeft.setVisibility(View.VISIBLE);
            dragRight.setVisibility(View.VISIBLE);
        }
    }

    /**
     * onClick method that takes in img_to_main_btn button view object
     *      and finishes/closes activity_op_cvimage.xml view,
     *      resumes activity_main.xml view
     * @param view
     */
    public void backToMain(View view){
        finish();
    }



}
