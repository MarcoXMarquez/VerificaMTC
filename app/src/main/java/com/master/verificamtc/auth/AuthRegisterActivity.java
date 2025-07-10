package com.master.verificamtc.auth;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.app.DatePickerDialog;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.master.verificamtc.helpers.FirebaseDatabaseHelper;
import com.master.verificamtc.admin.dashboard.RecognitionActivity;
import com.master.verificamtc.utils.SecurityHelper;
import com.master.verificamtc.R;
import java.util.Calendar;
import java.util.Locale;

public class AuthRegisterActivity extends AppCompatActivity {
    private FirebaseDatabaseHelper dbHelper;
    private ImageView validationStatus;
    private boolean isIdentityValidated = false;
    private EditText reDni, reNames, reLastNames, reEmail, rePassword, reDate;
    Button reRegisterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        dbHelper = new FirebaseDatabaseHelper(this);

        // Inicializar vistas
        reDni = findViewById(R.id.register_dni);
        reNames = findViewById(R.id.register_names);
        reLastNames = findViewById(R.id.register_lastnames);
        reEmail = findViewById(R.id.register_email);
        rePassword = findViewById(R.id.register_password);
        reDate = findViewById(R.id.register_birthdate);
        reRegisterButton = findViewById(R.id.register_button);
        Button btnValidateIdentity = findViewById(R.id.btn_validate_identity);
        validationStatus = findViewById(R.id.identity_validation_status);

        // Configurar listeners
        setupDatePicker();
        setupRegisterButton();
        setupIdentityValidationButton(btnValidateIdentity);
    }

    private void setupDatePicker() {
        reDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AuthRegisterActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String fechaSeleccionada = String.format(Locale.getDefault(),
                                "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                        reDate.setText(fechaSeleccionada);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void setupRegisterButton() {
        reRegisterButton.setOnClickListener(v -> addUser());
    }

    private void setupIdentityValidationButton(Button btnValidateIdentity) {
        btnValidateIdentity.setOnClickListener(v -> {
            String dniUsuario = reDni.getText().toString().trim();

            if (dniUsuario.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese su DNI antes de validar identidad", Toast.LENGTH_SHORT).show();
                reDni.setError("DNI requerido");
                reDni.requestFocus();
                return;
            }

            Toast.makeText(this, "Sincronizando rostros...", Toast.LENGTH_SHORT).show();

            dbHelper.getAllFaces(new FirebaseDatabaseHelper.SyncCompletionListener() {
                @Override
                public void onSyncComplete(boolean success) {
                    runOnUiThread(() -> {
                        if (success) {
                            Intent intent = new Intent(AuthRegisterActivity.this, RecognitionActivity.class);
                            intent.putExtra("DNI_USUARIO", dniUsuario);
                            startActivityForResult(intent, 1); // Usamos startActivityForResult
                        } else {
                            Toast.makeText(AuthRegisterActivity.this,
                                    "Error al sincronizar rostros", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) { // 1 es un código arbitrario para identificar la actividad
            isIdentityValidated = true;
            validationStatus.setImageResource(R.drawable.ic_check_green); // Asegúrate de tener este drawable
            validationStatus.setVisibility(View.VISIBLE);
        }
    }
    private void addUser() {
        // Primero verificar si la identidad está validada
        if (!isIdentityValidated) {
            Toast.makeText(this, "Por favor valide su identidad antes de registrarse", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = reDni.getText().toString().trim();
        String firstName = reNames.getText().toString().trim();
        String lastName = reLastNames.getText().toString().trim();
        String birthDate = reDate.getText().toString().trim();
        String email = reEmail.getText().toString().trim();
        String password = rePassword.getText().toString().trim();

        if (userId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() ||
                birthDate.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!SecurityHelper.isPasswordStrong(password)) {
            rePassword.setError("La contraseña debe tener:\n- 8+ caracteres\n- 1 mayúscula\n- 1 minúscula\n- 1 número");
            rePassword.requestFocus();
            return;
        }

        try {
            String hashedPassword = SecurityHelper.hashPassword(password);
            dbHelper.addUser(userId, firstName, lastName, birthDate, email, hashedPassword);
            Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Error de seguridad: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}