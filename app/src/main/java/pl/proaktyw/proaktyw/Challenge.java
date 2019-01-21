package pl.proaktyw.proaktyw;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Challenge extends AppCompatActivity {

    MultiAutoCompleteTextView challengeNameMultiTextView;
    MultiAutoCompleteTextView challengeMultiTextView;
    MultiAutoCompleteTextView challengePasswordMultiTextView;

    DBHelper dbHelper;

    String challengeName;
    String challenge;
    String challengePassword;
    Intent intentCheck;

    ArrayList<String> challengeNameArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Toolbar toolbar = findViewById(R.id.challenge_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.icon_color));
        toolbar.setTitle("Challenge");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        challengeNameMultiTextView = findViewById(R.id.challenge_name_multiTextView);
        challengeMultiTextView = findViewById(R.id.challenge_multiTextView);
        challengePasswordMultiTextView = findViewById(R.id.challenge_password_multiTextView);

        dbHelper = new DBHelper(this);
        getChallengeList();
        intentCheck = getIntent();

        if(intentCheck != null) {
            String strData = intentCheck.getStringExtra("ID");
            if (strData.equals("EDIT")) {
                challengeName = getIntent().getStringExtra("CHALLENGE_NAME");
                getChallengeText(challengeName);
            }
        }
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

            challengeName = challengeNameMultiTextView.getText().toString();
            challenge = challengeMultiTextView.getText().toString();
            challengePassword = challengePasswordMultiTextView.getText().toString();

            if(TextUtils.isEmpty(challengeName)){
                Toast.makeText(Challenge.this, "Pole nazwa nie może być puste",
                        Toast.LENGTH_SHORT).show();
            }else if(TextUtils.isEmpty(challenge)){
                Toast.makeText(Challenge.this, "Pole zadanie nie może być puste",
                        Toast.LENGTH_SHORT).show();
            }else if(TextUtils.isEmpty(challengePassword)){
                Toast.makeText(Challenge.this, "Pole hasło nie może być puste",
                        Toast.LENGTH_SHORT).show();
            }else{
                if(challengeNameArrayList.contains(challengeName)){
                    saveChallengeAlertBuilder();
                }else{
                    createNewChallenge();
                    getChallengeList();
                    finish();
                }

            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getChallengeList(){
        if(challengeNameArrayList != null){
            challengeNameArrayList.clear();
        }
        List<ChallengePOJO> projectNameList = dbHelper.getChallengeList();
        for (ChallengePOJO challengeName: projectNameList){
            challengeNameArrayList.add(challengeName.get_challenge_name());
        }
    }
    private void updateChallenge(){
        if(challengeNameArrayList.size() != 0){
            if(dbHelper.checkChallengeExists(challengeName)){
                ChallengePOJO challengePOJO = new ChallengePOJO();
                challengePOJO.set_challenge(challenge);
                challengePOJO.set_challenge_password(challengePassword);

                dbHelper.updateChallenge(challengeName,
                        challengePOJO, challengePOJO);

                Toast.makeText(Challenge.this, "Edytowano challenge", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createNewChallenge(){

        String name = challengeNameMultiTextView.getText().toString();
        String challenge = challengeMultiTextView.getText().toString();
        String password = challengePasswordMultiTextView.getText().toString();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(Challenge.this, "Pole nazwa nie może być puste",
                    Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(challenge)){
            Toast.makeText(Challenge.this, "Pole challenge nie może być puste", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(Challenge.this, "Pole password nie może być puste", Toast.LENGTH_SHORT).show();
        }else{

            ChallengePOJO challengePOJO = new ChallengePOJO();
            challengePOJO.set_challenge_name(name);
            challengePOJO.set_challenge(challenge);
            challengePOJO.set_challenge_password(password);


            dbHelper.createNewChallenge(
                    challengePOJO,
                    challengePOJO,
                    challengePOJO);

            Toast.makeText(Challenge.this, "Dodano challenge", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveChallengeAlertBuilder(){
        AlertDialog.Builder dialogSaveProject = new AlertDialog.Builder(Challenge.this);
        dialogSaveProject.setTitle("Challenge o tej nazwie już  istnieje !!!");
        dialogSaveProject.setMessage("Zapisać zmiany ?");
        dialogSaveProject.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialogSaveProject.setPositiveButton("Zapisz", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateChallenge();
                getChallengeList();
                finish();
            }
        });
        AlertDialog dialog = dialogSaveProject.create();
        dialog.show();
    }
    private void getChallengeText(String name){

        ChallengePOJO challengePOJO;
        challengePOJO = dbHelper.getChallengeFromDatabase(name);

        challenge = challengePOJO.get_challenge();
        challengePassword = challengePOJO.get_challenge_password();

        if(challenge != null){
            challengeNameMultiTextView.setText(name);
            challengeMultiTextView.setText(challenge);
            challengePasswordMultiTextView.setText(challengePassword);
        }
    }
}
