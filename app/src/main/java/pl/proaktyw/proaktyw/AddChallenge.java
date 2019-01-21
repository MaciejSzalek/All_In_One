package pl.proaktyw.proaktyw;

import android.content.Context;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddChallenge extends AppCompatActivity {

    ImageButton addChallengeInfoButton;
    ImageButton addChallengeButton;
    ListView addChallengeListView;

    DBHelper dbHelper;

    ArrayList<String> challengeNameArrayList = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    String projectName;
    String challengeName;
    String challengeText;
    String challengePassword;
    String markerIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_challenge);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        addChallengeInfoButton = findViewById(R.id.add_challenge_info_button);
        addChallengeButton = findViewById(R.id.add_challenge_button);

        addChallengeListView = findViewById(R.id.add_challenge_list);

        dbHelper = new DBHelper(this);
        getChallengeList();

        projectName = getIntent().getStringExtra("PROJECT_NAME");
        markerIndex = Integer.toString(getIntent().getIntExtra("MARKER_INDEX", 0));

        Toolbar toolbar = findViewById(R.id.add_challenge_editor_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.icon_color));
        toolbar.setTitle("Dodaj challenge");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addChallengeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(challengeNameArrayList.contains(challengeName)){
                    addChallengeToMarkerList();
                }else{
                    if(TextUtils.isEmpty(challengeName)){
                        Toast.makeText(AddChallenge.this, "Zaznacz challenge",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        addChallengeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                challengeName = challengeNameArrayList.get(position);
            }
        });

        addChallengeInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(challengeNameArrayList.contains(challengeName)){
                    showChallengeInfoDialog(AddChallenge.this);
                }else{
                    if(TextUtils.isEmpty(challengeName)){
                        Toast.makeText(AddChallenge.this, "Zaznacz challenge",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private void addChallengeToMarkerList(){

        ChallengePOJO challengePOJO;
        challengePOJO = dbHelper.getChallengeFromDatabase(challengeName);
        challengeText = challengePOJO.get_challenge();
        challengePassword = challengePOJO.get_challenge_password();

        MarkerPOJO markerPOJO = new MarkerPOJO();
        markerPOJO.set_challenge(challengeText);
        markerPOJO.set_challenge_password(challengePassword);
        markerPOJO.set_challenge_name(challengeName);

        dbHelper.addChallengeToMarkerList(projectName, markerIndex,
                markerPOJO, markerPOJO, markerPOJO);

        Toast.makeText(AddChallenge.this, "Dodano challenge",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void getChallengeList(){
        if(challengeNameArrayList != null){
            challengeNameArrayList.clear();
        }
        List<ChallengePOJO> challengeNameList = dbHelper.getChallengeList();
        for (ChallengePOJO challengeName: challengeNameList){
            challengeNameArrayList.add(challengeName.get_challenge_name());
        }
        Collections.sort(challengeNameArrayList);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.list_white_text, challengeNameArrayList);
        addChallengeListView.setAdapter(arrayAdapter);
    }
    private void showChallengeInfoDialog(Context context){

        final AlertDialog.Builder challengeInfoDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.challenge_info, null);
        challengeInfoDialogBuilder.setView(view);
        challengeInfoDialogBuilder.setCancelable(true);
        final AlertDialog dialog = challengeInfoDialogBuilder.create();
        dialog.show();

        ImageButton infoCloseButton = view.findViewById(R.id.challenge_info_close_button);
        TextView infoProjectName = view.findViewById(R.id.challenge_info_project_name);
        TextView infoPassword = view.findViewById(R.id.challenge_info_password);
        TextView infoText = view.findViewById(R.id.challenge_info_text);

        ChallengePOJO challengePOJO;
        challengePOJO = dbHelper.getChallengeFromDatabase(challengeName);
        challengeText = challengePOJO.get_challenge();
        challengePassword = challengePOJO.get_challenge_password();

        infoProjectName.setText("Challenge: " + challengeName);
        infoPassword.setText("Hasło: " + challengePassword);
        infoText.setText("Treść zadania:\n" + challengeText);

        infoCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}

