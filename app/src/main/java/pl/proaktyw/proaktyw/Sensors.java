package pl.proaktyw.proaktyw;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by Maciej Szalek on 2018-04-17.
 */

public class Sensors {

    public static double x;
    public static double y;
    public static double z;

    private Sensor mSensor;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;

    public Sensors(Context context){

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                for(int i = 0; i < event.values.length; i++){
                    if(i==0){
                        x = (double) event.values[i];
                    }
                    if(i==1){
                        y = (double) event.values[i];
                    }
                    if(i==2){
                        z = (double) event.values[i];
                    }
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }
    public static double getX(){
        return x;
    }
    public void register(){
        mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_UI);
    }
    public void unregister(){
        mSensorManager.unregisterListener(mSensorEventListener);
    }
}
