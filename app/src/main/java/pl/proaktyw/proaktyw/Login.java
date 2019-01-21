package pl.proaktyw.proaktyw;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;


public class Login extends AppCompatActivity {

    private LinearLayout loginLayout;
    private ProgressBar loginProgressBar;
    private EditText emailEditText;
    private EditText passwordEditText;
    public Button registerTextView;
    public Button loginButton;
    public CheckBox loginCheckBox;

    private DBHelper dbHelper;
    private FirebaseAuth firebaseAuth;

    private String email;
    private String password;

    public int ID = 1;

    String testLogin = "admin";
    String testPassword = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Toolbar toolbar = findViewById(R.id.login_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.icon_color));
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loginLayout = findViewById(R.id.login_layout);
        loginProgressBar = findViewById(R.id.login_progressBar);
        emailEditText = findViewById(R.id.email_edit);
        passwordEditText = findViewById(R.id.password_edit);
        registerTextView = findViewById(R.id.register_text);
        loginButton = findViewById(R.id.login_button);
        loginCheckBox = findViewById(R.id.login_checkBox);

        dbHelper = new DBHelper(this);
        firebaseAuth = FirebaseAuth.getInstance();

        if(!dbHelper.checkUserExists(Integer.toString(ID))){
            UserPOJO userPOJO = new UserPOJO();
            userPOJO.setEmail("");
            userPOJO.setUserPassword("");
            dbHelper.createUser(userPOJO, userPOJO);
        }
        UserPOJO userPOJO = dbHelper.getUserDatabase(Integer.toString(ID));
        password = userPOJO.getUserPassword();
        if(!TextUtils.isEmpty(password)){
            emailEditText.setText(userPOJO.getEmail());
            passwordEditText.setText(userPOJO.getUserPassword());
            loginCheckBox.setChecked(true);
        }

        loginButton.setBackgroundColor(Color.BLACK);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();

                if(email.equals(testLogin) && password.equals(testPassword)){
                    if(loginCheckBox.isChecked()){
                        rememberUser();
                    }else{
                        notRememberUser();
                    }
                    goToEditorMenuActivity();
                    finish();
                    Toast.makeText(Login.this, "ADMIN LOGIN...",
                            Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(email)){
                    Toast.makeText(Login.this, "Pole E-mail nie może być puste",
                            Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(password)){
                    Toast.makeText(Login.this, "Pole hasło nie może być puste",
                            Toast.LENGTH_SHORT).show();
                }else{
                    loginProgressBar.setVisibility(View.VISIBLE);
                    loginLayout.setVisibility(View.INVISIBLE);
                    firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        if(loginCheckBox.isChecked()){
                                            rememberUser();
                                        }else{
                                            notRememberUser();
                                        }
                                        loginProgressBar.setVisibility(View.INVISIBLE);
                                        loginLayout.setVisibility(View.VISIBLE);
                                        goToEditorMenuActivity();
                                        finish();
                                        Toast.makeText(Login.this, "ZALOGOWANY",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    if(!task.isSuccessful()){
                                        loginProgressBar.setVisibility(View.INVISIBLE);
                                        loginLayout.setVisibility(View.VISIBLE);
                                        showNoConnectionAlertBuilder();
                                    }
                                }
                            });
                }
            }
        });

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegisterActivity();
            }
        });


    }
    private void notRememberUser(){

        UserPOJO userPOJO = new UserPOJO();
        userPOJO.setEmail(email);
        userPOJO.setUserPassword("");

        if(dbHelper.checkUserExists(Integer.toString(ID))){
            dbHelper.updateUser(Integer.toString(ID), userPOJO, userPOJO);
        }else{
            dbHelper.createUser(userPOJO, userPOJO);
        }
    }
    private void rememberUser(){
        UserPOJO userPOJO = new UserPOJO();
        userPOJO.setEmail(email);
        userPOJO.setUserPassword(password);

        if(dbHelper.checkUserExists(Integer.toString(ID))){
            dbHelper.updateUser(Integer.toString(ID), userPOJO, userPOJO);
        }else{
            dbHelper.createUser(userPOJO, userPOJO);
        }
    }
    public void goToEditorMenuActivity(){
        Intent intent = new Intent(this, EditorMenu.class);
        startActivity(intent);
    }
    public void goToRegisterActivity(){
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);
        finish();
    }
    //Alert Builder
    private void showNoConnectionAlertBuilder(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(Login.this);
        alertBuilder.setTitle("Błąd logowania !!!");
        alertBuilder.setMessage("Sprawdź transfer danych i spróbuj ponownie");
        alertBuilder.setCancelable(false);
        alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }
}
