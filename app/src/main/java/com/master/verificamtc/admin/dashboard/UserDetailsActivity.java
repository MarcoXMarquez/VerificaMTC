// UserDetailsActivity.java
package com.master.verificamtc.admin.dashboard;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.master.verificamtc.R;
import com.master.verificamtc.helpers.FirebaseDatabaseHelper;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserDetailsActivity extends AppCompatActivity {

    private TextView tvUserDetails;
    private Button btnAddSchedule;
    private FirebaseDatabaseHelper databaseHelper;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        tvUserDetails = findViewById(R.id.tvUserDetails);
        btnAddSchedule = findViewById(R.id.btnAddSchedule);
        databaseHelper = new FirebaseDatabaseHelper(this);

        // Obtener el DNI del usuario del intent
        userId = getIntent().getStringExtra("USER_DNI");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Error: No se proporcionó ID de usuario", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserDetails();

        btnAddSchedule.setOnClickListener(v -> showScheduleDialog());
    }

    private void loadUserDetails() {
        databaseHelper.getDatabaseReference().child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            FirebaseDatabaseHelper.User user = dataSnapshot.getValue(FirebaseDatabaseHelper.User.class);
                            if (user != null) {
                                String details = "DNI: " + user.dni + "\n" +
                                        "Nombre: " + user.firstName + " " + user.lastName + "\n" +
                                        "Email: " + user.email + "\n" +
                                        "Estado de pago: " + (user.paymentStatus ? "Pagado" : "Pendiente") + "\n" +
                                        "Examen práctico: " + (user.drivingExamPassed ? "Aprobado" : "Pendiente");

                                tvUserDetails.setText(details);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(UserDetailsActivity.this,
                                "Error al cargar detalles: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showScheduleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_schedule, null);
        builder.setView(dialogView);

        TextInputEditText etDate = dialogView.findViewById(R.id.etDate);
        TextInputEditText etTime = dialogView.findViewById(R.id.etTime);
        Button btnSave = dialogView.findViewById(R.id.btnSaveSchedule);

        AlertDialog dialog = builder.create();

        // Configurar selector de fecha
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                        etDate.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        // Configurar selector de hora
        etTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePicker = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                        etTime.setText(selectedTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePicker.show();
        });

        btnSave.setOnClickListener(v -> {
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();

            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            saveSchedule(date, time);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveSchedule(String date, String time) {
        databaseHelper.addSchedule(userId, date, time, new FirebaseDatabaseHelper.ScheduleCompletionListener() {
            @Override
            public void onSuccess(String scheduleId) {
                runOnUiThread(() -> {
                    Toast.makeText(UserDetailsActivity.this,
                            "Horario guardado exitosamente", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(UserDetailsActivity.this,
                            "Error al guardar horario: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}