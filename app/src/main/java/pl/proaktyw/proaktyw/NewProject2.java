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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewProject2 extends AppCompatActivity {

    EditText newProjectNameEditText2;
    ListView newProjectListView2;

    DBHelper dbHelper;
    ArrayList<String> projectNameArrayList = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    String newProjectName2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_project2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Toolbar toolbar = findViewById(R.id.new_project_toolbar2);
        toolbar.setTitleTextColor(getResources().getColor(R.color.icon_color));
        toolbar.setTitle("Nowy projekt");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        dbHelper = new DBHelper(this);
        newProjectNameEditText2 = findViewById(R.id.new_project_name2);
        newProjectListView2 = findViewById(R.id.new_project_list2);

        getProjectNameListDatabase();
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
            String text = newProjectNameEditText2.getText().toString();
            if(projectNameArrayList.contains(text)){
                Toast.makeText(NewProject2.this, "Projekt o tej nazwie już istnieje", Toast.LENGTH_SHORT).show();
            }else{
                if(TextUtils.isEmpty(text)){
                    Toast.makeText(NewProject2.this, "Pole nie może być puste", Toast.LENGTH_SHORT).show();
                }else{
                    newProjectName2 = text;
                    createNewProjectName(newProjectName2);
                    getProjectNameListDatabase();
                    goToMapActivity();
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
        newProjectListView2.setAdapter(arrayAdapter);
    }
    private void createNewProjectName(String newProjectName){
        ProjectName projectName = new ProjectName();
        projectName.set_projectName(newProjectName);
        dbHelper.addNewProjectName(projectName);
    }
    private void goToMapActivity(){
        Intent intent = new Intent(NewProject2.this, Maps.class);
        intent.putExtra("NEW_PROJECT_NAME2", newProjectName2);
        setResult(RESULT_OK, intent);
        finish();
    }
}
