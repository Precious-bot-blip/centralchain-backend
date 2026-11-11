package com.centralchain.app.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.centralchain.app.R;
import com.centralchain.app.adapters.MessageAdapter;
import com.centralchain.app.models.Message;
import com.centralchain.app.models.SendMessageRequest;
import com.centralchain.app.network.ApiClient;
import com.centralchain.app.network.ChatSocket;
import com.centralchain.app.network.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// chat for one order
public class ChatActivity extends AppCompatActivity {

    private long orderId;
    private List<Message> messages = new ArrayList<>();
    private MessageAdapter adapter;
    private RecyclerView list;
    private EditText inputMessage;

    // live updates, if the socket fails we check every 5 seconds instead
    private ChatSocket chatSocket = new ChatSocket();
    private boolean pageOpen = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refresh = new Runnable() {
        @Override
        public void run() {
            loadMessages();
            handler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        orderId = getIntent().getLongExtra("orderId", 0);

        TextView title = findViewById(R.id.text_chat_title);
        TextView subtitle = findViewById(R.id.text_chat_subtitle);
        title.setText("Chat");
        String poNumber = getIntent().getStringExtra("poNumber");
        subtitle.setText(poNumber != null ? poNumber : "Order #" + orderId);

        adapter = new MessageAdapter(messages, SessionManager.getUserId(this));
        list = findViewById(R.id.list_messages);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        inputMessage = findViewById(R.id.input_message);
        findViewById(R.id.btn_send).setOnClickListener(v -> sendMessage());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        pageOpen = true;
        loadMessages();

        chatSocket.connect(this, orderId, new ChatSocket.Listener() {
            @Override
            public void onNewMessage() {
                loadMessages();
            }

            @Override
            public void onFailed() {
                // fall back to checking every 5 seconds
                if (pageOpen) {
                    handler.removeCallbacks(refresh);
                    handler.postDelayed(refresh, 5000);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop listening when the page is closed
        pageOpen = false;
        chatSocket.disconnect();
        handler.removeCallbacks(refresh);
    }

    private void loadMessages() {
        ApiClient.getApi(this).getMessages(orderId).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // only update if there are new messages
                    if (response.body().size() != messages.size()) {
                        messages.clear();
                        messages.addAll(response.body());
                        adapter.notifyDataSetChanged();
                        list.scrollToPosition(messages.size() - 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                // try again next time
            }
        });
    }

    private void sendMessage() {
        String text = inputMessage.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }

        long myId = SessionManager.getUserId(this);
        SendMessageRequest request = new SendMessageRequest(orderId, myId, text);

        ApiClient.getApi(this).sendMessage(request).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if (response.isSuccessful()) {
                    inputMessage.setText("");
                    loadMessages();
                } else {
                    Toast.makeText(ChatActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }
}
