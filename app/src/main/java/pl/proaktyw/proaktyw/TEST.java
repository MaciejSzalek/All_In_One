package pl.proaktyw.proaktyw;

import android.hardware.GeomagneticField;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;


public class TEST extends AppCompatActivity {

    ImageButton meterOnOffButton;
    ImageView meterHandClock;
    ImageView gpsOnOff;
    TextView timerHours;
    TextView timerMinutes;
    TextView timerSeconds;

    TextView testText;

    int showingFirst;

    DBHelper dbHelper;
    ArrayAdapter arrayAdapter;
    private Compass compass;

    private Handler timerHandler = new Handler();
    private Long startTime = 0L;
    Long timeInMillisecond = 0L;
    Long timeSwapBuff = 0L;
    Long updatedTime = 0L;

    public GeomagneticField geomagneticField;
    private RotateAnimation animate;

    private float currentAzimuth;
    private float theoreticlaAzimuth;
    private double mAzimuthReal = 0;
    private double mAzimuthTheoretical = 0;
    private static double AZIMUTH_ACCURACY = 5;
    private double myLatitude = 0;
    private double myLongitude = 0;

    public Location start;
    public Location meta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        meterOnOffButton = findViewById(R.id.meter_on_off_button);
        meterHandClock = findViewById(R.id.meter_hand_clock);
        gpsOnOff = findViewById(R.id.gps_on_off);
        timerHours  = findViewById(R.id.timer_hours);
        timerMinutes  = findViewById(R.id.timer_minutes);
        timerSeconds  = findViewById(R.id.timer_seconds);

        testText = findViewById(R.id.test_text);

        setupCompass();

        //50.069395, 21.707325
        start = new Location("START");
        start.setLatitude(50.069395);
        start.setLongitude(21.707325);

        //50.069108, 21.707610
        meta = new Location("META");
        meta.setLatitude(50.069108);
        meta.setLongitude(21.707610);

        showingFirst = 0;
        meterOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showingFirst == 0){
                    meterOnOffButton.setImageResource(R.mipmap.start_off);
                    compass.stopListener();
                    stopTimer();
                    showingFirst = 1;

                    currentAzimuth = 0;
                    float rotateLast = currentAzimuth;
                    animate = new RotateAnimation(rotateLast, currentAzimuth, Animation.RELATIVE_TO_SELF,
                            0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    animate.setFillAfter(true);
                    animate.setInterpolator(new LinearInterpolator());
                    animate.setDuration(10);
                    meterHandClock.startAnimation(animate);

                }else{
                    meterOnOffButton.setImageResource(R.mipmap.start_on);
                    compass.startListener();
                    startTimer();
                    showingFirst = 0;
                }
            }
        });
    }
    private void startTimer(){
        startTime = SystemClock.uptimeMillis();
        timerHandler.postDelayed(updatedTimerThread, 0);
    }
    private void stopTimer(){
        timeSwapBuff += timeInMillisecond;
        timerHandler.removeCallbacks(updatedTimerThread);
    }
    private Runnable updatedTimerThread = new Runnable() {
        @Override
        public void run() {
            timeInMillisecond = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMillisecond;

            Integer seconds = (int) (updatedTime/1000);
            Integer minutes = seconds/60;
            seconds = seconds % 60;
            Integer hours  = minutes/60;
            minutes = minutes % 60;

            DecimalFormat decimalFormat = new DecimalFormat("#00");
            timerHours.setText(String.valueOf(decimalFormat.format(hours)));
            timerMinutes.setText(String.valueOf(decimalFormat.format(minutes)));
            timerSeconds.setText(String.valueOf(decimalFormat.format(seconds)));

            timerHandler.postDelayed(this, 0);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //compass.startListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //compass.stopListener();
    }

    private void setupCompass(){
        compass = new Compass(this);
        Compass.CompassListener cl = new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(float azimuth) {

                currentAzimuth = azimuth;
                float rotateMax = 300f;
                float rotateScale = 1.67f;
                float theoreticalAzimuth = calculateTheoreticalAzimuth();
                float difference;

                difference = 180 - Math.abs(Math.abs(currentAzimuth - theoreticalAzimuth) - 180);

                DecimalFormat decimalFormat = new DecimalFormat("###0");
                String text = String.valueOf(decimalFormat.format(currentAzimuth));
                testText.setText("Azimuth: " + text
                        + "\nbearing: " + String.valueOf(decimalFormat.format(theoreticalAzimuth)));

                if(difference >=0 && difference < 180){
                    float rotate = rotateMax - (difference * rotateScale);
                    animate = new RotateAnimation(rotate, rotate, Animation.RELATIVE_TO_SELF,
                            0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    animate.setFillAfter(true);
                    animate.setInterpolator(new LinearInterpolator());
                    animate.setDuration(10);
                    meterHandClock.startAnimation(animate);
                }
            }
        };
        compass.setListener(cl);
    }

    public float calculateTheoreticalAzimuth(){
        //double dX = poiLatitude - myLatitude;
        //double dY = poiLongitude - myLongitude;

        double dX = meta.getLatitude() - start.getLatitude();
        double dY = meta.getLongitude() - start.getLongitude();

        double phiAngle;
        double tanPhi;
        float azimuth = 0;

        tanPhi = Math.abs(dY / dX);
        phiAngle = Math.atan(tanPhi);
        phiAngle = Math.toDegrees(phiAngle);

        if (dX > 0 && dY > 0) { // I quarter
            azimuth = (float) phiAngle;
        } else if (dX < 0 && dY > 0) { // II
            azimuth = (float) (180 - phiAngle);
        } else if (dX < 0 && dY < 0) { // III
            azimuth = (float) (180 + phiAngle);
        } else if (dX > 0 && dY < 0) { // IV
            azimuth = (float) (360 - phiAngle);
        }
        return azimuth;
    }

}
