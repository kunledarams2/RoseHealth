package com.digicomme.tremendocdoctor.binder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ahamed.multiviewadapter.ItemBinder;
import com.ahamed.multiviewadapter.ItemViewHolder;
import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.model.Prescription;

public class PrescriptionBinder extends ItemBinder<Prescription, PrescriptionBinder.PrescriptionHolder> {

    @Override
    public PrescriptionHolder create(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.holder_prescription, parent, false);
        return new PrescriptionHolder(view);
    }

    @Override
    public void bind(PrescriptionHolder holder, Prescription item) {
        holder.bind(item, holder.isItemExpanded());
    }

    @Override
    public boolean canBindData(Object item) {
        return item instanceof Prescription;
    }

    static class PrescriptionHolder extends ItemViewHolder<Prescription> {
        TextView patientName, doctorName;
        View itemView, heading, intro, body;
        TextView drugsView, dosageView;
        ImageButton moreBtn;
        TextView startDateView, endDateView, instructionView, reasonView;

        PrescriptionHolder(View view) {
            super(view);
            itemView = view;
            heading = view.findViewById(R.id.heading);
            intro = view.findViewById(R.id.intro);
            body = view.findViewById(R.id.body);

            patientName = view.findViewById(R.id.patient_name);
            doctorName = view.findViewById(R.id.doctor_name);

            drugsView = view.findViewById(R.id.drugs);
            dosageView = view.findViewById(R.id.dosage);
            moreBtn = view.findViewById(R.id.more_btn);

            startDateView = view.findViewById(R.id.start_date);
            endDateView = view.findViewById(R.id.end_date);
            instructionView = view.findViewById(R.id.instruction);
            reasonView = view.findViewById(R.id.reason);
        }

        void bind(Prescription prescription, boolean show) {
            patientName.setText(prescription.getPatientName());
            doctorName.setText(prescription.getDoctorName());

            drugsView.setText(prescription.getDrugs());
            dosageView.setText(prescription.getDosage());
            moreBtn.setOnClickListener(btn -> toggleItemExpansion());

            startDateView.setText(prescription.getStartDate());
            endDateView.setText(prescription.getEndDate());
            instructionView.setText(prescription.getInstruction());
            reasonView.setText(prescription.getReason());

            if (show) {
                body.setVisibility(View.VISIBLE);
                moreBtn.setImageResource(R.drawable.ic_expand_less_black);
            } else {
                body.setVisibility(View.GONE);
                moreBtn.setImageResource(R.drawable.ic_expand_more_black);
            }

            //setItemClickListener((view, item) -> toggleItemExpansion());
        }
    }
}
