package com.tremendoc.tremendocdoctor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.model.Appointment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    List<? extends Appointment> list;


    public AppointmentAdapter() {
    }

    public void setAppointments(final List<Appointment> appointments) {
        list = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_appment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        final View rootView;
        TextView avatar, patientName, status, dateView, timeView, typeView;

        public AppointmentViewHolder(View view) {
            super(view);
            this.rootView = view;
            avatar = view.findViewById(R.id.avatar);
            patientName = view.findViewById(R.id.patient_name);
            status = view.findViewById(R.id.status);
            dateView = view.findViewById(R.id.date);
            timeView = view.findViewById(R.id.time);
            typeView = view.findViewById(R.id.type);
        }

        void bind(Appointment appointment) {
            avatar.setText(String.valueOf(appointment.getPatientName().charAt(0)).toUpperCase());
            patientName.setText(appointment.getPatientName());
            status.setText(appointment.getStatus());
            dateView.setText(appointment.getDate());
            timeView.setText(appointment.getTime());
            typeView.setText(appointment.getType());
        }
    }
}
