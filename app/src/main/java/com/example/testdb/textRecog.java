package com.example.testdb;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Locale;

public class textRecog extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG="TextActivity";
    private TextToSpeech  narrator;
    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase cameraBridgeViewBase;
    private TextRecognizer textRecognizer;
    private ImageView captureButton;
    private TextView textView;
    private Bitmap bitmap=null;
    boolean isSecondpress=false;

    private String camOrRecog="camera";
    private BaseLoaderCallback mLoaderCallback =new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface
                    .SUCCESS) {
                Log.i(TAG, "OpenCv Is loaded");
                cameraBridgeViewBase.enableView();
            }
            super.onManagerConnected(status);
        }
    };

    public textRecog(){
        Log.i(TAG,"Instantiated new "+this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA=0;
        // if camera permission is not given it will ask for it on device
        if (ContextCompat.checkSelfPermission(textRecog.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(textRecog.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        setContentView(R.layout.activity_text);


        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.frameSurface);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCameraPermissionGranted();
        cameraBridgeViewBase.setCvCameraViewListener(this);

        textRecognizer= TextRecognition.getClient(new TextRecognizerOptions.Builder().build());
        textView = findViewById(R.id.textView);
        captureButton = findViewById(R.id.capture);
        textView.setVisibility(View.GONE);
        Intent intent=new Intent(this,CameraActivity.class);
        isSecondpress=false;

        captureButton.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    return true;
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    if (isSecondpress){
                        finish();
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

                    }
                    if (camOrRecog == "camera"){
                        textView.setVisibility(View.VISIBLE);
                        Mat a = mRgba.t();
                        Core.flip(a,mRgba,1);
                        a.release();

                        bitmap = Bitmap.createBitmap(mRgba.cols(),mRgba.rows(),Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(mRgba,bitmap);
                        cameraBridgeViewBase.disableView();
                        camOrRecog="recog";
                        InputImage inputImage = InputImage.fromBitmap(bitmap,180);
                        Task<Text> result = textRecognizer.process(inputImage)
                                .addOnSuccessListener(new OnSuccessListener<Text>() {
                                    @Override
                                    public void onSuccess(Text text) {
                                        textView.setText(text.getText());
                                        String filename = "Belge";
                                        SaveText saveText = new SaveText((String) textView.getText(), filename);
                                        SaveTextRepository saveTextRepository = new SaveTextRepository(getApplicationContext());
                                        saveTextRepository.InsertTask(saveText);
                                        Log.d(TAG, "onSuccess: "+ text);;
                                        narrator=new TextToSpeech(textRecog.this, new TextToSpeech.OnInitListener() {
                                            @Override
                                            public void onInit(int i) {
                                                if(i!=TextToSpeech.ERROR) {
                                                    Locale locale = new Locale("tr", "TR");
                                                    narrator.setLanguage(locale);
                                                    narrator.speak("Okutulan Belge"+textView.getText(), TextToSpeech.QUEUE_FLUSH,null);

                                                }
                                            }
                                        });
                                        setContentView(R.layout.activity_classificationimport);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        narrator=new TextToSpeech(textRecog.this, new TextToSpeech.OnInitListener() {
                                            @Override
                                            public void onInit(int i) {
                                                if(i!=TextToSpeech.ERROR) {
                                                    Locale locale = new Locale("tr", "TR");
                                                    narrator.setLanguage(locale);
                                                    narrator.speak("Tespit edilemedi tekrar deneyin "+textView.getText(), TextToSpeech.QUEUE_FLUSH,null);

                                                }
                                            }
                                        });
                                    }
                                });
                    }
                    return true;
                }

                return false;

            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            //if load success
            Log.d(TAG,"Opencv initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            //if not loaded
            Log.d(TAG,"Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mLoaderCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraBridgeViewBase.setCameraPermissionGranted();  // <------ THIS!!!
                } else {
                    // permission denied
                }
                return;
            }
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(cameraBridgeViewBase !=null){
            cameraBridgeViewBase.disableView();
        }

    }

    public void onCameraViewStarted(int width ,int height){
        mRgba=new Mat(height,width, CvType.CV_8UC4);
        mGray =new Mat(height,width,CvType.CV_8UC1);
    }
    public void onCameraViewStopped(){
        mRgba.release();
    }
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        mRgba=inputFrame.rgba();
        mGray=inputFrame.gray();
        Size newSize = new Size(400, 200);
        Mat fit = new Mat(newSize, CvType.CV_8UC4);
        Imgproc.resize(mRgba,fit,newSize);


        Imgproc.resize(fit,fit,mRgba.size());

        return fit;

    }

}