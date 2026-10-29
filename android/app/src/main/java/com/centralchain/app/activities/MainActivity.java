package com.centralchain.app.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.centralchain.app.R;
import com.centralchain.app.models.Role;
import com.centralchain.app.network.SessionManager;

// first screen, sends you to login or your home page
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SessionManager.isLoggedIn(this)) {
            openHome(this, SessionManager.getRole(this));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }

    // open the home screen for the role
    public static void openHome(Context context, String role) {
        Intent intent;
        if (Role.BUYER.equals(role)) {
            intent = new Intent(context, BuyerActivity.class);
        } else if (Role.DRIVER.equals(role)) {
            intent = new Intent(context, DriverActivity.class);
        } else if (Role.ADMIN.equals(role)) {
            intent = new Intent(context, AdminActivity.class);
        } else {
            intent = new Intent(context, SupplierActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    // shows the role badge in the header
    public static void showRole(Activity activity) {
        TextView textRole = activity.findViewById(R.id.text_role);
        textRole.setText(Role.getLabel(SessionManager.getRole(activity)));
        textRole.setVisibility(View.VISIBLE);
    }

    // log out and go back to the login page
    public static void logout(Activity activity) {
        SessionManager.logout(activity);
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
