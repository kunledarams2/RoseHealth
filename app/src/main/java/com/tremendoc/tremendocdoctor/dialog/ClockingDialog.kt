package com.tremendoc.tremendocdoctor.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import com.tremendoc.tremendocdoctor.EndPointAPI.DoctorSchedule
import com.tremendoc.tremendocdoctor.R
import com.tremendoc.tremendocdoctor.activity.MainActivity
import com.tremendoc.tremendocdoctor.api.API
import com.tremendoc.tremendocdoctor.api.StringCall
import com.tremendoc.tremendocdoctor.api.URLS
import com.tremendoc.tremendocdoctor.model.DoctorClocking
import com.tremendoc.tremendocdoctor.utils.DoctorScheduleContants
import com.tremendoc.tremendocdoctor.utils.Formatter
import com.tremendoc.tremendocdoctor.utils.IO
import com.tremendoc.tremendocdoctor.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_clocking_dialog.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class ClockingDialog(context: Context) : Dialog(context) {

    private var doctorSchedule:DoctorSchedule?=null
    private val TAG = ClockingDialog::class.java.simpleName
    private var canClockIn:Boolean = false
    private var bundle:Bundle?=null
    private  var  clockTimeValue:String?=null

    private var doctorClocking: DoctorClocking?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clocking_dialog)

        doctorSchedule= DoctorSchedule(context)
        doctorClocking= DoctorClocking(context)
        bundle= Bundle()

        setupView()
    }

    fun setupView(){

        switchClockIn!!.isChecked = canClockIn
        switchClockIn !!.setOnClickListener { clockMeIn()
//            doctorClocking!!.set(DoctorClocking.CLOCKING_TIME,clockTimeValue)
        }
//        switchClockIn.setOnCheckedChangeListener { compoundButton, b ->  }

        closeDialog!!.setOnClickListener {
//            val intent =Intent(context, MainActivity::class.java)
//            intent.putExtras(bundle)
//            context.startActivity(intent)

            Toast.makeText(context, bundle!!.getString(DoctorScheduleContants.NEXTCLOCKIN),Toast.LENGTH_LONG).show()
            dismiss()
        }
    }


    companion object{

       val CLOCK_IN_DOCTOR= "description"
    }


    fun clockMeIn() {

        val stringCall = StringCall(context)
        val params = HashMap<String, String>()
        val doctorId = API.getDoctorId(context)
        params["doctorId"] = doctorId

        stringCall.post(URLS.DOCTOR_SCHEDULE_CLOCKIN, params,
                { response ->

                    log("clockIn:  $response")
                    try {
                        val obj = JSONObject(response)
                        if (obj.has("code") && obj.getInt("code") == 0) {

                            clockTimeValue= obj.getString("description")
                            tvDescription.text=clockTimeValue


                            IO.setData(context,API.NEXT_CLOCK_IN_TIME,obj.getString("nextClockInTime"))
                            bundle!!.putString(DoctorScheduleContants.NEXTCLOCKIN,obj.getString("nextClockInTime"))

//                            API.setUserData(context, obj.getString("nextClockInTime"))
//                            clockTimeValue=obj.getString("nextClockInTime")

                            switchClockIn.isChecked=true
                            switchClockIn.isEnabled =false

                        } else  {
                            ToastUtil.showLong(context, obj.getString("description"))
                            tvDescription.text = obj.getString("description")
                            switchClockIn.isChecked=false


                        }

                    } catch (e: JSONException) {

                    }
                },

                { error ->
                    if(error.networkResponse !=null){
                        log("Network oops")
                    }

                })

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    fun log(mgs: String) {
        Log.d(TAG, "---____---__---____----___--____$mgs")
    }

}
