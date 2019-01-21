package pl.proaktyw.proaktyw;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SaveProject extends AppCompatActivity {

    ListView saveProjectListView;
    EditText saveEditText;
    String projectName;
    String newProjectName;

    DBHelper dbHelper;

    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> saveProjectArrayList = new ArrayList<>();
    ArrayList<LatLng> markerList = new ArrayList<>();
    ArrayList<Integer> markerIndexList = new ArrayList<>();
    ArrayList<Integer> idArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_project);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Toolbar toolbar = findViewById(R.id.save_project_toolbar);
        toolbar.setTitle("Zapisz projekt");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                saveEditText.setText("");
            }
        });

        saveEditText = findViewById(R.id.save_project_name);
        saveProjectListView = findViewById(R.id.save_project_list);

        markerList = getIntent().getParcelableArrayListExtra("MARKER_LIST");
        newProjectName = getIntent().getStringExtra("NEW_PROJECT_NAME");

        dbHelper = new DBHelper(this);
        getProjectNameListDatabase();

        if(newProjectName != null){
            saveEditText.setText(newProjectName);
        }else{
            saveEditText.setText("");
        }
        saveProjectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                saveEditText.setText(saveProjectArrayList.get(position));
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
            projectName = saveEditText.getText().toString();
            if(saveProjectArrayList.contains(projectName)){
                saveProjectAlertBuilder();
            }else{
                if(TextUtils.isEmpty(projectName)){
                    Toast.makeText(SaveProject.this, "Pole nie może być puste", Toast.LENGTH_SHORT).show();
                }else{
                    createNewProjectName();
                    getProjectNameListDatabase();
                    addMarkerToTable();
                    saveEditText.setText("");
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createNewProjectName(){
        String text = saveEditText.getText().toString();
        ProjectName projectName = new ProjectName();
        projectName.set_projectName(text);
        dbHelper.addNewProjectName(projectName);
    }

    private void addMarkerToTable(){
        String text = saveEditText.getText().toString();
        if(markerList.size() !=0){
            for(int i=0; i<markerList.size(); i++){

                MarkerPOJO markerPOJO = new MarkerPOJO();

                markerPOJO.set_project_name(text);
                markerPOJO.set_marker_index(i);
                markerPOJO.set_marker_latitude(markerList.get(i).latitude);
                markerPOJO.set_marker_longitude(markerList.get(i).longitude);

                dbHelper.addMarkersToTable(markerPOJO, markerPOJO, markerPOJO,
                        markerPOJO);
            }
            Toast.makeText(SaveProject.this, "Projekt zapisany", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(SaveProject.this, "lista pusta", Toast.LENGTH_SHORT).show();
        }
    }
    public void saveProject(String text){

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

    private void getProjectNameListDatabase(){
        if(saveProjectArrayList != null){
            saveProjectArrayList.clear();
        }
        List<ProjectName> projectNameList = dbHelper.getAllProjectName();
        for (ProjectName projectName: projectNameList){
            saveProjectArrayList.add(projectName.get_projectName());
        }
        Collections.sort(saveProjectArrayList);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.list_white_text, saveProjectArrayList);
        saveProjectListView.setAdapter(arrayAdapter);
    }
    private void deleteSingleMarker(String project, String markerIndex){
        dbHelper.deleteSingleMarker(project, markerIndex);
    }
    private void updateMarkerIdIndex(){
        String text = saveEditText.getText().toString();
        for(int i=0; i < idArrayList.size(); i++){
            String id = Integer.toString(idArrayList.get(i));
            MarkerPOJO markerPOJO = new MarkerPOJO();
            markerPOJO.set_marker_index(i);
            dbHelper.updateMarkerIndex(text, id, markerPOJO);
        }
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
    private void saveProjectAlertBuilder(){
        AlertDialog.Builder dialogSaveProject = new AlertDialog.Builder(SaveProject.this);
        dialogSaveProject.setTitle("ZAPISZ PROJEKT");
        dialogSaveProject.setMessage("Projekt o tej nazwie już istnieje\nZapisać projekt?");
        dialogSaveProject.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialogSaveProject.setPositiveButton("Zapisz", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveProject(projectName);
                Toast.makeText(SaveProject.this, "Projekt zapisany", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog dialog = dialogSaveProject.create();
        dialog.show();
    }
}
