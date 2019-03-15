package com.digicomme.tremendocdoctor.dialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.activity.MainActivity;
import com.digicomme.tremendocdoctor.api.StringCall;
import com.digicomme.tremendocdoctor.api.URLS;
import com.digicomme.tremendocdoctor.utils.FileUploader;
import com.digicomme.tremendocdoctor.utils.Formatter;
import com.digicomme.tremendocdoctor.utils.IO;
import com.digicomme.tremendocdoctor.utils.Permission;
import com.digicomme.tremendocdoctor.utils.ToastUtil;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;

public class NewTipDialog extends Dialog {

    private View view;
    private boolean isBusy = false;
    private ProgressBar progressBar;
    private EditText titleField, summaryField, bodyField;
    private Snackbar snackbar;
    private Activity activity;
    private Uri imageUri;
    private Bitmap bitmap;
    private FileUploader fileUploader;
    private View uploadOverlay;
    private ImageView imagePreview, uploadDone;
    private TextView uploadCancelBtn;
    private ProgressBar uploadProgress;

    private String uploadedFileName;

    public NewTipDialog(Activity activity) {
        super(activity, R.style.FullScreenDialog);
        this.activity = activity;
        view = LayoutInflater.from(activity).inflate(R.layout.dialog_new_tip, null, false);
        setContentView(view);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        setupViews();
    }

    private void saveTip(String title, String summary, String body, String image) {
        Context ctx = getContext();
        progressBar.setVisibility(View.VISIBLE);
        isBusy = true;

        Map<String, String> params = new HashMap<>();
        params.put("title", title);
        params.put("summary", summary);
        params.put("body", body);
        params.put("imagePath", image);

        StringCall call = new StringCall(ctx);
        call.post(URLS.SAVE_TIP, params, response -> {
            progressBar.setVisibility(View.INVISIBLE);
            isBusy = false;

            try {
                JSONObject resObj = new JSONObject(response);
                if (resObj.has("code") &&  resObj.getInt("code") == 0) {
                    ToastUtil.showLong(ctx, "Note saved successfully");
                    NewTipDialog.this.cancel();
                } else if (resObj.has("description")) {
                    ToastUtil.showModal(ctx, resObj.getString("description"));
                }
            } catch (JSONException e) {
                ToastUtil.showModal(ctx, e.getMessage());
            }

        }, error -> {
            progressBar.setVisibility(View.INVISIBLE);
            isBusy = false;
            log("VOLLEY ERROR");
            log(error.getMessage());
            if (error.networkResponse == null) {
                log("Network response is null");
                ToastUtil.showModal(ctx, "Please check your internet connection");
            } else {
                String errMsg = Formatter.bytesToString(error.networkResponse.data);
                ToastUtil.showModal(ctx, errMsg);
                log("DATA: " + errMsg);
            }
        });
    }

    public void trySaveTip() {
        if (isBusy) return;

        Context ctx = getContext();

        String title = titleField.getText().toString();
        String summary = summaryField.getText().toString();
        String body = bodyField.getText().toString();
        String image = uploadedFileName; //imageField.getText().toString();

        if (TextUtils.isEmpty(title)) {
            ToastUtil.showLong(ctx, "Title field is required");
            return;
        }

        if (TextUtils.isEmpty(summary)) {
            ToastUtil.showLong(ctx, "Summary field is required");
            return;
        }

        if (TextUtils.isEmpty(body)) {
            ToastUtil.showLong(ctx, "Body field is required");
            return;
        }

        if (TextUtils.isEmpty(image)) {
            ToastUtil.showLong(ctx, "Image field is required");
            return;
        }

        if (Permission.permissionIsGranted(ctx, Manifest.permission.INTERNET)) {
            saveTip(title, summary, body, image);
        } else {
        }
    }

