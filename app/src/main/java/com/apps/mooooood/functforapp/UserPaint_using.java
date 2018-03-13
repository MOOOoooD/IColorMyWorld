package com.apps.mooooood.functforapp;
import android.annotation.SuppressLint;
import android.content.DialogInterface;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.support.v7.widget.PopupMenu;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.CvType;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by denise on 1/28/18.
 */

public class UserPaint_using extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    public static final String P_TAG = "Paint Debug";
    public static final String USER_PAINT = "UserPaint_using.java";

    //private FloatingActionButton floatActBtn;
    private PntCustView_using pCustomView;
    Button load_btn;
    Button colorBtn;
    ImageButton blackBtn;
    ImageButton redBtn;
    ImageButton blueBtn;
    ImageButton greenBtn;
    ImageButton yellowBtn;
    //Button fillBtn;

    int imgWidth = 640;// standard
    int imgHeight = 480;// standard

    Button menuBtn;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_PopupOverlay);
        super.onCreate(savedInstanceState);
        Log.d(P_TAG, "in onCreate before cust view");
        setContentView(R.layout.activity_user_paint);
        System.loadLibrary("opencv_java3");

        load_btn = findViewById(R.id.load_images);

        colorBtn = findViewById(R.id.color_button);
        blackBtn = findViewById(R.id.black_paint);
        redBtn = findViewById(R.id.red_paint);
        blueBtn = findViewById(R.id.blue_paint);
        greenBtn = findViewById(R.id.green_paint);
        yellowBtn = findViewById(R.id.yellow_paint);
        //fillBtn = findViewById(R.id.fill_button);
        menuBtn = findViewById(R.id.menu_button);

        pCustomView = findViewById(R.id.paint_custom_view);
        load_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent image = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(image, RESULT_LOAD_IMG);
            }
        });

//        toolbarTop = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbarTop);

