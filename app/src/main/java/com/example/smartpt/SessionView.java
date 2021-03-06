package com.example.smartpt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class SessionView extends AppCompatActivity {

    Timer timer;
    VideoView v;
    ProgressDialog pd;
    private FirebaseFirestore db;
    private FirebaseAuth uAuth;
    private String id;
    private int week;
    private Double weekD;
    private int  FBindex ;
    private Double FBindexD;
//    private String userIp;
    private double set , Res;
    private int s, re, i,sIndex,counter;
    private TextView sets, instTxt,exerciseName,counterTxt,rest,timertxt,exeNum;
    private ImageView exist,buttonSpeaker;
    private Button nextbtn,skipbtn,pausebtn;
    private String inst, SessionNo, level,currDay,day,nextExercise, exName, videoLink,audioLink;
    String instArray[] = new String[5];
    String dayAr[];
    private int prog;

    private int end;
    private TextToSpeech mTTS;
    public boolean isSpeak;
    private ProgressBar progress_bar;
    AlertDialog.Builder builder;
    private TimerTask timerTask;
    private Double time;
    private MediaPlayer player,restAudio;
    public final static String shared="sharedPrefs";
    private int done;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String todaytDate;

    DBHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_view);
        DB=new DBHelper(this);

        timer=new Timer();
        prog=0;
        end=0;
        dayAr=new String[50];
        exName="";
        counter=getIntent().getIntExtra("counter",0);
        progress_bar= (ProgressBar)findViewById(R.id.progress_bar);
        updteProgressBar();
        sets=(TextView) findViewById(R.id.sets); //Done
        exerciseName=(TextView) findViewById(R.id.exerciseName);
        counterTxt=(TextView) findViewById(R.id.counter);
        instTxt=(TextView) findViewById(R.id.instTxt);
        rest=(TextView) findViewById(R.id.rest);
        v= (VideoView)findViewById(R.id.video);
        timertxt=(TextView)findViewById(R.id.timertxt);
        exeNum=(TextView)findViewById(R.id.exNum);
        nextbtn =(Button)findViewById(R.id.nextbtn);
        skipbtn =(Button)findViewById(R.id.skipbtn);
        pausebtn =(Button)findViewById(R.id.pausebtn);
        exist =(ImageView) findViewById(R.id.exist);
        builder= new AlertDialog.Builder(this);
        SessionNo=getIntent().getStringExtra("SessionNo");
        level =getIntent().getStringExtra("level");
        currDay=getIntent().getStringExtra("currDay");
        time= getIntent().getDoubleExtra("duration",-1);
        buttonSpeaker=findViewById(R.id.buttonSpeaker);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        todaytDate = dateFormat.format(calendar.getTime());


        SharedPreferences sharedPreferences = getSharedPreferences(shared,MODE_PRIVATE);
        done= sharedPreferences.getInt("sessionDone",0);
        //##########################################################################################
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.CANADA);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        buttonSpeaker.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        //############################################################################################

        retrieveExerciseName();


        updteProgressBar();


        i=0;
        sIndex=0;

        if(time==-1||time ==0){
            time=0.0;
            startTimer();
        }
        else{
            time=time;
            startTimer();
        }



