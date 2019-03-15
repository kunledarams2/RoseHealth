package com.digicomme.tremendocdoctor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.model.CallLog;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.LogHolder> {

    private List<CallLog> callLogs;

    @NonNull
    @Override
    public LogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_calllog, parent, false);
        return new LogHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogHolder holder, int position) {
        CallLog log = callLogs.get(position);
        holder.bind(log);
    }

    @Override
    public int getItemCount() {
        return callLogs.size();
    }

    public void setCallLogs(List<CallLog> callLogs) {
        this.callLogs = callLogs;
        notifyDataSetChanged();
    }

    class LogHolder extends RecyclerView.ViewHolder{
        private TextView callerId, time, count;
        private ImageButton voiceBtn, videoBtn, chatBtn;

        LogHolder(View view) {
            super(view);
            callerId = view.findViewById(R.id.caller_id);
            time = view.findViewById(R.id.call_time);
            count = view.findViewById(R.id.call_counts);
            voiceBtn = view.findViewById(R.id.voice_call_btn);
            videoBtn = view.findViewById(R.id.video_call_btn);
            chatBtn = view.findViewById(R.id.chat_btn);
        }

        void bind(CallLog log) {
            callerId.setText(log.getCallerId());
            time.setText(log.getTime());
            count.setText(log.getCount() + " missed call");
        }

    }
}
