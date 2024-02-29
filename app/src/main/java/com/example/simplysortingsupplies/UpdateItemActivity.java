package com.example.simplysortingsupplies;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.simplysortingsupplies.databinding.ActivityMainBinding;
import com.example.simplysortingsupplies.databinding.ActivityUpdateItemBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateItemActivity extends DrawerBaseActivity {

    ActivityUpdateItemBinding activityUpdateItemBinding;

    CircleImageView updateItemImage;
    ImageView deleteItemImage;
    TextInputLayout updateItemName;
    EditText updateItemAmount;
    Button btnUpdateInventoryItem;
    DatabaseReference mItemRef;
    StorageReference StorageRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    public Uri photoUri;
    private ProgressBar progressBar;

    //user selects item image
    private final ActivityResultLauncher<Intent> launcher  = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    photoUri = result.getData().getData();
                    updateItemImage.setImageURI(photoUri);
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityUpdateItemBinding = activityUpdateItemBinding.inflate(getLayoutInflater());
        setContentView(activityUpdateItemBinding.getRoot());
        allocateActivityTitle("");

        String itemID=getIntent().getStringExtra("itemKey");

        updateItemImage = findViewById(R.id.updateItemImage);
        updateItemName = findViewById(R.id.updateItemName);
        updateItemAmount = findViewById(R.id.updateItemAmount);
        btnUpdateInventoryItem = findViewById(R.id.btnUpdateInventoryItem);
        deleteItemImage = findViewById(R.id.deleteItemImage);
        progressBar = findViewById(R.id.updateProgressBar);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mItemRef = FirebaseDatabase.getInstance().getReference().child("Items").child(itemID);
        StorageRef = FirebaseStorage.getInstance().getReference().child("ItemImage").child(itemID);

        //loads item current info
        LoadItem();

        //updates image of item
        updateItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                launcher.launch(intent);
            }
        });

        //delete item
        deleteItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteItem(itemID);
            }
        });

        //update item
        btnUpdateInventoryItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateData();
            }
        });
    }

    private void DeleteItem(String itemID) {
        progressBar.setVisibility(View.VISIBLE);
        mItemRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(UpdateItemActivity.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateItemActivity.this, MainActivity.class);
                    startActivity(intent);

                }
               else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(UpdateItemActivity.this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Loads item current info
    private void LoadItem() {

        mItemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    String itemImageUrl = snapshot.child("itemImage").getValue().toString();
                    String itemName = snapshot.child("itemName").getValue().toString();
                    String itemAmount = snapshot.child("itemAmount").getValue().toString();

                    Picasso.get().load(itemImageUrl).into(updateItemImage);
                    updateItemName.getEditText().setText(itemName);
                    updateItemAmount.setText(itemAmount);

                }
                else {

                    Toast.makeText(UpdateItemActivity.this, "Data does not exist", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(UpdateItemActivity.this, "" +error.getMessage().toString(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void UpdateData() {

        final String itemAmount = updateItemAmount.getText().toString();
        final String itemName = updateItemName.getEditText().getText().toString();

        //checks users input
        if(mUser == null) {
            Toast.makeText(this, "Something went wrong! User's details are not available at the moment", Toast.LENGTH_LONG).show();
        }
        else if (itemName.isEmpty()) {
            showError(updateItemName, "Item name is not valid");
        } else if (itemAmount.isEmpty()) {
            Toast.makeText(this, "Item amount is not valid", Toast.LENGTH_SHORT).show();
        }

        //photo not updated
        else if (photoUri == null) {

            progressBar.setVisibility(View.VISIBLE);

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("itemName", itemName);
            hashMap.put("itemAmount", itemAmount);

            mItemRef.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {

                    progressBar.setVisibility(View.INVISIBLE);

                    Intent intent = new Intent(UpdateItemActivity.this, MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(UpdateItemActivity.this, "Item Updated", Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(UpdateItemActivity.this, "Item Failed to Update", Toast.LENGTH_SHORT).show();

                }
            });

            //photo is updated
        } else {
            hideError(updateItemName);
            progressBar.setVisibility(View.VISIBLE);

            StorageRef.putFile(photoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        StorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("itemName", itemName);
                                hashMap.put("itemAmount", itemAmount);
                                hashMap.put("itemImage", uri.toString());

                                mItemRef.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {

                                        progressBar.setVisibility(View.INVISIBLE);
                                        Intent intent = new Intent(UpdateItemActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        Toast.makeText(UpdateItemActivity.this, "Item Added", Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        progressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(UpdateItemActivity.this, "Item Failed to Add", Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        });
                    }
                }
            });
        }

    }

    private void showError(TextInputLayout input, String text) {
        input.setError(text);
        input.requestFocus();
    }

    private void hideError(TextInputLayout field) {
        field.setError(null);
    }
}