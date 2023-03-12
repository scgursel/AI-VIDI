package com.example.testdb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.example.testdb.Db.SaveText;
import com.example.testdb.Db.SaveTextRepository;

import java.util.ArrayList;
import java.util.Locale;

public class InsertActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SPEECH_INPUT=1000;
    private static final int REQUEST_CODE_SPEECH_INPUT1=10000;
    private static final int REQUEST_CODE_SPEECH_INPUT2=100000;
    EditText edt_text,edt_filename;
    ImageView btn_submit;
    ImageView mic_btn_file,mic_btn_text;
    String stext="",sfilename="";
    private TextToSpeech narrator;
    private String narration=("Kayıt ekranı açıldı." +
            "Oluşturduğunuz kayıdı kaydet için kaydet diyin"+
            "Kameraya geçmek için kamera diyin." +
            "Ayarları değiştirmek için Ayarlar diyin." +
            "Kayıtlara ulaşmak için kayıtlara bak diyin."+
            "Tekrar dinlemek için tekrar dinle diyin.");

    SharedPreferences settingPrefs;
    String checkBoxForSwipeStatus;

    private GestureDetectorCompat detector;//needed to wire inner class to main activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        edt_text=(EditText) findViewById(R.id.edt_text);
        edt_filename=(EditText) findViewById(R.id.edt_filename);
        btn_submit=(ImageView)findViewById(R.id.btn_submit);
        mic_btn_file=(ImageView)findViewById(R.id.mic_btn_file);
        mic_btn_text=(ImageView)findViewById(R.id.mic_btn_text);

        detector = new GestureDetectorCompat(this, new ınsertpageSwipeHandler()); //wiring of inner class /swipehandler

        //settingPrefs=getSharedPreferences(getString(R.string.checkBoxForSwipe),MODE_PRIVATE); //This always returns default, no good.
        settingPrefs= PreferenceManager.getDefaultSharedPreferences(this); //Remember to figure out why this works but the above doesnt.
        checkBoxForSwipeStatus=settingPrefs.getString(getString(R.string.checkBoxForSwipe),"empty");



        mic_btn_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak(mic_btn_text);
            }
        });

        mic_btn_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak(mic_btn_file);
            }
        });


        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(btn_submit);

            }
        });
        narrator=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=TextToSpeech.ERROR) {
                    //Locale.TURKISH isnt incorporated for some fucking reason.
                    //So need to define it then pass it to the narrator as a parameter.
                    Locale locale = new Locale("tr", "TR");
                    narrator.setLanguage(locale);
                    narrator.speak(narration, TextToSpeech.QUEUE_FLUSH,null);
                }
            }
        });


    }

    public boolean onTouchEvent(MotionEvent event) { //we need this to either let our inner class or another construct to handle the event.
        if(detector.onTouchEvent(event)){
            return true;
        }else{
            super.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }


    class ınsertpageSwipeHandler extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX= e2.getX()-e1.getX();
            float diffY= e2.getY()-e1.getY();
            float swipe_threshold=10;
            float swipe_speed_threshold=10;

            if(Math.abs(diffX)>Math.abs(diffY)){
                //horizontal swipe
                if (Math.abs(diffX)>swipe_threshold&&Math.abs(velocityX)>swipe_speed_threshold){
                    //valid swipe
                    if(diffX>0){
                        //right swipe
                        if(checkBoxForSwipeStatus.equals("True")) { //check to see if swipe setting is enabled, need to move this up and completely avoid the swipe checks if setting is disabled.
                            //narrator.speak("SAĞ TARAFA kaydırdınız.", TextToSpeech.QUEUE_FLUSH, null);
                            Toast.makeText(getApplicationContext(), "Right swipe", Toast.LENGTH_SHORT).show();//getAppContext because "this" refers to the object which may not be the correct context.
                            startActivity(new Intent(getApplicationContext(),ViewActivity.class));
                        }else{
                            Toast.makeText(getApplicationContext(), "Swipe disabled", Toast.LENGTH_SHORT).show();//getAppContext because "this" refers to the object which may not be the correct context.
                        }
                        //Toast.makeText(getApplicationContext(), "AAA", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(),checkBoxForSwipeStatus.toLowerCase(Locale.ROOT),Toast.LENGTH_LONG).show();
                    }else{
                        //left swipe
                        if(checkBoxForSwipeStatus.equals("True")) {//check to see if swipe setting is enabled, need to move this up and completely avoid the swipe checks if setting is disabled.
                            //narrator.speak("SOL TARAFA kaydırdınız.", TextToSpeech.QUEUE_FLUSH, null);
                            Toast.makeText(getApplicationContext(), "Left swipe", Toast.LENGTH_SHORT).show(); //getAppContext because "this" refers to the object which may not be the correct context.
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }else{
                            Toast.makeText(getApplicationContext(), "Swipe disabled", Toast.LENGTH_SHORT).show();//getAppContext because "this" refers to the object which may not be the correct context.
                        }
                        //Toast.makeText(getApplicationContext(), "AAA", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(),checkBoxForSwipeStatus,Toast.LENGTH_LONG).show();
                    }
                    return true;//this was a valid swipe and this inner class handled it.
                }else{
                    //not a valid swipe, let the super handle it.
                    return super.onFling(e1,e2,velocityX,velocityY);
                }
            }else{
                //vertical swipe
                //i dont give a fuck about vertical swipes. For now.

            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    private void speak(ImageView ımageView){

        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-tr");
        try {
            if(ımageView==mic_btn_file){
                startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT1);
            }
            else if(ımageView==mic_btn_text){
                startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT);
            }
            else{
                startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT2);
            }

        }catch (Exception e){
            Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case REQUEST_CODE_SPEECH_INPUT:{
                if(resultCode==RESULT_OK && null!=data){
                    ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    edt_text.setText(result.get(0));
                }
                break;
            }

            case REQUEST_CODE_SPEECH_INPUT1: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    edt_filename.setText(result.get(0));
                }
                break;
            }

            case REQUEST_CODE_SPEECH_INPUT2:{
                ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                switch (result.get(0)){
                    case "kamera":
                        startActivity(new Intent(this,CameraActivity.class));
                        break;
                    case "camera":
                        startActivity(new Intent(this,CameraActivity.class));
                        break;
                    case "ayarlar":
                        startActivity(new Intent(this,SettingsActivity.class));
                        break;
                    case "ana sayfa":
                        startActivity(new Intent(this,MainActivity.class));
                        break;
                    case "kayıt ekle":

                        startActivity(new Intent(this,InsertActivity.class));
                        break;
                    case "kayıtlara bak":

                        startActivity(new Intent(this,ViewActivity.class));
                        break;
                    case "kaydet":

                        sfilename=edt_filename.getText().toString().trim();
                        stext= edt_text.getText().toString().trim();

                        SaveText saveText=new SaveText(stext,sfilename);
                        SaveTextRepository saveTextRepository=new SaveTextRepository(getApplicationContext());
                        saveTextRepository.InsertTask(saveText);
                        break;
                    default:
                        narrator.speak("Anlaşılmadı, tekrar söyleyin.",TextToSpeech.QUEUE_FLUSH,null);
                        break;
                }
                break;
            }
        }
    }
}

