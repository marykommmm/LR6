package com.example.mygyrotracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etUser, etPass;
    private Button btnLogin, btnGoReg;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etUser = findViewById(R.id.etUsername);
        etPass = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoReg = findViewById(R.id.btnGoRegister);

        btnLogin.setOnClickListener(v -> {
            String email = etUser.getText().toString().trim();
            String pass = etPass.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Заповніть всі поля", Toast.LENGTH_SHORT).show();
                return;
            }

            // Вхід Firebase
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                            // ⬇️ ЗБЕРЕЖЕННЯ UID
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            getSharedPreferences("gyro_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("user_id", uid)
                                    .apply();

                            // ⬇️ ПЕРЕХІД У МЕЙН
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                    } else {
                            Toast.makeText(this,
                                    "Error: " + task.getException().toString(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        btnGoReg.setOnClickListener(v ->
                startActivity(new Intent(this, RegistrationActivity.class))
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Якщо юзер вже залогінений — одразу в MainActivity
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
