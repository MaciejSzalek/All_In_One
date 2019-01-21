package pl.proaktyw.proaktyw;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Maps extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    PolylineOptions polylineOptions;
    Polyline polyline;
    Marker mMarker;
    Marker mDistanceMarker;
    MarkerOptions markerOptions;
    DBHelper dbHelper;

    DrawerLayout mLayout;
    SupportMapFragment mapFragment;

    ArrayList<LatLng> markerList = new ArrayList<>();
    ArrayList<Marker> distanceMarkerList = new ArrayList<>();
    ArrayList<LatLng> polylinePoints = new ArrayList<>();
    ArrayList<Polyline> polylineArrayList = new ArrayList<>();
    private Map<Marker, Integer> markerHashMap = new HashMap<>();

    ArrayList<LatLng> loadMarkerArrayList = new ArrayList<>();
    ArrayList<LatLng> loadMarkerArrayList2 = new ArrayList<>();
    ArrayList<Integer> challengeMarkerIndexList = new ArrayList<>();
    ArrayList<LatLng> challengeLatLngList = new ArrayList<>();

    ArrayList<Integer> markerIndexList = new ArrayList<>();
    ArrayList<Integer> idArrayList = new ArrayList<>();

    Integer markerIndex = -1;
    int markerIndexCopy;
    int markerNumber = 0;
    private int currentPoint;
    private int challengePoint;
    double[] distanceBetween;

    private static final int LOAD_PROJECT_ACTIVITY_REQUEST_CODE = 0;
    private static final int NEW_PROJECT_ACTIVITY_REQUEST_CODE = 1;

    RelativeLayout markerMenuLayout;
    TextView projectNameTextView;
    ImageButton markerDeleteButton;
    ImageButton markerMenuCloseButton;
    ImageButton markerMenuChallengeButton;

    String projectName;
    Intent intentCheck;
    String challenge;
    String challengePassword;
    String challengeName;
    LatLng challengeLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout view = new RelativeLayout(this);
        mInflater.inflate(R.layout.distance_layout, view, true);
        mLayout = findViewById(R.id.drawer_layout);

        projectNameTextView = findViewById(R.id.project_name_text_view);

        setNavigationDrawer();
        dbHelper = new DBHelper(this);
        intentCheck = getIntent();

        if (intentCheck != null) {
            String strData = intentCheck.getStringExtra("ID");
            if (strData.equals("LOAD_PROJECT_2")) {
                if (markerList.size() != 0) {
                    mMap.clear();
                    markerList.clear();
                    polylinePoints.clear();
                    polylineArrayList.clear();
                    markerHashMap.clear();
                    markerNumber = 0;
                    markerIndex = -1;
                }
                loadMarkerArrayList2 = getIntent().getParcelableArrayListExtra("LOAD_MARKER_LIST");
                projectName = getIntent().getStringExtra("LOAD_PROJECT_NAME");
                projectNameTextView.setText("Projekt: " + projectName);
                if (loadMarkerArrayList2.size() != 0) {
                    getMarkersListFromEditorMenu();
                    Toast.makeText(Maps.this, "LOAD PROJECT", Toast.LENGTH_SHORT).show();
                }
            }
            if (strData.equals("NEW_PROJECT")) {
                projectName = getIntent().getStringExtra("NEW_PROJECT_NAME");
                projectNameTextView.setText("Projekt: " + projectName);
                Toast.makeText(Maps.this, "NEW PROJECT", Toast.LENGTH_SHORT).show();
            }
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = findViewById(R.id.map_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.icon_color));
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayout.openDrawer(Gravity.START);
            }
        });

        if (savedInstanceState != null) {
            markerList = savedInstanceState.getParcelableArrayList("markerList");
        }

        markerMenuLayout = findViewById(R.id.marker_menu_layout);

        markerMenuChallengeButton = findViewById(R.id.marker_menu_challenge_button);
        markerMenuChallengeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProject(projectName);
                String index = Integer.toString(markerIndexCopy);
                if (dbHelper.checkMarkerHaveChallenge(projectName, index)) {
                    showMarkerChallengeDialogBuilder(Maps.this);
                } else {
                    goToAddChallenge(findViewById(R.id.add_challenge));
                }
            }
        });

        markerMenuCloseButton = findViewById(R.id.marker_menu_close_button);
        markerMenuCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMarker.hideInfoWindow();
                markerMenuLayout.setVisibility(View.INVISIBLE);
            }
        });

        markerDeleteButton = findViewById(R.id.marker_menu_delete_button);
        markerDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMarkerDialogBuilder();
            }
        });
    }

    public void setNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.new_game_project) {
                    goToNewProject2(findViewById(R.id.new_project2));
                    mLayout.closeDrawers();

                } else if (itemId == R.id.save_game_project) {
                    if (markerList.size() == 0) {
                        Toast.makeText(Maps.this, "Nie można zapisać projektu,\nBRAK MARKERÓW !!!", Toast.LENGTH_LONG).show();
                    } else {
                        goToSaveProject(findViewById(R.id.save_project));
                        mLayout.closeDrawers();
                        Toast.makeText(Maps.this, "save game", Toast.LENGTH_SHORT).show();
                    }

                } else if (itemId == R.id.load_game_project) {
                    goToLoadProject(findViewById(R.id.load_project));
                    mLayout.closeDrawers();
                    Toast.makeText(Maps.this, "load game", Toast.LENGTH_SHORT).show();

                } else if (itemId == R.id.manage_game_project) {
                    goToManageProject(findViewById(R.id.manage_project));
                    mLayout.closeDrawers();
                    Toast.makeText(Maps.this, "edit game", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("markerList", markerList);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.map_type_normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.map_type_satellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.map_type_hybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.map_type_terrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            case R.id.map_type_none:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                return true;
            case R.id.line_distance:
                polylinePoints.clear();
                if (markerList.size() > 1) {
                    for (int i = 0; i < markerList.size(); i++) {
                        drawPolyline(markerList.get(i));
                    }
                    removeDistanceMarkers();
                    distanceMarkerList.clear();
                    drawDistanceMarker();
                    Toast.makeText(Maps.this, "Trasa/Dystans", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Maps.this, "Brak odpowiedniej ilości markerów", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.delete_line_distance:
                if (distanceMarkerList != null) {
                    removePolyline(polylineArrayList);
                    polylineArrayList.clear();
                    removeDistanceMarkers();
                    distanceMarkerList.clear();
                    Toast.makeText(Maps.this, "Trasę/Dystans usunięto", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Maps.this, "Trasę/Dystans usunięto", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.simulation:
                startAnimateCamera(0);
                return true;

            case R.id.editor:
                goToChallengeEditor(findViewById(R.id.challenge_editor));
                return true;
            case R.id.delete_all:
                deleteAllMarkersDialogBuilder();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMarkerDragListener(this);
        setMapLongClickListener(mMap);
        setOnMarkerClickListener(mMap);
        setMapClickListener(mMap);
        if (markerList != null){
            //setOnInfoWindowClickListener(mMap);
            for(int i=0; i<markerList.size(); i++){
                drawMarker(markerList.get(i));
                markerHashMap.put(mMarker, markerIndex);
            }
        }
    }
    private void setMapClickListener(final GoogleMap map){
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(markerList.size() !=0 ){
                    markerMenuLayout.setVisibility(View.INVISIBLE);
                    mMarker.hideInfoWindow();
                }
            }
        });
    }
    private void setMapLongClickListener(final GoogleMap map){
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                drawMarker(latLng);
                markerList.add(latLng);
                markerHashMap.put(mMarker, markerIndex);
            }
        });
    }
    private void setOnMarkerClickListener(final GoogleMap map){
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                markerMenuLayout.setVisibility(View.VISIBLE);
                mMarker = marker;
                markerIndexCopy = markerHashMap.get(mMarker);
                challengeLatLng = marker.getPosition();
                return false;
            }
        });
    }
    /*private void setOnInfoWindowClickListener(final GoogleMap map){
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                deleteMarkerDialogBuilder();
            }
        });
    }*/

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Szukam lokalizacji...", Toast.LENGTH_LONG ).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "My location\n" + location, Toast.LENGTH_LONG).show();

    }
    @Override
    public void onMarkerDragStart(Marker marker) {
        removePolyline(polylineArrayList);
        polylineArrayList.clear();
        //removeDistanceMarkers();
        //distanceMarkerList.clear();
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        if(distanceMarkerList.size() != 0){
            removeDistanceMarkers();
            markerList.set(markerHashMap.get(marker), new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
            drawDistanceMarker();
            //polylinePoints.clear();
            //removePolyline(polylineArrayList);
            /*for(int i=0; i<markerList.size(); i++){
                drawPolyline(markerList.get(i));
            }*/
        }
        markerList.set(markerHashMap.get(marker), new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
        //marker.setSnippet("Lat"+ Double.toString(marker.getPosition().latitude) + "Lng" + Double.toString(marker.getPosition().longitude));
        //marker.showInfoWindow();

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        if(distanceMarkerList.size() != 0){
            removeDistanceMarkers();
            distanceMarkerList.clear();
            markerList.set(markerHashMap.get(marker), new LatLng(marker.getPosition().latitude,
                    marker.getPosition().longitude));
            drawDistanceMarker();
            polylinePoints.clear();
            for(int i=0; i<markerList.size(); i++){
                drawPolyline(markerList.get(i));
            }
        }
        markerList.set(markerHashMap.get(marker), new LatLng(marker.getPosition().latitude,
                marker.getPosition().longitude));

        marker.setSnippet("Index po: "+Integer.toString(markerHashMap.get(marker)));

    }

    public void drawMarker(LatLng latLng){
        markerNumber++;
        markerIndex++;
        String text = String.valueOf(markerNumber);
        Bitmap bitmap = makeMarkerBitmap(this, text);

        markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        if(markerNumber == 1){
            markerOptions.title("START");
        }else{
            markerOptions.title("Checkpoint");
        }
        markerOptions.snippet("Index: " + markerIndex);
        mMarker = mMap.addMarker(markerOptions);
    }
    private void drawDistanceMarker(){

        if(markerList.size() > 1){
            String text;
            float distance[] = new float[markerList.size()-1];
            distanceBetween = new double[markerList.size()-1];
            Location[] startLocation = new Location[markerList.size()];
            Location[] endLocation = new Location[markerList.size()];

            double[] lat1 = new double[markerList.size()-1];
            double[] lng1 = new double[markerList.size()-1];
            double[] lat2 = new double[markerList.size()-1];
            double[] lng2 = new double[markerList.size()-1];
            double midLat[] = new double[markerList.size()-1];
            double midLng[] = new double[markerList.size()-1];

            for(int i = 0; i<markerList.size()-1; i++){

                lat1[i] = markerList.get(i).latitude;
                lng1[i] = markerList.get(i).longitude;
                lat2[i] = markerList.get(i+1).latitude;
                lng2[i] = markerList.get(i+1).longitude;

                midLat[i] = (lat1[i] + lat2[i]) / 2;
                midLng[i] = (lng1[i] + lng2[i]) / 2;

                startLocation[i] = new Location("start");
                startLocation[i].setLatitude(markerList.get(i).latitude);
                startLocation[i].setLongitude(markerList.get(i).longitude);
                endLocation[i] = new Location("end");
                endLocation[i].setLatitude(markerList.get(i+1).latitude);
                endLocation[i].setLongitude(markerList.get(i+1).longitude);

                distance[i] = startLocation[i].distanceTo(endLocation[i]);
                distanceBetween[i] = distance[i];

                if (distanceBetween[i] > 1000){
                    DecimalFormat decimalFormat = new DecimalFormat("###0.0");
                    text = String.valueOf(decimalFormat.format(distanceBetween[i] / 1000) + "Km");
                    Bitmap distanceBitmap = makeDistanceText(this, text);
                    markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(midLat[i], midLng[i]));
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(distanceBitmap));
                    mDistanceMarker = mMap.addMarker(markerOptions);
                    distanceMarkerList.add(mDistanceMarker);
                    //distanceMarkerHashMap.put(mDistanceMarker, markerIndex);

                }
                if(distanceBetween[i] < 1000){
                    DecimalFormat decimalFormat = new DecimalFormat("####0");
                    text = String.valueOf(decimalFormat.format(distanceBetween[i]) + "m");
                    Bitmap distanceBitmap = makeDistanceText(this, text);
                    markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(midLat[i], midLng[i]));
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(distanceBitmap));
                    mDistanceMarker = mMap.addMarker(markerOptions);
                    distanceMarkerList.add(mDistanceMarker);
                    //distanceMarkerHashMap.put(mDistanceMarker, markerIndex);

                }
            }
        }
    }
    private void removeDistanceMarkers(){
        if(distanceMarkerList != null){
            for(Marker mDistanceMarker: distanceMarkerList) {
                mDistanceMarker.remove();
            }
        }
    }
    public void drawPolyline(LatLng latLng){

        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.GREEN);
        polylineOptions.width(3);
        polylinePoints.add(latLng);
        polylineOptions.addAll(polylinePoints);
        polyline = mMap.addPolyline(polylineOptions);
        if(markerList != null){
            polylineArrayList.add(polyline);
        }
    }
    public void removePolyline(ArrayList<Polyline> polylineArrayList){
        if(polylineArrayList.size() != 0){
            for(Polyline polyline : polylineArrayList){
                polyline.remove();
            }
        }
    }
    public Bitmap makeMarkerBitmap(Context context, String text){
        Resources resources = context.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.marker_one);
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK); //Text color
        paint.setTextSize(14 * scale); //Text size inside marker
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE); //Text shadow
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0 , text.length(), bounds);

        int x = bitmap.getWidth() - bounds.width() - 12; // 10 for padding from right
        int y = bounds.height();
        canvas.drawText(text, x, y, paint);

        return bitmap;
    }
    public Bitmap makeDistanceText(Context context, String text){

        RelativeLayout distanceMarkerLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.distance_layout, null);
        distanceMarkerLayout.setDrawingCacheEnabled(true);
        distanceMarkerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        distanceMarkerLayout.layout(0, 0, distanceMarkerLayout.getMeasuredWidth(), distanceMarkerLayout.getMeasuredHeight());
        distanceMarkerLayout.buildDrawingCache(true);

        TextView textView = distanceMarkerLayout.findViewById(R.id.distance_text);
        textView.setText(text);
        textView.setTextColor(Color.RED);
        Bitmap distanceBitmap = Bitmap.createBitmap(distanceMarkerLayout.getDrawingCache());
        distanceMarkerLayout.setDrawingCacheEnabled(false);

        return distanceBitmap;
    }
    private void saveProject(String text){
        if(idArrayList.size() == 0){
            getAllMarkers();
        }else{
            idArrayList.clear();
            markerIndexList.clear();
            getAllMarkers();
        }

        if(markerList.size() !=0){
            if(markerList.size() < idArrayList.size()){
                for(int i=markerList.size(); i< idArrayList.size(); i++){
                    String markerIndex = Integer.toString(i);
                    deleteSingleMarker(text, markerIndex);
                }

                updateMarkerIdIndex();

                for(int i=0; i<markerList.size(); i++){
                    if(dbHelper.checkMarkerExists(projectName, Integer.toString(i))){

                        String markerIndexString = Integer.toString(i);

                        MarkerPOJO markerPOJO = new MarkerPOJO();
                        markerPOJO.set_marker_latitude(markerList.get(i).latitude);
                        markerPOJO.set_marker_longitude(markerList.get(i).longitude);
                        dbHelper.updateProjectMarkerList(text, markerIndexString, markerPOJO, markerPOJO);
                    }
                }

            }else{
                for(int i=0; i<markerList.size(); i++){
                    if(dbHelper.checkMarkerExists(projectName, Integer.toString(i))){

                        String markerIndexString = Integer.toString(i);

                        MarkerPOJO markerPOJO = new MarkerPOJO();
                        markerPOJO.set_marker_latitude(markerList.get(i).latitude);
                        markerPOJO.set_marker_longitude(markerList.get(i).longitude);
                        dbHelper.updateProjectMarkerList(text, markerIndexString, markerPOJO, markerPOJO);
                    }else{
                        MarkerPOJO markerPOJO = new MarkerPOJO();
                        markerPOJO.set_project_name(text);
                        markerPOJO.set_marker_index(i);
                        markerPOJO.set_marker_latitude(markerList.get(i).latitude);
                        markerPOJO.set_marker_longitude(markerList.get(i).longitude);


                        dbHelper.addMarkersToTable(markerPOJO, markerPOJO, markerPOJO,
                                markerPOJO);
                    }
                }
            }
        }
    }

    private void deleteSingleMarker(String project, String markerIndex){
        dbHelper.deleteSingleMarker(project, markerIndex);
    }

    private void getAllMarkers(){

        List<MarkerPOJO> markerPOJOList =  dbHelper.getAllMarkers(projectName);
        for(MarkerPOJO markerPOJO : markerPOJOList){

            int Id = markerPOJO.get_id();
            int index = markerPOJO.get_marker_index();

            idArrayList.add(Id);
            markerIndexList.add(index) ;
        }
    }

    private void updateMarkerIdIndex(){
        for(int i=0; i < idArrayList.size(); i++){
            String id = Integer.toString(idArrayList.get(i));
            MarkerPOJO markerPOJO = new MarkerPOJO();
            markerPOJO.set_marker_index(i);
            dbHelper.updateMarkerIndex(projectName, id, markerPOJO);
        }
    }

    private void startChallenge(LatLng latLng){
        if(challengeLatLngList.size() != 0){
            if(challengeLatLngList.contains(latLng)){
                showChallengeDialogBuilder(Maps.this, mMap);
            }
        }
    }
    private void getChallengeData(){

        List<MarkerPOJO> markerPOJOList = dbHelper.getChallengeDataFromMarkerTable(projectName);
        for(MarkerPOJO markerPOJO: markerPOJOList){

            double Lat = markerPOJO.get_marker_latitude();
            double Lng = markerPOJO.get_marker_longitude();

            challengeMarkerIndexList.add(markerPOJO.get_marker_index());
            challengeLatLngList.add(new LatLng(Lat, Lng));
        }
    }
    private void getSingleChallengeFromMarkerTable(String index){

        MarkerPOJO markerPOJO;
        markerPOJO = dbHelper.getSingleChallengeFromMarkerTable(projectName, index);

        challenge = markerPOJO.get_challenge();
        challengePassword = markerPOJO.get_challenge_password();
        challengeName = markerPOJO.get_challenge_name();
    }

    private void startAnimateCamera(Integer point){
        saveProject(projectName);
        if(challengeMarkerIndexList.size() != 0){
            challengeMarkerIndexList.clear();
            challengeLatLngList.clear();
            getChallengeData();
        }else{
            getChallengeData();
        }
        if(polylineArrayList.size() != 0 ){
            removePolyline(polylineArrayList);
            polylinePoints.clear();
            polylineArrayList.clear();
        }
        if(distanceMarkerList.size() != 0){
            removeDistanceMarkers();
            distanceMarkerList.clear();
        }
        if(markerList.size() < 2 ){
            Toast.makeText(Maps.this, "Brak odpowiedniej ilości markerów !!!", Toast.LENGTH_SHORT).show();
        }else{
            mMarker.hideInfoWindow();
            markerMenuLayout.setVisibility(View.INVISIBLE);
            for(int i=0; i<markerList.size(); i++){
                drawPolyline(markerList.get(i));
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLng(markerList.get(point)),
                    myCancelableCallback);
            currentPoint = point;
        }
    }
    GoogleMap.CancelableCallback myCancelableCallback = new GoogleMap.CancelableCallback() {
        @Override
        public void onFinish() {
            if(++currentPoint < markerList.size()){
                mMap.animateCamera(CameraUpdateFactory.newLatLng(markerList.get(currentPoint)),
                        myCancelableCallback);
                challengePoint = currentPoint;
                getSingleChallengeFromMarkerTable(Integer.toString(currentPoint - 1));
                startChallenge(mMap.getCameraPosition().target);

            }else{
                removePolyline(polylineArrayList);
                polylinePoints.clear();
                polylineArrayList.clear();
            }
        }
        @Override
        public void onCancel() {
            polylinePoints.clear();
            removePolyline(polylineArrayList);
            polylineArrayList.clear();
        }
    };
    public void goToNewProject2(View view){
        Intent intent = new Intent(this, NewProject2.class);
        startActivityForResult(intent, NEW_PROJECT_ACTIVITY_REQUEST_CODE);
    }

    public void goToSaveProject(View view){
        Intent intent = new Intent(this, SaveProject.class);
        intent.putParcelableArrayListExtra("MARKER_LIST", markerList);
        intent.putExtra("NEW_PROJECT_NAME", projectName);
        startActivity(intent);
    }

    public void goToLoadProject(View view){
        Intent intent = new Intent(this, LoadProject.class);
        startActivityForResult(intent, LOAD_PROJECT_ACTIVITY_REQUEST_CODE);
    }

    public void goToManageProject(View view){
        Intent intent =  new Intent(this, ManageProject.class);
        intent.putParcelableArrayListExtra("MARKER_LIST", markerList);
        startActivity(intent);
    }

    public void goToChallengeEditor(View view){
        Intent intent = new Intent(this, ChallengeEditor.class);
        startActivity(intent);
    }
    public void goToAddChallenge(View view){
        Intent intent = new Intent(this, AddChallenge.class);
        intent.putExtra("MARKER_INDEX", markerIndexCopy);
        intent.putExtra("PROJECT_NAME", projectName);
        startActivity(intent);
    }

    public void getMarkersListFromEditorMenu(){
        if(markerList.size() != 0){
            mMap.clear();
            markerList.clear();
            polylinePoints.clear();
            polylineArrayList.clear();
            markerHashMap.clear();
            markerNumber = 0;
            markerIndex = -1;
        }
        for(int i=0; i < loadMarkerArrayList2.size(); i++){
            markerList.add(i,  loadMarkerArrayList2.get(i));
        }
    }

    @Override
    public void onBackPressed(){
        closeMapsDialogBuilder();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }
    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == LOAD_PROJECT_ACTIVITY_REQUEST_CODE){
                mMap.clear();
                markerList.clear();
                polylinePoints.clear();
                polylineArrayList.clear();
                markerHashMap.clear();
                markerNumber = 0;
                markerIndex = -1;

                loadMarkerArrayList = data.getParcelableArrayListExtra("MARKER_LIST");
                projectName = data.getStringExtra("LOAD_PROJECT_NAME");
                projectNameTextView.setText("Projekt: " + projectName);
                if(loadMarkerArrayList.size() != 0){
                    for(int i=0; i < loadMarkerArrayList.size(); i++){
                        markerList.add(i,  loadMarkerArrayList.get(i));
                    }
                }
                if (markerList != null){
                    //setOnInfoWindowClickListener(mMap);
                    for(int i=0; i<markerList.size(); i++){
                        drawMarker(markerList.get(i));
                        markerHashMap.put(mMarker, markerIndex);
                    }
                }
            }
            if(requestCode == NEW_PROJECT_ACTIVITY_REQUEST_CODE){
                mMap.clear();
                markerList.clear();
                polylinePoints.clear();
                polylineArrayList.clear();
                markerHashMap.clear();
                markerNumber = 0;
                markerIndex = -1;

                projectName = data.getStringExtra("NEW_PROJECT_NAME2");
                projectNameTextView.setText("Projekt: " + projectName);
            }
        }
    }
    // DIALOG BUILDER VOID'S
    private void closeMapsDialogBuilder(){
        AlertDialog.Builder dialogClose = new AlertDialog.Builder(Maps.this);
        dialogClose.setTitle("Zamknij edytor");
        dialogClose.setMessage("Czy na pewno chcesz zamknąć edytor ?\n" +
                "Utracisz wszystkie niezapisane dane.");
        dialogClose.setCancelable(true);
        dialogClose.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialogClose.setPositiveButton("Zamknij", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = dialogClose.create();
        dialog.show();
    }

    private void deleteMarkerDialogBuilder(){
        AlertDialog.Builder dialogMarker = new AlertDialog.Builder(Maps.this);
        dialogMarker.setTitle("Usuń marker !!!");
        dialogMarker.setMessage("Czy na pewno chcesz usunąć marker ?");
        dialogMarker.setCancelable(true);
        dialogMarker.setNegativeButton("Anuluj",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMarker.hideInfoWindow();
                    }
                });
        dialogMarker.setCancelable(true);
        dialogMarker.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        markerList.remove(mMarker.getPosition());
                        markerHashMap.clear();
                        mMap.clear();
                        removePolyline(polylineArrayList);
                        polylineArrayList.clear();
                        removeDistanceMarkers();
                        distanceMarkerList.clear();
                        markerNumber = 0;
                        markerIndex = -1;
                        if(markerList != null){
                            for(int i=0; i<markerList.size(); i++){
                                drawMarker(markerList.get(i));
                                markerHashMap.put(mMarker, markerIndex);
                            }
                        }
                    }
                });
        AlertDialog dialog = dialogMarker.create();
        dialog.show();
        markerMenuLayout.setVisibility(View.INVISIBLE);
    }

    private void deleteAllMarkersDialogBuilder(){
        AlertDialog.Builder dialogDeleteProject = new AlertDialog.Builder(this);
        dialogDeleteProject.setTitle("Usuń wszystkie markery !!!");
        dialogDeleteProject.setMessage("Czy na pewno chcesz usunąć wszystkie markery ?");
        dialogDeleteProject.setCancelable(true);
        dialogDeleteProject.setNegativeButton("Anuluj",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialogDeleteProject.setCancelable(true);
        dialogDeleteProject.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMap.clear();
                        markerList.clear();
                        distanceMarkerList.clear();
                        loadMarkerArrayList.clear();
                        loadMarkerArrayList2.clear();
                        polylinePoints.clear();
                        polylineArrayList.clear();
                        markerHashMap.clear();
                        markerNumber = 0;
                        markerIndex = -1;
                        Toast.makeText(Maps.this, "Usunięto markery", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog dialog = dialogDeleteProject.create();
        dialog.show();
    }

    private void showChallengeDialogBuilder(Context context,GoogleMap map){

        final AlertDialog.Builder challengeDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.challenge_dialog, null);
        challengeDialogBuilder.setView(view);
        challengeDialogBuilder.setCancelable(true);
        map.stopAnimation();
        final AlertDialog dialog = challengeDialogBuilder.create();
        dialog.show();

        ImageButton challengeDialogOkButton = view.findViewById(R.id.challenge_dialog_ok_button);
        TextView challengeDialogTextView = view.findViewById(R.id.challenge_dialog_text_view);
        final EditText challengeDialogPassword = view.findViewById(R.id.challenge_dialog_password);

        challengeDialogPassword.setVisibility(View.VISIBLE);

        challengeDialogTextView.setText("Challenge: " + challengeName
                + "\nHasło: " + challengePassword
                + "\nOpis zadania:\n " + challenge);
        challengeDialogOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = challengeDialogPassword.getText().toString();
                if(challengePassword.equals(password)){
                    dialog.dismiss();
                    startAnimateCamera(challengePoint);
                }else{
                    Toast.makeText(Maps.this, "Nieprawidłowe hasło !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showMarkerChallengeDialogBuilder(Context context){
        final AlertDialog.Builder challengeDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.marker_challenge_dialog, null);
        challengeDialogBuilder.setView(view);
        challengeDialogBuilder.setCancelable(true);
        final AlertDialog dialog = challengeDialogBuilder.create();
        dialog.show();

        ImageButton closeButton = view.findViewById(R.id.marker_challenge_close_button);
        ImageButton deleteButton = view.findViewById(R.id.marker_challenge_delete_button);
        ImageButton editButton = view.findViewById(R.id.marker_challenge_edit_button);
        TextView name = view.findViewById(R.id.marker_challenge_name);
        TextView password = view.findViewById(R.id.marker_challenge_password);
        TextView info = view.findViewById(R.id.challenge_info_text);

        String index = Integer.toString(markerIndexCopy);
        getSingleChallengeFromMarkerTable(index);

        name.setText("Challenge: " + challengeName);
        password.setText("Hasło: " + challengePassword);
        info.setText("Opis zadania: " + challenge);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSingleChallengeAlertBuilder(dialog);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAddChallenge(findViewById(R.id.add_challenge));
                dialog.dismiss();
            }
        });

    }
    private void deleteSingleChallengeAlertBuilder(final Dialog challengeDialog){
        AlertDialog.Builder dialogDeleteChallenge = new AlertDialog.Builder(this);
        dialogDeleteChallenge.setTitle("Usuń challenge !!!");
        dialogDeleteChallenge.setMessage("Czy na pewno chcesz usunąć challenge ?");
        dialogDeleteChallenge.setCancelable(true);
        dialogDeleteChallenge.setNegativeButton("Anuluj",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialogDeleteChallenge.setCancelable(true);
        dialogDeleteChallenge.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String index = Integer.toString(markerIndexCopy);
                        MarkerPOJO markerPOJO = new MarkerPOJO();
                        markerPOJO.set_challenge(null);
                        markerPOJO.set_challenge_password(null);
                        markerPOJO.set_challenge_name(null);

                        dbHelper.deleteSingleChallengeFromMarkerList(projectName,
                                index, markerPOJO, markerPOJO, markerPOJO);
                        challengeDialog.dismiss();

                        Toast.makeText(Maps.this, "Usunięto challenge", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog deleteDialog = dialogDeleteChallenge.create();
        deleteDialog.show();
    }

}