    private void setupViews() {

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        titleField = findViewById(R.id.title_field);
        summaryField = findViewById(R.id.summary_field);
        bodyField = findViewById(R.id.body_field);
        imagePreview = findViewById(R.id.image_preview);
        uploadOverlay = findViewById(R.id.upload_overlay);
        uploadCancelBtn = findViewById(R.id.upload_cancel_btn);
        uploadProgress = findViewById(R.id.upload_progress);
        uploadDone = findViewById(R.id.upload_complete);
        uploadOverlay.setVisibility(View.GONE); //setBackgroundResource(android.R.color.transparent);
        //uploadProgress.setVisibility(View.GONE);
        uploadDone.setVisibility(View.GONE);
        imagePreview.setOnClickListener(iv -> IO.galleryIntent(activity));

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        toolbar.setNavigationOnClickListener(v -> NewTipDialog.this.cancel());
        toolbar.getMenu().add("Attach Photo")
                .setIcon(R.drawable.ic_add_a_photo)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .setTitle("Attach Photo")
                .setOnMenuItemClickListener(menuItem -> {
                    //IO.galleryIntent(activity);
                    ((MainActivity) activity).galleryIntent();
                    /*if (snackbar.isShown()) {
                        snackbar.dismiss();
                    } else {
                        snackbar.show();
                    }*/
                    return false;
                });
        toolbar.getMenu().add("Save Health Tip")
                .setIcon(R.drawable.ic_check_white)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .setTitle("Save Health Tip")
                .setOnMenuItemClickListener(menuItem -> {
                    trySaveTip();
                    return false;
                });

        createSnackbar();
    }

    private void createSnackbar() {
        snackbar = Snackbar.make(view, "Upload a photo", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        View snackView = LayoutInflater.from(getContext()).inflate(R.layout.snackbar_image_upload, null, false);
        ImageButton cameraBtn = snackView.findViewById(R.id.camera);
        ImageButton galleryBtn = snackView.findViewById(R.id.gallery);
        ImageButton removeBtn = snackView.findViewById(R.id.remove);
        cameraBtn.setOnClickListener(btn -> IO.cameraIntent(activity));
        galleryBtn.setOnClickListener(btn -> IO.galleryIntent(activity));
        removeBtn.setOnClickListener(btn -> imagePreview.setImageURI(null));
        layout.addView(snackView);
    }

    public void onCameraResult(Intent data) {
        if (IO.getCameraPhotoUri() != null) {
            imageUri = IO.getCameraPhotoUri();
            IO.saveCameraCaptureToGallery(getContext(), imageUri);
            getContext().getContentResolver().notifyChange(imageUri, null);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                imagePreview.setImageURI(imageUri);
            } catch (IOException e) {
                ToastUtil.showLong(getContext(), e.getLocalizedMessage());
            }
        }
    }

    public void onGalleryResult(Intent data) {
        log("_--___-- onGalleryResult");
        if (data != null) {
            log("Data is not Null");
            imageUri = data.getData();
            if (fileUploader == null) {
                initFileUploader();
            }
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                File file = new File(imageUri.getPath().split(":")[1]);
                //File file = new File(imageUri.getPath());
                //String[] frags = file.getAbsolutePath().split(":");
                //file = new File( frags[1]);
                fileUploader.upload("images", file, "jdjjahfjhfah.jpg");
                imagePreview.setImageURI(imageUri);
                uploadOverlay.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                ToastUtil.showLong(getContext(), e.getLocalizedMessage());
            }
        }
        log("Data maybe null");
    }

    private void initFileUploader() {
        fileUploader = new FileUploader(getContext());
        fileUploader.setFileUploadListener(new FileUploader.FileUploadListener() {
            @Override
            public void onStartUpload() {
                uploadOverlay.setBackgroundResource(R.color.colorPrimary);
                uploadOverlay.setAlpha(0.5f);
                uploadProgress.setVisibility(View.VISIBLE);
                uploadCancelBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onComplete(String filePath) {
                log("Uploaded file path "+ filePath);
                uploadedFileName = filePath;
                uploadOverlay.setBackgroundResource(android.R.color.transparent);
                uploadOverlay.setAlpha(1.0f);
                uploadProgress.setVisibility(View.GONE);
                uploadCancelBtn.setVisibility(View.GONE);
                uploadDone.setVisibility(View.VISIBLE);
            }

            @Override
            public void onUploadProgress(int percentage) {
                log("Percentage " + percentage);
                uploadProgress.setProgress(percentage);
            }

            @Override
            public void onError(Exception e) {
                log("File upload error "+ e.getMessage());
            }

            @Override
            public void canceled() {
                log("Canceled");
                uploadOverlay.setBackgroundResource(android.R.color.transparent);
                uploadOverlay.setAlpha(1.0f);
                uploadProgress.setVisibility(View.GONE);
                uploadCancelBtn.setVisibility(View.GONE);
                imagePreview.setImageResource(R.drawable.ic_photo_placeholder);
            }
        });
    }

    private void log(String log) {
        Log.d("NewTipDialog", "---_--_---_---__---_----------__--__" + log);
    }


}
