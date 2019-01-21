package pl.proaktyw.proaktyw;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class ARActivity extends Activity {

    private CameraPreview mPreview;
    private GLSurfaceView mGlSurfaceView;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;
    private Sensor mSensor;
    private double x, y, z;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final float distance[] = new float[4];
        final Location PoiLocation[] = new Location[4];
        //Sędziszów Wejście
        PoiLocation[0] = new Location("Sędziszów Wejście");
        PoiLocation[0].setLatitude(50.069486);
        PoiLocation[0].setLongitude(21.707464);
        //Sędziszów Parking
        PoiLocation[1] = new Location("Sędzi. Parking");
        PoiLocation[1].setLatitude(50.069052);
        PoiLocation[1].setLongitude(21.707480);
        //Zglobień 1
        PoiLocation[2] = new Location("Zgłobień 1");
        PoiLocation[2].setLatitude(50.014255);
        PoiLocation[2].setLongitude(21.866056);
        //Zgłobień 2
        PoiLocation[3] = new Location("Zgłobień 2");
        PoiLocation[3].setLatitude(50.0050678);
        PoiLocation[3].setLongitude(21.5158508);


        final FrameLayout frame = new FrameLayout(this);
        mGlSurfaceView = new GLSurfaceView(this);
        mPreview = new CameraPreview(this);

        mGlSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGlSurfaceView.setRenderer(new GLRenderer(true, this.getApplicationContext()));
        mGlSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mGlSurfaceView.setVisibility(View.INVISIBLE);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        final int mScreenRotation = getWindowManager().getDefaultDisplay().getRotation();


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

            }
        });

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("BRAK DOSTĘPU DO LOKALZACJI");
        dialogBuilder.setMessage("Sprawdź ustawienia");
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton("Anuluj",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton("Ustawienia",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                     Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
            }
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            AlertDialog dialog = dialogBuilder.create();
                            dialog.show();
                        }
                    }
                });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    for (int i = 0; i < PoiLocation.length; i++) {
                        distance[i] = location.distanceTo(PoiLocation[i]);
                        //for (int i=0; i < distance.length; i++) {
                        if (distance[i] < 20) {
                            mSensorListener = new SensorEventListener() {
                                @Override
                                public void onSensorChanged(SensorEvent event) {
                                    final float alpha = (float) 0.9;
                                    final float gravity[] = new float[3];

                                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                                    switch (mScreenRotation) {
                                        case Surface.ROTATION_0:
                                            x = event.values[0] - gravity[0];
                                            y = event.values[1] - gravity[1];
                                            z = event.values[2] - gravity[2];
                                            break;
                                        case Surface.ROTATION_90:
                                            x = -(event.values[1] - gravity[1]);
                                            y = event.values[0] - gravity[0];
                                            z = event.values[2] - gravity[2];
                                            break;
                                        case Surface.ROTATION_180:
                                            x = -(event.values[0] - gravity[0]);
                                            y = -(event.values[1] - gravity[1]);
                                            z = event.values[2] - gravity[2];
                                            break;
                                        case Surface.ROTATION_270:
                                            x = event.values[1] - gravity[1];
                                            y = -(event.values[0] - gravity[0]);
                                            z = event.values[2] - gravity[2];

                                    }
                                    if (y > 3.5 && y < 7.5) {
                                        mGlSurfaceView.setVisibility(View.VISIBLE);
                                    } else {
                                        mGlSurfaceView.setVisibility(View.INVISIBLE);
                                    }

                                }

                                @Override
                                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                                }
                            };
                        }
                        continue;
                    }
                }
            }
        };
        frame.addView(mGlSurfaceView);
        frame.addView(mPreview);
        setContentView(frame);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
        startSensorListener();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                     Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
            }
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }
    private void startSensorListener(){
        mSensorManager.registerListener(mSensorListener, mSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause(){
        super.onPause();
        stopLocationUpdates();
        stopSensorListener();

    }
    private void stopLocationUpdates(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
    private  void stopSensorListener(){
        mSensorManager.unregisterListener(mSensorListener);
    }
}



