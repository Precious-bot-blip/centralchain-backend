package com.centralchain.app.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.centralchain.app.R;
import com.centralchain.app.models.Delivery;
import com.centralchain.app.network.ApiClient;
import com.centralchain.app.utils.Validation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// proof of delivery with photo, name and signature
public class ProofOfDeliveryActivity extends AppCompatActivity {

    private long deliveryId;
    private File photoFile;
    private EditText inputRecipient, inputSignature;
    private ImageView imagePhoto;
    private ProgressBar progress;

    // opens the camera app
    private ActivityResultLauncher<Uri> takePhoto = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    showPhoto();
                } else {
                    photoFile = null;
                }
            });

    // opens the gallery
    private ActivityResultLauncher<String> pickPhoto = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    copyPhoto(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proof);

        deliveryId = getIntent().getLongExtra("deliveryId", 0);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Proof of delivery");
        TextView subtitle = findViewById(R.id.screen_subtitle);
        subtitle.setText(getIntent().getStringExtra("poNumber"));
        subtitle.setVisibility(View.VISIBLE);

        inputRecipient = findViewById(R.id.input_recipient);
        inputSignature = findViewById(R.id.input_signature);
        imagePhoto = findViewById(R.id.photo_preview);
        progress = findViewById(R.id.progress);

        findViewById(R.id.btn_camera).setOnClickListener(v -> openCamera());
        findViewById(R.id.btn_gallery).setOnClickListener(v -> pickPhoto.launch("image/*"));
        findViewById(R.id.btn_submit).setOnClickListener(v -> submit());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void openCamera() {
        photoFile = newPhotoFile(".jpg");
        Uri uri = FileProvider.getUriForFile(this, "com.centralchain.app.fileprovider", photoFile);
        takePhoto.launch(uri);
    }

    // make an empty file in the app folder
    private File newPhotoFile(String extension) {
        File folder = new File(getCacheDir(), "pod");
        folder.mkdirs();
        return new File(folder, "pod_" + System.currentTimeMillis() + extension);
    }

    // copy the gallery photo into our own file
    private void copyPhoto(Uri uri) {
        String type = getContentResolver().getType(uri);
        String extension = ".jpg";
        if ("image/png".equals(type)) {
            extension = ".png";
        } else if ("image/webp".equals(type)) {
            extension = ".webp";
        }

        try {
            photoFile = newPhotoFile(extension);
            InputStream in = getContentResolver().openInputStream(uri);
            OutputStream out = new FileOutputStream(photoFile);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
            showPhoto();
        } catch (Exception e) {
            photoFile = null;
            Toast.makeText(this, "Could not use that photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhoto() {
        imagePhoto.setImageURI(Uri.fromFile(photoFile));
        imagePhoto.setVisibility(View.VISIBLE);
        findViewById(R.id.photo_placeholder).setVisibility(View.GONE);
    }

    private void submit() {
        String recipient = inputRecipient.getText().toString().trim();
        String signature = inputSignature.getText().toString().trim();

        // check the photo and names
        if (photoFile == null || !Validation.isValidPhoto(photoFile.getName())) {
            Toast.makeText(this, "Take or choose a photo first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (recipient.isEmpty()) {
            inputRecipient.setError("Recipient name is required");
            return;
        }
        if (signature.isEmpty()) {
            inputSignature.setError("Signature is required");
            return;
        }

        // mark it delivered, this also takes the stock off
        progress.setVisibility(View.VISIBLE);
        ApiClient.getApi(this).markDelivered(deliveryId).enqueue(new Callback<Delivery>() {
            @Override
            public void onResponse(Call<Delivery> call, Response<Delivery> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    // todo: the backend only saves links for the proof and has no photo upload yet
                    // when it does, upload the photo and send the links
                    Toast.makeText(ProofOfDeliveryActivity.this, "Order delivered", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(ProofOfDeliveryActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Delivery> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ProofOfDeliveryActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }
}
