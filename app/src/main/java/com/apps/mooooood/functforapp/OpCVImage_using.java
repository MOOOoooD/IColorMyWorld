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
 * Created by denise on 1/28/18.
 */

public class OpCVImage_using extends AppCompatActivity{

    Button dragModeBtn;

    ImageButton zoomIn;
    ImageButton zoomOut;
    ImageButton dragUp;
    ImageButton dragDown;
    ImageButton dragLeft;
    ImageButton dragRight;
    boolean lap = false;
    boolean can = false;

    private static final String SOBELTAG = "SOBEL";
    private static final String CANNYTAG = "CANNY";
    private static final String LAPTAG = "LAPLACE";
    private static final String MERGEDTAG = "SOB-CAN";

    private static int RESULT_LOAD_IMG = 1;
    private static final String  TAG = "CheckOpCVImage_using";
    private static final String CHECK_TAG = "Check-OpCVImage_using ";
    private static final String HWCHECK = "OpCVImage_using-HW ";

    private static float MIN_ZOOM = .6f;
    private static float MAX_ZOOM = 6f;
    private static float SCALE_CHANGE = 0.4f;
    private static float SHIFT_UD = 30F;
    private static float SHIFT_LR = 40F;
    private float scaleFactor = 1.f;
    //private float btnScaleFactor = 1.f;

    // Use in conjnction with pinch to zoom - scale gesture
    //private float previousScaleFactor = 0.0f;
    private ScaleGestureDetector detector;


    boolean DRAG_MODE = true;
    private final static int NONE = 0;
    private final static int PAN = 1;
    private final static int ZOOM = 2;
    private int mEventState;
    private float mStartX = 0;
    private float mStartY = 0;
    private float prevTranslateX = 0;
    private float prevTranslateY = 0;

    private float mTranslateX = 0;
    private float mTranslateY = 0;

