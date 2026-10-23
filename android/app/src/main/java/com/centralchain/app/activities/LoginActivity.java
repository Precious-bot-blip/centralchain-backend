package com.centralchain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.centralchain.app.R;
import com.centralchain.app.models.LoginRequest;
import com.centralchain.app.models.LoginResponse;
import com.centralchain.app.models.User;
import com.centralchain.app.network.ApiClient;
import com.centralchain.app.network.SessionManager;
import com.centralchain.app.utils.Validation;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// login page for everyone, the role from the api decides where you go
public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private MaterialButton btnLogin;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Log in");

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        btnLogin = findViewById(R.id.btn_login);
        progress = findViewById(R.id.progress);

        btnLogin.setOnClickListener(v -> login());
        findViewById(R.id.btn_go_signup).setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
    }

    private void login() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString();

        // check the fields
        boolean valid = true;
        if (email.isEmpty()) {
            inputEmail.setError("Email is required");
            valid = false;
        } else if (!Validation.isValidEmail(email)) {
            inputEmail.setError("Enter a valid email address");
            valid = false;
        }
        if (password.isEmpty()) {
            inputPassword.setError("Password is required");
            valid = false;
        }
        if (!valid) {
            return;
        }

        // remove any old login first
        SessionManager.logout(this);

        progress.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        LoginRequest request = new LoginRequest(email, password);
        ApiClient.getApi(this).login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progress.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse user = response.body();
                    SessionManager.saveLogin(LoginActivity.this, user);
                    getUserId(user.email, user.roleName);
                } else {
                    Toast.makeText(LoginActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    // login does not send the user id so get it from the users endpoint
    private void getUserId(String email, String userRole) {
        progress.setVisibility(View.VISIBLE);
        ApiClient.getApi(this).getUserByEmail(email).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    SessionManager.saveUserId(LoginActivity.this, response.body().userId);
                    MainActivity.openHome(LoginActivity.this, userRole);
                    finish();
                } else {
                    SessionManager.logout(LoginActivity.this);
                    Toast.makeText(LoginActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progress.setVisibility(View.GONE);
                SessionManager.logout(LoginActivity.this);
                Toast.makeText(LoginActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }
}
