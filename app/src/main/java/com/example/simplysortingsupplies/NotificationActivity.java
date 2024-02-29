package com.example.simplysortingsupplies;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.simplysortingsupplies.databinding.ActivityDrawerBaseBinding;
import com.example.simplysortingsupplies.databinding.ActivityNotificationBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class NotificationActivity extends DrawerBaseActivity {

    private TextInputLayout inputPhoneNumber;
    Button btnNotification, btnAddPhone;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mRef;
    ProgressBar progressBar;
    ActivityNotificationBinding activityNotificationBinding;
    private static final String PERMISSION_SEND_SMS = Manifest.permission.SEND_SMS;
    private static final int UNIQUE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityNotificationBinding = activityNotificationBinding.inflate(getLayoutInflater());
        setContentView(activityNotificationBinding.getRoot());
        allocateActivityTitle("");

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child("Phone Numbers");

        inputPhoneNumber = findViewById(R.id.inputPhoneNumber);
        progressBar = findViewById(R.id.notificationProgressBar);
        btnNotification = findViewById(R.id.btnNotification);
        btnAddPhone = findViewById(R.id.btnAddPhone);


        btnAddPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //checks user's input
                final String phoneNumber = inputPhoneNumber.getEditText().getText().toString().trim();
                String phonePattern = "^[+]?[0-9]{10,13}$";
                if(phoneNumber.isEmpty() || !phoneNumber.matches(phonePattern)) {
                    showError(inputPhoneNumber, "Phone number not valid");
                } else
                {
                    progressBar.setVisibility(View.VISIBLE);
                    hideError(inputPhoneNumber);
                    AddPhone(phoneNumber);
                }
            }
        });

        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestRuntimePermission();

            }
        });

    }

    private void requestRuntimePermission() {

        //Permission Granted
        if (ActivityCompat.checkSelfPermission(this, PERMISSION_SEND_SMS)
        == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Text Message Permission Granted", Toast.LENGTH_SHORT).show();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_SEND_SMS)) {

            //Permission denied
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This app requires Text Message permission for particular features to work as expected.")
                    .setTitle("Permission Required")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(NotificationActivity.this, new String[]{PERMISSION_SEND_SMS}, UNIQUE_REQUEST_CODE);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", (((dialog, which) -> dialog.dismiss())));

            builder.show();

        } else {

            //Request Permissions
            ActivityCompat.requestPermissions(this, new String[]{PERMISSION_SEND_SMS}, UNIQUE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == UNIQUE_REQUEST_CODE) {
            //allow - text msg
            if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Text Message Permission Granted", Toast.LENGTH_SHORT).show();

                //denied -text msg
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_SEND_SMS)) {

                //sends users to settings
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("This feature is unavailable because it requires permission that you have denied." +
                        "Please allow Text Message permission from settings to proceed.")
                        .setTitle("Permission Required")
                        .setCancelable(false)
                        .setNegativeButton("Cancel", (((dialog, which) -> dialog.dismiss())))
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);

                                dialog.dismiss();
                            }
                        });
                builder.show();
            } else {
                requestRuntimePermission();
            }

        }
    }

    //adds user's phone number
    private void AddPhone(String phoneNumber) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("phoneNumber", phoneNumber);

        mRef.child(mUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {

                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(NotificationActivity.this, "Phone Number Added", Toast.LENGTH_SHORT).show();
                inputPhoneNumber.getEditText().getText().clear();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(NotificationActivity.this, "Phone Number not added", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void showError(TextInputLayout input, String text) {
        input.setError(text);
        input.requestFocus();
    }

    private void hideError(TextInputLayout field) {
        field.setError(null);
    }

}