package com.master.verificamtc.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.master.verificamtc.R;
import com.master.verificamtc.admin.dashboard.AdminDashboardActivity;
import android.widget.EditText;
import android.os.Handler;

public class AuthAdminActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int loginAttempts = 0;
    private static final int MAX_ATTEMPTS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        findViewById(R.id.loginButton).setOnClickListener(v -> {
            String email = ((EditText) findViewById(R.id.username)).getText().toString();
            String password = ((EditText) findViewById(R.id.password)).getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this.peekAvailableContext(), "Ingrese email y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            authenticateAdmin(email, password);
        });
    }

    private void authenticateAdmin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        verifyAdminRole(user.getUid());
                    } else {
                        loginAttempts++;
                        if (loginAttempts >= MAX_ATTEMPTS) {
                            //blockAccessTemporarily();
                        } else {
                            Toast.makeText(this, "Credenciales incorrectas. Intentos: " +
                                    (MAX_ATTEMPTS - loginAttempts), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void verifyAdminRole(String uid) {
        db.collection("admins").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && document.getBoolean("isAdmin")) {
                        startActivity(new Intent(this, AdminDashboardActivity.class));
                        finish();
                    } else {
                        mAuth.signOut();
                        Toast.makeText(this, "No tiene privilegios de administrador", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void blockAccessTemporarily() {
        // Implementar bloqueo por 5 minutos
        Toast.makeText(this, "Demasiados intentos. Espere 5 minutos", Toast.LENGTH_LONG).show();
        findViewById(R.id.loginButton).setEnabled(false);
        new Handler().postDelayed(() -> {
            findViewById(R.id.loginButton).setEnabled(true);
            loginAttempts = 0;
        }, 300000); // 5 minutos
    }
}