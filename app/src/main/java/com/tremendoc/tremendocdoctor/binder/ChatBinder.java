package com.tremendoc.tremendocdoctor.binder;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.model.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatBinder extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Message> messages;
    private boolean isGroup;

    private static final int SENT_MESSAGE = 0;
    private static final int RECEIVED_MESSAGE = 1;

    public ChatBinder(Context context, boolean isGroup) {
        this.context = context;
        this.messages = messages = new ArrayList<>();
        this.isGroup = isGroup;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.getType().equals(Message.Type.INCOMING) ? RECEIVED_MESSAGE : SENT_MESSAGE;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENT_MESSAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_message_holder, parent, false);
            return new SentMessageHolder(view);
        } else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.received_message_holder, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (message.getType().equals(Message.Type.SENT)) {
            ((SentMessageHolder) holder).bind(message);
        } else {
            ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    public void add(Message message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    public void setData(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    class SentMessageHolder extends RecyclerView.ViewHolder{
        TextView textView;
        SentMessageHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.message);
        }

        void bind(Message message) {
            textView.setText(message.getContent());
        }
    }

    class ReceivedMessageHolder extends RecyclerView.ViewHolder{
        TextView sender, textView;
        ReceivedMessageHolder(View view) {
            super(view);
            sender = view.findViewById(R.id.sender);
            textView = view.findViewById(R.id.message);
        }

        void bind(Message message) {
            if (isGroup) {
                sender.setText(message.getSender());
            }

            sender.setText(message.getSender());
            textView.setText(message.getContent());
        }
    }
}
