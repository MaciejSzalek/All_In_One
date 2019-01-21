package pl.proaktyw.proaktyw;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class GAME extends AppCompatActivity {

    ImageButton meterOnOffButton;
    ImageButton meterBackArrow;
    ImageView meterHandClock;
    ImageView gpsOnOff;
    TextView timerHours;
    TextView timerMinutes;
    TextView timerSeconds;

    TextView testText;

    DBHelper dbHelper;
    private Compass compass;

    private Handler timerHandler = new Handler();
    private Long startTime = 0L;
    Long timeInMillisecond = 0L;
    Long timeSwapBuff = 0L;
    Long updatedTime = 0L;

    public FusedLocationProviderClient mFusedLocationClient;
    public LocationRequest mLocationRequest;
    public LocationCallback mLocationCallback;
    private RotateAnimation animate;

    private String projectName;
    private String challenge;
    private String challengePassword;
    private int markerIndex;
    int showingFirst;
    public int timerStarter;

    private ArrayList<LatLng> markerList = new ArrayList<>();
    private ArrayList<Integer> challengeMarkerIndexList = new ArrayList<>();

    private float distance;
    private float currentAzimuth;
    private float theoreticalAzimuth;

    public Location currentLocation;
    public Location markerLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        meterOnOffButton = findViewById(R.id.meter_on_off_button);
        meterBackArrow = findViewById(R.id.meter_beck_arrow);
        meterHandClock = findViewById(R.id.meter_hand_clock);
        gpsOnOff = findViewById(R.id.gps_on_off);
        timerHours  = findViewById(R.id.timer_hours);
        timerMinutes  = findViewById(R.id.timer_minutes);
        timerSeconds  = findViewById(R.id.timer_seconds);

        testText = findViewById(R.id.test_text);

        dbHelper = new DBHelper(this);
        projectName = getIntent().getStringExtra("PROJECT_NAME");

        getMarkerListFromDatabase();
        getChallengeData();
        if (markerList != null) {
            markerIndex = 0;
            markerLocation = new Location("MARKER_LOCATION");
            markerLocation.setLatitude(markerList.get(markerIndex).latitude);
            markerLocation.setLongitude(markerList.get(markerIndex).longitude);
        }
        setupCompass();
        timerStarter = 0;

        meterOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showingFirst == 0){
                    meterOnOffButton.setImageResource(R.mipmap.start_off);
                    compass.stopListener();
                    distance = 0;
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
                    showingFirst = 0;
                    if(timerStarter == 0){
                        startTimer();
                    }
                }

            }
        });

        meterBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
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
                            gpsOnOff.setImageResource(R.mipmap.gps_off);
                            Toast.makeText(GAME.this, "Brak dostępu do lokalizacji !!!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    gpsOnOff.setImageResource(R.mipmap.gps_off);
                    //return;
                }
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {

                        distance = location.distanceTo(markerLocation);
                        if(distance != 0 ){
                            gpsOnOff.setImageResource(R.mipmap.gps_on);
                        }

                        if (distance < 20) {
                            if (challengeMarkerIndexList.contains(markerIndex)) {
                                startChallenge(markerIndex);
                            } else {
                                Toast.makeText(GAME.this, "CHECKPOINT", Toast.LENGTH_LONG).show();
                                markerIndex++;
                                markerLocation = new Location("MARKER_LOCATION");
                                markerLocation.setLatitude(markerList.get(markerIndex).latitude);
                                markerLocation.setLongitude(markerList.get(markerIndex).longitude);
                            }
                        }
                        currentLocation = new Location("CURRENT_LOCATION");
                        currentLocation.set(location);


                    }
                }
            }
        };
        startLocationUpdates();
    }

    @Override
    public void onBackPressed(){
        showBackPressedAlert();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void startTimer(){
        startTime = SystemClock.uptimeMillis();
        timerHandler.postDelayed(updatedTimerThread, 0);
        timerStarter = 1;
    }
    private void stopTimer(){
        timeSwapBuff += timeInMillisecond;
        timerHandler.removeCallbacks(updatedTimerThread);
        timerStarter = 0;
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

    private void setupCompass(){
        compass = new Compass(this);
        Compass.CompassListener cl = new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(float azimuth) {

                currentAzimuth = azimuth;
                float difference;
                float rotateMax = 300f;
                float rotateScale = 1.67f;

                if(currentLocation != null){
                    theoreticalAzimuth = calculateTheoreticalAzimuth();
                }
                difference = 180 - Math.abs(Math.abs(currentAzimuth - theoreticalAzimuth) - 180);

                DecimalFormat decimalFormat = new DecimalFormat("###0");
                String text = String.valueOf(decimalFormat.format(currentAzimuth));
                testText.setText("Azimuth: " + text
                        + "\nbearing: " + String.valueOf(decimalFormat.format(theoreticalAzimuth))
                        + "\ndystans: " + String.valueOf(decimalFormat.format(distance)));

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
        double dX = markerLocation.getLatitude() - currentLocation.getLatitude();
        double dY = markerLocation.getLongitude() - currentLocation.getLongitude();

        double phiAngle;
        double tanPhi;
        float azimuth = 0;

        tanPhi = Math.abs(dY / dX);
        phiAngle = Math.atan(tanPhi);
        phiAngle = Math.toDegrees(phiAngle);

        if (dX > 0 && dY > 0) { // I quarter
            azimuth = (float) phiAngle;
        } else if (dX < 0 && dY > 0) { // II
            azimuth =(float) (180 - phiAngle);
        } else if (dX < 0 && dY < 0) { // III
            azimuth = (float) (180 + phiAngle);
        } else if (dX > 0 && dY < 0) { // IV
            azimuth = (float) (360 - phiAngle);
        }
        return azimuth;
    }

    private void startChallenge(int index) {
        String indexString = Integer.toString(index);
        getChallengeTextFromDatabase(indexString);
        showChallengeDialogBuilder(GAME.this);
    }

    private void getChallengeData() {
        if(markerList.size() != 0){
            List<MarkerPOJO> markerPOJOList = dbHelper.getChallengeDataFromMarkerTable(projectName);
            for(MarkerPOJO markerPOJO: markerPOJOList){

                challengeMarkerIndexList.add(markerPOJO.get_marker_index());
            }
        }
    }

    private void getChallengeTextFromDatabase(String index) {

        MarkerPOJO markerPOJO;
        markerPOJO = dbHelper.getSingleChallengeFromMarkerTable(projectName, index);

        challenge = markerPOJO.get_challenge();
        challengePassword = markerPOJO.get_challenge_password();
    }

    private void getMarkerListFromDatabase() {
        if (projectName != null) {
            List<MarkerPOJO> markerPOJOList = dbHelper.getAllMarkers(projectName);
            for (MarkerPOJO markerPOJO : markerPOJOList) {
                int index = markerPOJO.get_marker_index();
                double Lat = markerPOJO.get_marker_latitude();
                double Lng = markerPOJO.get_marker_longitude();
                markerList.add(index, new LatLng(Lat, Lng));
            }
        }
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

    private void stopLocationUpdates(){
        gpsOnOff.setImageResource(R.mipmap.gps_off);
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void showChallengeDialogBuilder(Context context) {

        final AlertDialog.Builder challengeDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.challenge_dialog, null);
        challengeDialogBuilder.setView(view);
        challengeDialogBuilder.setCancelable(true);

        final AlertDialog dialog = challengeDialogBuilder.create();
        dialog.show();

        ImageButton challengeDialogOkButton = view.findViewById(R.id.challenge_dialog_ok_button);
        TextView challengeDialogTextView = view.findViewById(R.id.challenge_dialog_text_view);
        final EditText challengeDialogPassword = view.findViewById(R.id.challenge_dialog_password);

        challengeDialogPassword.setVisibility(View.VISIBLE);

        challengeDialogTextView.setText("Opis zadania:\n " + challenge
                + "\nHasło: " + challengePassword);
        challengeDialogOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = challengeDialogPassword.getText().toString();
                if (challengePassword.equals(password)) {
                    markerIndex++;
                    markerLocation = new Location("MARKER_LOCATION");
                    markerLocation.setLatitude(markerList.get(markerIndex).latitude);
                    markerLocation.setLongitude(markerList.get(markerIndex).longitude);
                    dialog.dismiss();
                } else {
                    Toast.makeText(GAME.this, "Nieprawidłowe hasło !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void showBackPressedAlert(){
        AlertDialog.Builder alertBuilder= new AlertDialog.Builder(GAME.this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Wyjście z gry !!! ");
        alertBuilder.setMessage("Czy na pewno chcesz opuścić grę ?");
        alertBuilder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopTimer();
                finish();
            }
        });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }
}
