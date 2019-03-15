package com.digicomme.tremendocdoctor.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.net.URL;

public class ImageLoader extends AsyncTask<String, Void, Bitmap> {
    final ImageView imageView;

    public ImageLoader(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        String urlStr = strings[0];
        Bitmap bitmap = null;
        try {
            URL url = new URL(urlStr);
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            //imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.d("ImageLoader", e.getLocalizedMessage());
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
    }
}
