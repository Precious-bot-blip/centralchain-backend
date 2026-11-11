package com.centralchain.app.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

// live chat using the backend websocket
// the backend uses stomp, which is just text in a set format
public class ChatSocket {

    public interface Listener {
        // a new message was sent on this order
        void onNewMessage();

        // could not connect
        void onFailed();
    }

    private WebSocket socket;
    private Handler mainThread = new Handler(Looper.getMainLooper());

    public void connect(Context context, long poId, Listener listener) {
        String token = SessionManager.getToken(context);

        Request request = new Request.Builder()
                .url(ApiClient.SOCKET_URL)
                .addHeader("ngrok-skip-browser-warning", "true")
                .build();

        socket = new OkHttpClient().newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // log in with the token, every stomp message ends with a zero character
                webSocket.send("CONNECT\naccept-version:1.2\nheart-beat:0,0\nAuthorization:Bearer " + token + "\n\n\0");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                if (text.startsWith("CONNECTED")) {
                    // listen to this order's chat
                    webSocket.send("SUBSCRIBE\nid:chat\ndestination:/topic/orders/" + poId + "\n\n\0");
                } else if (text.startsWith("MESSAGE")) {
                    mainThread.post(() -> listener.onNewMessage());
                } else if (text.startsWith("ERROR")) {
                    mainThread.post(() -> listener.onFailed());
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                mainThread.post(() -> listener.onFailed());
            }
        });
    }

    public void disconnect() {
        if (socket != null) {
            socket.close(1000, null);
            socket = null;
        }
    }
}
