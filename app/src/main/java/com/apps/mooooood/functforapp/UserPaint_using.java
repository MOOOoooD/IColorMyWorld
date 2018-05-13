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
 * UserPaint_using.java - inflates main drawing user interface activity,
 *  activity_user_paint.xml which holds custom views activity_paint_custom_view.xml
 *  and activity_menu_cust_view.xml
 *  Class instantiates buttons and performs image conversion so user can interact with
 *  image, color, save, and share converted image *
 *
 * @author Denise Fullerton
 * @since created 1/28/18
 * @since last updated - 5/12/18
 */


public class UserPaint_using extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    // constants for log tags
    public static final String P_TAG = "Paint Debug";
    public static final String USER_PAINT = "UserPaint_using.java";
    public static final String EXT_STORAGE = "EXT-STORAGE";
    private static final String  TAG = "Checking OpenCV";
    private static final String CHECK_TAG = "Check ";

    // creates instance of menuCustView for custom menu buttons
    MenuCustView menuCustView;

    //private FloatingActionButton floatActBtn;
    private PntCustView_using pCustomView;
    Button brownBtn, blueBtn, purpleBtn, redBtn, orangeBtn, yellowBtn, greenBtn, blackBtn;
    Button loadImgBtn;

    // name of album to add to image gallery
    private final String ALBUM_NAME = "/I_Color_My_World";

    // from activity_menu_cust_view
    Button color;

    // standard image width - to use for smaller devices
    int imgWidth = 640;
    int imgHeight = 480;

    private LinearLayout pbar;

    // toggle for draw_tool
    private boolean eraseDraw = true;

    // img resize variables
    int width = 0;
    int height = 0;

    // Activity result intent result code
    private static int RESULT_LOAD_IMG = 1;

    /**
     * Inflates activity_user_paint.xml
     *      This includes the custom views for activity_paint_custom_view.xml and
     *      activity_menu_cust_view.xml
     *      instantiates buttons and onTouchListener for color button
     * @param savedInstanceState
     *
     * supressLint due to on touch listener being attached to button
     */
    @SuppressLint({"WrongViewCast", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_PopupOverlay);
        super.onCreate(savedInstanceState);

        Log.d(P_TAG, "in onCreate before cust view");
        setContentView(R.layout.activity_user_paint);

        // buttons for color pallete
        blackBtn = findViewById(R.id.colorBlkBtn);
        brownBtn = findViewById(R.id.colorBrBtn);
        blueBtn = findViewById(R.id.colorBlueBtn);
        purpleBtn = findViewById(R.id.colorPBtn);
        redBtn = findViewById(R.id.colorRedBtn);
        orangeBtn = findViewById(R.id.colorOBtn);
        yellowBtn = findViewById(R.id.colorYBtn);
        greenBtn = findViewById(R.id.colorGBtn);

        // palette bar to hold color buttons
        pbar = findViewById(R.id.palette_bar);

        // custom view - drawing interaction area
        pCustomView = findViewById(R.id.paint_custom_view);

        // custom view  - menu holding undo, redo, color, and draw_tool buttons
        menuCustView = findViewById(R.id.menu_cust_view);

        // press and hold button to apply drawpaths and paint to image
        color= findViewById(R.id.color_button);

        // button in center of custom view to initiate load image from gallery
        loadImgBtn = findViewById(R.id.load_image);

        // sets loadImgBtn visibility
        buttonVisibility();

        /**
         * onTouchListener for touch and hold button
         * When user holds button and drags pencil, creates drawPaths
         * for 'coloring' on canvas
         * function requires performClick() method, but does not use
         */
        color.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pCustomView.colorToggle = true;
                        //Log.d("COLOR_","In ColorBtn Down: "+pCustomView.colorToggle);
                        break;
                    case MotionEvent.ACTION_UP:
                        pCustomView.colorToggle = false;
                        //Log.d("COLOR_","In ColorBtn Up: "+pCustomView.colorToggle);
                        view.performClick();
                        break;
                }
                return false;
            }
        });
    }


    /**
     * onClick method that takes in loadImgBtn Button object and uses intent to
     *      access image gallery on device - once user selectes image,
     *      URI for image is returned and processed in onActivityResult
     *      Changes text on loadImgBtn to indicate image is processing
     * @param v
     */
    public void loadImg(View v){
        Intent image = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(image, RESULT_LOAD_IMG);
        try {
            Thread.sleep(500);
            loadImgBtn.setText(R.string.loading);
        }catch (Exception e){
            Log.d(USER_PAINT, "Error "+e);
        }
    }

    /**
     * onClick method that takes in menuBtn ImageButton and
     *      inflates a popup menu that displays options
     *      for the user save, delete, or share images
     *      Uses draw_menu.xml for menu options
     * @param v
     */
    public void listMenu(View v){
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.draw_menu);
        popupMenu.show();
    }

    /**
     * onClick method that takes in undoBtn ImageButton and calls function in
     *      paintCustomView object to take move drawpath objects from
     *      active ArrayList and adds the drawpath objects to
     *      the storing drawpath ArrayList so the drawpath and
     *      associated paint object will be removed from onscreen
     * @param v
     */
    public void undoClicked(View v){
        Toast.makeText(this, "Undo Line", Toast.LENGTH_SHORT).show();
        pCustomView.onClickUndo();
    }

    /**
     * onClick method that takes in redoBtn ImageButton and calls function in
     *      paintCustomView object to take move drawpath objects from
     *      storing ArrayList and adds the drawpath objects back to to
     *      the active drawpath ArrayList so the drawpath and
     *      associated paint object will be redisplayed onscreen
     * @param v
     */
    public void redoClicked(View v){
        Toast.makeText(this, "Redraw Line", Toast.LENGTH_SHORT).show();
        pCustomView.onClickRedo();
    }

    /**
     * onClick method that takes in the ImageButton draw_tool_btn view that switches
     *      the draw_tool displayed on screen to the pencil_icon or eraser_icon
     *      respectively, by resetting drawable image tied to ImageButton
     *      and color associated with the respective tool
     *      calls methods in PntCustView to initiate onDraw() call to update
     *      screen to display the switch
     * @param v
     */
    public void eraseDrawClicked(View v){
        eraseDraw = !eraseDraw;
        pCustomView.onClickEraseDraw(eraseDraw);
        if(!eraseDraw) {
            Toast.makeText(this, "Erasing", Toast.LENGTH_SHORT).show();
            pCustomView.setLastPaintColor();
            pCustomView.setPaint(getResources().getColor(R.color.erase));
            v.setBackgroundResource(R.drawable.pencil_icon);
        }else{
            Toast.makeText(this, "Coloring", Toast.LENGTH_SHORT).show();
            v.setBackgroundResource(R.drawable.eraser_icon);
            pCustomView.setPaint(pCustomView.getLastPaintColor());
        }
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
                //Toast.makeText(this, "Delete clicked", Toast.LENGTH_SHORT).show();
                deleteDialog();
                return true;
            case R.id.action_share:
                Toast.makeText(this, "Share selected", Toast.LENGTH_SHORT).show();
                sharePainting();
                return true;
            case R.id.action_save:
                Toast.makeText(this, "Your picture has been saved", Toast.LENGTH_SHORT).show();
                savePainting();
                return true;
            default:
                return false;
        }
    }

    /**
     * Method to close current activity and displays confirmation message
     *      to user to verify they want to delete working image
     *      Uses onClick method for delete Button in pop-up menu
     *      if yes, restarts activity, clearing current memory and allowing
     *      user to start over on activity
     *      if no, cancel, return to current screen
     */
    private void deleteDialog(){
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
        deleteDialog.setTitle(getString(R.string.delete_drawing));
        deleteDialog.setMessage(getString(R.string.new_draw_warning));
        deleteDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
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
     * Method to create a save name for images that are to be saved
     *      uses dat eand time in image name to ensure unique name for all saved images
     * @return String image name
     */
    private String imageName(){
        Date current = new Date();
        SimpleDateFormat fileSaveDate = new SimpleDateFormat("MMddyy_kkmmss");
        String imgName = "cmw"+fileSaveDate.format(current).toString()+".png";
        return imgName;
    }

    /**
     * Method to check for album directory in image gallery to store completed images
     *      If album does not exist, will make album directory
     *      returns the path to the directory to save image in
     * @return String directory name
     */
    private File directoryName(){
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ ALBUM_NAME;
        File colorDir = new File(filePath);
        if(!colorDir.exists()){
            colorDir.mkdirs();
        }
        return colorDir;
    }

    /**
     * Method to save painting using save Button in pop-up menu
     *      takes image data from pCustomView object, stores it
     *      in cache - uses FileOutputStream to save image in app album in
     *      gallery
     */
    private void savePainting(){

        pCustomView.save = !pCustomView.save;
        pCustomView.setDrawingCacheEnabled(true);
        pCustomView.invalidate();
        //Log.d("in SAVE Painting", "hope it workd");

        File cmvSaveImg = new File(directoryName(), imageName());
        try{
            FileOutputStream saveOut = new FileOutputStream(cmvSaveImg);

            int startW, startH, endW, endH;
            //int startW=481, startH=302, endW=1365, endH=767;
            //int startW=162, startH=6, endW=640, endH=480;

            /**
             * if img width and height is less than actual size
             * use pcustview.center img w and h for start and pcustview imgwidth/height for end
             * if pcustview.center img h or w is < 0, then use 0 for the right one to endpoint
             * pcustview.getwidth()/height()
             *
             */
            if(pCustomView.centerImg_W < 0){
               // Log.d("CIMGw<0", " pcustView Cimg W: "+pCustomView.centerImg_W);
                startW = 0;
                endW = pCustomView.getWidth();
            }else{
           //     Log.d("CIMGELS", " pcustView Cimg W else: "+pCustomView.centerImg_W);
                startW = pCustomView.centerImg_W;
                endW = pCustomView.imgWidth;
            }
            if(pCustomView.centerImg_H<0){
               // Log.d("CIMGh<0", " pcustView Cimg H: "+pCustomView.centerImg_H);
                startH = 0;
                endH = pCustomView.getHeight();
            }else{
              //  Log.d("CIMGELS", " pcustView Cimg H else: "+pCustomView.centerImg_H);
                startH = pCustomView.centerImg_H;
                endH = pCustomView.imgHeight;
            }
          //  Log.d("SAVE-T", "savePainting: check coordinates: Start  "+startW+", "+startH+"  End: "+endH+", "+endW);
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
            Log.d(USER_PAINT,"Could not save file: \n"+e.getStackTrace());
        }
        pCustomView.save = !pCustomView.save;
    }


    /*
     need to implement functionality to save current working image to reload
     and continue work at a later time

    //Save for later
    private String progressFolder = "/Inprogress";

    private void savePaintingForLater() {

        pCustomView.save = !pCustomView.save;
        pCustomView.setDrawingCacheEnabled(true);
        pCustomView.invalidate();
        File cmvSaveImg = new File(directoryName(), imageName());
        try {
            FileOutputStream saveOut = new FileOutputStream(cmvSaveImg);
            pCustomView.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 100, saveOut);
            saveOut.flush();
            saveOut.close();
            fileAvailabilityMediaScanner(cmvSaveImg);
        } catch (Exception e) {
            e.printStackTrace();
            //Log.d("In Exception", "   BLAHHHHH");
        }
        pCustomView.save = !pCustomView.save;
    }
    */



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
                        //Log.d(EXT_STORAGE, "Scanned "+ path+":");
                        //Log.d(EXT_STORAGE, "-> uri= "+ uri);
                    }
                });
    }

    /**
     * Method to share painting using share Button in pop-up menu
     *      takes image data from pCustomView object, stores it
     *      in cache - uses FileOutputStream to save image in app album in
     *      gallery
     */
    private void sharePainting(){

        pCustomView.save = !pCustomView.save;
        pCustomView.setDrawingCacheEnabled(true);
        pCustomView.invalidate();
        try {
            String path = Environment.getExternalStorageDirectory().toString();
            OutputStream fileOut = null;
            File file = new File(path, imageName());
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (Exception e) {
                Log.e(P_TAG, "New File: " + e.getCause() + e.getMessage());
            }
            try {
                fileOut = new FileOutputStream(file);
            } catch (Exception e) {
                Log.e(P_TAG, "FileOut " + e.getCause() + e.getMessage());
            }
            if (pCustomView.getDrawingCache() == null) {
                Log.e(P_TAG, "Unable to get drawing cache ");
            }

            int startW, startH, endW, endH;
            //int startW=481, startH=302, endW=1365, endH=767;
            //int startW=162, startH=6, endW=640, endH=480;
            if (pCustomView.centerImg_W < 0) {
                // Log.d("CIMGw<0", " pcustView Cimg W: "+pCustomView.centerImg_W);
                startW = 0;
                endW = pCustomView.getWidth();
            } else {
                //     Log.d("CIMGELS", " pcustView Cimg W else: "+pCustomView.centerImg_W);
                startW = pCustomView.centerImg_W;
                endW = pCustomView.imgWidth;
            }
            if (pCustomView.centerImg_H < 0) {
                // Log.d("CIMGh<0", " pcustView Cimg H: "+pCustomView.centerImg_H);
                startH = 0;
                endH = pCustomView.getHeight();
            } else {
                //  Log.d("CIMGELS", " pcustView Cimg H else: "+pCustomView.centerImg_H);
                startH = pCustomView.centerImg_H;
                endH = pCustomView.imgHeight;
            }
            //  Log.d("SAVE-T", "savePainting: check coordinates: Start  "+startW+", "+startH+"  End: "+endH+", "+endW);
            // save only the range of the colored bitmap
            Bitmap savImg = Bitmap.createBitmap(pCustomView.getDrawingCache(), startW, startH, endW, endH);
            for (int x = 0; x < savImg.getWidth(); x++) {
                for (int y = 0; y < savImg.getHeight(); y++) {
                    int bmColor = savImg.getPixel(x, y);
                    int r = Color.alpha(bmColor);
                    if (r == 0) {
                        savImg.setPixel(x, y, Color.rgb(255, 255, 255));
                    }
                }
            }
            savImg.compress(Bitmap.CompressFormat.PNG, 100, fileOut);
            savImg.recycle();
            fileOut.flush();
            fileOut.close();

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            shareIntent.setType("image/png");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        }catch (Exception e){
            Log.d(USER_PAINT,"Could not share image: \n"+e.getStackTrace());
        }
        pCustomView.save = !pCustomView.save;
    }

    /**
     * onClick buttons for palette to change  pen/paint color
     * when color selected, the push to color button changes to gradient of selected color
     * and white, so user knows the color has been selected
     * @param view
     */
    public void colorBlack(View view){
        if(eraseDraw) {
            pCustomView.setPaint(getResources().getColor(R.color.black));
            int colors[] = {Color.parseColor("#ffffff"), Color.parseColor("#000000")};
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
            gd.setCornerRadius(8);
            color.setBackground(gd);
        }
    }
    public void colorBrown(View view){
        if(eraseDraw) {
            pCustomView.setPaint(getResources().getColor(R.color.brown));
            int colors[] = {Color.parseColor("#ffffff"), Color.parseColor("#8B4513")};
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
            gd.setCornerRadius(8);
            color.setBackground(gd);
        }
    }
    public void colorBlue(View view){
        if(eraseDraw) {
            pCustomView.setPaint(getResources().getColor(R.color.blue));
            int colors[] = {Color.parseColor("#ffffff"), Color.parseColor("#0000FF")};
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
            gd.setCornerRadius(8);
            color.setBackground(gd);
        }
    }
    public void colorPurple(View view){
        if(eraseDraw) {
            pCustomView.setPaint(getResources().getColor(R.color.purple));
            int colors[] = {Color.parseColor("#ffffff"), Color.parseColor("#FF00FF")};
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
            gd.setCornerRadius(8);
            color.setBackground(gd);
        }
    }
    public void colorRed(View view){
        if(eraseDraw) {
            pCustomView.setPaint(getResources().getColor(R.color.red));
            int colors[] = {Color.parseColor("#ffffff"), Color.parseColor("#FF0000")};
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
            gd.setCornerRadius(8);
            color.setBackground(gd);
        }
    }
    public void colorOrange(View view){
        if(eraseDraw) {
            pCustomView.setPaint(getResources().getColor(R.color.orange));
            int colors[] = {Color.parseColor("#ffffff"), Color.parseColor("#ffa500")};
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
            gd.setCornerRadius(8);
            color.setBackground(gd);
        }
    }
    public void colorYellow(View view){
        if(eraseDraw) {
            pCustomView.setPaint(getResources().getColor(R.color.yellow));
            int colors[] = {Color.parseColor("#ffffff"), Color.parseColor("#FFFF00")};
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
            gd.setCornerRadius(8);
            color.setBackground(gd);
        }
    }
    public void colorGreen(View view){
        if(eraseDraw) {
            pCustomView.setPaint(getResources().getColor(R.color.green));
            int colors[] = {Color.parseColor("#ffffff"), Color.parseColor("#00FF00")};
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
            gd.setCornerRadius(8);
            color.setBackground(gd);
        }
    }

    /**
     * callback method which allows the OpenCV API to be available to use in functions
     * at a later time -
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
           // Log.d(CHECK_TAG, "in BLC onManConn");
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    //Log.d(CHECK_TAG, "Switch Default: "+status);

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

        //Log.d(CHECK_TAG, "in onResume");

        if (!OpenCVLoader.initDebug()) {
            //Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            //Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        //Log.d(CHECK_TAG, "onResume, after if ");

    }


    /**
     * Cloning objects - this is used to ensure date pointer passed is not incremented
     * can be used for deep copying other objects
     * @return
     * @throws CloneNotSupportedException
     */
    protected Object clone()throws CloneNotSupportedException{
        return super.clone();
    }

    /**
     * re-inflates activity_paint_custom_view.xml after image is selected in from the device
     *      image gallery by the user, and displays resulting image based after
     *      image filters are applied
     *      method takes in request code to identify which intent the callback is using,
     *      resultCode from URI to ensure image will be returned, and intent including description
     *      of action to be performed and passed in activity
     *      scales image to keep memory size down,
     *      runs through all openCV image conversion filters to prepare image for
     *      drawing
     *      converts Bitmap to Mat for openCV functions, and back to Bitmap to
     *      display on canvas
     *
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
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            Mat loadedImg = new Mat();
            Utils.bitmapToMat(bitmap, loadedImg );
            // scaling image to fit 480x640 image space
            imgWidth = pCustomView.canvasWidth;
            imgHeight = pCustomView.canvasHeight;
            //Log.d(USER_PAINT, "P Cust in UP width: "+imgWidth+"  height: "+imgHeight);
            //Log.d(USER_PAINT, "p-cust width: "+loadedImg.width()+"  height: "+loadedImg.height());
            int setMaxW = 1365;
            int setMaxH = 1024;
            double thresh = 9;
            double kernalSz = 3;
            //Log.d("Thresh: ","CUrrent Thresh - "+thresh);

            if(loadedImg.width()>imgWidth||loadedImg.height()>imgHeight){
                //Log.d(USER_PAINT, "P Cust in iff width: "+imgWidth+"  height: "+imgHeight);

                double scale;
                //double scaleH;
                if((double)imgWidth > setMaxW && (double)loadedImg.width() > setMaxW){
                    scale = ((double)setMaxW/(double)loadedImg.width());
                }
                else if((double)imgHeight > setMaxW && (double)loadedImg.height() > setMaxH){
                    scale = ((double)setMaxH/(double)loadedImg.height());
                }
                else if(((double)imgWidth/(double)loadedImg.width()) <1.0) {
                    scale = ((double) imgWidth / (double) loadedImg.width());
                    //scaleH = ((double) imgWidth / (double) loadedImg.height());
                }
                else{
                    //scaleW = ((double) imgHeight / (double) loadedImg.width());
                    scale = ((double) imgHeight / (double) loadedImg.height());
                }
                //Log.d(USER_PAINT, "loaded width: "+loadedImg.width()+"  height: "+loadedImg.height());

                //resize variables
                width = (int)(Math.min(scale,scale) * loadedImg.width());
                height = (int)(Math.min(scale,scale)* loadedImg.height());
                //Log.d(USER_PAINT, "width: "+width+"  height: "+height);
                //Log.d(USER_PAINT, "width: "+scale+"  height: "+scale);

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
                //Log.d(USER_PAINT, "in else : "+width+"  height: "+height);

            }
            pCustomView.imgHeight = height;
            pCustomView.imgWidth = width;

            Log.d(USER_PAINT, "PCust width: "+pCustomView.imgWidth+"  PCust height: "+pCustomView.imgHeight);
            Log.d(USER_PAINT,"ih="+loadedImg.height()+", iw="+loadedImg.width());
            Log.d(USER_PAINT,"scaled h="+height+", scaled w="+width);

            // using with Laplacian
            Imgproc.GaussianBlur(loadedImg, loadedImg, new Size(5,5),0,0,Core.BORDER_DEFAULT);

            /**
             * setting bit map through stages for presentation - Gauss
             */