//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        userIp = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        //to get user email
        uAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = uAuth.getCurrentUser();
        id = curUser.getEmail();

        //get data from database
        db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("userProfile").document(id).collection("WorkoutPlan").document(id);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {



                set=value.getDouble("sets");
                s=(int)set;
                sets.setText("0/"+s+"");


                Res=value.getDouble("rest");
                re=(int)Res;



            }
        });

        documentReference = db.collection("Progress").document(id).collection("index").document("weeks");
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {


                weekD= value.getDouble("week");
                week=(int)Math.round(weekD);
//                getDuration(week, currDay);


            }
        });



        skipbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prog=0;

                if(skipbtn.getText().equals("Finish")){
                    endSession(99);
                }
                else {
                    updteProgressBar();
                    nextExercise(-1);
                }
            }
        });
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(i+1==s){
//                    sets.setText("done");
                    i++;
                    sets.setText(i+"/"+s);
                    prog+=(100/s);
                    prog=0;
                    updteProgressBar();
                    if(skipbtn.getText().equals("Finish")){
                        endSession(99);
                    }
                    else {
                        nextExercise(re);
                    }
                }
                else{
                    sets.setText(i+1+"/"+s);
                    i=i+1;
//                    String sessionComp="Gooood you complete "+i+"set";
//                    mTTS.setSpeechRate(1+sessionComp.length()/200 );
//                    mTTS.speak(sessionComp, TextToSpeech.QUEUE_FLUSH, null);

                    if(prog<=90){
                        prog+=(100/s);
                        updteProgressBar();
//                        String audioUrl = "https://od.lk/s/NzVfMzI5OTA2NTJf/ttsMP3.com_VoiceText_2022-3-3_17_50_19.mp3";
//
//                                restAudio = new MediaPlayer();
//
//                                restAudio.setAudioStreamType(AudioManager.STREAM_MUSIC);
//
//
//
//                                try {
//                                    restAudio.setDataSource(audioUrl);
//                                    // below line is use to prepare
//                                    // and start our media player.
//                                    restAudio.prepare();
//                                    restAudio.start();
//
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
                        String sessionComp="Take A rest";
                        mTTS.setSpeechRate(1+sessionComp.length()/200 );
                        mTTS.speak(sessionComp, TextToSpeech.QUEUE_FLUSH, null);
                        new CountDownTimer(10000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                counterTxt.setVisibility(View.VISIBLE);
                                rest.setVisibility(View.VISIBLE);
                                nextbtn.setVisibility(View.INVISIBLE);
                                skipbtn.setVisibility(View.INVISIBLE);
                                pausebtn.setVisibility(View.INVISIBLE);
                                counterTxt.setText(String.valueOf(millisUntilFinished/1000));


                            }

                            @Override
                            public void onFinish() {
                                counterTxt.setVisibility(View.INVISIBLE);
                                rest.setVisibility(View.INVISIBLE);
                                nextbtn.setVisibility(View.VISIBLE);
                                skipbtn.setVisibility(View.VISIBLE);
                                pausebtn.setVisibility(View.VISIBLE);

                                String sessionComp="Gooood you complete "+i+"set";
                                mTTS.setSpeechRate(1+sessionComp.length()/200 );
                                mTTS.speak(sessionComp, TextToSpeech.QUEUE_FLUSH, null);


                            }
                        }.start();


                    }
                }
            }
        });

        exist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                builder.setTitle("").setTitle("Are you sure you want to end the session?").setMessage("By clicking Yes, you will start from the beginning.").setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                end=1;
                                endSession(0);

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                }).show();
            }
        });

        pausebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.setTitle("Pause the session").setMessage("Are you sure you want to pause the session and complete it later ?").setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                endSession(counter);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                }).show();

            }
        });


    }
    public void updteProgressBar(){

        progress_bar.setProgress(prog);

    }
    public void nextExercise(int re){


        SharedPreferences sharedPreferences = getSharedPreferences(shared,MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putString("duration",time+"");
        editor.apply();

        counter = counter+1;
        Map<String,Object> user = new HashMap<>();
        user.put("exerciseIndex",counter);
        user.put("duration",time);
        db.collection("Progress").document(id).collection("index").document("weeks").collection("week"+week).document("day"+currDay).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SessionView.this,"successful",Toast.LENGTH_SHORT);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SessionView.this,"Faild",Toast.LENGTH_SHORT);

            }
        });

        user.put("sets",i);
        db.collection("Progress").document(id).collection("index").document("weeks").collection("week"+week).document("day"+currDay).collection("progressDay"+currDay).document("exercise"+(counter-1)).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        saveProgress(exName,todaytDate,set);

        Intent intent= new Intent(this, StartSession.class);
        intent.putExtra("rest",re);
        intent.putExtra("restText","Rest");
        intent.putExtra("SessionNo",SessionNo);
        intent.putExtra("level",level);
        intent.putExtra("counter",counter);
        intent.putExtra("currDay",currDay);
        intent.putExtra("nextEx",nextExercise);
        intent.putExtra("week",week);
        intent.putExtra("duration",time);
        startActivity(intent);

    }
    public void endSession(int c){

        SharedPreferences sharedPreferences = getSharedPreferences(shared,MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putString("duration",time+"");
        editor.apply();

        c=c;


        Map<String,Object> user = new HashMap<>();
        user.put("exerciseIndex",c);
        if(c==0 && end==1){
            user.put("duration",0);
        }
        else {

            user.put("duration", time);
        }
        db.collection("Progress").document(id).collection("index").document("weeks").collection("week"+week).document("day"+currDay).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
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

        if(c==99){
            saveProgress(exName,todaytDate,set);

            editor.putInt("sessionDone",done+1);
            editor.apply();
            Intent intent= new Intent(this, feedback.class);
            intent.putExtra("SessionNo", SessionNo);
            intent.putExtra("level", level);
            intent.putExtra("counter", c);
            intent.putExtra("currDay",currDay);
            intent.putExtra("week",week);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(this, PlanView.class);
            intent.putExtra("SessionNo", SessionNo);
            intent.putExtra("level", level);
            intent.putExtra("counter", c);
            startActivity(intent);
        }

    }

    public void retreiveInstructions(String exName) throws IOException {
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        // creating python object
        PyObject pyObj= py.getModule("myscript"); // call the python file
        PyObject instructions = pyObj.callAttr("retreiveInstructions",exName); // call the  method in python
        PyObject video = pyObj.callAttr("retreiveVideo",exName); // call the  method in python
        PyObject audio = pyObj.callAttr("retreiveAudio",exName); // call the  method in python

        videoLink = video.toString();

        inst = instructions.toString();//retrieve  output
        instArray=inst.split("_");
        inst="";
        if(!instArray[0].equals("0")){
            inst=inst+"1. "+instArray[0]+"\n\n";
        }
        if(!instArray[1].equals("0")){
            inst=inst+"2. "+instArray[1]+"\n\n";
        }
        if(!instArray[2].equals("0")){
            inst=inst+"3. "+instArray[2]+"\n\n";
        }
        if(!instArray[3].equals("0")){
            inst=inst+"4. "+instArray[3]+"\n\n";
        }

        instTxt.setText(inst);

        instTxt.setMovementMethod(new ScrollingMovementMethod());

        Uri uri = Uri.parse(videoLink);
        v.setVideoURI(uri);
        MediaController mediaController= new MediaController(this);
        v.setMediaController(mediaController);
        mediaController.setAnchorView(v);
        v.start();
        v.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
//                pd.dismiss();
            }
        });
        v.setVideoURI(uri);
        v.setMediaController(new MediaController(this));

        v.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        audioLink=audio.toString();

        // initializing media player
        player = new MediaPlayer();
        MediaPlayer test =new MediaPlayer();
        // below line is use to set the audio
        // stream type for our media player.
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        test.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // below line is use to set our
        // url to our media player.
        player.setDataSource(audioLink);
        test.setDataSource(audioLink);
        // below line is use to prepare
        // and start our media player.
        player.prepare();
        test.prepare();
        //player.start();
        float pitch = 5;
        if (pitch < 0.1) pitch = 0.1f;
        float speed = 0.9f;
        if (speed < 0.1) speed = 0.1f;
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer player) {
                player.stop();
               // test.start();

            }

        });
        player.start();


    }



    public void retrieveExerciseName() {
//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        String userIp = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        //to get user email
        uAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = uAuth.getCurrentUser();
        id = curUser.getEmail();
        db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("userProfile").document(id).collection("WorkoutPlan").document(id).collection(id).document("day"+(currDay));
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                day = value.getString("plan");
                day = day.substring(2, day.length() - 3);
                dayAr = day.split("_");
//                exName= dayAr[counter];
                exeNum.setText("Exercise: "+(counter+1));

                exerciseName.setText(dayAr[counter]);
                try {
                    retreiveInstructions(dayAr[counter]);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(counter==dayAr.length-1 || counter==dayAr.length+1  || counter==dayAr.length){
                    nextExercise="finish";
                    skipbtn.setText("Finish");
//                    skipbtn.
//                    skipbtn.setVisibility(View.INVISIBLE);
                }
                else {
                    nextExercise = dayAr[counter + 1];
                }

            }
        });
    }

    private void startTimer(){



        timerTask=new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        time++;
                        timertxt.setText(getTimertxt());
                    }
                });



            }
        };
        timer.scheduleAtFixedRate(timerTask,0,1000);
    }

    private String getTimertxt() {

        int rounded = (int) Math.round(time);
        int second=((rounded % 86400)%3600)%60;
        int min=((rounded % 86400)%3600)/60;
        int hours=((rounded % 86400)/3600);

        return String.format("%02d",hours)+" : "+String.format("%02d",min)+" : "+String.format("%02d",second);
    }
    private void speak() {

        mTTS.setSpeechRate(0.8f);

        mTTS.speak(inst, TextToSpeech.QUEUE_FLUSH, null);

    }

    public void pause(long duration){
        mTTS.playSilence(duration, TextToSpeech.QUEUE_FLUSH, null);
    }



    public void testspeaker(View view){
        if (isSpeak==false){
            speak();
            isSpeak=true;

        }else {
            pause(10);
            isSpeak=false;

        }

    }

    public void saveProgress(String eName, String today, double sets){


       String eName1= exerciseName.getText().toString();
       String setS=i+"";

        uAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = uAuth.getCurrentUser();
        id = curUser.getEmail();

        Boolean check=DB.insertuserdata(eName1,today,setS,"10","Enter", id);

        if(check==true){
//            Toast.makeText(SessionView.this,"new entry",Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(SessionView.this,"NO",Toast.LENGTH_SHORT).show();

        }

    }

}