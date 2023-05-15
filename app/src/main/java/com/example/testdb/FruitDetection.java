package com.example.testdb;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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
import androidx.collection.CircularArray;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.testdb.ml.AndroidFruit;


import com.google.mlkit.vision.common.InputImage;

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
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FruitDetection extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG="ParaActivity";
    private TextToSpeech  narrator3;

    private Mat mRgba;
    private CameraBridgeViewBase mOpenCvCameraView;


    boolean isSecondpress=false;

    private ImageView captureButton;
    private TextView textView;
    private Bitmap bitmap=null;
    private ImageView currentImage;
    private TextView result;
    private String camOrRecog="camera";

    List<Integer> colors = Arrays.asList(
            Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY,
            Color.BLACK, Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    );
    Map<String, String> fruitMap = new HashMap<String, String>() {{
        put("apple","elma");
        put("banana","muz");
        put("orange","portakal");
        put("cucumber","salatalık");
        put("pineapple","ananas");
        put("carrot","havuç");
        put("lemon","limon");




    }};


    private BaseLoaderCallback mLoaderCallback =new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG,"OpenCv Is loaded");
                    mOpenCvCameraView.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);

                }
                break;
            }
        }
    };

    public FruitDetection(){
        Log.i(TAG,"Instantiated new "+this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        int MY_PERMISSIONS_REQUEST_CAMERA=0;
        // if camera permission is not given it will ask for it on device
        if (ContextCompat.checkSelfPermission(FruitDetection.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(FruitDetection.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        setContentView(R.layout.activity_detection_last);
        mOpenCvCameraView=(CameraBridgeViewBase) findViewById(R.id.frame_surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCameraPermissionGranted();
        mOpenCvCameraView.setCvCameraViewListener(this);
        textView = findViewById(R.id.text_view);
        captureButton = findViewById(R.id.cptr);
        currentImage = findViewById(R.id.crntImg);
        textView.setVisibility(View.GONE);

        Intent intent=new Intent(this,CameraActivity.class);
        isSecondpress=false;


        captureButton.setOnTouchListener(new View.OnTouchListener() {
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
                        Mat a = mRgba.t();
                        Core.flip(a,mRgba,1);
                        a.release();

                        bitmap = Bitmap.createBitmap(mRgba.cols(),mRgba.rows(),Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(mRgba,bitmap);
                        mOpenCvCameraView.disableView();
                        camOrRecog="recog";
                        getPredictions(bitmap);

                    }
                    return true;
                }
                return false;
            }
        });
    }
    private void getPredictions(Bitmap bitmap) {

        try {
            AndroidFruit model = AndroidFruit.newInstance(FruitDetection.this);

            TensorImage image = TensorImage.fromBitmap(bitmap);

            AndroidFruit.Outputs outputs = model.process(image);
            AndroidFruit.DetectionResult detectionResult = outputs.getDetectionResultList().get(0);

            List<AndroidFruit.DetectionResult> detectionResultList = outputs.getDetectionResultList();

            RectF location = detectionResult.getLocationAsRectF();
            String category = detectionResult.getCategoryAsString();
            float score = detectionResult.getScoreAsFloat();

            for (int i = 0; i<detectionResultList.size(); i++){
                Log.d(TAG, "getPredictions: "+ detectionResultList.get(i).getCategoryAsString()+"="+detectionResultList.get(i).getScoreAsFloat());
            }

            Log.d(TAG, "getPredictions: "+category+score);



            Bitmap mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            Canvas canvas = new Canvas(mutable);
            int h = mutable.getHeight();
            int w = mutable.getWidth();


            Paint paint = new Paint();
            paint.setTextSize(h/15f);
            paint.setStrokeWidth(h/85f);
            if (score > 0.4) {
                narrator3=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if(i!=TextToSpeech.ERROR) {
                            Locale locale = new Locale("tr", "TR");
                            narrator3.setLanguage(locale);
                            narrator3.speak(fruitMap.get(category), TextToSpeech.QUEUE_FLUSH,null);
                        }
                    }
                });
                paint.setColor(colors.get((int)Math.floor(Math.random()*10)));
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(location, paint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawText(
                        fruitMap.get(category),
                        location.top,
                        location.left,
                        paint
                );
            }
            else {
                narrator3=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if(i!=TextToSpeech.ERROR) {
                            Locale locale = new Locale("tr", "TR");
                            narrator3.setLanguage(locale);
                            narrator3.speak("tespit edilemedi, tekrar deneyin", TextToSpeech.QUEUE_FLUSH,null);
                        }
                    }
                });
            }
            isSecondpress=true;

            currentImage.setImageBitmap(mutable);

            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }





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
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }

    }

    public void onCameraViewStarted(int width ,int height){
        mRgba=new Mat(height,width, CvType.CV_8UC4);
    }
    public void onCameraViewStopped(){
        mRgba.release();
        mOpenCvCameraView.setVisibility(View.GONE);

    }
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        mRgba=inputFrame.rgba();

        Size newSize = new Size(400, 200);
        Mat fit = new Mat(newSize, CvType.CV_8UC4);
        Imgproc.resize(mRgba,fit,newSize);

        Imgproc.resize(fit,fit,mRgba.size());

        return mRgba;
    }
}