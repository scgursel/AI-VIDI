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
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testdb.ViewActivity;

import java.util.ArrayList;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {


    SharedPreferences settingPrefs;
    String checkBoxForSwipeStatus;

    private GestureDetectorCompat detector;//needed to wire inner class to main activity

    private SharedPreferences settingsSharedPrefs;
    private SharedPreferences.Editor settingsSharedPrefsEditor;
    private CheckBox swipeCb;
    private TextView textForResults;
    private TextToSpeech narrator2;
    private String narration=("Ayarlar sekmesi açıldı." +
            "Kaydırma özelliğini açmak için kaydırmayı aç, kapatmak için kaydırmatı kapat diyin." +
            "Kamera sekmesine geçmek için kamera diyin"+
            "Anasayfaya dönmek için anasayfa diyin." +
            "Tekrar dinlemek için tekrar dinle diyin.");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        textForResults=findViewById(R.id.resultText2);
        swipeCb=findViewById(R.id.swipeCheckBox);
        //region sharedPref
        settingsSharedPrefs= PreferenceManager.getDefaultSharedPreferences(this);
        settingsSharedPrefsEditor=settingsSharedPrefs.edit();
        checkSettingPreferences();//need to call this immediately after shared prefs object is declared.
        //endregion

        detector = new GestureDetectorCompat(this, new settingsSwipeHandler()); //wiring of inner class /swipehandler

        //settingPrefs=getSharedPreferences(getString(R.string.checkBoxForSwipe),MODE_PRIVATE); //This always returns default, no good.
        settingPrefs= PreferenceManager.getDefaultSharedPreferences(this); //Remember to figure out why this works but the above doesnt.
        checkBoxForSwipeStatus=settingPrefs.getString(getString(R.string.checkBoxForSwipe),"empty");



        narrator2=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=TextToSpeech.ERROR) {
                    Locale locale = new Locale("tr", "TR");
                    narrator2.setLanguage(locale);
                    narrator2.speak(narration, TextToSpeech.QUEUE_FLUSH,null);
                }
            }
        });
    }

    public void onPause() {
        if(narrator2 !=null){
            narrator2.stop();
            narrator2.shutdown();
        }
        super.onPause();
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


    class settingsSwipeHandler extends GestureDetector.SimpleOnGestureListener{
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
                            startActivity(new Intent(getApplicationContext(),CameraActivity.class));
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

    private void checkSettingPreferences(){//checks for the saved settings and sets them accordingly.
        String checkBoxForSwipe=settingsSharedPrefs.getString(getString(R.string.checkBoxForSwipe),"False");
        if(checkBoxForSwipe.equals("True")){
            swipeCb.setChecked(true);
        }else{
            swipeCb.setChecked(false);
        }
    }

    public void recordVoice(View view){

        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-tr");

        startActivityForResult(intent,10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //NEED TO ADD REQUEST CODE CHECKS HERE OR MAYHEM!
        ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        textForResults.setText(result.get(0));
        super.onActivityResult(requestCode, resultCode, data);


        switch (result.get(0)){
            case "ana sayfa":
                startActivity(new Intent(this,MainActivity.class));
                break;
            case "anasayfa":
                startActivity(new Intent(this,MainActivity.class));
                break;
            case "kamera":
                startActivity(new Intent(this,CameraActivity.class));
                break;
            case "camera":
                startActivity(new Intent(this,CameraActivity.class));
                break;
            case "tekrar dinle":
                narrator2.speak(narration, TextToSpeech.QUEUE_FLUSH,null);
                break;
            case "kaydırmayı aç":
                //NEEDED BECAUSE THE PREF VALUE IS ONLY TAKEN DURING ONCREATE
                checkSettingPreferences();
                settingPrefs= PreferenceManager.getDefaultSharedPreferences(this); //Remember to figure out why this works but the above doesnt.
                checkBoxForSwipeStatus=settingPrefs.getString(getString(R.string.checkBoxForSwipe),"empty");
                //
                swipeCb.setChecked(true);
                settingsSharedPrefsEditor.putString(getString(R.string.checkBoxForSwipe),"True");
                settingsSharedPrefsEditor.clear();
                //settingsSharedPrefsEditor.apply();
                settingsSharedPrefsEditor.commit();
                Toast.makeText(getApplicationContext(),"Preferences passed",Toast.LENGTH_LONG).show();
                narrator2.speak("Kaydırma özelliği açıldı.", TextToSpeech.QUEUE_FLUSH,null);
                break;
            case "kaydırmayı kapat":
                //NEEDED BECAUSE THE PREF VALUE IS ONLY TAKEN DURING ONCREATE
                checkSettingPreferences();
                settingPrefs= PreferenceManager.getDefaultSharedPreferences(this); //Remember to figure out why this works but the above doesnt.
                checkBoxForSwipeStatus=settingPrefs.getString(getString(R.string.checkBoxForSwipe),"empty");
                //
                swipeCb.setChecked(false);
                settingsSharedPrefsEditor.putString(getString(R.string.checkBoxForSwipe),"False");
                settingsSharedPrefsEditor.clear();
                settingsSharedPrefsEditor.commit();
                Toast.makeText(getApplicationContext(),"Preferences passed",Toast.LENGTH_LONG).show();
                narrator2.speak("Kaydırma özelliği kapatıldı.", TextToSpeech.QUEUE_FLUSH,null);
                break;
            case "kayıtlara bak":
                startActivity(new Intent(this, ViewActivity.class));
                break;
            case "kayıt ekle":
                startActivity(new Intent(this, InsertActivity.class));
                break;
            default:
                narrator2.speak("Anlaşılmadı, tekrar söyleyin.",TextToSpeech.QUEUE_FLUSH,null);
                break;
        }
    }
}