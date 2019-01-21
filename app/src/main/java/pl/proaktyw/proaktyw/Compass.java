package pl.proaktyw.proaktyw;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by Maciej Szalek on 2018-09-04.
 */

public class Compass implements SensorEventListener {

    public interface CompassListener{
        void onNewAzimuth(float azimuth);
    }

    private CompassListener listener;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetic;

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float[] Rot = new float[9];
    private float[] I = new float[9];

    static final float ALPHA = 0.99f;

    private float azimuthFix;

    public Compass(Context context){
        sensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }
    public void startListener(){
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_NORMAL);
    }
    public void stopListener(){
        sensorManager.unregisterListener(this);
    }
    public void setAzimuthFix(float fix){
        azimuthFix = fix;
    }
    public void resetAzimuthFix(){
        setAzimuthFix(0);
    }
    public void setListener(CompassListener l){
        listener = l;
    }
    protected float[] lowPassFilter(float[] input, float[] output){
        if(output == null){
            return input;
        }
        for(int i=0; i<input.length; i++){
            output[i] = output[i] * ALPHA + (1.0f - ALPHA) * input[i];
        }
        return output;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        synchronized (this){
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                mGravity = lowPassFilter(event.values.clone(), mGravity);
            }
            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                mGeomagnetic = lowPassFilter(event.values.clone(), mGeomagnetic);
            }
            boolean success = SensorManager.getRotationMatrix(Rot, I, mGravity, mGeomagnetic);
            if(success){
                float orientation[] = new float[3];
                SensorManager.getOrientation(Rot, orientation);
                float azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + azimuthFix + 360) % 360;
                if(listener != null){
                    listener.onNewAzimuth(azimuth);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
