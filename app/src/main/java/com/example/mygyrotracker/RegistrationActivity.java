package com.example.mygyrotracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private EditText etUser, etPass;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etUser = findViewById(R.id.etUsername);
        etPass = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String email = etUser.getText().toString().trim();
            String pass = etPass.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Заповніть всі поля", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            String uid = mAuth.getCurrentUser().getUid();

                            // Додати профіль у Firestore
                            Map<String, Object> user = new HashMap<>();
                            user.put("email", email);
                            user.put("deviceId", Build.ID);
                            user.put("deviceModel", Build.MODEL);
                            user.put("osVersion", Build.VERSION.RELEASE);

                            firestore.collection("users")
                                    .document(uid)
                                    .set(user)
                                    .addOnSuccessListener(a -> {
                                        Toast.makeText(this, "Registered", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Cloud error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                    );

                        } else {
                            Toast.makeText(this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
