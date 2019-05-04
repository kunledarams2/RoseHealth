package com.tremendoc.tremendocdoctor.binder;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahamed.multiviewadapter.ItemBinder;
import com.ahamed.multiviewadapter.ItemViewHolder;
import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.model.Appointment;
import com.tremendoc.tremendocdoctor.utils.Formatter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CircleAppBinder extends RecyclerView.Adapter<CircleAppBinder.AppointmentHolder> {

    //private Context context;
    private List<String> appointments;

    public CircleAppBinder(){
        appointments =  new ArrayList<>();
        //context = ctx;
    }

    @NonNull
    @Override
    public AppointmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment_circle, parent, false);
        return new AppointmentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentHolder holder, int position) {
        String string = appointments.get(position);
        holder.bind(string);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void setAppointments(List<String> appointments) {
        this.appointments = appointments;
        notifyDataSetChanged();
    }

    class AppointmentHolder extends RecyclerView.ViewHolder {
        TextView date, month;
        //CheckBox checkBox;
        //ImageView icon, checkBox;
        View view;
        AppointmentHolder(View view){
            super(view);
            this.view = view;
            date = view.findViewById(R.id.date);
            month = view.findViewById(R.id.month);
        }

        void bind(final String string) {
            String dateStr = string.split("T")[0];
            String[] frags = dateStr.split("-");
            if (frags.length == 3) {
                date.setText(frags[2]);
                try {
                    month.setText(Formatter.formatMonth(Integer.parseInt(frags[1])));
                }catch (NumberFormatException e) {
                    Log.d("CircleAppBinder", e.getMessage());
                }
            }
        }
    }
}
