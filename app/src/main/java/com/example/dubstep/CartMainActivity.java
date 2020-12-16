package com.example.dubstep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dubstep.Model.CartInfo;
import com.example.dubstep.Model.CartItem;
import com.example.dubstep.Model.User;
import com.example.dubstep.ViewHolder.CartItemsAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class CartMainActivity extends AppCompatActivity {


    CartItemsAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MaterialButton mplaceOrder;
    private TextView mPriceTotal;
    private TextView mCartTotal;
    private String myOrderMessage;
    private TextView mDelivery;
    private DatabaseReference userref;
    private DatabaseReference mCartRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_main);

        firebaseAuth = FirebaseAuth.getInstance();
        userref = FirebaseDatabase.getInstance().getReference("user").child(firebaseAuth.getCurrentUser().getUid().toString());
        mCartRef = FirebaseDatabase.getInstance().getReference("Cart").child(firebaseAuth.getCurrentUser().getUid().toString());

        mplaceOrder = findViewById(R.id.btn_place_order);

        setUpTotals();
        setUpRecycler();
        mplaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageManager pm = getPackageManager();
                try {
                    PackageInfo info = pm.getPackageInfo("com.whatsapp",PackageManager.GET_META_DATA);
                    if (info!=null){
//                        TODO: change the phone no. to clients business whatsapp no.
                        String phoneNumberWithCountryCode = "+919853386480";
                        String message = createMessage();
                        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                        sendIntent.setData(Uri.parse(String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                phoneNumberWithCountryCode,
                                message)));
                        sendIntent.setPackage("com.whatsapp");
                        startActivity(sendIntent);

                    }


                } catch (PackageManager.NameNotFoundException e){
                    Toast.makeText(getBaseContext(),"Whatsapp is not installed please install that first",Toast.LENGTH_SHORT).show();

                }
//                final Intent intent = new Intent(CartMainActivity.this,MapsActivity.class);
//                intent.putExtra("UID",firebaseAuth.getCurrentUser().getUid());
//
//                userref.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        user u = dataSnapshot.getValue(user.class);
//                        intent.putExtra("PhoneNumber", u.PhoneNumber);
//
//                        mCartRef.child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                CartInfo cartInfo = dataSnapshot.getValue(CartInfo.class);
//                                intent.putExtra("cartTotal", Double.toString(cartInfo.getCartTotal()) );
//
//                                startActivity(intent);
//
//                                //Toast.makeText(CartMainActivity.this,"Cart Total : "+cartInfo.getCartTotal(),Toast.LENGTH_SHORT).show();
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        });
//
//                        //startActivity(intent);
//                        //Toast.makeText(CartMainActivity.this,"Phone Number : "+u.PhoneNumber,Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
            }
        });
    }

    private String createMessage() {
        return myOrderMessage;
    }

    private void setUpTotals() {
        final ProgressDialog progressDialog = new ProgressDialog(CartMainActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );

        mPriceTotal = findViewById(R.id.total_price_text_view);
        mCartTotal = findViewById(R.id.cart_total_textView);
//        TODO: use R.id.DdiscountTextView as R.id.DdeliveryTextView
        mDelivery = findViewById(R.id.DdeliveryTextView);

        //int cartTotal = 0;
        //double discount = 0;


        mCartRef.child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int cartTotal = 0;
                myOrderMessage = "";
                int index = 0;
                if(dataSnapshot.exists()){
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        index++;
                        CartItem item = snap.getValue(CartItem.class);
                        cartTotal += (Integer.parseInt(item.getPrice()) * Integer.parseInt(item.getQuantity()));
                        myOrderMessage = myOrderMessage +
                                String.format(
                                        "Item %s : %s , Qty : %s \n",
                                        index,
                                        item.getName(),
                                        item.getQuantity()
                                );
                        final int finalCartTotal = cartTotal;
                        userref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                TODO: Delivery Charge addition
                                double deliveryCharge = 0;

                                mDelivery.setText("Delivery Charge : \u20B9 "+deliveryCharge);

                                double TotalPrice = finalCartTotal -deliveryCharge;

                                mPriceTotal.setText("Total Price : \u20B9 "+TotalPrice);

                                HashMap<String,Object> cartInfo = new HashMap<>();
                                cartInfo.put("CartItemsTotal",finalCartTotal);
                                cartInfo.put("Delivery",deliveryCharge);
                                cartInfo.put("CartTotal",TotalPrice);

                                mCartRef.child("Info").setValue(cartInfo);


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }

                mCartTotal.setText("CART TOTAL : \u20B9 "+cartTotal);
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    private void setUpRecycler() {

        recyclerView = findViewById(R.id.cart_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);



        FirebaseRecyclerOptions<CartItem> options = new FirebaseRecyclerOptions.Builder<CartItem>()
                .setQuery(mCartRef.child("Products"),CartItem.class)
                .build();

        adapter = new CartItemsAdapter(options);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new CartItemsAdapter.OnItemClickListener() {
            @Override
            public void onItemDelete(String PID, int position) {
                mCartRef.child("Products").child(PID).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    Toast.makeText(CartMainActivity.this,"ITEM REMOVED", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        adapter.setOnValueChangeListener(new CartItemsAdapter.OnValueChangeListener() {
            @Override
            public void onQuantityChange(String PID, int quantity) {

                String Qty = Integer.toString(quantity);
                mCartRef.child("Products").child(PID).child("Quantity").setValue(Qty)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    Toast.makeText(CartMainActivity.this, "Quantity Updated", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(CartMainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }
}
