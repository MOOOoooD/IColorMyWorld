package com.apps.mooooood.functforapp;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;

/**
 * Created by denise on 1/28/18.
 */

public class LoadSaveImages {
    private Context thisContext;

    // private strings to name folder and begin image name
    private String galeryFolder = "/Color_My_World";
    private String imageName = "CMW_";
    public static final String EXT_STORAGE = "EXT-STORAGE";

    public void saveCMWImage(Context context, Bitmap cmwImage){

        // getting context from activity calling save image
        thisContext = context;

        // creates string to represent file path for gallery album
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+galeryFolder;

        File gImgFolder = new File(filePath);
        if(!gImgFolder.exists()){
            gImgFolder.mkdirs();
        }
        // using date to save unique image files
        Date current = new Date();
        SimpleDateFormat fileSaveDate = new SimpleDateFormat("MMddyy_kkmmss");

        // naming image to folder
        File saveImgFile = new File(gImgFolder,imageName +fileSaveDate.format(current).toString()+".png" );
        try{
            // try to save file, flush file outputstream and close file output stream
            FileOutputStream fOut = new FileOutputStream(saveImgFile);
            cmwImage.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
            fileAvailabilityMediaScanner(saveImgFile);
            Log.d("GOOD-SAVE", "Image saved");

        }catch(FileNotFoundException e){
            // exception thrown if no file created
            Log.d("BAD-FILE-SAVE", "File not created");
        }catch (Exception e){
            // other exception thrown if cannot save in general
            Log.d("BAD-SAVE", " cannot save file");
        }

    }

    // Jeff Jones - https://www.youtube.com/watch?v=2Wg45ia6nXs
    private void fileAvailabilityMediaScanner(File file){
        MediaScannerConnection.scanFile(thisContext, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener(){
                    public void onScanCompleted(String path, Uri uri){
                        Log.d(EXT_STORAGE, "Scanned "+ path+":");
                        Log.d(EXT_STORAGE, "-> uri= "+ uri);
                    }
                });
    }
}