//            try {
//                bitmap = Bitmap.createBitmap(loadedImg.width(), loadedImg.height(),Bitmap.Config.ARGB_8888 );
//                Utils.matToBitmap(loadedImg, bitmap);
//                pCustomView.setBitmap(bitmap, true);
//                pCustomView.invalidate();
//                savePainting();
//                //Log.d("GAUS_E", "Checking flags "+pCustomView.imgToggle);
//                //Log.d("GAUS_E", "Checking flags "+pCustomView.setCan);
//                //Log.d("GAUS_E", "Checking flags "+pCustomView.t_up);
//                //Thread.sleep(2500);
//
//            }catch (Exception e){
//                Log.d("ERR-GA", " Gauss Error "+e);
//            }

            Imgproc.cvtColor(loadedImg, loadedImg, Imgproc.COLOR_RGB2GRAY);
//            Utils.matToBitmap(loadedImg, bitmap);
            //Log.d("IMG_TYPE","Type = "+loadedImg.type());


            /**
             * setting bit map through stages for presentation - Grayscale
             */

//            try {
//                bitmap = Bitmap.createBitmap(loadedImg.width(), loadedImg.height(),Bitmap.Config.ARGB_8888 );
//                Utils.matToBitmap(loadedImg, bitmap);
//                pCustomView.setBitmap(bitmap, true);
//                pCustomView.invalidate();
//                savePainting();
//                //Log.d("GRAY_E", "Checking flags "+pCustomView.imgToggle);
//                //Log.d("GRAY_E", "Checking flags "+pCustomView.setCan);
//                //Log.d("GRAY_E", "Checking flags "+pCustomView.t_up);
//                //Thread.sleep(2500);
//            }catch (Exception e){
//                Log.d("ERR-GR", " GRAY Error "+e);
//            }

            Mat bLap = new Mat();
            Imgproc.Laplacian(loadedImg, bLap, CvType.CV_16S,(int)kernalSz,1,0,Core.BORDER_DEFAULT);
            loadedImg.release();
            Mat lapImg = new Mat();
            bLap.convertTo(lapImg,CvType.CV_8UC4);

            /**
             * setting bit map through stages - Lap
             */
