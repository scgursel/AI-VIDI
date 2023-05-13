package com.example.testdb;

import android.Manifest;
import android.app.Activity;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.testdb.ml.Android2;


import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognizer;

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
import java.util.List;
import java.util.Locale;

public class ClothesDetection extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG="ParaActivity";
    private TextToSpeech  narrator3;

    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpenCvCameraView;



    private TextRecognizer textRecognizer;
    private ImageView captureButton;
    private TextView textView;
    private Bitmap bitmap=null;
    private ImageView currentImage;
    private TextView result;
    private String camOrRecog="camera";
    // deneme
    Paint paint = new Paint();
    Button btn;
    List<Integer> colors = Arrays.asList(
            Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY,
            Color.BLACK, Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    );
    List<String> labels;
    @NonNull Android2 model;
    ImageProcessor imageProcessor = new ImageProcessor.Builder()
            .add(new ResizeOp(320, 320, ResizeOp.ResizeMethod.BILINEAR))
            .build();



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

    public ClothesDetection(){
        Log.i(TAG,"Instantiated new "+this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            model = Android2.newInstance(this);
        } catch (IOException e) {
            Log.d(TAG, "onCreate: model init failed");
            throw new RuntimeException(e);
        }
        try {
            labels = FileUtil.loadLabels(this, "label.txt");
        } catch (IOException e) {
            Log.d(TAG, "onCreate: .txt init failed");

            throw new RuntimeException(e);
        }

        int MY_PERMISSIONS_REQUEST_CAMERA=0;
        // if camera permission is not given it will ask for it on device
        if (ContextCompat.checkSelfPermission(ClothesDetection.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(ClothesDetection.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
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

        result = findViewById(R.id.resultDetection);

        captureButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    return true;
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    if (camOrRecog == "camera"){
                        Mat a = mRgba.t();
                        Core.flip(a,mRgba,1);
                        a.release();

                        bitmap = Bitmap.createBitmap(mRgba.cols(),mRgba.rows(),Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(mRgba,bitmap);
                        mOpenCvCameraView.disableView();
                        camOrRecog="recog";
                        InputImage inputImage = InputImage.fromBitmap(bitmap,0);
                        getPredictions(bitmap);

                    }
                    return true;
                }
                return false;
            }
        });
    }
    private void getPredictions(Bitmap bitmap) {
        TensorImage image = TensorImage.fromBitmap(bitmap);
        image = imageProcessor.process(image);



        Android2.Outputs outputs = model.process(image);
        float[] locations = outputs.getCategoryAsTensorBuffer().getFloatArray();
        float[] classes = outputs.getCategoryAsTensorBuffer().getFloatArray();
        float[] scores = outputs.getScoreAsTensorBuffer().getFloatArray();
        float[] numberOfDetections = outputs.getNumberOfDetectionsAsTensorBuffer().getFloatArray();
        @NonNull List<Android2.DetectionResult> detectionResultList = outputs.getDetectionResultList();

        Android2.DetectionResult detectionResult = outputs.getDetectionResultList().get(0);
        float score = detectionResult.getScoreAsFloat();
        RectF location = detectionResult.getLocationAsRectF();
        String category = detectionResult.getCategoryAsString();

        Log.d(TAG, "getPredictions: "+category+score);

        for (int i=0; i<detectionResultList.size(); i++){
            Log.d(TAG, "getPredictions: "+ detectionResultList.get(i).getCategoryAsString()+"="+detectionResultList.get(i).getScoreAsFloat());
        }
        Bitmap mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutable);
        int h = mutable.getHeight();
        int w = mutable.getWidth();


        Paint paint = new Paint();
        paint.setTextSize(h/15f);
        paint.setStrokeWidth(h/85f);
        for (int index = 0; index < scores.length; index++) {
            float fl = scores[index];
            if (fl > 0.2) {
                narrator3=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if(i!=TextToSpeech.ERROR) {
                            Locale locale = new Locale("tr", "TR");
                            narrator3.setLanguage(locale);
                            narrator3.speak(category, TextToSpeech.QUEUE_FLUSH,null);
                        }
                    }
                });


                result.setText(labels.get((int) classes[index]));
                int x = index * 4;
                paint.setColor(colors.get(index));
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(new RectF(
                        locations[x+1] * w,
                        locations[x] * h,
                        locations[x+3] * w,
                        locations[x+2] * h
                ), paint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawText(
                        labels.get((int) classes[index]) + " " + Float.toString(fl),
                        locations[x+1] * w,
                        locations[x] * h,
                        paint
                );
            }
        }
        currentImage.setRotation(270);
        currentImage.setImageBitmap(mutable);


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

        return mRgba;
    }
}