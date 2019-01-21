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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class Register extends AppCompatActivity {

    private LinearLayout registerLayout;
    private ProgressBar registerProgressBar;
    private EditText mailEditText;
    private EditText passwordEditText;
    public TextView clickToLoginTextView;
    public Button registerButton;

    private FirebaseAuth firebaseAuth;

    private String password;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Toolbar toolbar = findViewById(R.id.register_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.icon_color));
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        clickToLoginTextView = findViewById(R.id.click_to_login_text);
        registerLayout = findViewById(R.id.register_layout);
        registerProgressBar = findViewById(R.id.register_progressBar);
        mailEditText = findViewById(R.id.email_edit);
        passwordEditText = findViewById(R.id.password_edit);
        registerButton = findViewById(R.id.register_button);

        firebaseAuth = FirebaseAuth.getInstance();

        registerButton.setBackgroundColor(Color.BLACK);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mailEditText.getText().toString();
                password = passwordEditText.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(Register.this, "Pole E-mail nie może być puste",
                            Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this, "Pole hasło nie może być puste",
                            Toast.LENGTH_SHORT).show();
                }else if(password.length() < 6){
                    Toast.makeText(Register.this, "Hasło musi zawierać minimum 6 znaków",
                            Toast.LENGTH_SHORT).show();
                }else{
                    registerLayout.setVisibility(View.INVISIBLE);
                    registerProgressBar.setVisibility(View.VISIBLE);
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(Register.this, "Rejestracja zakończona pomyślnie",
                                                Toast.LENGTH_SHORT).show();
                                        registerLayout.setVisibility(View.VISIBLE);
                                        registerProgressBar.setVisibility(View.GONE);
                                        goToLoginActivity();
                                    }
                                    if(!task.isSuccessful()){
                                        registerLayout.setVisibility(View.VISIBLE);
                                        registerProgressBar.setVisibility(View.GONE);
                                        try{
                                            throw task.getException();

                                        } catch (FirebaseAuthUserCollisionException e){
                                            showUserCollisionAlertBuilder();

                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            Toast.makeText(Register.this, "Błędny email !!!",
                                                    Toast.LENGTH_SHORT).show();

                                        } catch(Exception e) {
                                            showNoConnectionAlertBuilder();
                                        }
                                    }
                                }
                            });
                }
            }
        });

        clickToLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLoginActivity();
            }
        });

    }
    public void goToLoginActivity(){
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
        finish();
    }

    //Alert Dialog Builders
    private void showUserCollisionAlertBuilder(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(Register.this);
        alertBuilder.setTitle("Błąd rejestracji !!!");
        alertBuilder.setMessage("Podany adres email jest już zarejestrowny");
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
    private void showNoConnectionAlertBuilder(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(Register.this);
        alertBuilder.setTitle("Błąd rejestracji !!!");
        alertBuilder.setMessage("Brak połączenia. Sprawdź transfer danych i spróbuj ponownie");
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
