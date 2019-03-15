package com.digicomme.tremendocdoctor.activity;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.binder.ChatBinder;
import com.digicomme.tremendocdoctor.model.Message;

import java.util.ArrayList;

public class DoctorsChatActivity extends BaseActivity {
    ChatBinder binder;
    RecyclerView recyclerView;
    LinearLayoutManager manager;
    ArrayList<Message> messages;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_doctors_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_navigation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        messages = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        binder = new ChatBinder(this, messages, true);
        setupAdapter();
    }

    private void setupAdapter() {
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(binder);
        populate();
    }

    private void populate() {
        String[] strings = {
                "Hello guys! I need your help...",
                "... on a consultation", "Hello Doctor",
                "I have not feeling well lately", "Me too I need someone",
                "How are we doing today?", "How are we doing today?"};
        String[] doctors = {"Dr John", "Dr Mark", "Me", "Me", "Dr Francis", "Dr Hinn", "Dr Ben"};
        for (int i = 0; i < strings.length; i++) {
            Message message = new Message();
            message.setContent(strings[i]);
            message.setSender(doctors[i]);
            if (doctors[i].equals("Me")) {
               message.setType(Message.Type.SENT);
            }
            else {
                message.setType(Message.Type.INCOMING);
            }

            messages.add(message);
        }
        binder.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fragment", MainActivity.CHATROOM);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


}
