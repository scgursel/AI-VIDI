package com.example.testdb;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.testdb.ml.Model;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity { ////// REMEMBER TO CLOSE CAMERA WHEN DONE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    //
    // EARLIER ISSUE WAS WITH CAMERA PERMISSIONS. NOW FIXED
    // STILL GOTTA WORK ON PERMS, CURRENTLY REQUIRED TO ACCEPT PERMS THE FIRST TIME, HARD IF YOU ARE BLIND, CHECK PERMISSION OVERRIDES.
    private TextToSpeech narrator3,narrator;
    private String narration=("Kamerayı başlatmak için ekrana dokunun." +
            "Anasayfaya dönmek için anasayfa diyin." +
            "Kaydetmek için kaydetme işlemi diyin."+
            "Kayıtlara ulaşmak için kayıtlara bak diyin."+
            "Ayarlara ulaşmak için ayarlar diyin"+
            "Tekrar dinlemek için tekrar dinle diyin.");

    Button camera, gallery;
    ImageView imageView;

    TextView result;
    int imageH= 512;
    int imageW= 512;
    Bitmap imageBitmap;


    //
    //
    // SOMETHING WRONG WITH THIS cameraId=cameraManager.getCameraIdList()[0];
    // OR
    // THIS
    // cameraManager.openCamera(cameraId, stateCallbackForCamera, null);
    // cameraManager may be null? DUNNO IF INITIALIZATION IS FUCKED.



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classificationimport);

        ///////////////////////////////////////////////
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
        /////////////////////////////////////////////


        //region import from imageclassification

        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);

        //endregion

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




    public void classifyImage(Bitmap image){
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, imageW, imageH, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageW * imageH * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageW * imageH];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for(int i = 0; i < imageH; i ++){
                for(int j = 0; j < imageW; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            String[] classes = {"10", "100", "20","200","5","50"};

            for (int i = 0; i < confidences.length; i++) {
                System.out.println(classes[i]+"="+confidences[i]);
                Log.d("asd",classes[i]+"="+confidences[i]);
                Log.d("asd","Classification 2.fora girildi.");
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            result.setText(classes[maxPos]);
            int finalMaxPos = maxPos;
            narrator=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    if(i!=TextToSpeech.ERROR) {
                        Locale locale = new Locale("tr", "TR");
                        narrator.setLanguage(locale);
                        narrator.speak("Okunulan değer"+classes[finalMaxPos], TextToSpeech.QUEUE_FLUSH,null);
                    }
                }
            });
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
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
                    Log.d("asd","right before records page");
                    startActivity(new Intent(this,ViewActivity.class));
                    break;
                case "tekrar dinle":
                    narrator3.speak(narration, TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case "kaydetme işlemi":
                    saveRecord();
                    break;
                case "ayarlar":
                    startActivity(new Intent(this,SettingsActivity.class));
                    break;
                case "fotoğraf çek":
                case "Fotoğraf çek":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, 3);
                        } else {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                        }
                    }
                    break;
                case "Belge":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, 2);
                        } else {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                        }
                    }
                    break;
                default:
                    narrator3.speak("Anlaşılmadı, tekrar söyleyin.", TextToSpeech.QUEUE_FLUSH, null);
                    break;
            }
        }else{//request not handled.
            //narrator3.speak("", TextToSpeech.QUEUE_FLUSH, null);
        }

        //region classification import
        if(resultCode == RESULT_OK){
            if(requestCode == 3){
                Bundle bundle = data.getExtras();
                imageBitmap = (Bitmap) bundle.get("data");
                imageView.setImageBitmap(imageBitmap);
                detectText();
                //classifyImage();

            }
        }
        if(resultCode == 2){
            if(requestCode == 3){
                Bundle bundle = data.getExtras();
                imageBitmap = (Bitmap) bundle.get("data");
                imageView.setImageBitmap(imageBitmap);
                classifyImage(imageBitmap);

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
        //endregion



    }

    private void detectText(){
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> rs = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                StringBuilder rs = new StringBuilder();
                for (Text.TextBlock block: text.getTextBlocks()){
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockFramae = block.getBoundingBox();
                    for (Text.Line line : block.getLines()){
                        String lineText  = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect linRect = line.getBoundingBox();
                        for(Text.Element element : line.getElements()){
                            String elementText = element.getText();
                            rs.append(elementText);
                        }
                        result.setText(blockText);

                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CameraActivity.this, "fail to text recogg: "+ e.getMessage(),Toast.LENGTH_SHORT);
            }
        });

    }
// public void capturePhoto(){
// Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

// startActivityForResult(intent,2);
// }
}