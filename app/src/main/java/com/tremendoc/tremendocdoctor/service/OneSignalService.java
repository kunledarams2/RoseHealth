package com.tremendoc.tremendocdoctor.service;



public class OneSignalService { /*extends NotificationExtenderService {

    public static final String DOCTOR_UUID = "doctorUuid";
    public static final String CHAT_UP = "chat-up";
    public static final String HANG_UP = "hang-up";

    private OneSignalInterface oneSignalInterface = new OneSignalInterface();


    protected boolean onNotificationProcessing (OSNotificationReceivedResult result) {
        JSONObject payload = result.payload.additionalData;
        Log.d("OneSignalService", payload.toString());
        Log.d("OneSignalService Raw", result.payload.rawPayload);
        OverrideSettings settings = new OverrideSettings();
        settings.extender = new NotificationCompat.Extender() {
            @Override
            public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
                // Sets the background notification color to Red on Android 5.0+ devices.
                //Bitmap icon = BitmapFactory.decodeResource(MyApplication.getContext().getResources(),
                //        R.);
                //builder.setLargeIcon(icon);
                return builder.setColor(new BigInteger("FF0000FF", 16).intValue());
            }
        };

        /*settings.extender = new NotificationCompat.Extender() {
            @Override
            public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
                return null;
            }
        };*
        return true;
    }

    public OneSignalService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        oneSignalInterface.connect();
        return oneSignalInterface;
    }

    public class OneSignalInterface extends Binder {

        private ChatListener chatListener;

        public void setChatListener(ChatListener listener) {
            chatListener = listener;
        }

        public void connect() {
            OneSignal.setSubscription(true);
        }

        public void disconnect() {
            OneSignal.setSubscription(false);
        }

        public void sendChatNotification(String receiverUuid, String consultationId) {
            try {
                JSONObject contents = new JSONObject();
                contents.put(CONSULTATION_ID, consultationId);
                contents.put("doctorId", API.getDoctorId(OneSignalService.this));
                Map<String, String> data = API.getCredentials(OneSignalService.this);
                contents.put("doctorName", data.get(API.FIRST_NAME) + " " + data.get(API.LAST_NAME));
                contents.put("doctorUuid", DeviceName.getUUID(OneSignalService.this));
                JSONArray userIds = new JSONArray();
                userIds.put(receiverUuid);

                JSONObject payload = new JSONObject();
                payload.put("contents", contents.toString());
                payload.put("include_external_user_ids", userIds.toString());

                OneSignal.postNotification(payload, new OneSignal.PostNotificationResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        log("onSuccess() " + response.toString());
                        if (chatListener != null) {
                            chatListener.onChatProgressing();
                        }
                    }

                    @Override
                    public void onFailure(JSONObject response) {
                        log("onFailure() " + response.toString());
                    }
                });
            } catch (JSONException e ){
                log(e.getMessage());
            }
        }

        public void acceptChat() {

        }

        public void endChat(String reason) {

        }

    }


    private void log(String log) {
        Log.d("OneSignalService", "__----__--__----_---_--  " + log);
    }


    public  interface  ChatListener {
        void onChatEnded(String reason);
        void onChatEstablished();
        void onChatProgressing();
        void onTyping(boolean isTyping);
    }
*/
}
