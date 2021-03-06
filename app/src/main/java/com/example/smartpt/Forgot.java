package com.example.smartpt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Forgot extends AppCompatActivity {

    private FirebaseAuth uAuth;
    private EditText email;
    private TextView reset;
    private ProgressBar prog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        uAuth= FirebaseAuth.getInstance();
        email=(EditText) findViewById(R.id.resetEmail);
        reset=(TextView) findViewById(R.id.passReset);
        prog=(ProgressBar) findViewById(R.id.resetprog);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });


    }

    private void resetPassword() {
        String mail= email.getText().toString().trim();

        if(mail.isEmpty()){
            email.setError("Email cannot be empty");
            email.requestFocus();
            return;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            email.setError("invalid email format");
            email.requestFocus();
            return;
        }
        prog.setVisibility(View.VISIBLE);
        uAuth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(Forgot.this,"if your email exist, you will have a message to reset your password",Toast.LENGTH_LONG).show();
                prog.setVisibility(View.INVISIBLE);
            }
        });
    }
}