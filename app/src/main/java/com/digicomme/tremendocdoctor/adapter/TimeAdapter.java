package com.digicomme.tremendocdoctor.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahamed.multiviewadapter.ItemBinder;
import com.ahamed.multiviewadapter.ItemViewHolder;
import com.digicomme.tremendocdoctor.R;

public class TimeAdapter extends ItemBinder<String, TimeAdapter.TimeHolder> {

    public TimeAdapter() {
        //super(itemDecorator);
    }

    public TimeHolder create(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.holder_time, parent, false);
        return new TimeHolder(view);
    }

    @Override
    public void bind(TimeAdapter.TimeHolder holder, String item) {
        holder.bind(item);
    }

    @Override
    public boolean canBindData(Object item) {
        return  item instanceof String;
    }

    @Override
    public int getSpanSize(int maxSpanCount) {
        return maxSpanCount;
    }

    static class TimeHolder extends ItemViewHolder<String> {
        private TextView textView;

        TimeHolder(View view) {
            super(view);

            textView = view.findViewById(R.id.time);
        }

        void bind(String text) {
            textView.setText(text);

            if (isItemSelected()) {
                textView.setTypeface(null, Typeface.BOLD);
            } else {
                textView.setTypeface(null, Typeface.NORMAL);
            }

            setItemClickListener((v, item) -> {
                toggleItemSelection();
                if (isItemSelected()) {
                    textView.setTypeface(null, Typeface.BOLD);
                } else {
                    textView.setTypeface(null, Typeface.NORMAL);
                }
            });
        }
    }
}
