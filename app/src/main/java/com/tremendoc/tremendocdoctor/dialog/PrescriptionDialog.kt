package com.tremendoc.tremendocdoctor.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tremendoc.tremendocdoctor.R
import com.tremendoc.tremendocdoctor.activity.BaseActivity
import com.tremendoc.tremendocdoctor.api.StringCall
import com.tremendoc.tremendocdoctor.api.URLS
import com.tremendoc.tremendocdoctor.utils.Formatter
import com.tremendoc.tremendocdoctor.utils.ToastUtil
import kotlinx.android.synthetic.main.dialog_new_prescription.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class PrescriptionDialog(val ctx: AppCompatActivity, val patientId: String?, val consultationId: String?): Dialog(ctx, R.style.FullScreenDialog) {
    private var isBusy: Boolean? = false
    private var dialogOpen: Boolean? = false

    init {
        this.setContentView(R.layout.dialog_new_prescription)
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT)
        setupViews()
    }

    private fun setupViews() {
        toolbar.setNavigationIcon(R.drawable.ic_close_white)
        toolbar.setNavigationOnClickListener {
            cancel()
            BaseActivity.hideKeyboard(ctx)
        }

        save_btn.setOnClickListener { clickSavePrescription() }

        start_date_field.setOnClickListener {
            if ( dialogOpen != true) {
                openDatepicker(start_date_field)
            }
        }

        end_date_field.setOnClickListener {
            if (dialogOpen != true) {
                openDatepicker(end_date_field)
            }
        }
    }

    private fun clickSavePrescription() {
        val dosage = dosages_field.text.toString()
        val startDate = start_date_field.text.toString()
        val endDate = end_date_field.text.toString()
        val medication = medication_field.text.toString()
        val reason = reason_field.text.toString()
        val instruction = special_field.text.toString()
        when {
            TextUtils.isEmpty(medication) -> ToastUtil.showLong(context, "You haven't entered a medication")
            TextUtils.isEmpty(dosage) -> ToastUtil.showLong(context, "You haven't entered a dosage")
            TextUtils.isEmpty(startDate) -> ToastUtil.showLong(context, "You haven't entered the date this prescription starts")
            TextUtils.isEmpty(endDate) -> ToastUtil.showLong(context, "You haven't entered the date this prescription ends")
            else -> savePrescription(dosage, medication, startDate, endDate, reason, instruction)
        }
    }

    private fun savePrescription(dosage: String, medication: String, startDate: String, endDate: String, reason: String, instruction: String) {
        BaseActivity.hideKeyboard(ctx)
        progressBar.visibility = View.VISIBLE
        isBusy = true

        val params = HashMap<String, String?>()
        params["consultationId"] = consultationId
        params["patientId"] = patientId
        params["dosage"] = dosage
        params["medication"] = medication
        params["startDate"] = startDate
        params["endDate"] = endDate
        params["doctorReason"] = reason
        params["specialInstruction"] = instruction

        log(consultationId)
        log(patientId)
        log(dosage)
        log(medication)

        val call = StringCall(ctx)
        call.post(URLS.SAVE_PRESCRIPTION, params, { response ->
            progressBar.visibility = View.INVISIBLE
            isBusy = false

            try {
                val resObj = JSONObject(response)
                if (resObj.has("code") && resObj.getInt("code") == 0) {
                    //ToastUtil.showLong(ctx, "Prescription saved successfully");
                    Toast.makeText(ctx, "Prescription saved successfully", Toast.LENGTH_LONG).show()
                    cancel()
                } else if (resObj.has("description")) {
                    ToastUtil.showModal(ctx, resObj.getString("description"))
                }
            } catch (e: JSONException) {
                ToastUtil.showModal(ctx, e.message)
            }

        }, { error ->
            progressBar.visibility = View.INVISIBLE
            isBusy = false
            log("VOLLEY ERROR")
            log(error.message)
            if (error.networkResponse == null) {
                log("Network response is null")
                ToastUtil.showModal(ctx, "Please check your internet connection")
            } else {
                val errMsg = Formatter.bytesToString(error.networkResponse.data)
                ToastUtil.showModal(ctx, errMsg)
                log("DATA: $errMsg")
            }
        })
    }


    private fun openDatepicker(editText: EditText) {
        val now = Calendar.getInstance()
        val dialog = DatePickerDialog(context, R.style.DatepickerTheme, { datePicker, year, month, day ->
            val strDay = if (day.toString().length == 1) "0$day" else day.toString() + ""
            val strMonth = if ((month + 1).toString().length == 1) "0$month" else month.toString() + ""
            val date = "$year-$strMonth-$strDay"
            editText.setText(date)
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE))

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setOnCancelListener { d -> dialogOpen = false }
        dialog.setOnDismissListener { d -> dialogOpen = false }
        dialog.show()
        dialogOpen = true
    }


    private fun log(log: String?) {
        Log.e("PrescriptionDialog", "--__--_--__-----___-----__-----_--_-----   $log")
    }

}