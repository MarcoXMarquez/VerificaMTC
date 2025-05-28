package com.master.verificamtc;

import android.content.Intent;
import android.database.Cursor;
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

public class LoginUserActivity extends AppCompatActivity {
    EditText username; // Este será el DNI
    EditText password;
    Button loginButton;
    TextView signup;
    DatabaseScheme dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_userlogin);

        dbHelper = new DatabaseScheme(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.userlogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signup = findViewById(R.id.signup);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dni = username.getText().toString().trim();
                String inputPassword = password.getText().toString().trim();

                if (dni.isEmpty() || inputPassword.isEmpty()) {
                    Toast.makeText(LoginUserActivity.this, "DNI y contraseña requeridos", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int userId = Integer.parseInt(dni);
                    if (authenticateUser(userId, inputPassword)) {
                        Toast.makeText(LoginUserActivity.this, "Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginUserActivity.this, UserLobbyActivity.class);
                        intent.putExtra("USER_ID", userId); // Pasamos el ID del usuario
                        startActivity(intent);
                        finish(); // Cierra la actividad de login
                    } else {
                        Toast.makeText(LoginUserActivity.this, "DNI o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(LoginUserActivity.this, "DNI debe ser numérico", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginUserActivity.this, RegisterActivity.class));
            }
        });
    }

    private boolean authenticateUser(int dni, String inputPassword) {
        // 1. Obtener el usuario de la base de datos
        Cursor cursor = dbHelper.getReadableDatabase().query(
                DatabaseScheme.TABLE_AUTH,
                new String[]{DatabaseScheme.COLUMN_ID, DatabaseScheme.COLUMN_PASSWORD},
                DatabaseScheme.COLUMN_ID + " = ?",
                new String[]{String.valueOf(dni)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            // 2. Obtener el hash almacenado
            String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseScheme.COLUMN_PASSWORD));
            cursor.close();

            // 3. Verificar la contraseña
            return PasswordHasher.checkPassword(inputPassword, storedHash);
        }

        if (cursor != null) {
            cursor.close();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}