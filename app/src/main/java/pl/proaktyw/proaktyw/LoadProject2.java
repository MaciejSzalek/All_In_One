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

public class LoadProject2 extends AppCompatActivity {

    EditText loadProjectName2;
    ListView loadProjectListView2;

    DBHelper dbHelper;
    ArrayList<String> projectNameArrayList2 = new ArrayList<>();
    ArrayList<LatLng> loadMarkerArrayList2 = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    String loadProjectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_project2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Toolbar toolbar = findViewById(R.id.load_project2_toolbar);
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
        loadProjectName2 = findViewById(R.id.load_project2_name);
        loadProjectListView2 = findViewById(R.id.load_project2_list);

        getProjectNameListDatabase();

        loadProjectListView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadProjectName2.setText(projectNameArrayList2.get(position));
                loadProjectName = loadProjectName2.getText().toString();
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
            String text = loadProjectName2.getText().toString();
            if(projectNameArrayList2.contains(text)){
                getMarkerListFromDatabase();
                if(loadMarkerArrayList2 != null){
                    goToMapActivity();
                }
            }else{
                if(TextUtils.isEmpty(text)){
                    Toast.makeText(LoadProject2.this, "Zaznacz lub wpisz nazwę projekt",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(LoadProject2.this, "Projekt o tej nazwie nie istnieje",
                            Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getProjectNameListDatabase(){
        if(projectNameArrayList2 != null){
            projectNameArrayList2.clear();
        }
        List<ProjectName> projectNameList = dbHelper.getAllProjectName();
        for (ProjectName projectName: projectNameList){
            projectNameArrayList2.add(projectName.get_projectName());
        }
        Collections.sort(projectNameArrayList2);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.list_white_text, projectNameArrayList2);
        loadProjectListView2.setAdapter(arrayAdapter);
    }
    private void getMarkerListFromDatabase(){
        String text = loadProjectName2.getText().toString();
        List<MarkerPOJO> markerPOJOList = dbHelper.getAllMarkers(text);
        for(MarkerPOJO markerPOJO : markerPOJOList){
            int index = markerPOJO.get_marker_index();
            double Lat = markerPOJO.get_marker_latitude();
            double Lng = markerPOJO.get_marker_longitude();
            loadMarkerArrayList2.add(index, new LatLng(Lat, Lng));
        }
    }
    public void goToMapActivity(){
        loadProjectName = loadProjectName2.getText().toString();
        Intent intent = new Intent(LoadProject2.this, Maps.class);
        intent.putExtra("ID", "LOAD_PROJECT_2");
        intent.putParcelableArrayListExtra("LOAD_MARKER_LIST", loadMarkerArrayList2);
        intent.putExtra("LOAD_PROJECT_NAME", loadProjectName);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onResume(){
        super.onResume();
        getMarkerListFromDatabase();
    }
    @Override
    protected void onPause(){
        super.onPause();
    }
}
