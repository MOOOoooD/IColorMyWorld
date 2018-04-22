package com.apps.mooooood.functforapp;
import android.annotation.SuppressLint;
import android.content.DialogInterface;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.PopupMenu;
import android.widget.LinearLayout;
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
    Button brownBtn;
    Button blueBtn;
    Button purpleBtn;
    Button redBtn;
    Button orangeBtn;
    Button yellowBtn;
    Button greenBtn;

    // from activity_menu_cust_view
    Button color;

    int imgWidth = 640;// standard
    int imgHeight = 480;// standard

    private MenuCustView menuCustView;
    private LinearLayout pbar;


    @SuppressLint({"WrongViewCast", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_PopupOverlay);
        super.onCreate(savedInstanceState);

        Log.d(P_TAG, "in onCreate before cust view");
        setContentView(R.layout.activity_user_paint);

        // loads the openCV library
        System.loadLibrary("opencv_java3");


        /** may not need to find view by id **/
        //  blackBtn = findViewById(R.id.black_paint);
        brownBtn = findViewById(R.id.colorBrBtn);
        blueBtn = findViewById(R.id.colorBlueBtn);
        purpleBtn = findViewById(R.id.colorPBtn);
        redBtn = findViewById(R.id.colorRedBtn);
        orangeBtn = findViewById(R.id.colorOBtn);
        yellowBtn = findViewById(R.id.colorYBtn);
        greenBtn = findViewById(R.id.colorGBtn);
        pCustomView = findViewById(R.id.paint_custom_view);
        color= findViewById(R.id.color_button);
        pbar = findViewById(R.id.pallete_bar);


        /**
         * setting onTouchListener for touch and hold button
         * may switch to on long click -
         * When user holds button and drags pencil, creates drawPaths
         * for 'coloring' on canvas
         *
         */
        color.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pCustomView.colorToggle = true;
                        //pCustomView.invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        pCustomView.colorToggle = false;
                        view.performClick();
                        //pCustomView.invalidate();
                        break;
                }
                return false;
            }
        });
    }



    /**
     * onClick method to load images
     * @param v - load_btn
     */
    public void loadImg(View v){
        Intent image = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(image, RESULT_LOAD_IMG);
    }

    /**
     * onClick method to display popup menu
     * @param v - menu_btn
     */
    public void listMenu(View v){
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.draw_menu);
        popupMenu.show();
    }

    /**
     * Menu items - with Toasts for menu item selected
     * takes in item based onMenuItemClickListener
     * returns boolean based on item clicked
     * @param item
     * @return
     */
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

    /*************************************************
For menu items on bottom tool bar

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
    ***********************************************/



    // Allert Dialog - confirm the wipe screen call

    /**
     * Delete dialogs
     * need to connect delete behavior
     */
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

    /**
     * need to implement delete painting and option to load image
     *
     */
    private void deletePainting(){

    }

    /**
     * name for gallery saved on phone
     */
    private String galleryFolder = "/Color_My_World";


    /**
     * saves colored canvas in gallery established by app
     */
    private void savePainting(){

        pCustomView.save = !pCustomView.save;
        pCustomView.setDrawingCacheEnabled(true);
        pCustomView.invalidate();
        //LoadSaveImages saveImage = new LoadSaveImages();

        //saveImage.saveCMWImage(this, pCustomView.getCanvasBitmap());
        Log.d("in SAVE Painting", "hope it workd");
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ galleryFolder;
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

            int startW, startH, endW, endH;

            /**
             * if img width and height is less than actual size
             * use pcustview.center img w and h for start and pcustview imgwidth/height for end
             * if pcustview.center img h or w is < 0, then use 0 for the right one to endpoint
             * pcustview.getwidth()/height()
             *
             */
            if(pCustomView.centerImg_W < 0){
                startW = 0;
                endW = pCustomView.getWidth();
            }else{
                startW = pCustomView.centerImg_W;
                endW = pCustomView.imgWidth;
            }
            if(pCustomView.centerImg_H<0){
                startH = 0;
                endH = pCustomView.getHeight();
            }else{
                startH = pCustomView.centerImg_H;
                endH = pCustomView.imgHeight;
            }

            // save only the range of the colored bitmap
             Bitmap savImg = Bitmap.createBitmap(pCustomView.getDrawingCache(),startW, startH,endW,endH);

            for(int x = 0; x < savImg.getWidth(); x++){
                for(int y = 0; y < savImg.getHeight(); y++){
                    int bmColor = savImg.getPixel(x,y);
                    int r = Color.alpha(bmColor);
                    if(r==0){
                        savImg.setPixel(x,y,Color.rgb(255,255,255));
                    }
                }
            }

            //newBitmap.setHasAlpha(false);
            savImg.compress(Bitmap.CompressFormat.PNG, 100, saveOut);
            savImg.recycle();
            saveOut.flush();
            saveOut.close();
            fileAvailabilityMediaScanner(cmvSaveImg);


        }catch(Exception e){
            //e.printStackTrace();
            Log.d("In Exception","Could not save file: \n"+e.getStackTrace());
        }
        pCustomView.save = !pCustomView.save;

    }


    /**
     * need to implement save for later
     */

    //SAve for later
    private String progressFolder = "/Inprogress";

    private void savePaintingForLater() {

        pCustomView.save = !pCustomView.save;
        pCustomView.setDrawingCacheEnabled(true);
        pCustomView.invalidate();


        //LoadSaveImages saveImage = new LoadSaveImages();

        //saveImage.saveCMWImage(this, pCustomView.getCanvasBitmap());
        Log.d("in SAVE Painting", "hope it workd");
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + galleryFolder;
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


    /**
     *  Jeff Jones - https://www.youtube.com/watch?v=2Wg45ia6nXs
     *  checks for existing file
     *  takes in file implemented by app
     * @param file
     */
    private void fileAvailabilityMediaScanner(File file){
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener(){
                    public void onScanCompleted(String path, Uri uri){
                        Log.d(EXT_STORAGE, "Scanned "+ path+":");
                        Log.d(EXT_STORAGE, "-> uri= "+ uri);
                    }
                });
    }

    /**
     *
     */
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



    public void colorBlack(View view){

        pCustomView.setPaint(getResources().getColor(R.color.black));
        int colors [] = {Color.parseColor("#ffffff"),Color.parseColor("#000000")};
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors);
        gd.setCornerRadius(8);
        color.setBackground(gd);

    }
    public void colorBrown(View view){
        pCustomView.setPaint(getResources().getColor(R.color.brown));
        int colors [] = {Color.parseColor("#ffffff"),Color.parseColor("#8B4513")};
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors);
        gd.setCornerRadius(8);
        color.setBackground(gd);
    }
    public void colorBlue(View view){
        pCustomView.setPaint(getResources().getColor(R.color.blue));
        int colors [] = {Color.parseColor("#ffffff"),Color.parseColor("#0000FF")};
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors);
        gd.setCornerRadius(8);
        color.setBackground(gd);
    }
    public void colorPurple(View view){
        pCustomView.setPaint(getResources().getColor(R.color.purple));
        int colors [] = {Color.parseColor("#ffffff"),Color.parseColor("#FF00FF")};
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors);
        gd.setCornerRadius(8);
        color.setBackground(gd);
    }
    public void colorRed(View view){
        pCustomView.setPaint(getResources().getColor(R.color.red));
        int colors [] = {Color.parseColor("#ffffff"),Color.parseColor("#FF0000")};
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors);
        gd.setCornerRadius(8);
        color.setBackground(gd);
    }
    public void colorOrange(View view){
        pCustomView.setPaint(getResources().getColor(R.color.orange));
        int colors [] = {Color.parseColor("#ffffff"),Color.parseColor("#ffa500")};
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors);
        gd.setCornerRadius(8);
        color.setBackground(gd);
    }
    public void colorYellow(View view){
        pCustomView.setPaint(getResources().getColor(R.color.yellow));
        int colors [] = {Color.parseColor("#ffffff"),Color.parseColor("#FFFF00")};
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors);
        gd.setCornerRadius(8);
        color.setBackground(gd);
    }
    public void colorGreen(View view){
        pCustomView.setPaint(getResources().getColor(R.color.green));
        int colors [] = {Color.parseColor("#ffffff"),Color.parseColor("#00FF00")};
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors);
        gd.setCornerRadius(8);
        color.setBackground(gd);
    }


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


    /**
     * callback method which allows the OpenCV API to be available to use in functions
     * at a later time -
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


            // using with Laplacian
            //Imgproc.GaussianBlur(loadedImg, loadedImg, new Size(5,5),0,0,Core.BORDER_DEFAULT);
            Imgproc.GaussianBlur(loadedImg, loadedImg, new Size(3,3),0,0,Core.BORDER_DEFAULT);
            Imgproc.cvtColor(loadedImg, loadedImg, Imgproc.COLOR_RGB2GRAY);
//            Utils.matToBitmap(loadedImg, bitmap);
            Log.d("IMG_TYPE","Type = "+loadedImg.type());

            Mat bLap = new Mat();
            Imgproc.Laplacian(loadedImg, bLap, CvType.CV_16S,3,1,0,Core.BORDER_DEFAULT);
            loadedImg.release();

            Mat lapImg = new Mat();
            bLap.convertTo(lapImg,CvType.CV_8UC4);
            bLap.release();

//            //Core.convertScaleAbs(loadedImg, lapImg);
            Log.d("IMG_TYPE","Lap_Type_After = "+lapImg.type());
            Log.d("IMG_TYPE","Img_Values_2 = "+lapImg);
            // invert colors
            Imgproc.threshold(lapImg, lapImg, 17.0, 255, Imgproc.THRESH_BINARY_INV);
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
            pCustomView.pbarWidth = pbar.getWidth();
            pCustomView.pbarHeight = pbar.getHeight();
            pCustomView.setBitmap(bitmap, true);
        }
    }

}
