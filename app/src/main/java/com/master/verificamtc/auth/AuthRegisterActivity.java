package com.master.verificamtc.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.app.DatePickerDialog;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.master.verificamtc.helpers.FirebaseDatabaseHelper;
import com.master.verificamtc.utils.SecurityHelper;
import com.master.verificamtc.R;

import java.util.Calendar;
import java.util.Locale;

public class AuthRegisterActivity extends AppCompatActivity {
    private FirebaseDatabaseHelper dbHelper;
    private EditText reDni, reNames, reLastNames, reEmail, rePassword, reDate;
    Button reRegisterButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase helper
        dbHelper = new FirebaseDatabaseHelper(this);

        reDni = findViewById(R.id.register_dni);
        reNames = findViewById(R.id.register_names);
        reLastNames = findViewById(R.id.register_lastnames);
        reEmail = findViewById(R.id.register_email);
        rePassword = findViewById(R.id.register_password);
        reDate= findViewById(R.id.register_birthdate);
        reRegisterButton = findViewById(R.id.register_button);

        reDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();

                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        AuthRegisterActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            String fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                            reDate.setText(fechaSeleccionada);
                        },
                        year, month, day
                );

                datePickerDialog.show();
            }
        });
        reRegisterButton.setOnClickListener(v -> addUser());
    }
    private void addUser() {
        String userId = reDni.getText().toString().trim();
        String firstName = reNames.getText().toString().trim();
        String lastName = reLastNames.getText().toString().trim();
        String birthDate = reDate.getText().toString().trim();
        String email = reEmail.getText().toString().trim();
        String password = rePassword.getText().toString().trim();

        if (userId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() ||
                birthDate.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        // Validación mejorada
        if (!SecurityHelper.isPasswordStrong(password)) {
            rePassword.setError("La contraseña debe tener:\n- 8+ caracteres\n- 1 mayúscula\n- 1 minúscula\n- 1 número");
            rePassword.requestFocus();
            return;
        }

        try {
            String hashedPassword = SecurityHelper.hashPassword(password);
            dbHelper.addUser(userId, firstName, lastName, birthDate, email, hashedPassword);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Error de seguridad: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
}