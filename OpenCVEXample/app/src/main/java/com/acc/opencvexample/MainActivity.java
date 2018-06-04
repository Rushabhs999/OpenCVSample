package com.acc.opencvexample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private int w, h;
    private CameraBridgeViewBase mOpenCvCameraView;
    Scalar RED = new Scalar(255, 0, 0);
    Scalar GREEN = new Scalar(0, 255, 0);
    FeatureDetector detector;
    DescriptorExtractor descriptor;
    DescriptorMatcher matcher;
    Mat descriptors2, descriptors1,mRgba,hsv_scale,mIntermediateMat;
    Mat img1;
    MatOfKeyPoint keypoints1, keypoints2;
    Activity context;

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    try {
                        initializeOpenCVDependencies();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                    break;
                }

            }
        }
    };

    private void initializeOpenCVDependencies() throws IOException {
        mOpenCvCameraView.enableView();
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        img1 = new Mat();
//        AssetManager assetManager = getAssets();
//        InputStream istr = assetManager.open("a.jpeg");
//        Bitmap bitmap = BitmapFactory.decodeStream(istr);
//        Utils.bitmapToMat(bitmap, img1);
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGB2GRAY);
        img1.convertTo(img1, 0); //converting the image to match with the type of the cameras image
        descriptors1 = new Mat();
        keypoints1 = new MatOfKeyPoint();
        detector.detect(img1, keypoints1);
        descriptor.compute(img1, keypoints1, descriptors1);

    }

