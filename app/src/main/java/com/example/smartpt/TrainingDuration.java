package com.example.smartpt;

import static com.example.smartpt.TrainingTime.tTime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TrainingDuration extends AppCompatActivity {
    public static String tDuration;
    private RadioButton mor;
    private RadioButton noon;
    private RadioButton ev;
    private Button time;
    private FirebaseFirestore db;
    private FirebaseAuth uAuth;
    private String id;
    //    private String userIp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_duration);

        db = FirebaseFirestore.getInstance();
        //to get user email
        uAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = uAuth.getCurrentUser();
        id = curUser.getEmail();
//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        userIp= Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

        mor=findViewById(R.id.mor);
        noon=findViewById(R.id.noon);
        ev=findViewById(R.id.ev);
        time=findViewById(R.id.time);
        tDuration=30+"";


        time.setOnClickListener(new View.OnClickListener() {
            @Override



            public void onClick(View v) {



                Map<String,Object> user = new HashMap<>();
                user.put("TrainingDuration",tDuration);
                db.collection("userProfile").document(id).update(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //Toast.makeText(Goal.this,"successful",Toast.LENGTH_SHORT);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(Goal.this,"Faild",Toast.LENGTH_SHORT);

                    }
                });

                startActivity(new Intent(TrainingDuration.this, LoadPa.class));


            }

        });






    }

    public void rntn(View v){
        if(mor.isChecked()){
            tDuration=30+"";
        }
        else if(noon.isChecked()){
            tDuration=45+"";
        }
        else { tDuration= 60+"";}

    }



}


