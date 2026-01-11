package com.example.nesvie_copyzalo;

import android.util.Log;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketManager {
    private static final String TAG = "SocketManager";
    private static Socket mSocket;

    public interface OnMessageListener {
        void onMessageReceived(Message m);
        void onAckReceived(String messageId, String status);
    }

    public interface OnFriendRequestListener {
        void onNewFriendRequest(FriendRequest request);
        void onFriendRequestUpdated(FriendRequest request);
    }

    private static OnMessageListener onMessageListener;
    private static OnFriendRequestListener onFriendRequestListener;

    public static void init(String serverUrl, String currentUserId) {
        try {
            IO.Options opts = new IO.Options();
            opts.reconnection = true;
            mSocket = IO.socket(serverUrl, opts);
            mSocket.connect();

            mSocket.on(Socket.EVENT_CONNECT, args -> {
                Log.d(TAG, "Socket connected");
                mSocket.emit("join", currentUserId);
            });

            // ================= Messages =================
            mSocket.on("receiveMessage", args -> {
                try {
                    JSONObject obj = (JSONObject) args[0];
                    Message m = new Message();
                    m.setMessageId(obj.optString("id"));
                    m.setSenderId(obj.optString("senderId"));
                    m.setReceiverId(obj.optString("receiverId"));
                    m.setContent(obj.optString("content"));
                    m.setTimestamp(obj.optLong("timestamp", System.currentTimeMillis()));
                    m.setStatus("delivered");

                    if (onMessageListener != null) onMessageListener.onMessageReceived(m);
                } catch (Exception e) {
                    Log.e(TAG, "parse receiveMessage error", e);
                }
            });

            mSocket.on("messageAck", args -> {
                try {
                    JSONObject obj = (JSONObject) args[0];
                    String id = obj.optString("id");
                    String status = obj.optString("status");
                    if (onMessageListener != null) onMessageListener.onAckReceived(id, status);
                } catch (Exception e) {
                    Log.e(TAG, "messageAck parse error", e);
                }
            });

            // ================= Friend Requests =================
            mSocket.on("newFriendRequest", args -> {
                try {
                    JSONObject obj = (JSONObject) args[0];
                    FriendRequest request = new FriendRequest(
                            obj.getInt("id"),
                            obj.getString("fromId"),
                            obj.getString("toId"),
                            obj.getString("status"),
                            obj.getLong("timestamp")
                    );
                    if (onFriendRequestListener != null) {
                        onFriendRequestListener.onNewFriendRequest(request);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "parse newFriendRequest error", e);
                }
            });

            mSocket.on("friendRequestUpdated", args -> {
                try {
                    JSONObject obj = (JSONObject) args[0];
                    FriendRequest request = new FriendRequest(
                            obj.getInt("id"),
                            obj.getString("fromId"),
                            obj.getString("toId"),
                            obj.getString("status"),
                            obj.getLong("timestamp")
                    );
                    if (onFriendRequestListener != null) {
                        onFriendRequestListener.onFriendRequestUpdated(request);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "parse friendRequestUpdated error", e);
                }
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static Socket socket() { return mSocket; }

    public static void setOnMessageListener(OnMessageListener l) { onMessageListener = l; }

    public static void setOnFriendRequestListener(OnFriendRequestListener l) { onFriendRequestListener = l; }

    public static void disconnect() {
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket = null;
        }
    }
}
