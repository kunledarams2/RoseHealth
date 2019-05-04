package com.tremendoc.tremendocdoctor.binder;


import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.dialog.NoteDialog;
import com.tremendoc.tremendocdoctor.model.Note;
import com.tremendoc.tremendocdoctor.ui.sectionedrecyclerviewadapter.SectionParameters;
import com.tremendoc.tremendocdoctor.ui.sectionedrecyclerviewadapter.StatelessSection;

import java.util.List;


public class NoteBinder extends StatelessSection {
    private String title;
    List<Note> list;
    private Context context;

    public NoteBinder(Context context, String title, List<Note> list) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.holder_note)
                .headerResourceId(R.layout.header_holder)
                .build());
        this.context = context;
        this.title = title;
        this.list = list;
    }

    @Override
    public int getContentItemsTotal() {
        return list.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ItemViewHolder itemHolder = (ItemViewHolder) holder;
        Note tranx = list.get(position);
        itemHolder.bind(tranx);
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        headerHolder.bind(title);
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;

        HeaderViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
        }

        void bind(String title) {
            this.title.setText(title);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        View view;
        private TextView __name;
        private ImageButton __viewBtn;

        ItemViewHolder(View itemView){
            super(itemView);
            view = itemView;
            __name = itemView.findViewById(R.id.patient_name);
            __viewBtn = itemView.findViewById(R.id.view_btn);
        }

        void bind(final Note note) {
            __name.setText(note.getPatientName());
            view.setOnClickListener(view -> new NoteDialog(view.getContext(), note).show());
        }

    }

}
