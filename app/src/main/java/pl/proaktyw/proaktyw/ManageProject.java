package pl.proaktyw.proaktyw;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManageProject extends AppCompatActivity {

    ListView manageProjectListView;
    ImageButton deleteProjectButton;
    ImageButton infoProjectButton;
    ImageButton pullProjectButton;
    ImageButton publishGameButton;

    AlertDialog projectExistsDialog;
    AlertDialog publishDialog;
    AlertDialog progressDialog;
    TextView projectExistsText;
    EditText projectExistsEditText;
    Button projectExistsCancelButton;
    Button projectExistsOkButton;

    DBHelper dbHelper;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    ArrayList<String> manageProjectArrayList = new ArrayList<>();
    ArrayList<String> markerIndexArrayList = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    String projectName;
    String newProjectName;
    boolean projectExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_project);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Toolbar toolbar = findViewById(R.id.manage_project_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.icon_color));
        toolbar.setTitle("Manager projektów");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        manageProjectListView = findViewById(R.id.manage_project_list_view);
        deleteProjectButton = findViewById(R.id.manage_project_delete_button);
        infoProjectButton = findViewById(R.id.manage_project_info_button);
        pullProjectButton = findViewById(R.id.manage_project_pull_button);
        publishGameButton = findViewById(R.id.manage_publish_button);

        dbHelper = new DBHelper(ManageProject.this);
        getProjectNameListDatabase();

        firebaseDatabase = FirebaseDatabase.getInstance("https://fir-proaktyw.firebaseio.com/");

        manageProjectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                projectName = manageProjectArrayList.get(position);
            }
        });

        infoProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        pullProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(haveNetworkConnection()){
                    goToPullProject(findViewById(R.id.pull_project));
                }else{
                    Toast.makeText(ManageProject.this, "Trenfser danych wyłączony !!! " +
                            "\n Sprawdź ustawienia.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manageProjectArrayList.contains(projectName)){
                    showDeleteProjectDialogBuilder();
                }else{
                    if(TextUtils.isEmpty(projectName)){
                        Toast.makeText(ManageProject.this, "Zaznacz projekt",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        publishGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(haveNetworkConnection()){
                    if(TextUtils.isEmpty(projectName)){
                        Toast.makeText(ManageProject.this, "Zaznacz projekt",
                                Toast.LENGTH_SHORT).show();
                    }else{
                        showPublishProjectAlertBuilder(ManageProject.this);
                    }
                }else{
                    Toast.makeText(ManageProject.this, "Trenfser danych wyłączony !!! " +
                                    "\n Sprawdź ustawienia.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    @Override
    protected void onResume(){
        super.onResume();
        getProjectNameListDatabase();
    }
    public void goToPullProject(View view){
        Intent intent = new Intent(ManageProject.this, PullProject.class);
        startActivity(intent);
    }

    private void checkProjectExistsOnFirebase(){
        showProgressAlertBuilder(ManageProject.this);
        final int[] count = {0};
        DatabaseReference reference = firebaseDatabase.getReference("games");
        reference.orderByKey().equalTo(newProjectName).
                addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                count[0]++;
                projectExists = dataSnapshot.exists();
                if(count[0] >= dataSnapshot.getChildrenCount()){
                    progressDialog.dismiss();
                    if(projectExists){
                        showProjectExistsAlertDialog(ManageProject.this);
                    }else{
                        newProjectName = projectName;
                        publishGame();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ManageProject.this, "Brak połączenia...", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getProjectNameListDatabase(){
        if(manageProjectArrayList != null){
            manageProjectArrayList.clear();
        }
        List<ProjectName> projectNameList = dbHelper.getAllProjectName();
        for (ProjectName projectName: projectNameList){
            manageProjectArrayList.add(projectName.get_projectName());
        }
        Collections.sort(manageProjectArrayList);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.list_white_text, manageProjectArrayList);
        manageProjectListView.setAdapter(arrayAdapter);
    }

    private boolean haveNetworkConnection(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        return isConnected;
    }

    private void publishGame(){
        final int[] count = {0};
        showProgressAlertBuilder(ManageProject.this);
        if(markerIndexArrayList.size() != 0){
            markerIndexArrayList.clear();
        }else{
            List<MarkerPOJO> markerPOJOList = dbHelper.getAllMarkers(projectName);
            for(MarkerPOJO markerPOJO: markerPOJOList){
                int index = markerPOJO.get_marker_index();
                markerIndexArrayList.add(Integer.toString(index));
            }
        }
        UserPOJO userPOJO = dbHelper.getUserDatabase("1");
        String email = userPOJO.getEmail();

        databaseReference = firebaseDatabase.getReference("games");

        for(int i=0; i<markerIndexArrayList.size(); i++ ){

            String index = markerIndexArrayList.get(i);
            MarkerPOJO markerPOJO = dbHelper.getSingleMarkerData(projectName, index);
            Integer markerIndex = markerPOJO.get_marker_index();
            Double lat = markerPOJO.get_marker_latitude();
            Double lng = markerPOJO.get_marker_longitude();
            String challenge = markerPOJO.get_challenge();
            String challengePassword = markerPOJO.get_challenge_password();
            String challengeName = markerPOJO.get_challenge_name();

            databaseReference.child(newProjectName).child("email").setValue(email);

            databaseReference.child(newProjectName).child("project_details").child("marker_" + index)
                    .child("index").setValue(markerIndex);
            databaseReference.child(newProjectName).child("project_details").child("marker_" + index)
                    .child("lat").setValue(lat);
            databaseReference.child(newProjectName).child("project_details").child("marker_" + index)
                    .child("lng").setValue(lng);
            databaseReference.child(newProjectName).child("project_details").child("marker_" + index)
                    .child("challenge").setValue(challenge);
            databaseReference.child(newProjectName).child("project_details").child("marker_" + index)
                    .child("challenge_password").setValue(challengePassword);
            databaseReference.child(newProjectName).child("project_details").child("marker_" + index)
                    .child("challenge_name").setValue(challengeName);

            count[0] = i;
            if(count[0] >= markerIndexArrayList.size() - 1){
                progressDialog.dismiss();
            }

        }
    }



    //Alert builder
    private void  showProgressAlertBuilder(Context context){
        final AlertDialog.Builder progressDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.progress_dialog, null);
        progressDialogBuilder.setView(view);
        progressDialogBuilder.setCancelable(true);

        progressDialog = progressDialogBuilder.create();
        progressDialog.show();
    }
    private void showProjectExistsAlertDialog(Context context){
        final AlertDialog.Builder projectDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.project_exists_dialog, null);
        projectDialogBuilder.setView(view);
        projectDialogBuilder.setCancelable(true);
        projectExistsDialog = projectDialogBuilder.create();
        projectExistsDialog.show();
        publishDialog.dismiss();

        projectExistsText = view.findViewById(R.id.project_exists_text);
        projectExistsEditText = view.findViewById(R.id.project_exists_edit_text);
        projectExistsCancelButton = view.findViewById(R.id.project_exists_cancel_button);
        projectExistsOkButton = view.findViewById(R.id.project_exists_ok_button);

        projectExistsText.setText("Projekt o nazwie: " + newProjectName + " już istnieje !"
                + "\nMożesz opublikować projekt pod inną nazwą");
        projectExistsEditText.setText(newProjectName);

        projectExistsCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                projectExistsDialog.dismiss();
            }
        });

        projectExistsOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newProjectName = projectExistsEditText.getText().toString();
                checkProjectExistsOnFirebase();
                projectExistsDialog.dismiss();
            }
        });
    }

    private void showPublishProjectAlertBuilder(Context context){
        final AlertDialog.Builder projectDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.publish_dialog, null);
        projectDialogBuilder.setView(view);
        projectDialogBuilder.setCancelable(true);
        publishDialog = projectDialogBuilder.create();
        publishDialog.show();

        TextView publishMessage = view.findViewById(R.id.publish_dialog_message);
        Button publishCancelButton = view.findViewById(R.id.publish_dialog_cancel_button);
        Button publishOkButton = view.findViewById(R.id.publish_dialog_ok_button);

        publishMessage.setText("Czy na pewno chcesz opublikować \nprojekt: " + projectName + " ?");

        publishOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newProjectName = projectName;
                checkProjectExistsOnFirebase();
                publishDialog.dismiss();
            }
        });

        publishCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishDialog.dismiss();
            }
        });
    }

    private void showDeleteProjectDialogBuilder(){
        AlertDialog.Builder dialogMarker = new AlertDialog.Builder(ManageProject.this);
        dialogMarker.setTitle("Usuń projekt !!!");
        dialogMarker.setMessage("Czy na pewno chcesz usunąć projekt: "
                + projectName + " ?");
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
                        dbHelper.deleteProjectName(projectName);
                        dbHelper.deleteMarkersFromList(projectName);
                        getProjectNameListDatabase();
                    }
                });
        AlertDialog dialog = dialogMarker.create();
        dialog.show();
    }
}
