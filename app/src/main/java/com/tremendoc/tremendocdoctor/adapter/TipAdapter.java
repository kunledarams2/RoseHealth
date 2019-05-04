package com.tremendoc.tremendocdoctor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.callback.ClickListener;
import com.tremendoc.tremendocdoctor.model.Tip;
import com.tremendoc.tremendocdoctor.repository.TipRepository;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TipAdapter extends RecyclerView.Adapter<TipAdapter.TipHolder> {

    private List<Tip> tips;
    private ClickListener<Tip> clickListener;

    public TipAdapter() {
        this.tips = new ArrayList<>();
    }

    @NonNull
    @Override
    public TipHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_tip, parent, false);
        return new TipHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipHolder holder, int position) {
        holder.bind(tips.get(position));
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    public void appendData(List<Tip> list) {
        this.tips.addAll(list);
        notifyDataSetChanged();
    }

    public void setClickListener(ClickListener<Tip> clickListener) {
        this.clickListener = clickListener;
    }

    class TipHolder extends RecyclerView.ViewHolder {
        TextView title, teaser, likeBtn;
        Button viewBtn;
        //CheckBox checkBox;
        ImageView image;
        View view;
        TipHolder(View view){
            super(view);
            this.view = view;
            title = view.findViewById(R.id.tip_title);
            teaser = view.findViewById(R.id.tip_content);
            likeBtn = view.findViewById(R.id.like_btn);
            viewBtn = view.findViewById(R.id.view_btn);
            image = view.findViewById(R.id.tip_image);
        }

        void bind(final Tip tip) {
            likeBtn.setText(String.valueOf(tip.getLikes()));
            title.setText(tip.getTitle());
            teaser.setText(tip.getSummary());
            viewBtn.setOnClickListener(view -> {
                if (clickListener != null) {
                    clickListener.onClick(tip);
                }
            });
            likeBtn.setOnClickListener(view -> {
                TipRepository.getInstance(view.getContext()).like(tip.getId());
            });

            Picasso.get().load(tip.getImage()).into(image);
        }
    }

}
