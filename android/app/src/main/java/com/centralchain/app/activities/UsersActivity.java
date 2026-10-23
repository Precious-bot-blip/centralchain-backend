package com.centralchain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.centralchain.app.R;
import com.centralchain.app.adapters.UserAdapter;
import com.centralchain.app.models.User;
import com.centralchain.app.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// admin list of all users
public class UsersActivity extends AppCompatActivity {

    private List<User> users = new ArrayList<>();
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Users");

        adapter = new UserAdapter(users, user -> {
            Intent intent = new Intent(this, UserFormActivity.class);
            intent.putExtra("userId", user.userId);
            startActivity(intent);
        });
        RecyclerView list = findViewById(R.id.list_users);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        // no user id means add a new user
        findViewById(R.id.btn_add_user).setOnClickListener(v ->
                startActivity(new Intent(this, UserFormActivity.class)));
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        ApiClient.getApi(this).getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    users.clear();
                    users.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(UsersActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(UsersActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }
}
