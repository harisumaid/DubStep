package com.example.dubstep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dubstep.Model.CartInfo;
import com.example.dubstep.Model.CartItem;
import com.example.dubstep.Model.OrderInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ReferralActivity extends AppCompatActivity {
    EditText referral;
    Button placeOrder;
    TextView promoCodeText;
    TextView cartTotal;
    TextView totalPrice;
    Double totalDiscountPrice;
    boolean promoUsed;
    String currPromo;
    ProgressDialog progressDialog;
    TextView discountOnPromo;
    FirebaseUser mUser;
    DatabaseReference mCart;
    CartInfo cartInfo;
    List<Object> cartItemList;
    DatabaseReference mOrder;
    long count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referral);
        cartItemList = new ArrayList<>();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mCart = FirebaseDatabase.getInstance().getReference("Cart").child(mUser.getUid());
        mOrder = FirebaseDatabase.getInstance().getReference("order").child("placed");
        referral = findViewById(R.id.edit_text_referral);
        placeOrder = findViewById(R.id.button_place_order);
        promoCodeText = findViewById(R.id.promocode_dicount_text);
        cartTotal = findViewById(R.id.cart_total_without_promo);
        totalPrice = findViewById(R.id.cart_total_with_promo);
        discountOnPromo = findViewById(R.id.dicount_on_promo_textview);
        double cartTotalPrice = Double.parseDouble(getIntent().getStringExtra("cartTotal"));
        totalDiscountPrice = cartTotalPrice;
        cartTotal.setText(String.format("Cart Price : ₹ %s",cartTotalPrice));
        totalPrice.setText(String.format("Total Price : ₹ %s",totalDiscountPrice));
        discountOnPromo.setVisibility(View.GONE);
        promoUsed = false;
        progressDialog = new ProgressDialog(this);
    }

    public void btnPlaceOrder(View view) {
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popUpView = inflater.inflate(R.layout.activity_confirm, null);

//        create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popUpView, width, height);


        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        popUpView.findViewById(R.id.button_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        popUpView.findViewById(R.id.button_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackageManager pm = getPackageManager();
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );
                try {
//                    TODO:  1. Create a order table
//                           2. Create orders page that shows list of orders
                    Date date = new Date();

                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    final String dateString = formatter.format(date);
                    mCart.child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {

                                cartInfo = snapshot.getValue(CartInfo.class);
                                mCart.child("Products").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()) {
                                            for(DataSnapshot snap : snapshot.getChildren()) {


                                                cartItemList.add(snap.getValue(CartItem.class));
                                                OrderInfo orderInfo = new OrderInfo(
                                                        dateString,
                                                        cartItemList,
                                                        cartInfo,
                                                        mUser.getUid()
                                                );
                                                mOrder.child(mUser.getUid()).setValue(orderInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Order layout is to be created
                                                        finish();
                                                    }
                                                });

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


//                    HashMap<String, Object> order = new HashMap<>();



                } catch (Exception e){
                    Toast.makeText(getBaseContext(),"Something is not right",Toast.LENGTH_SHORT).show();

                }



            }
        });


    }


    public void applyPromo(View view) {
//        search if promocode exists
        if(referral.getText().toString().equals("")){
            Toast.makeText(this,"Enter Some Promocode to check",Toast.LENGTH_SHORT).show();
            discountOnPromo.setVisibility(View.GONE);
            return;
        }
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );
        promoCodeText.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference()
                .child("promocode")
                .child(referral.getText().toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
//                            promocode exists
//                            now check if it is disabled or not
                            boolean active;
                            if(snapshot.child("active").getValue() != null){
                                active = Boolean.valueOf(snapshot.child("active").getValue().toString());
                            } else {
                                active = false;
                            }
                            long limit;
                            if (snapshot.child("limit").getValue() != null) {
                                limit = Long.parseLong(snapshot.child("limit").getValue().toString());
                            } else {
                                limit = 0;
                            }
                            if (snapshot.child("count").getValue()!= null) {
                                count = Long.parseLong(snapshot.child("count").getValue().toString());
                            } else {
                                count = 0;
                            }

                            if (!active) {
                                promoCodeText.setText("Promocode disabled");
                            } else if (limit <= count) {
                                promoCodeText.setText("Promocode using limit exhausted");
                            } else if(true){
//                            now check for user exists within it or not
                            if (!snapshot.child("users").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//                                user has not used that promo
                                double discount = Double.parseDouble(snapshot.child("discount").getValue().toString());
                                double cartTotalPrice = Double.parseDouble(getIntent().getStringExtra("cartTotal"));
                                promoCodeText.setText(String.format("Promocode Applied \n Discount : %s %% ", discount));
                                discountOnPromo.setText(String.format("Discount : - ₹ %s", (discount / 100.0 * cartTotalPrice)));
                                cartTotal.setText(String.format("Cart Price : ₹ %s", cartTotalPrice));
                                totalDiscountPrice = cartTotalPrice - (discount / 100.0 * cartTotalPrice);
                                totalPrice.setText(String.format("Total Price : ₹ %s", totalDiscountPrice));
                                promoUsed = true;
                                currPromo = referral.getText().toString();
                                discountOnPromo.setVisibility(View.VISIBLE);
                            } else {
                                discountOnPromo.setVisibility(View.GONE);
                                promoUsed = false;
                                promoCodeText.setText("Promocode can be used only once");
                                double cartTotalPrice = Double.parseDouble(getIntent().getStringExtra("cartTotal"));
                                totalDiscountPrice = cartTotalPrice;
                                cartTotal.setText(String.format("Cart Price : ₹ %s", cartTotalPrice));
                                totalPrice.setText(String.format("Total Price : ₹ %s", totalDiscountPrice));
                            }
                        }

                        } else{
                            discountOnPromo.setVisibility(View.GONE);
                            promoUsed = false;
                            promoCodeText.setText("Promocode doesn't exists");
                            double cartTotalPrice = Double.parseDouble(getIntent().getStringExtra("cartTotal"));
                            totalDiscountPrice = cartTotalPrice;
                            cartTotal.setText(String.format("Cart Price : ₹ %s",cartTotalPrice));
                            totalPrice.setText(String.format("Total Price : ₹ %s",totalDiscountPrice));
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
//        see if that user has used it or not
    }
}