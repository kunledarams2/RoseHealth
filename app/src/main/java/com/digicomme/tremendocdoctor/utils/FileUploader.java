package com.digicomme.tremendocdoctor.utils;

import android.content.Context;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

public class FileUploader {

    private AmazonS3Client s3Client;
    private BasicAWSCredentials credentials;
    private TransferUtility transferUtility;
    private TransferObserver observer;
    private String bucketName;
    private FileUploadListener fileUploadListener;
    private int transferId;
    private boolean completed = false;

    private static final String DO_SPACES_ACCESS_KEY = "NEPLASGGHJ4GDRXXJ4KJ";
    private static final String DO_SPACES_SECRET_KEY = "QTSluRz4PyMfFkxtbJMPDexRUWpFozBCDrDogglsTcM";
    //private static final String DO_SPACES_ENDPOINT = "https://sfo2.digitaloceanspaces.com/tremendoc";
    private static final String DO_SPACES_ENDPOINT = "https://sfo2.digitaloceanspaces.com/";

    public FileUploader(Context context) {
        bucketName = "tremendoc";
        credentials = new BasicAWSCredentials(DO_SPACES_ACCESS_KEY, DO_SPACES_SECRET_KEY);
        s3Client = new AmazonS3Client(credentials);
        s3Client.setEndpoint(DO_SPACES_ENDPOINT);
        transferUtility = TransferUtility.builder()
                .context(context)
                .s3Client(s3Client)
                .build();

    }

    public void upload(String folder, File file, String fileName) {
        String key = folder + "/" + fileName;

        if (fileUploadListener != null)
            fileUploadListener.onStartUpload();

        observer = transferUtility.upload(bucketName, key, file);
        transferId = observer.getId();
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int i, TransferState state) {
                if (fileUploadListener == null) return;

                if (state.equals(TransferState.COMPLETED)) {
                    completed = true;
                    fileUploadListener.onComplete(observer.getAbsoluteFilePath());
                }
                if (state.equals(TransferState.CANCELED) || state.equals(TransferState.FAILED)){
                    fileUploadListener.canceled();
                }
            }

            @Override
            public void onProgressChanged(int i, long bytesUploaded, long bytesTotal) {
                float percentage =((float) bytesUploaded / (float) bytesTotal * 100);
                if (fileUploadListener != null)
                    fileUploadListener.onUploadProgress((int) percentage);
            }

            @Override
            public void onError(int i, Exception e) {
                if (fileUploadListener != null)
                    fileUploadListener.onError(e);
            }
        });
    }

    public void setFileUploadListener(FileUploadListener listener) {
        this.fileUploadListener = listener;
    }

    public void cancel() {
        transferUtility.cancel(transferId);
    }

    public boolean isCompleted() {
        return completed;
    }


    public interface FileUploadListener{

        void onStartUpload();

        void onComplete(String filePath);

        void onUploadProgress(int percentage);

        void onError(Exception e);

        void canceled();
    }
}
