package com.digicomme.tremendocdoctor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;


import com.digicomme.tremendocdoctor.R;

import androidx.appcompat.app.AlertDialog;

public class ProgressDialog extends Dialog {

    public ProgressDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_progress);
    }
}
