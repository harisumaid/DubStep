package com.example.dubstep.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dubstep.CartMainActivity;
import com.example.dubstep.Interface.ItemClickListener;
import com.example.dubstep.LoginActivity;
import com.example.dubstep.MainActivity;
import com.example.dubstep.Model.FoodItem;
import com.example.dubstep.R;
import com.example.dubstep.ViewHolder.FoodItemViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class HomeFragment extends Fragment {

// TODO: 1. manage this fragment lifecycle properly
//          showing loading dialog on each call rather than just once when initialised
//       2. Insert ImageView in each element in recycler view for item image
    FirebaseAuth firebaseAuth;
    private DatabaseReference userref;
    private DatabaseReference foodref;
    private DatabaseReference cartref;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    private FloatingActionButton mCartButton;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        firebaseAuth = FirebaseAuth.getInstance();


        userref = FirebaseDatabase.getInstance().getReference("user").child(firebaseAuth.getCurrentUser().getUid());



        foodref = FirebaseDatabase.getInstance().getReference().child("food_menu");


        cartref = FirebaseDatabase.getInstance().getReference("Cart");
        progressDialog = new ProgressDialog(getContext());
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );
        loaderOnFoodMenuChange();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        mCartButton = view.findViewById(R.id.cart_btn);
        recyclerView = view.findViewById(R.id.main_recyclerview);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        mCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CartMainActivity.class);
                startActivity(intent);
            }
        });

    }


    private void loaderOnFoodMenuChange() {
        foodref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<FoodItem> options = new FirebaseRecyclerOptions.Builder<FoodItem>().setQuery(foodref, FoodItem.class).build();
        final FirebaseRecyclerAdapter<FoodItem, FoodItemViewHolder> adapter =
                new FirebaseRecyclerAdapter<FoodItem, FoodItemViewHolder>(options) {

                    private ItemClickListener listener;

                    @Override
                    protected void onBindViewHolder(@NonNull final FoodItemViewHolder holder, final int position, @NonNull FoodItem model) {

                        holder.mFoodItemName.setText(model.getName());
                        holder.mFoodItemPrice.setText("Price: \u20B9 " + model.getBase_price());
                        holder.mAddToCart.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                holder.mAddToCart.setEnabled(false);
                                addToCart(getRef(position).getKey());
                                //Toast.makeText(MainActivity.this,getRef(position).getKey(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item_layout, parent, false);
                        FoodItemViewHolder holder = new FoodItemViewHolder(view);
                        return holder;
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }


    public void addToCart(final String ref) {
        DatabaseReference foodItemRef = foodref.child(ref);
        foodItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FoodItem addedItem = dataSnapshot.getValue(FoodItem.class);
                final HashMap<String, Object> cartMap = new HashMap<>();
                cartMap.put("Name", addedItem.getName().toString());
                cartMap.put("Price", addedItem.getBase_price().toString());
                cartMap.put("Quantity", "1");
                cartMap.put("Product_ID", ref);

                cartref.child(firebaseAuth.getCurrentUser().getUid().toString()).child("Products")
                        .child(ref)
                        .updateChildren(cartMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getContext(), "Added to Cart", Toast.LENGTH_SHORT).show();
                            }
                        });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
