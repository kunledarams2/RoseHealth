package com.tremendoc.tremendocdoctor.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/*import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
*/
import java.io.File;

import androidx.appcompat.app.AppCompatActivity;

public class Uploader {
    private static final String DO_SPACES_ACCESS_KEY = "NEPLASGGHJ4GDRXXJ4KJ";
    private static final String DO_SPACES_SECRET_KEY = "QTSluRz4PyMfFkxtbJMPDexRUWpFozBCDrDogglsTcM";
    //private static final String DO_SPACES_ENDPOINT = "https://sfo2.digitaloceanspaces.com/tremendoc";
    private static final String DO_SPACES_ENDPOINT = "https://sfo2.digitaloceanspaces.com/";

    public void upload(Context activity, String fileName, File file) {
        /*AmazonS3Client s3;
        BasicAWSCredentials credentials;
        TransferUtility transferUtility;
        final TransferObserver observer;
        String key = DO_SPACES_ACCESS_KEY;
        String secret = DO_SPACES_SECRET_KEY;


        TransferNetworkLossHandler.getInstance(activity);
        credentials = new BasicAWSCredentials(key, secret);
        s3 = new AmazonS3Client(credentials);
        //s3.setEndpoint("https://BUCKET_NAME.nyc3.digitaloceanspaces.com/DIRECTORY_IF_NEEDED");
        s3.setEndpoint("https://sfo2.digitaloceanspaces.com/images");

        transferUtility = new TransferUtility(s3, activity);
        CannedAccessControlList filePermission = CannedAccessControlList.PublicRead;

        observer = transferUtility.upload(
                "tremendoc", //empty bucket name, included in endpoint
                fileName,
                file, //a File object that you want to upload
                filePermission
        );

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.COMPLETED.equals(observer.getState())) {
                    Log.d("UPLOADER", "$$$$$$$$$$$$$$$$$$$$$$$$$ upload complet");
                    Toast.makeText(activity, "Space upload completed !!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("UPLOADER", "$$$$$$$$$$$$$$$$$$$$$$$$$ upload error " + ex.getMessage());
                Toast.makeText(activity, "Space upload error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }); */
    }
}
