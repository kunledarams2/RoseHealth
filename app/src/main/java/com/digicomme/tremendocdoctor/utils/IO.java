package com.digicomme.tremendocdoctor.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;


import com.digicomme.tremendocdoctor.api.API;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.core.content.FileProvider;

/**
 * Created by ikay on 9/17/2018.
 */

public class IO {
    public static final int REQUEST_CAMERA = 102;
    public static final int REQUEST_GALLERY = 103;

    public static boolean write(String entity, JSONArray messages) {
        try {
            writeToFile(entity, messages.toString());
            return true;
            //showMessage(messages.length() + " messages exported successfully", true);
        } catch (IOException e) {
            return false;
            //showMessage(e.getMessage(), false);
        }
    }

    private  static void writeToFile(String entity, String data) throws IOException {

        File folder = new File(Environment.getExternalStorageDirectory(), "SMSExport");
        if(!folder.exists()) folder.mkdirs();
        //File path = context.getExternalFilesDir(null);;
        File file = new File(folder, entity + "-export.json");
        FileOutputStream stream = new FileOutputStream(file);
        try {
            stream.write(data.getBytes());
        } finally {
            stream.close();
        }

    }

    public static JSONArray read(String entity) throws Exception{
        String string = readFromFile(entity);
        JSONArray array = new JSONArray(string);
        return array;
    }

    private static String readFromFile(String entity) throws IOException{
        File folder = new File(Environment.getExternalStorageDirectory(), "SMSExport");
        if(!folder.exists()) folder.mkdirs();
        //File path = context.getExternalFilesDir(null);;
        File file = new File(folder, entity + "-export.json");
        int length = (int) file.length();

        byte[] bytes = new byte[length];

        FileInputStream in = new FileInputStream(file);
        try {
            in.read(bytes);
        } finally {
            in.close();
        }

        String contents = new String(bytes);
        return contents;
    }

    public static void setData(Context context, String key, String value){
        SharedPreferences.Editor editor = context.getSharedPreferences(API.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getData(Context context, String key){
        SharedPreferences prefs = context.getSharedPreferences(API.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String string = prefs.getString(key,"");
        return string;
    }

    public static void deleteData(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(API.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().remove(key).apply();
    }

    private static Uri cameraPhotoUri;

    public static void cameraIntent(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File destination = new File(getImageFolder(), "Tremendoc_" + timestamp + ".jpg");
        Uri imageUri = FileProvider.getUriForFile(activity, "com.digicomme.tremendocdoctor.provider", destination);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
        //Uri uriToUploadMedia = Uri.fromFile(destination);
        cameraPhotoUri = imageUri;
        activity.startActivityForResult(intent, REQUEST_CAMERA);
    }

    public static Uri getCameraPhotoUri() {
        return cameraPhotoUri;
    }

    public static void galleryIntent(Activity activity) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY);
        Log.d("IO __--_-_--", "galleryIntent: ");
    }

    private static File getImageFolder() {
        File folder = new File(Environment.getExternalStorageDirectory(), "Tremendoc/Media/Images");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    public static void saveCameraCaptureToGallery(Context context, Uri uri) {
        Intent saveIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        saveIntent.setData(uri);
        context.sendBroadcast(saveIntent);
    }


}
