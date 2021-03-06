package com.tremendoc.tremendocdoctor.api;

import com.tremendoc.tremendocdoctor.BuildConfig;

public class URLS {
//    private static final String IP = BuildConfig.DEBUG ? "178.128.8.26" : "178.128.8.26";

    private static final String IP = BuildConfig.DEBUG ? "161.35.87.69" : "178.128.8.26";
//    private static final String IP = BuildConfig.DEBUG ? "192.168.88.23" : "192.168.88.23";
//private static final String IP = BuildConfig.DEBUG ? "192.168.88.28" : "192.168.88.28";


    public static final String SERVER = "http://" + IP + ":9000/tremendoc/api/";

    public static String USER_LOGIN = SERVER + "doctor/authenticate";
    public static String INITIATE_PASSWORD_RESET = SERVER + "doctor/reset";
    public static String COMPLETE_PASSWORD_RESET = SERVER + "doctor/complete/reset";

    public static String PROFILE = SERVER + "doctor/profile/";

    public static String ONLINE_STATUS = SERVER + "doctor/online-mode";

    public static String SAVE_NOTE = SERVER + "doctor_notes/add";
    public static String SAVE_PRESCRIPTION = SERVER + "prescriptions/add";
    public static String DOCTORS_NOTES = SERVER + "doctor_notes/doctor/";

    public static String FETCH_TIPS = SERVER + "healthtips/get";
    public static String LIKE_TIP = SERVER + "healthtip/like";
    public static String SAVE_TIP = SERVER + "healthtip/add";

    public static final String SAVE_CALENDAR = SERVER + "calendar/add";
    public static final String CALENDAR_RETRIEVE = SERVER + "calendar/retrieve/";

    public static String UPDATE_CONSULTATION = SERVER + "consultation/update";

    public static final String PRESCRIPTIONS = SERVER + "prescriptions/doctor/";
    public static final String PRESCRIPTION_SEARCH = SERVER + "prescriptions/search/";

    public static final String MEDICAL_RECORD = SERVER + "customer/profile";

    public static final String CURRENT_EARNINGS = SERVER + "consultation/earnings/current";

    public static String APPOINTMENTS = SERVER + "appointments/retrieve/";
    public static String INITIATE_CONSULTATION = SERVER + "consultation/initiate";
    public static final String CHAT = SERVER + "pusher/message/send";
    public static final String PUSH_TOKEN = SERVER + "push/pushtoken/add";

    public static String CALL_LOG_STATUS= SERVER + "doctor/consultation-call-log/";

    public static String DOCTOR_CHECK_SCHEDULE = SERVER + "doctor-schedule/check-clock-in";
    public static String DOCTOR_SCHEDULE_CLOCKIN= SERVER + "doctor-schedule/clock-in";
    public static String DOCTOR_SCHEDULE_GET_TIME= SERVER + "doctor/get-clock-in";

}
