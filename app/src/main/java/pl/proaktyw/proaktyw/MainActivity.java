package pl.proaktyw.proaktyw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    public void  goToNewGameActivity(View view){
        Intent intent = new Intent(this, NewGameActivity.class);
        startActivity(intent);
    }
    public void  goToChatActivity(View view){
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }
    public void goToGameEditorActivity(View view){
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
    public void  goToInstructionActivity(View view){
        Intent intent = new Intent(this, TEST.class);
        startActivity(intent);
    }

}
