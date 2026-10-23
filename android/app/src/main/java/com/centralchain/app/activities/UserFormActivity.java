package com.centralchain.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.centralchain.app.R;
import com.centralchain.app.models.Role;
import com.centralchain.app.models.User;
import com.centralchain.app.models.UserRequest;
import com.centralchain.app.network.ApiClient;
import com.centralchain.app.utils.Validation;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// admin adds or edits a user
public class UserFormActivity extends AppCompatActivity {

    private long userId;
    private boolean isNewUser;
    private EditText inputName, inputEmail, inputPassword;
    private Spinner spinnerRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_form);

        userId = getIntent().getLongExtra("userId", 0);
        isNewUser = userId == 0;

        inputName = findViewById(R.id.input_name);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        spinnerRole = findViewById(R.id.spinner_role);

        // role names in the spinner
        String[] labels = new String[Role.ALL.length];
        for (int i = 0; i < Role.ALL.length; i++) {
            labels[i] = Role.getLabel(Role.ALL[i]);
        }
        spinnerRole.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));

        TextView title = findViewById(R.id.screen_title);
        if (isNewUser) {
            title.setText("New user");
        } else {
            title.setText("Edit user");
            inputPassword.setHint("Leave empty to keep the password");
            findViewById(R.id.btn_delete).setVisibility(View.VISIBLE);
            loadUser();
        }

        findViewById(R.id.btn_save).setOnClickListener(v -> saveUser());
        findViewById(R.id.btn_delete).setOnClickListener(v -> confirmDelete());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadUser() {
        ApiClient.getApi(this).getUser(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    inputName.setText(user.fullName);
                    inputEmail.setText(user.email);
                    // pick their role in the spinner
                    for (int i = 0; i < Role.ALL.length; i++) {
                        if (Role.ALL[i].equals(user.roleName)) {
                            spinnerRole.setSelection(i);
                        }
                    }
                } else {
                    Toast.makeText(UserFormActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(UserFormActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveUser() {
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString();
        String role = Role.ALL[spinnerRole.getSelectedItemPosition()];

        // check the fields
        boolean valid = true;
        if (name.isEmpty()) {
            inputName.setError("Full name is required");
            valid = false;
        }
        if (!Validation.isValidEmail(email)) {
            inputEmail.setError("Enter a valid email address");
            valid = false;
        }
        // password is needed for new users, optional when editing
        if ((isNewUser || !password.isEmpty()) && !Validation.isValidPassword(password)) {
            inputPassword.setError("Password must be at least 6 characters");
            valid = false;
        }
        if (!valid) {
            return;
        }

        // empty password means keep the old one
        if (password.isEmpty()) {
            password = null;
        }
        UserRequest request = new UserRequest(name, email, password, role);

        Call<User> call;
        if (isNewUser) {
            call = ApiClient.getApi(this).addUser(request);
        } else {
            call = ApiClient.getApi(this).updateUser(userId, request);
        }

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(UserFormActivity.this, "User saved", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UserFormActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(UserFormActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete this user?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser() {
        ApiClient.getApi(this).deleteUser(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(UserFormActivity.this, "User deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UserFormActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(UserFormActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }
}
