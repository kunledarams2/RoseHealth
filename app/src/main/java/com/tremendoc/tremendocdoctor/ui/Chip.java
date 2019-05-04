package com.tremendoc.tremendocdoctor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tremendoc.tremendocdoctor.R;

public class Chip extends RelativeLayout {
    private TextView textView;

    private OnChipCloseListener onChipCloseListener;

    public Chip(Context context){
        super(context);
        init(null);
    }

    public Chip(Context context, AttributeSet attr) {
        super(context, attr);
        init(attr);
    }

    public Chip(Context context, AttributeSet attr, int style){
        super(context, attr, style);
        init(attr);
    }

    private void init(AttributeSet attrs) {
        View view = inflate(getContext(), R.layout.chip, null);
        textView = view.findViewById(R.id.chip_text);
        if (attrs != null) {
            String packageName = "http://schemas.android.com/apk/res-auto";
            String label = attrs.getAttributeValue(packageName, "label");
            textView.setText(label);
        }
        ImageButton closeBtn = view.findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(btn -> {
            removeView(view);
            if (onChipCloseListener != null) {
                onChipCloseListener.onClose(Chip.this);
            }
        });
        addView(view);
    }

    public void setLabel(String label){
        if (textView != null) {
            textView.setText(label);
        }
    }

    public void setOnChipCloseListener(OnChipCloseListener listener){
        this.onChipCloseListener = listener;
    }

    public interface OnChipCloseListener{
        void onClose(Chip chip);
    }
}
