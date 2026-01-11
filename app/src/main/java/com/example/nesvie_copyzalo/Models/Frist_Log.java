package com.example.nesvie_copyzalo.Models;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nesvie_copyzalo.R;
import com.example.nesvie_copyzalo.Sign_in;
import com.example.nesvie_copyzalo.Sign_up;

public class Frist_Log extends AppCompatActivity {

    private Button btnSignIn;

    private Button btnSignUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_frist_log);


        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển sang SignInActivity
                Intent intent = new Intent(Frist_Log.this, Sign_in.class);
                startActivity(intent);
            }


        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển sang SignUpActivity
                Intent intent = new Intent(Frist_Log.this, Sign_up.class);
                startActivity(intent);
            }
        });


    }
}