//            try {
//
//                bitmap = Bitmap.createBitmap(lapImg.width(), lapImg.height(),Bitmap.Config.ARGB_8888 );
//                Utils.matToBitmap(lapImg, bitmap);
//                pCustomView.setBitmap(bitmap, true);
//                pCustomView.invalidate();
//                savePainting();
//                //Thread.sleep(2500);
//            }catch (Exception e){
//                Log.d("ERR-LA", " LAP Error "+e);
//            }

            bLap.release();
            Imgproc.threshold(lapImg, lapImg, thresh, 255, Imgproc.THRESH_BINARY_INV);
            //int imgCH = lapImg.channels();
            //Log.d(TAG, "Pixel val "+ lapImg.type()+ " chan "+ lapImg.channels());

            /**
             * setting bit map through stages for presentation - Threshold binary inverse
             */
//            try {
//               // Log.d("ERR-BI", " BINARY Error ");
//
//                bitmap = Bitmap.createBitmap(lapImg.width(), lapImg.height(),Bitmap.Config.ARGB_8888 );
//                Utils.matToBitmap(lapImg, bitmap);
//                pCustomView.setBitmap(bitmap, true);
//                pCustomView.invalidate();
//                savePainting();
//                //Thread.sleep(2500);
//            }catch (Exception e){
//                Log.d("ERR-BI", " BINARY Error "+e);
//            }

            //    Core.bitwise_not(lapImg, lapImg);
            bitmap = Bitmap.createBitmap(lapImg.width(), lapImg.height(),Bitmap.Config.ARGB_8888 );
            Utils.matToBitmap(lapImg, bitmap);
            lapImg.release();

            // sets alpha of all white values in image to 0 to display as transparent png
            // and only displays black lines in resulting immage
            for(int x = 0; x < bitmap.getWidth(); x++){
                for(int y = 0; y < bitmap.getHeight(); y++){
                    int bmColor = bitmap.getPixel(x,y);
                    int r = Color.red(bmColor);
                    if(r==255){
                        bitmap.setPixel(x,y,Color.alpha(0));
                    }
                }
            }
            buttonVisibility();
            pCustomView.pbarWidth = pbar.getWidth();
            pCustomView.pbarHeight = pbar.getHeight();
            pCustomView.setBitmap(bitmap, true);
            pCustomView.invalidate();
        }
    }

    /**
     * Method switches visibility of loadImgBtn object within pCustomView object
     *      to be visible when image has not been selected, then not visible
     *      when image has loaded
     */
    private void buttonVisibility(){
        loadImgBtn.setVisibility(loadImgBtn.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

}
