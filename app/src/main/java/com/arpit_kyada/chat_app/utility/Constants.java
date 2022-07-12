package com.arpit_kyada.chat_app.utility;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "ChatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSingedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receierId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timeStamp";
    public static final String KEY_COLLECTION_CONVERSATION = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "sanderImg";
    public static final String KEY_RECEIVER_IMAGE = "receiverImg";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABLE = "available";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> remoteMsgHeaders = null;

    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAXc-oW3Q:APA91bFipeWVuaHgkfAX7KbsWC3_9etZajwD5k0ykUs7xKwULojtPDqVSK7ey7e0ZzdfZg94kPqSlluy6rqQ6lNpoe38xqqBJ64jpdzxULJjScs11_7FYKZvIRkRQaRoUqCX5FoA2UQ_"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }

}