//
//    static {
//        System.loadLibrary("opencv_java");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.CAMERA},0);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setMaxFrameSize(1024,768);
        mOpenCvCameraView.clearFocus();
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (!OpenCVLoader.initDebug()) {
                Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
            } else {
                Log.d(TAG, "OpenCV library found inside package. Using it!");
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    private void checkAndRequestPermissions() {
//        try {
//            if (hasPermissions(context, permission)) {
//                if (!OpenCVLoader.initDebug()) {
//                    Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
//                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
//                } else {
//                    Log.d(TAG, "OpenCV library found inside package. Using it!");
//                    mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//                }
//            } else if (!hasPermissions(context, permission)) {
////            ActivityCompat.requestPermissions(context, permission, permissonAll);
//                for (int i = 0; i < permission.length; i++) {
//                    ActivityCompat.requestPermissions(context, new String[]{permission[i]}, i);
//                }
//                if (!OpenCVLoader.initDebug()) {
//                    Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
//                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
//                } else {
//                    Log.d(TAG, "OpenCV library found inside package. Using it!");
//                    mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//                }
////            checkAndRequestPermissions();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    //
//    public static boolean hasPermissions(Context context, String... permissions) {
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
//            for (String permission : permissions) {
//                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        w = width;
        h = height;
        //4 channel
        mRgba = new Mat(width, height, CvType.CV_8UC4);
        hsv_scale = new Mat(width, height, CvType.CV_8UC3);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        //release
        mRgba.release();
    }

    public Mat recognize(Mat aInputFrame) {

        Imgproc.cvtColor(aInputFrame, aInputFrame, Imgproc.COLOR_RGB2GRAY);
        descriptors2 = new Mat();
        keypoints2 = new MatOfKeyPoint();
        detector.detect(aInputFrame, keypoints2);
        descriptor.compute(aInputFrame, keypoints2, descriptors2);

        // Matching
        MatOfDMatch matches = new MatOfDMatch();
        if (img1.type() == aInputFrame.type()) {
            matcher.match(descriptors1, descriptors2, matches);
        } else {
            return aInputFrame;
        }
        List<DMatch> matchesList = matches.toList();

        Double max_dist = 0.0;
        Double min_dist = 100.0;

        for (int i = 0; i < matchesList.size(); i++) {
            Double dist = (double) matchesList.get(i).distance;
            if (dist < min_dist)
                min_dist = dist;
            if (dist > max_dist)
                max_dist = dist;
        }

        LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
        for (int i = 0; i < matchesList.size(); i++) {
            if (matchesList.get(i).distance <= (1.5 * min_dist))
                good_matches.addLast(matchesList.get(i));
        }

        MatOfDMatch goodMatches = new MatOfDMatch();
        goodMatches.fromList(good_matches);
        Mat outputImg = new Mat();
        MatOfByte drawnMatches = new MatOfByte();
        if (aInputFrame.empty() || aInputFrame.cols() < 1 || aInputFrame.rows() < 1) {
            return aInputFrame;
        }
        Features2d.drawMatches(img1, keypoints1, aInputFrame, keypoints2, goodMatches, outputImg, GREEN, RED, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
        Imgproc.resize(outputImg, outputImg, aInputFrame.size());

        return outputImg;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        //Get Object Borders - Edge Detection (Contours)
        Mat gaussian_output = new Mat();
        mRgba = inputFrame.rgba();
        Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
        Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
        Imgproc.GaussianBlur(mIntermediateMat, gaussian_output, new Size(5, 5), 5);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Log.i("CONTROUS", "Contours");
        Mat gray = new Mat(gaussian_output.size(), CvType.CV_8UC1);
        Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(gray, contours, 1, new Scalar(0, 255, 0));
        Log.i("Controus done", "Contours done");

        //if no contours are detected
        if (contours.size() == 0) {
            Log.i("Controus", "contour size is 0");
        }
        // return ;

        /// Find contours
        //findContours( canny_output, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0) );
        return mRgba;




        //Get RGB Color code

//        //get each frame from camera
//        mRgba = inputFrame.rgba();
//
//        /**********HSV conversion**************/
//        //convert mat rgb to mat hsv
//        Imgproc.cvtColor(mRgba, hsv_scale, Imgproc.COLOR_RGB2HSV);
//
//        //find scalar sum of hsv
//        Scalar mColorHsv = Core.sumElems(hsv_scale);
//
//        int pointCount = 320*240;
//
//
//        //convert each pixel
//        for (int i = 0; i < mColorHsv.val.length; i++) {
//            mColorHsv.val[i] /= pointCount;
//        }
//
//        //convert hsv scalar to rgb scalar
//        Scalar mColorRgb = convertScalarHsv2Rgba(mColorHsv);
//
//    /*Log.d("intensity", "Color: #" + String.format("%02X", (int)mColorHsv.val[0])
//            + String.format("%02X", (int)mColorHsv.val[1])
//            + String.format("%02X", (int)mColorHsv.val[2]) );*/
//        //print scalar value
//        Log.d("intensity", "R:"+ String.valueOf(mColorRgb.val[0])+" G:"+String.valueOf(mColorRgb.val[1])+" B:"+String.valueOf(mColorRgb.val[2]));
//
//
//        /*Convert to YUV*/
//
//        int R = (int) mColorRgb.val[0];
//        int G = (int) mColorRgb.val[1];
//        int B = (int) mColorRgb.val[2];
//
//        int Y = (int) (R *  .299000 + G *  .587000 + B *  .114000);
//        int U = (int) (R * -.168736 + G * -.331264 + B *  .500000 + 128);
//        int V = (int) (R *  .500000 + G * -.418688 + B * -.081312 + 128);
//
//        //int I = (R+G+B)/3;
//
//
//        //Log.d("intensity", "I: "+String.valueOf(I));
//        Log.d("intensity", "Y:"+ String.valueOf(Y)+" U:"+String.valueOf(U)+" V:"+String.valueOf(V));
//
//        /*calibration*/
//        Imgproc.putText(mRgba,"R:"+R + "G:"+G +"B:"+B,new Point(10,52),Core.FONT_HERSHEY_COMPLEX,.7,new Scalar(5,255,255), 2,8,false );
//        return mRgba;



//        System.out.println(inputFrame.rgba());
//        return inputFrame.rgba();
////        return recognize(inputFrame.rgba());
    }

    //convert Mat hsv to scalar
    private Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB);

        return new Scalar(pointMatRgba.get(0, 0));
    }
}
