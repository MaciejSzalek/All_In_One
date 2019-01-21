package pl.proaktyw.proaktyw;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PullProject extends AppCompatActivity {

    EditText pullProjectEditText;
    ListView pullProjectListView;
    RelativeLayout pullProjectListLayout;

    AlertDialog downloadDialog;
    AlertDialog projectExistsDialog;
    TextView projectExistsText;
    EditText projectExistsEditText;
    Button projectExistsCancelButton;
    Button projectExistsOkButton;

    ProgressBar progressBar;
    ProgressBar downloadProgressBar;
    long downloadTotalSize[] = {0};
    int progress = 0;

    DBHelper dbHelper;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ArrayAdapter arrayAdapter;

    ArrayList<String> projectNameArrayList = new ArrayList<>();
    ArrayList<String> pullProjectNameArrayList = new ArrayList<>();
    String pullProjectName;
    String newProjectName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pull_project);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Toolbar toolbar = findViewById(R.id.pull_project_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.icon_color));
        toolbar.setTitle("Pobierz projekt");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        pullProjectEditText = findViewById(R.id.pull_project_name);
        pullProjectListView = findViewById(R.id.pull_project_list);
        pullProjectListLayout = findViewById(R.id.pull_project_list_layout);
        progressBar = findViewById(R.id.pull_project_progress_bar);

        dbHelper = new DBHelper(PullProject.this);
        firebaseDatabase = FirebaseDatabase.getInstance("https://fir-proaktyw.firebaseio.com/");
        newProjectName = null;

        if(pullProjectNameArrayList != null){
            pullProjectNameArrayList.clear();
        }
        if(projectNameArrayList != null){
            projectNameArrayList.clear();
        }

        getProjectNameListFromDatabase();
        getProjectNameListFromFirebase();

        pullProjectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pullProjectName = pullProjectNameArrayList.get(position);
                pullProjectEditText.setText(pullProjectName);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.confirm_toolbar, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if(i == R.id.confirm_ic){
            pullProjectName = pullProjectEditText.getText().toString();
            if(pullProjectNameArrayList.contains(pullProjectName)){
                if(projectNameArrayList.contains(pullProjectName)){
                    showDownloadProjectExistsAlertDialog(PullProject.this);
                }else{
                    new MyTask().execute();
                }
            }else{
                if(TextUtils.isEmpty(pullProjectName)){
                    Toast.makeText(PullProject.this, "Zaznacz lub wpisz nazwę projekt",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(PullProject.this, "Projekt o tej nazwie nie istnieje",
                            Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void downloadProject(){

        if(newProjectName != null){
            ProjectName projectName = new ProjectName();
            projectName.set_projectName(newProjectName);
            dbHelper.addNewProjectName(projectName);
        }else{
            newProjectName = pullProjectName;
            ProjectName projectName = new ProjectName();
            projectName.set_projectName(newProjectName);
            dbHelper.addNewProjectName(projectName);
        }

        databaseReference = firebaseDatabase.getReference("games");
        final DatabaseReference ref = databaseReference.child(pullProjectName);
        DatabaseReference projectRef = ref.child("project_details");
        projectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final long[] total = {0};
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    long bytes = data.getChildrenCount();
                    total[0] = total[0] + bytes;

                    Integer index = data.child("index").getValue(Integer.class);
                    Double lat = data.child("lat").getValue(Double.class);
                    Double lng = data.child("lng").getValue(Double.class);
                    String challenge = data.child("challenge").getValue(String.class);
                    String challengePassword = data.child("challenge_password").getValue(String.class);
                    String challengeName = data.child("challenge_name").getValue(String.class);


                    MarkerPOJO markerPOJO = new MarkerPOJO();
                    markerPOJO.set_project_name(newProjectName);
                    markerPOJO.set_marker_index(index);
                    markerPOJO.set_marker_latitude(lat);
                    markerPOJO.set_marker_longitude(lng);
                    markerPOJO.set_challenge(challenge);
                    markerPOJO.set_challenge_password(challengePassword);
                    markerPOJO.set_challenge_name(challengeName);

                    dbHelper.addMarkersToTable(markerPOJO, markerPOJO, markerPOJO,
                            markerPOJO);
                    dbHelper.addChallengeToMarkerList(newProjectName, String.valueOf(index),
                            markerPOJO, markerPOJO, markerPOJO);

                    progress = (int) ((100 * total[0]) / downloadTotalSize[0]);
                    downloadProgressBar.setProgress(progress);
                    if(progress >= 100){
                        downloadDialog.dismiss();
                        Toast.makeText(PullProject.this, "Projekt pobrany", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getProjectNameListFromDatabase(){
        if(projectNameArrayList != null){
            projectNameArrayList.clear();
        }
        List<ProjectName> projectNameList = dbHelper.getAllProjectName();
        for (ProjectName projectName: projectNameList){
            projectNameArrayList.add(projectName.get_projectName());
        }
    }
    private void getProjectNameListFromFirebase(){

        databaseReference = firebaseDatabase.getReference("games");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(pullProjectNameArrayList.size() < 0){
                    pullProjectNameArrayList.clear();
                }
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    String name = snapshot.getKey();
                    pullProjectNameArrayList.add(name);
                }
                if(pullProjectNameArrayList.size() > 0){
                    progressBar.setVisibility(View.INVISIBLE);
                    pullProjectListLayout.setVisibility(View.VISIBLE);
                    Collections.sort(pullProjectNameArrayList);
                    arrayAdapter = new ArrayAdapter<>(PullProject.this,
                            R.layout.list_white_text, pullProjectNameArrayList);
                    pullProjectListView.setAdapter(arrayAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PullProject.this, "Brak połączenia...", Toast.LENGTH_LONG).show();
            }
        });
    }

    private class MyTask extends AsyncTask<String, Integer, String>{

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            getDownloadSize();
            showProgressAlertBuilder(PullProject.this);
        }
        @Override
            protected String doInBackground(String... strings) {
            downloadProject();
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values){
            super.onProgressUpdate(values);
        }
        @Override
        protected void onPostExecute(String string){
            super.onPostExecute(string);
            //downloadDialog.dismiss();
        }
    }
    private void getDownloadSize(){
        databaseReference = firebaseDatabase.getReference("games");
        final DatabaseReference projectRef = databaseReference.child(pullProjectName);
        projectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    long bytes = data.getChildrenCount();
                    downloadTotalSize[0] = downloadTotalSize[0] + bytes;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PullProject.this, "Połączenie z serwerem nie powiodło się",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    //Alert builder
    private void showDownloadProjectExistsAlertDialog(Context context){
        final AlertDialog.Builder projectDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.project_exists_dialog, null);
        projectDialogBuilder.setView(view);
        projectDialogBuilder.setCancelable(true);
        projectExistsDialog = projectDialogBuilder.create();
        projectExistsDialog.show();

        projectExistsText = view.findViewById(R.id.project_exists_text);
        projectExistsEditText = view.findViewById(R.id.project_exists_edit_text);
        projectExistsCancelButton = view.findViewById(R.id.project_exists_cancel_button);
        projectExistsOkButton = view.findViewById(R.id.project_exists_ok_button);

        projectExistsText.setText("Projekt o nazwie " + pullProjectName +" już istnieje !"
                + "\nMożesz zapisać projekt pod inną nazwą");
        projectExistsEditText.setText(pullProjectName);

        projectExistsCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                projectExistsDialog.dismiss();
            }
        });

        projectExistsOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newProjectName = projectExistsEditText.getText().toString();
                new MyTask().execute();
                projectExistsDialog.dismiss();
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

        downloadProgressBar = view.findViewById(R.id.download_progress_bar_widget);

    }
}
