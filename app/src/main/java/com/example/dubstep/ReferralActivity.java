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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    long count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referral);

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
                    Intent intent = getIntent();
                    String message = intent.getStringExtra("message");
                    message+=String.format("Total Price: %s \n ",totalDiscountPrice);
                    if(promoUsed){
                        message+=String.format("Promocode : %s \n ",currPromo);
                    }
                    message+=String.format("Pincode: %s \n",intent.getStringExtra("pincode"));
                    message+=String.format("Address1: %s \n",intent.getStringExtra("address1"));
                    message+=String.format("Address2: %s \n",intent.getStringExtra("address2"));
                    message+=String.format("Address3: %s \n",intent.getStringExtra("address3"));
                    String number = intent.getStringExtra("wanumber");
                    PackageInfo info = pm.getPackageInfo("com.whatsapp",PackageManager.GET_META_DATA);
                    if (info!=null){
//                        change the phone no. to clients business whatsapp no.
                        String phoneNumberWithCountryCode = number;

                        final Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                        sendIntent.setData(Uri.parse(String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                phoneNumberWithCountryCode,
                                message)));
                        sendIntent.setPackage("com.whatsapp");
//                        1. cartClear
                        FirebaseDatabase.getInstance().getReference()
                                .child("Cart")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
//                              2. goto activity before cart
//                                add user to promocode used
                                if (promoUsed){
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("promocode")
                                            .child(currPromo)
                                            .child("users")
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(true);
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("promocode")
                                            .child(currPromo)
                                            .child("count")
                                            .setValue(++count);
                                }
                                progressDialog.dismiss();
                                startActivity(sendIntent);
                                finish();
                            }
                        });
                    }


                } catch (PackageManager.NameNotFoundException e){
                    Toast.makeText(getBaseContext(),"Whatsapp is not installed please install that first",Toast.LENGTH_SHORT).show();

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