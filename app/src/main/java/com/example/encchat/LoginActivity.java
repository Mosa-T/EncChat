package com.example.encchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {


    private TextInputLayout t_email, t_pass;
    private Button log_but;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;

    private DatabaseReference user_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pd = new ProgressDialog(this);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.login_appbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        t_email = (TextInputLayout) findViewById(R.id.log_email);
        t_pass = (TextInputLayout) findViewById(R.id.log_pass);
        log_but = (Button) findViewById(R.id.log_but);

        user_ref = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();

        log_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pd.setMessage("Logging you in..");
                pd.show();

                String email = t_email.getEditText().getText().toString().trim();
                String pass = t_pass.getEditText().getText().toString().trim();

                if(TextUtils.isEmpty(email)) {
                    t_email.setError("Empty Field");
                    pd.dismiss();
                }
                else if(TextUtils.isEmpty(pass)) {
                    t_pass.setError("Empty Field");
                    pd.dismiss();
                }
                else signIn(email,pass);
            }
        });

    }

    private void signIn(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (task.isSuccessful()) {
                                pd.dismiss();

                            String Uid = mAuth.getCurrentUser().getUid();

                            String token = FirebaseInstanceId.getInstance().getToken();
                            user_ref.child(Uid).child("device_token").setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent mainintent = new Intent(LoginActivity.this,MainActivity.class);
                                    mainintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(mainintent);
                                    finish();
                                }
                            });



                        }else {

                            pd.hide();
                            Toast.makeText(LoginActivity.this,"Error",Toast.LENGTH_SHORT).show();


                        }

                        // ...
                    }
                });
    }
}
