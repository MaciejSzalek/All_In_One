package pl.proaktyw.proaktyw;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewGameActivity extends AppCompatActivity {

    EditText newGameEditText;
    ListView newGameListView;

    DBHelper dbHelper;
    ArrayList<String> newGameList = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    String projectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Toolbar toolbar = findViewById(R.id.new_game_toolbar);
        toolbar.setBackgroundColor(Color.BLACK);
        toolbar.setTitleTextColor(getResources().getColor(R.color.icon_color));
        toolbar.setTitle("Nowa gra");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        dbHelper = new DBHelper(this);
        newGameEditText = findViewById(R.id.new_game_edit_text);
        newGameListView = findViewById(R.id.new_game_list);

        getGameListFromDatabase();

        newGameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                newGameEditText.setText(newGameList.get(position));
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
            String text = newGameEditText.getText().toString();
            if(newGameList.contains(text)){
                goToGame();
            }else{
                if(TextUtils.isEmpty(text)){
                    Toast.makeText(NewGameActivity.this, "Zaznacz lub wpisz nazwę gry",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(NewGameActivity.this, "Gra o tej nazwie nie istnieje",
                            Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void getGameListFromDatabase(){
        if(newGameList != null){
            newGameList.clear();
        }
        List<ProjectName> gameList = dbHelper.getAllProjectName();
        for (ProjectName projectName: gameList){
            newGameList.add(projectName.get_projectName());
        }
        Collections.sort(newGameList);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.list_white_text, newGameList);
        newGameListView.setAdapter(arrayAdapter);
    }
    private void goToGame(){
        projectName = newGameEditText.getText().toString();
        Intent intent = new Intent(NewGameActivity.this, GAME.class);
        intent.putExtra("PROJECT_NAME", projectName);
        startActivity(intent);
    }
    @Override
    protected void onStart(){
        super.onStart();
    }
    @Override
    protected void onPause(){
        super.onPause();
    }
}