package com.tremendoc.tremendocdoctor.service;


public class ChatServiceOld { /*extends Service {

    private WebSocketInterface webSocketInterface = new WebSocketInterface();
    private Socket mSocket;

    @Override
    public void onCreate() {
        super.onCreate();
        attemptAutoStart();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        webSocketInterface.disconnect();
    }


    private void attemptAutoStart() {
        if (!isConnected())
            webSocketInterface.start();
    }

    public boolean isConnected() {
        return  mSocket != null && mSocket.connected();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (!isConnected())
            attemptAutoStart();
        return webSocketInterface;
    }


    public class WebSocketInterface extends Binder {


        private WebSocketListener listener = new WebSocketListenerImpl();
        private ChatListener chatListener;

        public void setChatListener(ChatListener chatListener) {
            this.chatListener = chatListener;
        }

        private void start() {
            try {
                IO.Options options = new IO.Options();
                options.forceNew = false;
                options.reconnection = true;

                mSocket = IO.socket(WEBSOCKET_URL, options);
                mSocket.on(Socket.EVENT_CONNECT, args -> {

                    boolean isSetOnline = com.tremendoc.tremendocdoctor.utils.IO.getData(ChatServiceOld.this, CallConstants.ONLINE_STATUS).equals(CallConstants.ONLINE);
                    if (isSetOnline) {
                        listener.onConnect(mSocket, args);
                        setOnline();
                    }
                });

                mSocket.on(Socket.EVENT_CONNECTING, args -> listener.onConnecting(mSocket, args));

                mSocket.on(Socket.EVENT_CONNECT_ERROR, args -> listener.onConnectError(mSocket, args));

                mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, args -> listener.onConnectTimeout(mSocket, args));

                // mSocket.on("notify", args -> listener.onNotify(mSocket, args));
                mSocket.on(Socket.EVENT_DISCONNECT, args -> listener.onDisconnect(mSocket));

                mSocket.on("incoming-chat", args -> {
                    String callerId = (String) args[0];
                    String callerUuid = (String) args[1];
                    String callerName = (String) args[2];
                    String consultationId = (String) args[3];
                    com.tremendoc.tremendocdoctor.utils.IO.setData(ChatServiceOld.this, CallConstants.PATIENT_ID, callerId);
                    com.tremendoc.tremendocdoctor.utils.IO.setData(ChatServiceOld.this, CallConstants.PATIENT_NAME, callerName);
                    com.tremendoc.tremendocdoctor.utils.IO.setData(ChatServiceOld.this, CallConstants.PATIENT_UUID, callerUuid);
                    com.tremendoc.tremendocdoctor.utils.IO.setData(ChatServiceOld.this, CallConstants.CONSULTATION_ID, consultationId);

                    listener.onIncomingChat(mSocket, args);
                });

                mSocket.on("chat-accepted", args -> {
                    Log.d("WSService", "chat accepted");
                    //do something
                    chatListener.onChatEstablished();
                });

                mSocket.on("chat-ended", args -> {
                    Log.d("WSService", "chat ended");
                    String reason = (String) args[0];

                    chatListener.onChatEnded(reason);
                });

                mSocket.on("new-message", args -> {
                    String msg = (String) args[0];
                    Log.d("ChatServiceOld", msg );
                    chatListener.onNewMessage(msg);
                });

                mSocket.on("typing", args -> {
                    boolean typing = (boolean) args[0];
                    chatListener.onTyping(typing);
                });

                mSocket.connect();

                //Toast.makeText(this, "Web socket connected", Toast.LENGTH_LONG).show();
            } catch (URISyntaxException e) {
                Toast.makeText(ChatServiceOld.this, "URI Syntax error " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("MainActivity", "URI Syntax error " + e.getMessage());
            }

        }

        public void disconnect() {
            if (mSocket != null)
                mSocket.disconnect();

            ChatServiceOld.this.stopForeground(true);
        }

        public void setOnline() {
            if (!isConnected()) {
                start();
                UI.createNotification(ChatServiceOld.this, "You are now online", "You are available for consultations");
            }
            //String username = API.getDoctorId(ChatServiceOld.this);
            String username = DeviceName.getUUID(ChatServiceOld.this);
            mSocket.emit("set-online", username, (Ack) args -> {
                Log.d("WSService", (String) args[0]);
                //setNotification()
            });
        }

        public void chatUp(String uuid, String consultationId) {
            String name = API.getFullName();
            String myId = API.getDoctorId(ChatServiceOld.this);
            mSocket.emit("chatup-user", myId, uuid, name, consultationId, (Ack) args -> {
                chatListener.onChatProgressing();
            });
        }

        public void acceptChat() {
            String callerId = com.tremendoc.tremendocdoctor.utils.IO.getData(ChatServiceOld.this, CallConstants.PATIENT_UUID);
            mSocket.emit("accept-chat", callerId);
        }

        public void endChat(String reason) {
            String userId = com.tremendoc.tremendocdoctor.utils.IO.getData(ChatServiceOld.this, CallConstants.PATIENT_UUID);
            /*String direction = com.tremendoc.tremendocdoctor.utils.IO.getData(ChatServiceOld.this, CallService.CALL_DIRECTION);
            if (direction.equals(CallService.CallDirection.OUTGOING.name())) {
                userId = com.tremendoc.tremendocdoctor.utils.IO.getData(ChatServiceOld.this, CallService.PATIENT_ID);
            } else {
                userId = DeviceName.getUUID(ChatServiceOld.this);
            }*
            mSocket.emit("end-chat", userId, reason);
        }

        public void send(String msg) {
            mSocket.emit("message", msg);
        }

        public void send(String receiverId, String msg) {
            mSocket.emit("message", receiverId, msg);
        }

        public void setTyping(boolean typing) {
            mSocket.emit("typing", typing);
        }

        public boolean isConnected() {
            return ChatServiceOld.this.isConnected();
        }

        public void emit(String event, String msg) {
            mSocket.emit(event, msg);
        }

        public void emit(String event, Ack ack, String... msgs) {
            mSocket.emit(event, msgs, ack);
        }

    }

    public interface WebSocketListener {
        void onConnect(Socket socket, Object... args);
        void onConnecting(Socket socket, Object... args);
        void onConnectError(Socket socket, Object... args);
        void onConnectTimeout(Socket socket, Object... args);
        void onDisconnect(Socket socket);
        void onIncomingChat(Socket socket, Object... args);
    }

    public  interface  ChatListener {
        void onChatEnded(String reason);
        void onNewMessage(String message);
        void onChatEstablished();
        void onChatProgressing();
        void onTyping(boolean isTyping);
    }

    private class WebSocketListenerImpl implements WebSocketListener {
        @Override
        public void onConnect(Socket socket, Object... args) {
            String msg = args.length > 0 ? (String) args[0] : " No message";
            //Toast.makeText(WSService.this, "Web socket connected " + msg, Toast.LENGTH_LONG).show();
            log("Web socket connected " + msg);
        }

        @Override
        public void onConnectError(Socket socket, Object... args) {
            String msg = " No message";
            if (args.length > 0) {
                Exception err = (Exception) args[0];
                //EngineIOException err = (EngineIOException)args[0];
                msg = err.getMessage();
            }
            log("Web connection error " + msg );
        }

        @Override
        public void onConnecting(Socket socket, Object... args) {
            //String msg = args.length > 0 ? (String) args[0] : " No message";
            //Toast.makeText(WSService.this, "Connecting... " + msg, Toast.LENGTH_LONG).show();
            //log("Connecting...   " + msg);
        }

        @Override
        public void onConnectTimeout(Socket socket, Object... args) {
            //log(String.valueOf(args.length));
            //String msg = args.length > 0 ? (String) args[0] : " No message";
            //Toast.makeText(WSService.this, "Connection timeout " + msg, Toast.LENGTH_LONG).show();
            //log("Connection timeout " + msg);
        }

        @Override
        public void onDisconnect(Socket socket) {
            log("You have been disconnected");
        }

        @Override
        public void onIncomingChat(Socket socket, Object... args) {
            String msg = (String) args[0];
            //if (!msg.equals("me"))
            Intent intent = new Intent(ChatServiceOld.this, ChatActivityOld.class);
            //intent.putExtra("fragment", ContactActivity.CHAT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    private void log(String log){
        Log.e("ChatServiceOld", "--__-_--_--_--_-_--_--_--_--" + log);
    }*/
}
