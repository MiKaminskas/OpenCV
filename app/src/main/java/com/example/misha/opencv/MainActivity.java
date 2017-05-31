package com.example.misha.opencv;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final Scalar CONTOUR_COLOR = new Scalar(255, 0, 0, 255);

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba, mIntermediateMat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
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
        OpenCVLoader.initDebug();
        mOpenCvCameraView.enableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {

    }

    private int frameCounter = 0, objectCounter = 0;
    public Mat lines;
    private boolean firstframeflag = false;
    String TAG = "Main";
    private Point l1p1,l1p2,l2p1,l2p2,l3p1,l3p2;
    private Mat binarycopy;

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        int minlizesize = 300, lineGap = 10;
        mRgba = inputFrame.rgba();
        //Mat copy = new Mat();

        //mRgba.copyTo(copy);

        if (firstframeflag == false) {
            lines = new Mat();
            binarycopy = new Mat();
            Imgproc.Canny(inputFrame.gray(), binarycopy, 80, 100);
            //Imgproc.threshold(inputFrame.gray(), binarycopy, 100, 255, Imgproc.THRESH_BINARY);
            //Imgproc.HoughLinesP(binarycopy, lines, 1, Math.PI / 180, 20, minlizesize, lineGap);
            firstframeflag = true;
        }
        Imgproc.Canny(inputFrame.gray(), binarycopy, 80, 100);
        //Imgproc.threshold(inputFrame.gray(), binarycopy, 100, 255, Imgproc.THRESH_BINARY);


        frameCounter++;
        if (frameCounter == 5) {
            Imgproc.Canny(inputFrame.gray(), binarycopy, 80, 100);
            //Imgproc.threshold(inputFrame.gray(), binarycopy, 100, 255, Imgproc.THRESH_BINARY);

            Imgproc.HoughLinesP(binarycopy, lines, 1, Math.PI / 180, 20, minlizesize, lineGap);
            frameCounter = 0;
        }

        double[] vec;
        double x1, y1, x2, y2;
        for (int i = 0; i < lines.rows(); i++) {
            vec = lines.get(i, 0);
            x1 = vec[0];
            y1 = vec[1];
            x2 = vec[2];
            y2 = vec[3];
            l1p1 = new Point(x1, y1);
            l1p2 = new Point(x2, y2);
            for (int j = 0; j <= lines.rows(); j++) {

                if ((vec = lines.get(j, 0)) != null) {
                    x1 = vec[0];
                    y1 = vec[1];
                    x2 = vec[2];
                    y2 = vec[3];
                    l2p1 = new Point(x1, y1);
                    l2p2 = new Point(x2, y2);
                    if (lineIntersection(l1p1, l1p2, l2p1, l2p2) && pNotEqaul(l1p1, l2p1) && pNotEqaul(l1p2, l2p2)) {
                        Log.d(TAG, "first two find");
                        for (int x = 0; x <= lines.rows(); x++) {
                            if ((vec = lines.get(x, 0)) != null) {
                                x1 = vec[0];
                                y1 = vec[1];
                                x2 = vec[2];
                                y2 = vec[3];
                                l3p1 = new Point(x1, y1);
                                l3p2 = new Point(x2, y2);
                                if ((lineIntersection(l2p1, l2p2, l3p1, l3p2) || lineIntersection(l1p1, l1p2, l3p1, l3p2))
                                        && pNotEqaul(l1p1, l3p1) && pNotEqaul(l1p2, l3p2) && pNotEqaul(l2p1, l3p1) && pNotEqaul(l2p2, l3p2)) {
                                    // сохранить как объект
                                    objectCounter++;
                                    Log.d(TAG, " find object" + objectCounter);
                                    Imgproc.line(binarycopy, l1p1, l1p2, new Scalar(0, 255, 0, 255), 5);
                                    Imgproc.line(binarycopy, l2p1, l2p2, new Scalar(0, 255, 0, 255), 5);
                                    Imgproc.line(binarycopy, l3p1, l3p2, new Scalar(0, 255, 0, 255), 5);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        /*for (int x = 0; x < lines.rows()-1; x++) {
            double[] vec = lines.get(x, 0);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Point l1p1 = new Point(x1, y1);
            Point l1p2 = new Point(x2, y2);

            vec = lines.get(x+1,0);
            x1 = vec[0];
                    y1 = vec[1];
                    x2 = vec[2];
                    y2 = vec[3];
            Point l2p1 = new Point(x1,y1);
            Point l2p2 = new Point(x2,y2);


            double dx = x1 - x2;
            double dy = y1 - y2;

            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 300.d)  // рисовать все линии длинее 300
                Imgproc.line(mRgba, l1p1, l2p2, new Scalar(0, 255, 0, 255), 5);// добавляем к оригинальной картинке
        }*/

        return binarycopy;
    }

    private boolean pNotEqaul(Point p1, Point p2) {
        if (p1.x != p2.x && p1.y != p2.y) {
            return true;
        }
        return false;
    }

    private boolean lineIntersection(Point l1p1, Point l1p2, Point l2p1, Point l2p2) {
        double s1x, s1y, s2x, s2y, s, t;
        s1x = l1p2.x - l1p1.x;
        s1y = l1p2.y - l1p1.y;
        s2x = l2p2.x - l2p1.x;
        s2y = l2p2.y - l2p1.y;

        s = (-s1y * (l1p1.x - l2p1.x) + s1x * (l1p1.y - l2p1.y)) / (-s2x * s1y + s1x * s2y);
        t = (s2x * (l1p1.y - l2p1.y) - s2y * (l1p1.x - l2p1.y)) / (-s2x * s1y + s1x * s2y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
            return true;
        }
        return false;
    }


}
