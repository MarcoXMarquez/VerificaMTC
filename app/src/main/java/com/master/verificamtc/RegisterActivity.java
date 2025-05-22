package com.master.verificamtc;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.app.DatePickerDialog;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    EditText dni, names, lastNames, email, password, date;

    Button registerButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dni = findViewById(R.id.register_dni);
        names = findViewById(R.id.register_names);
        lastNames = findViewById(R.id.register_lastnames);
        email = findViewById(R.id.register_email);
        password = findViewById(R.id.register_password);
        date= findViewById(R.id.register_birthdate);
        registerButton = findViewById(R.id.register_button);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();

                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        RegisterActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            String fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                            date.setText(fechaSeleccionada);
                        },
                        year, month, day
                );

                datePickerDialog.show();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                DatabaseScheme myDB = new DatabaseScheme (RegisterActivity.this);
                myDB.addAuth(Integer.valueOf(dni.getText().toString().trim()),
                        names.getText().toString().trim(),
                        lastNames.getText().toString().trim(),
                        date.getText().toString().trim(),
                        email.getText().toString().trim(),
                        password.getText().toString().trim()
                );
            }

        });
    }
}