package com.example.testdb;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {
    private TextToSpeech  narrator3;
    private final String narration=(
            "Para okutmak için para " +
            "Sebze okutmak için sebze " +
            "kıyafet okutmak için kıyafet diyin" +
            "Anasayfaya dönmek için anasayfa diyin." +
            "Kaydetmek için kaydetme işlemi diyin."+
            "Kayıtlara ulaşmak için kayıtlara bak diyin."+
            "Ayarlara ulaşmak için ayarlar diyin"+
            "Tekrar dinlemek için tekrar dinle diyin."
    );
    private static final String TAG="CameraActivity";



    public CameraActivity(){
        Log.i(TAG,"Instantiated new "+this.getClass());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classificationimport);











        narrator3=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=TextToSpeech.ERROR) {
                    Locale locale = new Locale("tr", "TR");
                    narrator3.setLanguage(locale);
                    narrator3.speak(narration, TextToSpeech.QUEUE_FLUSH,null);
                }
            }
        });



    }

    public void onPause() {
        if(narrator3 !=null){
            narrator3.stop();
            narrator3.shutdown();
        }

        super.onPause();
    }

    public void recordVoice(View view){

        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-tr");
        startActivityForResult(intent,10);//10 for general purpose voice commands
    }

    public void recordVoiceForSaveRecord(){

        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-tr");
        startActivityForResult(intent,11);//11 for record saving db
    }

    public void saveRecord(){
        recordVoiceForSaveRecord();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //NEED TO ADD REQUEST CODE CHECKS HERE OR MAYHEM!
        ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);


        if(requestCode==10) {
            switch (result.get(0)) {
                case "ana sayfa":
                    startActivity(new Intent(this, MainActivity.class));
                    break;
                case "anasayfa":
                    startActivity(new Intent(this, MainActivity.class));
                    break;
                case "kayıtlara bak":
                    Log.d(TAG,"right before records page");
                    startActivity(new Intent(this,ViewActivity.class));
                    break;
                case "tekrar dinle":
                    narrator3.speak(narration, TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case "ayarlar":
                    startActivity(new Intent(this,SettingsActivity.class));
                    break;
                case "para":
                case "Para":
                    Intent intent1=new Intent(this,MoneyDetection.class);
                    intent1.addCategory(Intent.CATEGORY_HOME);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent1);
                    finish();
                    break;
                case "sebze":
                    Intent intent2=new Intent(this,FruitDetection.class);
                    intent2.addCategory(Intent.CATEGORY_HOME);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent2);
                    finish();
                    break;

                case "kıyafet":
                case "Kıyafet":

                    Intent intent3=new Intent(this,ClothesDetection.class);
                   intent3.addCategory(Intent.CATEGORY_HOME);
                   intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   startActivity(intent3);
                   finish();
                   break;
                case "belge":
                case "Belge":
                    Intent intent4=new Intent(this,textRecog.class);
                    intent4.addCategory(Intent.CATEGORY_HOME);
                    intent4.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent4);
                    finish();
                    break;
                default:
                    narrator3.speak("Anlaşılmadı, tekrar söyleyin.", TextToSpeech.QUEUE_FLUSH, null);
                    break;


            }
        }else{//request not handled.
            //narrator3.speak("", TextToSpeech.QUEUE_FLUSH, null);
        }
        super.onActivityResult(requestCode, resultCode, data);
        //endregion



    }
}