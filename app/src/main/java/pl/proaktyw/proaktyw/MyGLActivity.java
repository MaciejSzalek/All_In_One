package pl.proaktyw.proaktyw;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import static android.util.Half.EPSILON;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class MyGLActivity extends Activity {

    private CameraPreview mPreview;
    private GLSurfaceView mGlSurfaceView;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;
    private Sensor mSensor;
    private double x, y, z;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        RelativeLayout frame = new RelativeLayout(this);
        mGlSurfaceView = new GLSurfaceView(this);
        mPreview = new CameraPreview(this);

        mGlSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGlSurfaceView.setRenderer(new GLRenderer(true, this.getApplicationContext()));
        mGlSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mGlSurfaceView.setVisibility(View.INVISIBLE);


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        final int mScreenRotation = getWindowManager().getDefaultDisplay().getRotation();

        mSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                final float alpha = (float) 0.9;
                final float gravity[] = new float[3];

                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                switch (mScreenRotation){
                    case Surface.ROTATION_0:
                        x = event.values[0] - gravity[0];
                        y = event.values[1] - gravity[1];
                        z = event.values[2] - gravity[2];
                        break;
                    case Surface.ROTATION_90:
                        x = -(event.values[1] - gravity[1]);
                        y =   event.values[0] - gravity[0];
                        z =   event.values[2] - gravity[2];
                        break;
                    case Surface.ROTATION_180:
                        x = -(event.values[0] - gravity[0]);
                        y = -(event.values[1] - gravity[1]);
                        z =   event.values[2] - gravity[2];
                        break;
                    case Surface.ROTATION_270:
                        x =   event.values[1] - gravity[1];
                        y = -(event.values[0] - gravity[0]);
                        z =   event.values[2] - gravity[2];
                }

                if( y > 3.5 && y < 7.5){
                    mGlSurfaceView.setVisibility(View.VISIBLE);
                }else {
                    mGlSurfaceView.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        frame.addView(mGlSurfaceView);
        frame.addView(mPreview);
        setContentView(frame);

    }
    @Override
    protected void onStart(){
        super.onStart();
        mSensorManager.registerListener(mSensorListener, mSensor, SensorManager.SENSOR_DELAY_UI);

    }
    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(mSensorListener);
    }
}

