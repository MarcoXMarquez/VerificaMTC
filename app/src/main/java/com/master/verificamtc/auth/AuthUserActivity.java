package com.master.verificamtc.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    private EditText username; // DNI o email
    private EditText password;
    private Button loginButton;
    private TextView signup;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_userlogin);

        // Inicializar Firebase Auth y Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.userlogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signup = findViewById(R.id.signup);

        loginButton.setOnClickListener(view -> {
            String dniEmail = username.getText().toString().trim();
            String inputPassword = password.getText().toString().trim();

            if (dniEmail.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(AuthUserActivity.this, "DNI/Email y contraseña requeridos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Autenticar usando DNI (almacenado en Firebase) o email
            authenticateUser(dniEmail, inputPassword);
        });

        signup.setOnClickListener(view -> {
            startActivity(new Intent(AuthUserActivity.this, AuthRegisterActivity.class));
        });
    }

    private void authenticateUser(String dniEmail, String inputPassword) {
        // Primero intentamos encontrar el usuario por DNI o email
        mDatabase.child("users")
                .orderByChild("dni") // Asume que guardas el DNI en un campo 'dni'
                .equalTo(dniEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Usuario encontrado por DNI
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String email = userSnapshot.child("email").getValue(String.class);
                                if (email != null) {
                                    // Autenticar con Firebase Auth usando el email
                                    signInWithEmailPassword(email, inputPassword,
                                            userSnapshot.getKey());
                                }
                            }
                        } else {
                            // Si no se encuentra por DNI, asumimos que es un email
                            signInWithEmailPassword(dniEmail, inputPassword, null);
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

    private void signInWithEmailPassword(String email, String password, String userId) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Si no tenemos el userId (cuando se autentica por email)
                            if (userId == null) {
                                getUserIdByEmail(user.getEmail());
                            } else {
                                proceedToDashboard(userId);
                            }
                        }
                    } else {
                        Toast.makeText(AuthUserActivity.this,
                                task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getUserIdByEmail(String email) {
        mDatabase.child("users")
                .orderByChild("email")
                .equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                proceedToDashboard(userSnapshot.getKey());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(AuthUserActivity.this,
                                "Error al obtener datos de usuario",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void proceedToDashboard(String userId) {
        Toast.makeText(AuthUserActivity.this, "Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AuthUserActivity.this, UserDashboardActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verificar si el usuario ya está logueado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            getUserIdByEmail(currentUser.getEmail());
        }
    }
}