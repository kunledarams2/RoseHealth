package com.digicomme.tremendocdoctor.api;

public class URLS {
    public static final String WEBSOCKET_URL = "http://173.230.149.104:3210";
    public static String SERVER = "http://138.68.159.246:9000/tremendoc/api/";

    public static String USER_CREATE =  SERVER + "doctor/create";
    public static String USER_LOGIN = SERVER + "doctor/authenticate";
    public static String INITIATE_PASSWORD_RESET = SERVER + "doctor/reset";
    public static String COMPLETE_PASSWORD_RESET = SERVER + "doctor/complete/reset";

    public static String PROFILE = SERVER + "doctor/profile/";


    public static String ADD_CARD = SERVER + "payment/card/add";
    public static String ONLINE_STATUS = SERVER + "doctor/online-mode";

    public static String SAVE_NOTE = SERVER + "doctor_notes/add";
    public static String SAVE_PRESCRIPTION = SERVER + "prescriptions/add";
    public static String DOCTORS_NOTES = SERVER + "doctor_notes/doctor/";

    public static String FETCH_TIPS = SERVER + "healthtips/get";
    public static String LIKE_TIP = SERVER + "healthtip/like";
    public static String SAVE_TIP = SERVER + "healthtip/add";

    public static final String SAVE_CALENDAR = SERVER + "calendar/add";

    public static final String PRESCRIPTIONS = SERVER + "prescriptions/doctor/";

    public static final String MEDICAL_RECORD = SERVER + "customer/profile";
    public static final String PATIENT_PROFILE_UUID = SERVER + "account/customer/";

    public static final String CURRENT_EARNINGS = SERVER + "consultation/earnings/current";
    public static final String DAILY_APPOINTMENT = SERVER + "appointments/retrieve-by-date";

    public static String APPOINTMENTS = SERVER + "appointments/retrieve/";
    public static String INITIATE_CONSULTATION = SERVER + "consultation/initiate";

}
