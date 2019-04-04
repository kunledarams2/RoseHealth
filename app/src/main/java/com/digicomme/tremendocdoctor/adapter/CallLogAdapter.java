package com.digicomme.tremendocdoctor.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.model.CallLog;
import com.digicomme.tremendocdoctor.utils.Formatter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.LogHolder> {

    private List<CallLog> callLogs;
    private ClickListener clickListener;

    public CallLogAdapter() {
        callLogs = new ArrayList<>();
    }

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

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    class LogHolder extends RecyclerView.ViewHolder{
        private TextView img, callerId, time, count;
        private ImageButton voiceBtn, videoBtn, chatBtn;

        LogHolder(View view) {
            super(view);
            img = view.findViewById(R.id.img);
            callerId = view.findViewById(R.id.caller_id);
            time = view.findViewById(R.id.call_time);
            count = view.findViewById(R.id.call_counts);
            voiceBtn = view.findViewById(R.id.voice_call_btn);
            videoBtn = view.findViewById(R.id.video_call_btn);
            chatBtn = view.findViewById(R.id.chat_btn);
        }

        void bind(CallLog log) {
            img.setText(String.valueOf(log.getCallerName().charAt(0)).toUpperCase());
            callerId.setText(log.getCallerName());
            try {
                time.setText(Formatter.formatTime(log.getTime()));
            } catch (ParseException e) {
                Log.d("CallLogAdapter.time err", e.getMessage());
                time.setText(log.getTime());
            }
            //count.setText(log.getCount() + " missed call");
            count.setText(log.getCallType());

            if (clickListener != null) {
                chatBtn.setOnClickListener(btn -> clickListener.onChatClicked(log.getCallerId()));
                videoBtn.setOnClickListener(btn -> clickListener.onVideoClicked(log.getCallerId()));
                voiceBtn.setOnClickListener(btn -> clickListener.onVoiceClicked(log.getCallerId()));
            }
        }

    }

    public interface ClickListener {
        void onVideoClicked(int callerId);
        void onVoiceClicked(int callerId);
        void onChatClicked(int callerId);
    }


}

