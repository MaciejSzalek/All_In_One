package pl.proaktyw.proaktyw;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class EditorMenu extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_menu);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }
    public void goToNewProject(View view){
        Intent intent = new Intent(this, NewProject.class);
        startActivity(intent);
    }
    public void goToLoadProject(View view){
        Intent intent = new Intent(this, LoadProject2.class);
        startActivity(intent);
    }
}
