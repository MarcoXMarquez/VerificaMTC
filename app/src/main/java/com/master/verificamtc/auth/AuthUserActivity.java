package com.master.verificamtc.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.master.verificamtc.R;
import com.master.verificamtc.user.dashboard.UserDashboardActivity;
import com.master.verificamtc.utils.SecurityHelper;

public class AuthUserActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private Button loginButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    static {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize Firebase persistence FIRST
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_login);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.userlogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        TextView signup = findViewById(R.id.signup_user);

        if (username == null || password == null || loginButton == null || signup == null) {
            Toast.makeText(this, "Error initializing UI components", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupLoginButton();
        signup.setOnClickListener(view -> {
            try {
                Intent intent = new Intent(AuthUserActivity.this, AuthRegisterActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("AuthUserActivity", "Signup Error", e);
                Toast.makeText(AuthUserActivity.this,
                        "Error al abrir registro: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(view -> {
            String dni = username.getText().toString().trim();
            String inputPassword = password.getText().toString().trim();

            if (dni.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(AuthUserActivity.this, "DNI y contraseña requeridos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!dni.matches("\\d{8}")) {
                Toast.makeText(AuthUserActivity.this, "DNI debe tener 8 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

            authenticateUser(dni, inputPassword);
        });
    }

    private void authenticateUser(String dni, String inputPassword) {
        mDatabase.child("users")
                .orderByChild("dni")
                .equalTo(dni)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String storedPassword = userSnapshot.child("password").getValue(String.class);
                                if (storedPassword == null) {
                                    Toast.makeText(AuthUserActivity.this,
                                            "Error en los datos del usuario",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (SecurityHelper.checkPassword(inputPassword, storedPassword)) {
                                    proceedToDashboard(userSnapshot.getKey());
                                } else {
                                    Toast.makeText(AuthUserActivity.this,
                                            "Contraseña incorrecta",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(AuthUserActivity.this,
                                    "Usuario no encontrado",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(AuthUserActivity.this,
                                "Error de conexión: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void proceedToDashboard(String userId) {
        Toast.makeText(this, "Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, UserDashboardActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, redirect to dashboard
            mDatabase.child("users")
                    .orderByChild("email")
                    .equalTo(currentUser.getEmail())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    proceedToDashboard(snapshot.getKey());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(AuthUserActivity.this,
                                    "Error al verificar usuario",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}