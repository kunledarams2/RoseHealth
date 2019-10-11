package com.tremendoc.tremendocdoctor.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.model.CallLog;
import com.tremendoc.tremendocdoctor.model.NewCallLog;
import com.tremendoc.tremendocdoctor.utils.Formatter;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.LogHolder> {

//    private List<CallLog> callLogs;
    private ClickListener clickListener;
    private List<NewCallLog>callLogs = new ArrayList<>();
    private Context context;


    public CallLogAdapter( Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public LogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.holder_calllog, parent, false);
        return new LogHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogHolder holder, int position) {
        NewCallLog log = callLogs.get(position);
        holder.bind(log, context);
//        holder.callLog= new CallLog(context);
    }

    @Override
    public int getItemCount() {
        return callLogs.size();
    }

    public void setCallLogs(List<NewCallLog> callLogs) {
        this.callLogs = callLogs;
        notifyDataSetChanged();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    class LogHolder extends RecyclerView.ViewHolder{
        private TextView img, callerId, time, count,callStatus;
        private ImageButton voiceBtn, videoBtn, chatBtn;
        private CallLog callLog;

        LogHolder(View view) {
            super(view);
            img = view.findViewById(R.id.img);
            callerId = view.findViewById(R.id.caller_id);
            time = view.findViewById(R.id.call_time);
            count = view.findViewById(R.id.call_counts);
            callStatus=view.findViewById(R.id.consultationStatus);
            voiceBtn = view.findViewById(R.id.voice_call_btn);
            videoBtn = view.findViewById(R.id.video_call_btn);
            chatBtn = view.findViewById(R.id.chat_btn);

        }

        void bind(NewCallLog log, Context Conext){
            img.setText(String.valueOf(log.getUserName().charAt(0)).toUpperCase());
            count.setText(log.getCount());
            callStatus.setText(log.getConsultationType());
            callerId.setText(log.getUserName().toUpperCase());
            ColorGenerator colorGenerator=  ColorGenerator.MATERIAL;
            callLog= new CallLog(Conext);


//            time.setText(log.getTime());
            try {
                time.setText(Formatter.formatTime(log.getTime()));
                Log.d("timeChecker", Formatter.formatTime(log.getTime()));
            }catch (ParseException e){
                Log.d("callLog.time error", e.getMessage());
            }

            if (clickListener != null) {

//                chatBtn.setOnClickListener(btn -> {
//                    callLog.set(CallLog.CALL_TYPE, "CHAT");
////                    clickListener.onChatClicked(callLog);
//                });
                videoBtn.setOnClickListener(btn -> {

                    callLog.set(CallLog.PATIENT_UUID, log.getPatientUuid());
                    callLog.set(CallLog.DOCTOR_TOKEN,log.getDoctorToken());
                    callLog.set(CallLog.PATIENT_ID,log.getPatientId());
                    callLog.set(CallLog.PATIENT_NAME,log.getUserName());
                    callLog.set(CallLog.PATIENT_TOKEN,log.getPatientToken());

                    callLog.set(CallLog.CALL_TYPE, "VIDEO");
                    clickListener.onVideoClicked(callLog);
                });
                voiceBtn.setOnClickListener(btn -> {

                    callLog.set(CallLog.PATIENT_UUID, log.getPatientUuid());
                    callLog.set(CallLog.DOCTOR_TOKEN,log.getDoctorToken());
                    callLog.set(CallLog.PATIENT_ID,log.getPatientId());
                    callLog.set(CallLog.PATIENT_NAME,log.getUserName());
                    callLog.set(CallLog.PATIENT_TOKEN,log.getPatientToken());
                    callLog.set(CallLog.CONSULTATION_ID,log.getConsultationId());

                    callLog.set(CallLog.CALL_TYPE, "AUDIO");
                    clickListener.onVoiceClicked(callLog);
                });
            }
        }

//        void bind(NewCallLog log) {
//            img.setText(String.valueOf(log.get(NewCallLog.PATIENT_NAME).charAt(0)).toUpperCase());
//            callerId.setText(log.get(CallLog.PATIENT_NAME));
//            try {
//                time.setText(Formatter.formatTime(log.get(CallLog.TIME)));
//            } catch (ParseException e) {
//                Log.d("CallLogAdapter.time err", e.getMessage());
//                time.setText(log.get(CallLog.TIME));
//            }
//            //count.setText(log.getCount() + " missed " + (log.getCount() == 1 ? "call" : "calls"));
//            count.setText(log.get(CallLog.CALL_TYPE));
//
//            if (clickListener != null) {
//                chatBtn.setOnClickListener(btn -> {
//                    log.set(CallLog.CALL_TYPE, "CHAT");
//                    clickListener.onChatClicked(log);
//                });
//                videoBtn.setOnClickListener(btn -> {
//                    log.set(CallLog.CALL_TYPE, "VIDEO");
//                    clickListener.onVideoClicked(log);
//                });
//                voiceBtn.setOnClickListener(btn -> {
//                    log.set(CallLog.CALL_TYPE, "AUDIO");
//                    clickListener.onVoiceClicked(log);
//                });
//            }
//        }

    }

    public interface ClickListener {
        void onVideoClicked(CallLog log);
        void onVoiceClicked(CallLog log);
        void onChatClicked();
    }


}

