package com.example.simplysortingsupplies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.simplysortingsupplies.Utills.Items;
import com.example.simplysortingsupplies.databinding.ActivityMainBinding;
import com.example.simplysortingsupplies.databinding.ActivityNotificationBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

public class MainActivity extends DrawerBaseActivity {

    ActivityMainBinding activityMainBinding;

    FloatingActionButton btnAddItem;

    FirebaseRecyclerOptions<Items> options;
    FirebaseRecyclerAdapter<Items, ItemsViewHolder> adapter;

    DatabaseReference mItemRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = activityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        allocateActivityTitle("");

        btnAddItem = findViewById(R.id.btnAddItem);
        recyclerView = findViewById(R.id.recylerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mItemRef = FirebaseDatabase.getInstance().getReference().child("Items");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        //add item
        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                startActivity(intent);
            }
        });

        //loads current items
        LoadItems("");
    }

    private void LoadItems(String s) {

        Query query = mItemRef.orderByChild("itemName").startAt(s).endAt(s + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Items>().setQuery(query, Items.class).build();
        adapter = new FirebaseRecyclerAdapter<Items, ItemsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ItemsViewHolder holder, int position, @NonNull Items model) {

                //filters list based on user
                if(getRef(position).getKey().toString().contains(mUser.getUid())) {
                    Picasso.get().load(model.getItemImage()).into(holder.itemImage);
                    holder.itemDisplayName.setText(model.getItemName());
                    holder.itemDisplayAmount.setText(model.getItemAmount());
                } else {
                    //hides items that aren't created by user
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                }


                //user selects an item
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, UpdateItemActivity.class);
                        intent.putExtra("itemKey", getRef(position).getKey().toString());
                        startActivity(intent);

                    }
                });

            }

            //item layout
            @NonNull
            @Override
            public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_item, parent, false);
                return new ItemsViewHolder(view);

            }

        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }


}