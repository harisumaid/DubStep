package com.example.dubstep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.dubstep.Model.UserAddress;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SelectAddressActivity extends AppCompatActivity {

    TextView emptyAddressTextView;
    public TextView pincode ;
    public TextView address1;
    public TextView address2;
    public TextView address3;
    public MaterialCardView addressCard;
    public ExtendedFloatingActionButton setAddress;
    public ExtendedFloatingActionButton continueOrderBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address);
        emptyAddressTextView = findViewById(R.id.address_empty_textView);
        FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
        pincode = findViewById(R.id.pincode_textview);
        address1 = findViewById(R.id.address1_textView);
        address2 = findViewById(R.id.address2_textView);
        address3 = findViewById(R.id.address3_textView);
        addressCard = findViewById(R.id.show_address_card);
        setAddress = findViewById(R.id.set_address_btn);
        continueOrderBtn = findViewById(R.id.continueOrder);


        final Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("user_address")
                .child(mAuth.getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    setAddress.setVisibility(View.VISIBLE);
                    addressCard.setVisibility(View.GONE);
                    emptyAddressTextView.setVisibility(View.VISIBLE);
                    continueOrderBtn.setVisibility(View.GONE);
                } else {
                    continueOrderBtn.setVisibility(View.VISIBLE);
                    setAddress.setVisibility(View.GONE);
                    addressCard.setVisibility(View.VISIBLE);
                    emptyAddressTextView.setVisibility(View.GONE);
                    UserAddress address = snapshot.getValue(UserAddress.class);
                    pincode.setText(address.getPincode());
                    address1.setText(address.getAddress1());
                    address2.setText(address.getAddress2());
                    address3.setText(address.getAddress3());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void changeAddress(View view) {
        Intent intent = new Intent(SelectAddressActivity.this, AddAddressActivity.class);
        startActivity(intent);
    }

    public void ContinueOrder(View view) {
//        get all addresses and put in intent
        Intent intent = new Intent();
        intent.putExtra("pincode",pincode.getText());
        intent.putExtra("address1",address1.getText());
        intent.putExtra("address2",address2.getText());
        intent.putExtra("address3",address3.getText());

    }
}