//        Toolbar toolbarBottom = findViewById(R.id.toolbar_bottom);
//        toolbarBottom.inflateMenu(R.menu.draw_menu);
//
//        toolbarBottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                handleDrawingIconTouched(item.getItemId());
//                return false;
//            }
//        });
    }


    public void listMenu(View v){
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.draw_menu);
        popupMenu.show();

    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_delete:
                Toast.makeText(this, "Delete clicked", Toast.LENGTH_SHORT).show();
                deleteDialog();
                return true;
            case R.id.action_undo:
                Toast.makeText(this, "Undo clicked", Toast.LENGTH_SHORT).show();
                pCustomView.onClickUndo();
                return true;
            case R.id.action_redo:
                Toast.makeText(this, "Redo clicked", Toast.LENGTH_SHORT).show();
                pCustomView.onClickRedo();
                return true;
            case R.id.action_share:
                Toast.makeText(this, "Share clicked", Toast.LENGTH_SHORT).show();
                sharePainting();
                return true;
            case R.id.action_save:
                Toast.makeText(this, "Save clicked", Toast.LENGTH_SHORT).show();
                savePainting();
                return true;
            default:
                return false;
        }
    }


    private void handleDrawingIconTouched(int itemId){
        switch(itemId){
            case R.id.action_delete:
                deleteDialog();
                break;
            case R.id.action_undo:
                pCustomView.onClickUndo();
                break;
            case R.id.action_redo:
                pCustomView.onClickRedo();
                break;
            case R.id.action_share:
                sharePainting();
                break;
            case R.id.action_save:
                savePainting();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds the items to the action bar if present
        getMenuInflater().inflate(R.menu.draw_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    // Allert Dialog - confirm the wipe screen call
    private void deleteDialog(){
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
        deleteDialog.setTitle(getString(R.string.delete_drawing));
        deleteDialog.setMessage(getString(R.string.new_draw_warning));
        deleteDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                pCustomView.eraseAll();
                Log.d(P_TAG, " after erase in UserPaint_using...");
                dialogInterface.dismiss();
            }
        });
        deleteDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        deleteDialog.show();
    }


    private String galeryFolder = "/Color_My_World";

    private void savePainting(){

        pCustomView.save = !pCustomView.save;
        pCustomView.setDrawingCacheEnabled(true);
        pCustomView.invalidate();


        //LoadSaveImages saveImage = new LoadSaveImages();

        //saveImage.saveCMWImage(this, pCustomView.getCanvasBitmap());
        Log.d("in SAVE Painting", "hope it workd");
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+galeryFolder;
        File colorDir = new File(filePath);
        if(!colorDir.exists()){
            colorDir.mkdirs();
        }


        Date current = new Date();
        SimpleDateFormat fileSaveDate = new SimpleDateFormat("MMddyy_kkmmss");
        String cmwSaveImgName = "cmw"+fileSaveDate.format(current).toString()+".png";
        File cmvSaveImg = new File(colorDir, cmwSaveImgName);
        try{
            FileOutputStream saveOut = new FileOutputStream(cmvSaveImg);
            Log.d(TAG, "savePainting: INSAVE"+ pCustomView.imgWidth+", "+pCustomView.imgHeight);
            Log.d(TAG, "savePainting: INSAVE"+ pCustomView.centerImg_W+", "+pCustomView.centerImg_H);

            int endW = pCustomView.centerImg_W+pCustomView.imgWidth;
            int endH = pCustomView.centerImg_H+pCustomView.imgHeight;
            Log.d(TAG, "savePainting: INSAVE"+ endW+", "+endH);


            // save only the range of the colored bitmap
            Bitmap nbm ;
            //nbm = Bitmap.createBitmap(pCustomView.getDrawingCache(),pCustomView.centerImg_W,pCustomView.imgHeight,endW,endH);
            nbm = Bitmap.createBitmap(pCustomView.getDrawingCache(),pCustomView.centerImg_W,pCustomView.imgHeight,pCustomView.imgWidth,pCustomView.imgHeight);
            Log.d(TAG, "savePainting: INSAVE"+ pCustomView.imgWidth+", "+pCustomView.imgWidth);
            for(int x = 0; x < nbm.getWidth(); x++){
                for(int y = 0; y < nbm.getHeight(); y++){
                    int bmColor = nbm.getPixel(x,y);
                    int r = Color.alpha(bmColor);
                    if(r==0){
                        nbm.setPixel(x,y,Color.rgb(255,255,255));
                    }
                }
            }

            //newBitmap.setHasAlpha(false);
            nbm.compress(Bitmap.CompressFormat.PNG, 100, saveOut);

            saveOut.flush();
            saveOut.close();
            fileAvailabilityMediaScanner(cmvSaveImg);


            Log.d("in save...", "pleae saveimg");
            Log.d("SAveDirPlease",colorDir.toString());
            Log.d("SAveFile",cmwSaveImgName);

        }catch(Exception e){
            e.printStackTrace();
            Log.d("In Exception","   BLAHHHHH");
        }
        pCustomView.save = !pCustomView.save;

        //pCustomView.saveImage.recycle();


//        pCustomView.save = !pCustomView.save;
//        //cache drawing/img
//
//        pCustomView.setDrawingCacheEnabled(true);
//        pCustomView.invalidate();
//        pCustomView.save = !pCustomView.save;
//
//        String path = Environment.getExternalStorageDirectory().toString();
//        OutputStream fileOut = null;
//        File file = new File(path,"UserPaint_app.png");
//        file.getParentFile().mkdirs();
//
//        try{
//            file.createNewFile();
//        }catch (Exception e){
//            Log.e(P_TAG, "New File: "+e.getCause()+e.getMessage());
//        }
//
//        try{
//            fileOut = new FileOutputStream(file);
//        }catch(Exception e){
//            Log.e(P_TAG, "FileOut "+e.getCause()+e.getMessage());
//        }
//        if(pCustomView.getDrawingCache() == null){
//            Log.e(P_TAG, "Unable to get drawing cache ");
//        }
//        pCustomView.getDrawingCache().compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
//
//        try{
//            fileOut.flush();
//            fileOut.close();
//        }catch(IOException e){
//            Log.e(P_TAG, "Flush-Close: "+e.getCause()+e.getMessage());
//        }
//        Intent shareIntent = new Intent();
//        shareIntent.setAction(Intent.ACTION_SEND);
//        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//        shareIntent.setType("image/png");
//        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }


    //SAve for later
    private String progressFolder = "/Inprogress";

    private void savePaintingForLater() {

        pCustomView.save = !pCustomView.save;
        pCustomView.setDrawingCacheEnabled(true);
        pCustomView.invalidate();


        //LoadSaveImages saveImage = new LoadSaveImages();

        //saveImage.saveCMWImage(this, pCustomView.getCanvasBitmap());
        Log.d("in SAVE Painting", "hope it workd");
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + galeryFolder;
        File colorDir = new File(filePath);
        if (!colorDir.exists()) {
            colorDir.mkdirs();
        }


        Date current = new Date();
        SimpleDateFormat fileSaveDate = new SimpleDateFormat("MMddyy_kkmmss");
        String cmwSaveImgName = "cmw" + fileSaveDate.format(current).toString() + ".png";
        File cmvSaveImg = new File(colorDir, cmwSaveImgName);
        try {
            FileOutputStream saveOut = new FileOutputStream(cmvSaveImg);

            pCustomView.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 100, saveOut);

            saveOut.flush();
            saveOut.close();
            fileAvailabilityMediaScanner(cmvSaveImg);


            Log.d("in save...", "pleae saveimg");
            Log.d("SAveDirPlease", colorDir.toString());
            Log.d("SAveFile", cmwSaveImgName);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("In Exception", "   BLAHHHHH");
        }
        pCustomView.save = !pCustomView.save;
    }
    public static final String EXT_STORAGE = "EXT-STORAGE";

    // Jeff Jones - https://www.youtube.com/watch?v=2Wg45ia6nXs
    private void fileAvailabilityMediaScanner(File file){
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener(){
                    public void onScanCompleted(String path, Uri uri){
                        Log.d(EXT_STORAGE, "Scanned "+ path+":");
                        Log.d(EXT_STORAGE, "-> uri= "+ uri);
                    }
                });
    }

    private void sharePainting(){
        pCustomView.save = !pCustomView.save;
        //cache drawing/img

        pCustomView.setDrawingCacheEnabled(true);
        pCustomView.invalidate();
        pCustomView.save = !pCustomView.save;

        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fileOut = null;
        File file = new File(path,"UserPaint_app.png");
        file.getParentFile().mkdirs();

        try{
            file.createNewFile();
        }catch (Exception e){
            Log.e(P_TAG, "New File: "+e.getCause()+e.getMessage());
        }

        try{
            fileOut = new FileOutputStream(file);
        }catch(Exception e){
            Log.e(P_TAG, "FileOut "+e.getCause()+e.getMessage());
        }
        if(pCustomView.getDrawingCache() == null){
            Log.e(P_TAG, "Unable to get drawing cache ");
        }
        pCustomView.getDrawingCache().compress(Bitmap.CompressFormat.JPEG, 100, fileOut);

        try{
            fileOut.flush();
            fileOut.close();
        }catch(IOException e){
            Log.e(P_TAG, "Flush-Close: "+e.getCause()+e.getMessage());
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        shareIntent.setType("image/png");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Image"));

    }



    public void backToMain(View view){
        finish();
    }
    public void colorSwitch(View view){
        pCustomView.colorToggle = !pCustomView.colorToggle;
        if(pCustomView.colorToggle){

            colorBtn.setText(R.string.color_on);
        }else{
            colorBtn.setText(R.string.color_off);
        }
//
//        if(view.isActivated()){
//            view.setActivated(true);
//            pCustomView.colorToggle = !pCustomView.colorToggle;
//            colorBtn.setText(R.string.color_on);
//        }else{
//            view.setActivated(false);
//            pCustomView.colorToggle = !pCustomView.colorToggle;
//
//            colorBtn.setText(R.string.color_off);
//        }

    }
    public void colorBlack(View view){
        pCustomView.setPaint(getResources().getInteger(R.integer.black));
    }
    public void colorRed(View view){
        pCustomView.setPaint(getResources().getInteger(R.integer.red));
    }
    public void colorBlue(View view){
        pCustomView.setPaint(getResources().getInteger(R.integer.blue));
    }
    public void colorGreen(View view){
        pCustomView.setPaint(getResources().getInteger(R.integer.green));
    }
    public void colorYellow(View view){
        pCustomView.setPaint(getResources().getInteger(R.integer.yellow));
    }

    /*
    For Fill Button

    public void fillColor(View view){
        pCustomView.fillToggle = !pCustomView.fillToggle;
        if(pCustomView.fillToggle){

            fillBtn.setText(R.string.fill_on);
        }else{
            fillBtn.setText(R.string.fill_off);
        }

    }
    */




    private static int RESULT_LOAD_IMG = 1;
    private static final String  TAG = "Checking OpenCV";
    private static final String CHECK_TAG = "Check ";
    private static float MIN_ZOOM = .6f;
    private static float MAX_ZOOM = 6f;
    private static float SCALE_CHANGE = 0.4f;
    private float scaleFactor = 1.f;

    int lowThresh = 17;
    int ratio = 3;
    int kernalSize = 3;

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

    // img resize variables
    int width = 0;
    int height = 0;

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
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            Mat loadedImg = new Mat();
            Utils.bitmapToMat(bitmap, loadedImg );
            // scaling image to fit 480x640 image space
            //if(loadedImg.width()>imgWidth||loadedImg.height()>imgHeight){
            imgWidth = pCustomView.canvasWidth;
            imgHeight = pCustomView.canvasHeight;
            Log.d(USER_PAINT, "P Cust in UP width: "+imgWidth+"  height: "+imgHeight);
            Log.d(USER_PAINT, "p-cust width: "+loadedImg.width()+"  height: "+loadedImg.height());


            if(loadedImg.width()>imgWidth||loadedImg.height()>imgHeight){
                Log.d(USER_PAINT, "P Cust in iff width: "+imgWidth+"  height: "+imgHeight);


                double scale;
                //double scaleH;
                if(((double)imgWidth/(double)loadedImg.width()) <1.0) {
                    scale = ((double) imgWidth / (double) loadedImg.width());
                    //scaleH = ((double) imgWidth / (double) loadedImg.height());
                }else{
                    //scaleW = ((double) imgHeight / (double) loadedImg.width());
                    scale = ((double) imgHeight / (double) loadedImg.height());
                }
                Log.d(USER_PAINT, "loadedn width: "+loadedImg.width()+"  height: "+loadedImg.height());

                //resize variables
                //width = (int)(Math.min(scaleW,scaleH) * loadedImg.width());
                //height = (int)(Math.min(scaleW,scaleH)* loadedImg.height());
                width = (int)(Math.min(scale,scale) * loadedImg.width());
                height = (int)(Math.min(scale,scale)* loadedImg.height());
                //int width = loadedImg.width()/imgWidth;
                //int height = loadedImg.height()/imgWidth;
                Log.d(USER_PAINT, "width: "+width+"  height: "+height);
                Log.d(USER_PAINT, "width: "+scale+"  height: "+scale);

                Size imgReSize = new Size(width, height);
                Mat resizedImg = new Mat();
                Imgproc.resize(loadedImg, resizedImg, imgReSize, 0, 0, Imgproc.INTER_CUBIC);
                loadedImg.release();
                loadedImg = resizedImg.clone();
                resizedImg.release();
                bitmap.recycle();
            }else{
                width = loadedImg.width();
                height= loadedImg.height();
                Log.d(USER_PAINT, "in else : "+width+"  height: "+height);

            }
            pCustomView.imgHeight = height;
            pCustomView.imgWidth = width;


            Log.d(USER_PAINT, "PCust width: "+pCustomView.imgWidth+"  PCust height: "+pCustomView.imgHeight);

            Log.d(USER_PAINT,"ih="+loadedImg.height()+", iw="+loadedImg.width());
            Log.d(USER_PAINT,"scaled h="+height+", scaled w="+width);

/********************************************************

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

            Imgproc.threshold(loadedImg, loadedImg, 15.0, 255, Imgproc.THRESH_BINARY_INV);

            // invert colors
            //Core.bitwise_not(loadedImg, loadedImg);
            //Log.d(SOBELTAG,loadedImg.channels()+" ");

            // set Mat to Bitmap
            bitmap = Bitmap.createBitmap(loadedImg.width(), loadedImg.height(),Bitmap.Config.ARGB_8888 );
            Utils.matToBitmap(loadedImg, bitmap);
            loadedImg.release();

            for(int x = 0; x < bitmap.getWidth(); x++){
                for(int y = 0; y < bitmap.getHeight(); y++){
                    int bmColor = bitmap.getPixel(x,y);
                    int r = Color.red(bmColor);
                    if(r==255){
                        bitmap.setPixel(x,y,Color.alpha(0));
                    }
                }
            }
            pCustomView.setBitmap(bitmap, true);
****************************************************/

            // Canny Filter
//            Imgproc.cvtColor(loadedImg, loadedImg, Imgproc.COLOR_RGB2GRAY);
//
//            //Mat canImg = Mat.zeros(loadedImg.width(), loadedImg.height(), loadedImg.type());
//            //Mat canImg = new Mat();
//
//            Imgproc.blur(loadedImg, loadedImg, new Size(3,3) );
//            Imgproc.Canny(loadedImg, loadedImg, lowThresh, lowThresh * ratio, kernalSize, false );
//            //canImg = Scalar::all(0);
//
//            //loadedImg.copyTo(canImg, loadedImg);
//            // invert colors
//            Core.bitwise_not(loadedImg, loadedImg);

            //Bitmap btmp = Bitmap.createBitmap(loadedImg.width(), loadedImg.height(),Bitmap.Config.ARGB_8888 );
//            bitmap = Bitmap.createBitmap(loadedImg.width(), loadedImg.height(),Bitmap.Config.ARGB_8888 );
//            Utils.matToBitmap(loadedImg, bitmap);

            // using with Laplacian
            Imgproc.GaussianBlur(loadedImg, loadedImg, new Size(5,5),0,0,Core.BORDER_DEFAULT);
            Imgproc.GaussianBlur(loadedImg, loadedImg, new Size(3,3),0,0,Core.BORDER_DEFAULT);
            //Utils.matToBitmap(loadedImg, bitmap);

            Imgproc.cvtColor(loadedImg, loadedImg, Imgproc.COLOR_RGB2GRAY);
//            Utils.matToBitmap(loadedImg, bitmap);
            Log.d("IMG_TYPE","Type = "+loadedImg.type());

            Mat bLap = new Mat();
            Imgproc.Laplacian(loadedImg, bLap, CvType.CV_16S,3,1,0,Core.BORDER_DEFAULT);
            Log.d("IMG_TYPE","Lap_Type = "+loadedImg.type());
            loadedImg.release();
//            //Utils.matToBitmap(loadedImg, bitmap);
            Log.d("IMG_TYPE","Img_Values = "+bLap);

            Mat lapImg = new Mat();

            bLap.convertTo(lapImg,CvType.CV_8UC4);
            bLap.release();
//            //Core.convertScaleAbs(loadedImg, lapImg);
            Log.d("IMG_TYPE","Lap_Type_After = "+lapImg.type());
            Log.d("IMG_TYPE","Img_Values_2 = "+lapImg);
            // invert colors
            Imgproc.threshold(lapImg, lapImg, 13.0, 255, Imgproc.THRESH_BINARY_INV);
            int imgCH = lapImg.channels();
            Log.d(TAG, "Pixel val "+ lapImg.type()+ " chan "+ lapImg.channels());

            //    Core.bitwise_not(lapImg, lapImg);
            //loadedImg.release();
            bitmap = Bitmap.createBitmap(lapImg.width(), lapImg.height(),Bitmap.Config.ARGB_8888 );
            Utils.matToBitmap(lapImg, bitmap);
            lapImg.release();

            for(int x = 0; x < bitmap.getWidth(); x++){
                for(int y = 0; y < bitmap.getHeight(); y++){
                    int bmColor = bitmap.getPixel(x,y);
                    int r = Color.red(bmColor);
                    if(r==255){
                        bitmap.setPixel(x,y,Color.alpha(0));
                    }
                }
            }
            pCustomView.setBitmap(bitmap, true);
        }
    }
    /***  Sobel Filter  ***/
    public void sobFilter(String pictPath){

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

        Imgproc.threshold(loadedImg, loadedImg, 15.0, 255, Imgproc.THRESH_BINARY_INV);

        // invert colors
        //Core.bitwise_not(loadedImg, loadedImg);
        //Log.d(SOBELTAG,loadedImg.channels()+" ");

        // set Mat to Bitmap
        bitmap = Bitmap.createBitmap(loadedImg.width(), loadedImg.height(),Bitmap.Config.ARGB_8888 );
        Utils.matToBitmap(loadedImg, bitmap);
        loadedImg.release();

        for(int x = 0; x < bitmap.getWidth(); x++){
            for(int y = 0; y < bitmap.getHeight(); y++){
                int bmColor = bitmap.getPixel(x,y);
                int r = Color.red(bmColor);
                if(r==255){
                    bitmap.setPixel(x,y,Color.alpha(0));
                }
            }
        }
        pCustomView.setBitmap(bitmap, true);

    }


}
