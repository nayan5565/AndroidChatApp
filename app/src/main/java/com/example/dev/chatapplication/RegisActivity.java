package com.example.dev.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Dev on 1/15/2018.
 */

public class RegisActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edtEmail, edtUser, edtPass;
    private Button btnReg, btnLogin;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    public static String userNa="";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regis);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtUser = (EditText) findViewById(R.id.edtUser);
        edtPass = (EditText) findViewById(R.id.edtPass);
        btnReg = (Button) findViewById(R.id.btnReg);
        btnReg.setOnClickListener(this);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnReg) {
            Toast.makeText(RegisActivity.this, "click regis", Toast.LENGTH_SHORT).show();
            final String user, pass, email;
            user = edtUser.getText().toString().trim();
            pass = edtPass.getText().toString().trim();
            email = edtEmail.getText().toString().trim();
            userNa=user;
            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {
                firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = firebaseAuth.getCurrentUser().getUid();

                            DatabaseReference currentUserId = databaseReference.child(userId);
                            currentUserId.child("Name").setValue(user);
                            Toast.makeText(RegisActivity.this, "Successfully registration", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisActivity.this, LoginActivity.class));
                            finish();
                        }
                    }
                });
            }

        }
        if (view.getId() == R.id.btnLogin) {

            startActivity(new Intent(RegisActivity.this, LoginActivity.class));
            finish();
        }

    }
}
