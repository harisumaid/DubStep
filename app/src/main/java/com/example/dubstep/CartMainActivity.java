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

// TODO: 1. change place order button to Continue button that takes to new activity
//       2. Create a new activity where we have a editext and submit button
//       3. Editext for promocode and submit button for placing order
//       4. Submit Button should open a popup(Dialog) that askes for final confirmation
//       5. On yes send the message to whatsapp.

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
                Intent intent = new Intent(CartMainActivity.this, SelectAddressActivity.class);
                intent.putExtra("message",createMessage());
                intent.putExtra("wanumber","+916371830551");
                startActivity(intent);
//
//
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
        mDelivery = findViewById(R.id.DdeliveryTextView);

        //int cartTotal = 0;
        //double discount = 0;


        mCartRef.child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int cartTotal = 0;
                myOrderMessage = "";
                int index = 0;
//              TODO: Delivery Charge addition

                final double deliveryCharge = 50;
                if(dataSnapshot.exists()){
                    mplaceOrder.setClickable(true);
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
                        mCartTotal.setText("Cart Total : \u20B9 "+cartTotal);
                        userref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                mDelivery.setText("Delivery Charge : \u20B9 "+deliveryCharge);

                                double TotalPrice = finalCartTotal + deliveryCharge;

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
                else{
                    mplaceOrder.setClickable(false);
                    mCartTotal.setText("Cart Total : ₹ ---");
                    mPriceTotal.setText("Total Price : ₹ --");
                    mDelivery.setText("Delivery Charge : ₹ --");
                }
                myOrderMessage += "Total Price : \u20B9 "+(cartTotal+deliveryCharge)+"\n";

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
