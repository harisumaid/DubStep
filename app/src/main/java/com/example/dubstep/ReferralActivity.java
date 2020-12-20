package com.example.dubstep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ReferralActivity extends AppCompatActivity {
    EditText referral;
    Button placeOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referral);

        referral = findViewById(R.id.edit_text_referral);
        placeOrder = findViewById(R.id.button_place_order);



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
                try {
                    Intent intent = getIntent();
                    String message = intent.getStringExtra("message");
                    message+=String.format("Pincode: %s \n",intent.getStringExtra("pincode"));
                    message+=String.format("Address1: %s \n",intent.getStringExtra("address1"));
                    message+=String.format("Address2: %s \n",intent.getStringExtra("address2"));
                    message+=String.format("Address3: %s \n",intent.getStringExtra("address3"));
                    String number = intent.getStringExtra("wanumber");
                    PackageInfo info = pm.getPackageInfo("com.whatsapp",PackageManager.GET_META_DATA);
                    if (info!=null){
//                        TODO: change the phone no. to clients business whatsapp no.
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


}