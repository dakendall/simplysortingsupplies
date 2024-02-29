package com.example.simplysortingsupplies;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.simplysortingsupplies.databinding.ActivityAddItemBinding;
import com.example.simplysortingsupplies.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddItemActivity extends DrawerBaseActivity {

    ActivityAddItemBinding activityAddItemBinding;
    CircleImageView addItemImage;
    private TextInputLayout addItemName;
    private EditText addItemAmount;
    Button btnAddInventoryItem;
    FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mRef;
    private StorageReference StorageRef;
    public Uri photoUri;
    private ProgressBar progressBar;

    //user selects photo
    private final ActivityResultLauncher<Intent> launcher  = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        photoUri = result.getData().getData();
                        addItemImage.setImageURI(photoUri);
                    }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityAddItemBinding = activityAddItemBinding.inflate(getLayoutInflater());
        setContentView(activityAddItemBinding.getRoot());
        allocateActivityTitle("");

        addItemImage = findViewById(R.id.addItemImage);
        addItemName = findViewById(R.id.addItemName);
        addItemAmount = findViewById(R.id.addItemAmount);
        btnAddInventoryItem = findViewById(R.id.btnAddInventoryItem);
        progressBar = findViewById(R.id.addItemProgressBar);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child("Items");
        StorageRef = FirebaseStorage.getInstance().getReference().child("ItemImage");

        //user selects photo link
        addItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                launcher.launch(intent);
            }
        });

        //adds item button
        btnAddInventoryItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               SaveData();

            }
        });
    }

    private void SaveData() {

        final String itemName = addItemName.getEditText().getText().toString();
        final String itemAmount = addItemAmount.getText().toString();

        //checks user's input
        if(mUser == null) {
            Toast.makeText(this, "Something went wrong! User's details are not available at the moment", Toast.LENGTH_LONG).show();
        }
        else if (itemName.isEmpty()) {
            showError(addItemName, "Item name is not valid");
        } else if (itemAmount.isEmpty()) {
            Toast.makeText(this, "Item amount is not invalid", Toast.LENGTH_SHORT).show();
        }
        else if (photoUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        } else {

            //add item
            hideError(addItemName);
            progressBar.setVisibility(View.VISIBLE);

            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
            String strDate = formatter.format(date);

             StorageRef.child(mUser.getUid()+strDate).putFile(photoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        StorageRef.child(mUser.getUid()+strDate).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("itemName", itemName);
                                hashMap.put("itemAmount", itemAmount);
                                hashMap.put("itemImage", uri.toString());

                                mRef.child(mUser.getUid()+strDate).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {

                                        progressBar.setVisibility(View.INVISIBLE);
                                        Intent intent = new Intent(AddItemActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        Toast.makeText(AddItemActivity.this, "Item Added", Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        progressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(AddItemActivity.this, "Item Failed to Add", Toast.LENGTH_SHORT).show();
    
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