package pl.proaktyw.proaktyw;

import android.content.Intent;
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

public class LoadProject extends AppCompatActivity {

    EditText loadProjectName;
    ListView loadProjectListView;

    DBHelper dbHelper;
    ArrayList<String> projectNameArrayList = new ArrayList<>();
    ArrayList<LatLng> loadMarkerArrayList = new ArrayList<>();
    ArrayAdapter arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_project);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Toolbar toolbar = findViewById(R.id.load_project_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.icon_color));
        toolbar.setTitle("Wczytaj projekt");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        dbHelper = new DBHelper(this);

        loadProjectName = findViewById(R.id.load_project_name);
        loadProjectListView = findViewById(R.id.load_project_list);

        getProjectNameListDatabase();

        loadProjectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadProjectName.setText(projectNameArrayList.get(position));
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
            String text = loadProjectName.getText().toString();
            if(projectNameArrayList.contains(text)){
                getMarkerListFromDatabase();
                goToMapActivityWithResult();
            }else{
                if(TextUtils.isEmpty(text)){
                    Toast.makeText(LoadProject.this, "Zaznacz lub wpisz nazwę projekt",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(LoadProject.this, "Projekt o tej nazwie nie istnieje",
                            Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getProjectNameListDatabase(){
        if(projectNameArrayList != null){
            projectNameArrayList.clear();
        }
        List<ProjectName> projectNameList = dbHelper.getAllProjectName();
        for (ProjectName projectName: projectNameList){
            projectNameArrayList.add(projectName.get_projectName());
        }
        Collections.sort(projectNameArrayList);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.list_white_text, projectNameArrayList);
        loadProjectListView.setAdapter(arrayAdapter);
    }
    private void getMarkerListFromDatabase(){
        String text = loadProjectName.getText().toString();
        List<MarkerPOJO> markerPOJOList = dbHelper.getAllMarkers(text);
        for(MarkerPOJO markerPOJO : markerPOJOList){
            int index = markerPOJO.get_marker_index();
            double Lat = markerPOJO.get_marker_latitude();
            double Lng = markerPOJO.get_marker_longitude();
            loadMarkerArrayList.add(index, new LatLng(Lat, Lng));
        }
    }
    public void goToMapActivityWithResult(){
        String text = loadProjectName.getText().toString();
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("MARKER_LIST", loadMarkerArrayList);
        intent.putExtra("LOAD_PROJECT_NAME", text);
        setResult(RESULT_OK, intent);
        finish();
    }
}
