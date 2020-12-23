package com.example.dubstep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dubstep.ViewHolder.ThankYouActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Splash_Screen extends AppCompatActivity {

    TextView dubstep, dabba;
    ImageView logo;
    private FirebaseAuth mAuth;

    private static int SPLASH_SCREEN = 2500; //2.5 secs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash__screen);

        dubstep = findViewById(R.id.dubstepText);
        dabba = findViewById(R.id.dabbaText);
        logo = findViewById(R.id.logoImage);
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(currentUser != null) {
                    if (currentUser.isEmailVerified()){
                        startActivity(new Intent(Splash_Screen.this, MainActivity.class));
                    } else {
                        startActivity(new Intent(Splash_Screen.this, LoginActivity.class));
                    }
                } else {
                    startActivity(new Intent(Splash_Screen.this, LoginActivity.class));
                }
                finish();
            }
        },SPLASH_SCREEN);
    }
}