    ImageView loaded_img;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_op_cvimage);
        System.loadLibrary("opencv_java3");
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

        lap_Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                lap = true;
                can = false;
                Intent image = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(image, RESULT_LOAD_IMG);
            }
        });

        can_Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                lap = false;
                can = true;
                Intent image = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(image, RESULT_LOAD_IMG);
            }
        });

        sob_Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                lap = false;
                can = false;
                Intent image = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(image, RESULT_LOAD_IMG);
            }
        });


        loaded_img = (ImageView) findViewById(R.id.loaded_image);


        // Use in conjunciton with pinch to zoom - gesture detector
        detector = new ScaleGestureDetector(this, new MySimpleOnScaleGestureListener(loaded_img));

    }



    final int IsREMOVED = -1;

    // initialize the pointers for onTouch events
    int primaryPtr = IsREMOVED;
    int primaryPtrIndex = IsREMOVED;
    int secondPtr = IsREMOVED;
    int secondPtrIndex = IsREMOVED;

    @Override
    public boolean onTouchEvent(MotionEvent e){


        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                DRAG_MODE = true;
                //mEventState = PAN;

                // Primary pinter is registered
                primaryPtrIndex = e.getActionIndex();
                primaryPtr = e.getPointerId(primaryPtrIndex);
                if (primaryPtrIndex != IsREMOVED && DRAG_MODE) {
                    //mEventState = PAN;
                    mStartX = e.getX() - prevTranslateX;
                    mStartY = e.getY() - prevTranslateY;

                }

                break;

//                // saves previous finger up value for smooth translate
//                mStartX = e.getX() - prevTranslateX;
//                mStartY = e.getY() - prevTranslateY;
//                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mEventState = ZOOM;

                // Secondary pointer is registered
                //if(DRAG_MODE) {
                secondPtrIndex = e.getActionIndex();
                secondPtr = e.getPointerId(secondPtrIndex);
                //}
                break;


            case MotionEvent.ACTION_MOVE:
                mEventState = PAN;

                if (primaryPtrIndex != IsREMOVED) {
                    mTranslateX = e.getX() - mStartX;
                    mTranslateY = e.getY() - mStartY;
                    //    loaded_img.setX(e.getX(primaryPtrIndex));
                    //    loaded_img.setY(e.getY(primaryPtrIndex));
                }

                break;

            case MotionEvent.ACTION_POINTER_UP:
                primaryPtrIndex = IsREMOVED;
                secondPtrIndex = IsREMOVED;

                break;

            case MotionEvent.ACTION_UP:
                mEventState = NONE;
                //DRAG_MODE = false;
                // if (primaryPtrIndex != IsREMOVED && DRAG_MODE) {
                prevTranslateX = mTranslateX;
                prevTranslateY = mTranslateY;
                // }
                primaryPtrIndex = IsREMOVED;
                secondPtrIndex = IsREMOVED;

        }

        // Use in conjunction with pinch to zoom
        detector.onTouchEvent(e);
        if(mEventState == PAN && DRAG_MODE){
            loaded_img.setTranslationX(mTranslateX);
            loaded_img.setTranslationY(mTranslateY);
        }



        return true;
    }


    //  Use for pinch Zoom
    private class MySimpleOnScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{

        //ImageView viewImage;

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
                //float scale = detect.getScaleFactor()-1;
                scaleFactor *= detect.getScaleFactor();

                // minZoom before was 0.1f
                scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
//            if(DRAG_MODE){
                loaded_img.setScaleX(scaleFactor);
                loaded_img.setScaleY(scaleFactor);
            }
            return true;
        }

    }


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
    public void sobFilter(String pictPath){
        Log.d(SOBELTAG," --> ** START sobFilter(pp) ** <--");

        Bitmap bitmap = BitmapFactory.decodeFile(pictPath);
        Mat loadedImg = new Mat();
        Utils.bitmapToMat(bitmap, loadedImg );

        //Sobel
        Imgproc.GaussianBlur(loadedImg, loadedImg, new Size(3,3),0,0,Core.BORDER_DEFAULT);
        Imgproc.GaussianBlur(loadedImg, loadedImg, new Size(3,3),0,0,Core.BORDER_DEFAULT);

        //Utils.matToBitmap(loadedImg, bitmap);
        //Imgproc.erode(loadedImg,loadedImg, new Mat(), new Point(-1,-1),2,1, Scalar.all(1));
        //Imgproc.dilate(loadedImg,loadedImg, new Mat(), new Point(-1,-1),2,1, Scalar.all(1));

        Imgproc.cvtColor(loadedImg, loadedImg, Imgproc.COLOR_RGB2GRAY);

        // Varibles needed
        int scale = 1, delta = 0, xDx = 1, xDy = 0, yDy = 1, yDx = 0, kernalSz = 3;
        double alph = 0.5, bet = 0.5, gamm = 1;
        Mat gradXY = new Mat(), absGradX = new Mat(), absGradY = new Mat();

        // Gradient X
        Imgproc.Sobel(loadedImg, gradXY, CvType.CV_16S,xDx,xDy,kernalSz,scale,delta,Core.BORDER_DEFAULT);
        //Log.d(SOBELTAG,"SOBEL_Type = "+loadedImg.type());
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

        // invert colors
        //Core.bitwise_not(loadedImg, loadedImg);
        //Log.d(SOBELTAG,loadedImg.channels()+" ");

        // set Mat to Bitmap
        bitmap = Bitmap.createBitmap(loadedImg.width(), loadedImg.height(),Bitmap.Config.ARGB_8888 );
        Utils.matToBitmap(loadedImg, bitmap);
        loadedImg.release();

        Log.d(SOBELTAG," --> ** END sobFilter(pp) ** <--");

        loaded_img = findViewById(R.id.loaded_image);
        loaded_img.setImageBitmap(bitmap);
        //bitmap.recycle();
    }

    /***  Canny Filter  ***/
    public void canFilter(String pictPath){
        Log.d(CANNYTAG," --> ** START canFilter(pp) ** <--");
        Bitmap bitmap = BitmapFactory.decodeFile(pictPath);
        Mat loadedImg = new Mat();
        Utils.bitmapToMat(bitmap, loadedImg );
        // Canny filter
        Imgproc.cvtColor(loadedImg, loadedImg, Imgproc.COLOR_RGB2GRAY);
        Mat canImg = new Mat();

        // Normalized Block Blur
//            Imgproc.blur(loadedImg, loadedImg, new Size(3,3), new Point(-1,-1));

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

        //Log.d("CANNY","imgh="+loadedImg.height()+", iw="+loadedImg.width());

        // invert colors
        //Core.bitwise_not(loadedImg, canImg);
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
//            //Core.convertScaleAbs(loadedImg, lapImg);
        //Log.d(LAPTAG,"Lap_Type_After = "+lapImg.type());

        // invert colors
        // Core.bitwise_not(lapImg, lapImg);
        Imgproc.threshold(lapImg, lapImg, 15.0, 255, Imgproc.THRESH_BINARY_INV);
        //Log.d(LAPTAG,lapImg.channels()+" ");

        bitmap = Bitmap.createBitmap(lapImg.width(), lapImg.height(),Bitmap.Config.ARGB_8888 );
        Utils.matToBitmap(lapImg, bitmap);
        lapImg.release();
        lap = false;

        Log.d(LAPTAG," --> ** END lapFilter(pp) ** <--");

        loaded_img = findViewById(R.id.loaded_image);
        loaded_img.setImageBitmap(bitmap);
        //bitmap.recycle();
    }

    /***  Sobel AND Canny   ****/
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
//            Imgproc.blur(loadedImg, loadedImg, new Size(3,3), new Point(-1,-1));
        // Gaus Blur Filter
//            Imgproc.GaussianBlur(loadedImg, loadedImg, new Size(5,5),0,0,Core.BORDER_DEFAULT);
        // Median Filter
        Imgproc.medianBlur(canImg,canImg,7);
        //Log.d(MERGEDTAG,"--> In Canny sect <--");
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


    public static int calculateInSampleSize( BitmapFactory.Options options, int reqWidth, int reqHeight){

        // Raw Height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth){
            final int halfHeight = height/2;
            final int halfWidth = width/2;

            // Calculate the largest insampleSize value that is a power of 2 and keeps both
            //   height and width larger the the requested height and width
            while((halfHeight/inSampleSize) >= reqHeight && (halfWidth/inSampleSize) >= reqWidth){
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


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

    public void dragMode(View view){
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

    public void backToMain(View view){
        finish();
    }



}
