package pl.proaktyw.proaktyw;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

public class ChallengeEditor extends AppCompatActivity {

    ListView challengeEditorList;

    ImageButton challengeNewButton;
    ImageButton challengeDeleteButton;
    ImageButton challengeInfoButton;
    ImageButton challengeEditButton;

    DBHelper dbHelper;

    ArrayList<String> challengeNameArrayList = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    String challengeName;
    String challengeText;
    String challengePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge_editor);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Toolbar toolbar = findViewById(R.id.challenge_editor_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.icon_color));
        toolbar.setTitle("Challenge edytor");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        challengeEditorList = findViewById(R.id.challenge_editor_list);

        challengeNewButton = findViewById(R.id.challenge_new_button);
        challengeDeleteButton = findViewById(R.id.challenge_delete_button2);
        challengeInfoButton = findViewById(R.id.challenge_info_button);
        challengeEditButton = findViewById(R.id.challenge_edit_button);

        dbHelper = new DBHelper(this);
        getChallengeList();

        challengeEditorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                challengeName = challengeNameArrayList.get(position);
            }
        });

        challengeInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(challengeNameArrayList.contains(challengeName)){
                    showChallengeInfoDialog(ChallengeEditor.this);
                }else{
                    if(TextUtils.isEmpty(challengeName)){
                        Toast.makeText(ChallengeEditor.this, "Zaznacz challenge",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        challengeNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChallenge();
            }
        });

        challengeDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteChallenge();
                getChallengeList();
            }
        });

        challengeEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotToEditChallenge();
            }
        });

    }
    private void gotToEditChallenge(){
        if(challengeNameArrayList.contains(challengeName)){
            Intent intent = new Intent(ChallengeEditor.this, Challenge.class);
            intent.putExtra("ID", "EDIT");
            intent.putExtra("CHALLENGE_NAME", challengeName);
            startActivity(intent);
        }else{
            if(TextUtils.isEmpty(challengeName)){
                Toast.makeText(ChallengeEditor.this, "Zaznacz challenge",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void goToChallenge(){
        Intent intent = new Intent(ChallengeEditor.this, Challenge.class);
        intent.putExtra("ID", "NEW");
        startActivity(intent);
    }

    private void deleteChallenge(){
        if(challengeNameArrayList.contains(challengeName)){
            showDeleteChallengeDialogBuilder();
        }else{
            if(TextUtils.isEmpty(challengeName)){
                Toast.makeText(ChallengeEditor.this, "Zaznacz challenge",
                        Toast.LENGTH_SHORT).show();
            }
        }
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
        challengeEditorList.setAdapter(arrayAdapter);
    }
    @Override
    protected void onResume(){
        super.onResume();
        getChallengeList();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    //DIALOG BUILDER
    private void showDeleteChallengeDialogBuilder(){
        AlertDialog.Builder dialogMarker = new AlertDialog.Builder(ChallengeEditor.this);
        dialogMarker.setTitle("Usuń challenge !!!");
        dialogMarker.setMessage("Czy na pewno chcesz usunąć "
                + challengeName + " ?");
        dialogMarker.setCancelable(true);
        dialogMarker.setNegativeButton("Anuluj",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialogMarker.setCancelable(true);
        dialogMarker.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteChallenge(challengeName);
                        getChallengeList();
                    }
                });
        AlertDialog dialog = dialogMarker.create();
        dialog.show();
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
