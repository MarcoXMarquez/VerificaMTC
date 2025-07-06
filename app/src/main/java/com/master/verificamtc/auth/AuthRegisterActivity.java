package com.master.verificamtc.auth;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.master.verificamtc.R;
import com.master.verificamtc.models.User;

import java.util.Calendar;
import java.util.HashMap;

public class AuthRegisterActivity extends AppCompatActivity {

    private EditText etDni, etNames, etLastnames, etBirthdate, etEmail, etPassword;
    private Button btnRegister;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Referencia a la rama "users" en Firebase
        usersRef = FirebaseDatabase.getInstance()
                .getReference("users");

        // Mapeo de vistas
        etDni        = findViewById(R.id.register_dni);
        etNames      = findViewById(R.id.register_names);
        etLastnames  = findViewById(R.id.register_lastnames);
        etBirthdate  = findViewById(R.id.register_birthdate);
        etEmail      = findViewById(R.id.register_email);
        etPassword   = findViewById(R.id.register_password);
        btnRegister  = findViewById(R.id.register_button);

        // Mostrar DatePicker al tocar el campo Fecha de nacimiento
        etBirthdate.setOnClickListener(v -> showDatePicker());

        // Acción del botón de registro
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR), m = c.get(Calendar.MONTH), d = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // Formato YYYY-MM-DD
                    String fecha = String.format("%04d-%02d-%02d", year, month+1, dayOfMonth);
                    etBirthdate.setText(fecha);
                }, y, m, d);
        dpd.show();
    }

    private void attemptRegister() {
        String id   = etDni.getText().toString().trim();
        String fn   = etNames.getText().toString().trim();
        String ln   = etLastnames.getText().toString().trim();
        String bd   = etBirthdate.getText().toString().trim();
        String email= etEmail.getText().toString().trim();
        String pwd  = etPassword.getText().toString().trim();

        if(id.isEmpty()||fn.isEmpty()||ln.isEmpty()||bd.isEmpty()||email.isEmpty()||pwd.isEmpty()){
            Toast.makeText(this,"Complete todos los campos",Toast.LENGTH_SHORT).show();
            return;
        }

        // Referencia a /users en Firebase
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        String key = usersRef.push().getKey();
        User user = new User(
                etDni.getText().toString(),
                etNames.getText().toString(),
                etLastnames.getText().toString(),
                etBirthdate.getText().toString(),
                etEmail.getText().toString(),
                etPassword.getText().toString()
        );
        usersRef.child(id).setValue(user)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
