package com.example.testdb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testdb.Db.Adapter;
import com.example.testdb.Db.SaveText;
import com.example.testdb.Db.SaveTextRepository;

import java.util.ArrayList;
import java.util.List;

public class ViewActivity extends AppCompatActivity {

    RecyclerView savetextrecyclerview;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<SaveText> saveTextArrayList;
    Adapter adapter;

    private TextToSpeech narrator;
    private String narration=("Kayıt görünütüleme ekranı açıldı." +
            "Kayıt eklemek için kayıt ekle diyin."+
            "Kameraya geçmek için kamera diyin." +
            "Ayarları değiştirmek için Ayarlar diyin." +
            "Tekrar dinlemek için tekrar dinle diyin.");

    SharedPreferences settingPrefs;
    String checkBoxForSwipeStatus;

    private GestureDetectorCompat detector;//needed to wire inner class to main activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        savetextrecyclerview=(RecyclerView)findViewById(R.id.savetextrecyclerview);
        layoutManager=new LinearLayoutManager(getApplicationContext());
        savetextrecyclerview.setLayoutManager(layoutManager);

        new LoadDataTask().execute();

        detector = new GestureDetectorCompat(this, new viewpageSwipeHandler()); //wiring of inner class /swipehandler

        //settingPrefs=getSharedPreferences(getString(R.string.checkBoxForSwipe),MODE_PRIVATE); //This always returns default, no good.
        settingPrefs= PreferenceManager.getDefaultSharedPreferences(this); //Remember to figure out why this works but the above doesnt.
        checkBoxForSwipeStatus=settingPrefs.getString(getString(R.string.checkBoxForSwipe),"empty");

    }

    class viewpageSwipeHandler extends GestureDetector.SimpleOnGestureListener{
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
                            startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
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


    class LoadDataTask extends AsyncTask<Void,Void,Void>{
        SaveTextRepository saveTextRepository;
        List<SaveText> saveTextList;


        @Override
        protected Void doInBackground(Void... voids) {
            saveTextList=saveTextRepository.getSaveText();
            saveTextArrayList=new ArrayList<>();

            for(int i=0;i<saveTextList.size();i++){
                saveTextArrayList.add(saveTextList.get(i));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            adapter = new Adapter(saveTextArrayList,ViewActivity.this);
            savetextrecyclerview.setAdapter(adapter);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            saveTextRepository=new SaveTextRepository(getApplicationContext());
        }
    }
    public void recordVoice(View view){
        //narrator.stop(); NEED A WAY TO INTERRUPT NARRATION WHEN ACTIONS HAPPEN. Causes crash? IDK.
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-tr");

        startActivityForResult(intent,10);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //data is gathered in result as arrayList then use result.get(0) to access the data.
        ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
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