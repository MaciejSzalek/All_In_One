package pl.proaktyw.proaktyw;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class TestOdczytuBazy extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseStorage storage;
    private static final String FIREBASE_URL = "https://fir-proaktyw.firebaseio.com/";

    DBHelper dbHelper;
    Button readButton;
    Button deleteButton;
    TextView textView;
    TextView testView2;
    EditText editText;

    String projectTable;
    String projectName;
    String markerListSize;

    long downloadTotalSize[] = {0};
    int progress = 0;
    AlertDialog downloadDialog;
    ProgressBar progressBar;



    ArrayList<LatLng> markerLatLngArrayList = new ArrayList<>();
    ArrayList<String> projectNameArrayList = new ArrayList<>();
    ArrayList<Integer> markerIndexArrayList = new ArrayList<>();
    ArrayList<Integer> idArrayList  = new ArrayList<>();

    ArrayList<Integer> challengeIndexList = new ArrayList<>();
    ArrayList<LatLng> challengeLatLngList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_odczytu_bazy);

        progressBar = findViewById(R.id.download_progress_bar_widget);
        readButton = findViewById(R.id.button_read_data);
        deleteButton = findViewById(R.id.button_usun_test);
        textView = findViewById(R.id.read_text_view);
        testView2 = findViewById(R.id.test_text2);
        editText = findViewById(R.id.test_odczytu_editText);

        projectTable = getIntent().getStringExtra("PROJECT_TABLE");
        projectName = getIntent().getStringExtra("PROJECT_NAME");
        markerListSize = getIntent().getStringExtra("MARKER_LIST_SIZE");

        dbHelper = new DBHelper(this);
        firebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_URL);
        storage = FirebaseStorage.getInstance();

        if(idArrayList.size() == 0){
            getAllMarkers();
        }

        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getDownloadSize();
               new DownloadTask().execute();
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getDownloadFileSize();
            }
        });

    }
    private class DownloadTask extends AsyncTask<String, Integer, String>{

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            getDownloadSize();
            showProgressAlertBuilder(TestOdczytuBazy.this);
        }

        @Override
        protected String doInBackground(String... params) {
            downloadProject();
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values){
        }
        @Override
        protected void onPostExecute(String string){
            super.onPostExecute(string);
        }
    }
    private void downloadProject(){
        final long[] total = {0};
        final String projectName = "test";
        databaseReference = firebaseDatabase.getReference("games");
        final DatabaseReference projectRef = databaseReference.child(projectName);
        projectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot data: dataSnapshot.getChildren()){
                    long bytes = data.getChildrenCount();
                    total[0] = total[0] + bytes;

                    progress = (int) ((100 * total[0]) / downloadTotalSize[0]);
                    textView.append("\nTOTAL SIZE: " + Long.toString(downloadTotalSize[0])
                            + "\nprogress: " + Integer.toString(progress)
                            + "\nBytes: " + Long.toString(bytes)
                            + " total = " + Long.toString(total[0]));

                    progressBar.setProgress(progress);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void downloadTest(){
        final long[] total = {0};
        final String projectName = "test";
        databaseReference = firebaseDatabase.getReference("games");
        final DatabaseReference projectRef = databaseReference.child(projectName);
        projectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot data: dataSnapshot.getChildren()){
                    String marker = data.getKey();
                    long bytes = data.getChildrenCount();
                    Integer index = data.child("index").getValue(Integer.class);
                    Double lat = data.child("lat").getValue(Double.class);
                    Double lng = data.child("lng").getValue(Double.class);
                    String challengeName = data.child("challenge_name").getValue(String.class);

                    /*textView.append("\nmarker: " + marker
                            + "\nindex: " + String.valueOf(index)
                            + "\nlat: " + lat + " lng: " + lng
                            + "\nchallenge_name " + challengeName);*/

                    total[0] = total[0] + bytes;
                    textView.append("\nBytes: " + Long.toString(bytes)
                            + " TOTAL = " + Long.toString(total[0]));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getDownloadSize(){
        final String projectName = "test";
        databaseReference = firebaseDatabase.getReference("games");
        final DatabaseReference projectRef = databaseReference.child(projectName);
        projectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    long bytes = data.getChildrenCount();
                    downloadTotalSize[0] = downloadTotalSize[0] + bytes;
                    testView2.setText("TOTAL = " + Long.toString(downloadTotalSize[0]));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void  showProgressAlertBuilder(Context context){
        final AlertDialog.Builder progressDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.download_progress_bar, null);
        progressDialogBuilder.setView(view);
        progressDialogBuilder.setCancelable(true);
        downloadDialog = progressDialogBuilder.create();
        downloadDialog.show();

        progressBar = view.findViewById(R.id.download_progress_bar_widget);

    }
    private void displayMarkerData(){
        for(int i=0 ; i < markerIndexArrayList.size(); i++){
            textView.append("\n Marker list size: " + markerListSize + "\nLoad marker size: " +
                    markerLatLngArrayList.size()+
                    "\nName: " + projectNameArrayList.get(i)
                            + "\nId: " + idArrayList.get(i)
                            + "\nMarker index: " + markerIndexArrayList.get(i)
                            + "\nLatLng: " + markerLatLngArrayList.get(i)
                            + "\nchallenge index: " + challengeIndexList.get(i)
                            + "\nchallenge LatLng: " + challengeLatLngList.get(i));
        }
    }

    private void getAllMarkers(){

        List<MarkerPOJO> markerPOJOList =  dbHelper.getAllMarkers(projectName);
        for(MarkerPOJO markerPOJO : markerPOJOList){
            String text = markerPOJO.get_project_name();
            int Id = markerPOJO.get_id();
            int index = markerPOJO.get_marker_index();
            double Lat = markerPOJO.get_marker_latitude();
            double Lng = markerPOJO.get_marker_longitude();

            idArrayList.add(Id);
            markerIndexArrayList.add(index) ;
            markerLatLngArrayList.add(new LatLng(Lat, Lng));
            projectNameArrayList.add(text);
        }
    }
}
