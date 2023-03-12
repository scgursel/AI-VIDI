package com.example.testdb;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testdb.Db.SaveTextRepository;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    SharedPreferences settingPrefs;
    String checkBoxForSwipeStatus;

    private GestureDetectorCompat detector;//needed to wire inner class to main activity

    private TextView textForResults;
    private TextToSpeech narrator;
    private String narration=("Uygulama açıldı." +
            "Aygıtın alt kısmında ortada bulunan butona basarak konuşabilirsiniz." +
            "Bu butonun yeri her sekmede sabittir." +
            "Kameraya geçmek için kamera diyin." +
            "Ayarları değiştirmek için Ayarlar diyin." +
            "Kayıtlara ulaşmak için kayıtlara bak diyin."+
            "Tekrar dinlemek için tekrar dinle diyin.");
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textForResults=findViewById(R.id.resultText);

        detector = new GestureDetectorCompat(this, new homepageSwipeHandler()); //wiring of inner class /swipehandler
        SaveTextRepository saveTextRepository=new SaveTextRepository(MainActivity.this);


        //settingPrefs=getSharedPreferences(getString(R.string.checkBoxForSwipe),MODE_PRIVATE); //This always returns default, no good.
        settingPrefs= PreferenceManager.getDefaultSharedPreferences(this); //Remember to figure out why this works but the above doesnt.
        checkBoxForSwipeStatus=settingPrefs.getString(getString(R.string.checkBoxForSwipe),"empty");

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

    //region swipes
    @Override
    public boolean onTouchEvent(MotionEvent event) { //we need this to either let our inner class or another construct to handle the event.
        if(detector.onTouchEvent(event)){
            return true;
        }else{
            super.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }


    class homepageSwipeHandler extends GestureDetector.SimpleOnGestureListener{
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
                            startActivity(new Intent(getApplicationContext(),CameraActivity.class));
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
                            startActivity(new Intent(getApplicationContext(),InsertActivity.class));
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


    //endregion

    public void recordVoice(View view){
        //narrator.stop(); NEED A WAY TO INTERRUPT NARRATION WHEN ACTIONS HAPPEN. Causes crash? IDK.
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-tr");

        startActivityForResult(intent,10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //data is gathered in result as arrayList then use result.get(0) to access the data.
        ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        textForResults.setText(result.get(0));
        super.onActivityResult(requestCode, resultCode, data);

        // NEED A WAY TO KILL THE CURRENT ACTIVITY BEFORE TRANSITIONING OR IT LL BE SHIT.
        // PERFORMANCE PROBLEM
        // MEMORY LEAK
        // NARRATOR OVERLAP
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
            case "tekrar dinle":
                narrator.speak(narration, TextToSpeech.QUEUE_FLUSH,null);
                break;
            default:
                narrator.speak("Anlaşılmadı, tekrar söyleyin.",TextToSpeech.QUEUE_FLUSH,null);
                break;
        }
    }
}