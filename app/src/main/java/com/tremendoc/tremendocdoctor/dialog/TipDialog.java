package com.tremendoc.tremendocdoctor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.model.Tip;
import com.tremendoc.tremendocdoctor.repository.TipRepository;

import androidx.appcompat.widget.Toolbar;

public class TipDialog extends Dialog {

    Toolbar toolbar;
    ImageView imageView;
    ImageButton saveBtn;
    TextView contentView;
    EditText commentField;
    TextView likeBtn, commentBtn, shareBtn;

    public TipDialog(Context context, Tip tip) {
        super(context, R.style.FullScreenDialog);
        setContentView(R.layout.dialog_tip);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        setupViews();
        bind(tip);
    }

    private void setupViews() {
        imageView = findViewById(R.id.tip_image);
        contentView = findViewById(R.id.tip_content);
        saveBtn = findViewById(R.id.save_btn);
        shareBtn = findViewById(R.id.share_btn);
        commentBtn = findViewById(R.id.view_comments);
        likeBtn = findViewById(R.id.like_btn);
        commentField = findViewById(R.id.comment_field);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        toolbar.setNavigationOnClickListener(v -> cancel());
    }

    private void bind(Tip tip) {
        toolbar.setTitle(tip.getTitle());
        Picasso.get().load(tip.getImage()).into(imageView);
        contentView.setText(tip.getBody());
        likeBtn.setOnClickListener(btn -> TipRepository.getInstance(getContext()).like(tip.getId()));
    }
}
