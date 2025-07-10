package com.master.verificamtc.user.dashboard;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.master.verificamtc.R;
import com.master.verificamtc.helpers.FirebaseDatabaseHelper;
import com.master.verificamtc.user.circuit.UserCircuitActivity;
import com.master.verificamtc.user.schedule.UserScheduleActivity;
import com.master.verificamtc.user.payment.PaymentActivity;
import com.master.verificamtc.user.vehicle.UserVehicleActivity;

import java.util.ArrayList;
import java.util.List;

public class UserDashboardActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Get user ID from Intent
        userId = getIntent().getStringExtra("USER_ID");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Load user data from Firebase
        loadUserData();
    }

    private void loadUserData() {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    FirebaseDatabaseHelper.User user = userSnapshot.getValue(FirebaseDatabaseHelper.User.class);
                    if (user != null) {
                        TextView tvWelcome = findViewById(R.id.tvWelcome);
                        tvWelcome.setText("Bienvenido, " + user.firstName + " " + user.lastName);

                        // Calcular progreso
                        calculateProgress(user);

                        // Cargar datos del vehículo
                        loadVehicleData(user);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UserDashboardActivity.this,
                        "Error al cargar datos: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateProgress(FirebaseDatabaseHelper.User user) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvProgressText = findViewById(R.id.tvProgressText);

        // Definir los pasos requeridos para completar el trámite (ahora son 3)
        final int TOTAL_STEPS = 3;
        int completedSteps = 0;

        // Lista para almacenar los estados completados
        List<String> completedStatuses = new ArrayList<>();

        // 1. Verificar estado de pago
        if (user.paymentStatus) {
            completedSteps++;
            completedStatuses.add("Pago completado");
        } else {
            completedStatuses.add("Pago pendiente");
        }

        // 2. Verificar registro de vehículo
        if (user.hasVehicle) {
            completedSteps++;
            completedStatuses.add("Vehículo registrado");
        } else {
            completedStatuses.add("Vehículo no registrado");
        }

        // 3. Verificar horario asignado
        boolean hasSchedule = user.schedules != null && !user.schedules.isEmpty();
        if (hasSchedule) {
            completedSteps++;
            completedStatuses.add("Horario asignado");
        } else {
            completedStatuses.add("Horario no asignado");
        }

        // Calcular porcentaje de progreso
        int progressPercentage = (int) (((float) completedSteps / TOTAL_STEPS) * 100);

        // Actualizar la barra de progreso
        progressBar.setProgress(progressPercentage);

        // Configurar el texto de progreso
        tvProgressText.setText(progressPercentage + "% completado");

        // Determinar el estado general y el color del texto
        String overallStatus;
        int statusColor;

        if (completedSteps == 0) {
            overallStatus = "Trámite no iniciado";
            statusColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        } else if (completedSteps == TOTAL_STEPS) {
            overallStatus = "Trámite completo ✓";
            statusColor = ContextCompat.getColor(this, android.R.color.holo_green_dark);
        } else {
            overallStatus = "En progreso (" + completedSteps + "/" + TOTAL_STEPS + ")";
            statusColor = ContextCompat.getColor(this, android.R.color.holo_orange_dark);
        }

        // Construir el texto detallado del estado
        StringBuilder statusDetails = new StringBuilder();
        statusDetails.append(overallStatus).append("\n\n");
        statusDetails.append("Detalles:\n");

        for (String status : completedStatuses) {
            statusDetails.append("• ").append(status).append("\n");
        }

        // Actualizar la vista de estado
        tvStatus.setText(statusDetails.toString());
        tvStatus.setTextColor(statusColor);

        // Cambiar color de la barra de progreso según el estado
        if (progressPercentage < 30) {
            progressBar.getProgressDrawable().setColorFilter(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark),
                    PorterDuff.Mode.SRC_IN);
        } else if (progressPercentage < 100) {
            progressBar.getProgressDrawable().setColorFilter(
                    ContextCompat.getColor(this, android.R.color.holo_orange_dark),
                    PorterDuff.Mode.SRC_IN);
        } else {
            progressBar.getProgressDrawable().setColorFilter(
                    ContextCompat.getColor(this, android.R.color.holo_green_dark),
                    PorterDuff.Mode.SRC_IN);
        }
    }

    private void loadVehicleData(FirebaseDatabaseHelper.User user) {
        mDatabase.child("users").child(userId).child("vehicles")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot vehicleSnapshot) {
                        TextView tvUserInfo = findViewById(R.id.tvUserInfo);

                        String vehicleInfo = "Vehículo: No registrado";
                        if (vehicleSnapshot.exists()) {
                            for (DataSnapshot carSnapshot : vehicleSnapshot.getChildren()) {
                                FirebaseDatabaseHelper.Car car = carSnapshot.getValue(FirebaseDatabaseHelper.Car.class);
                                if (car != null) {
                                    vehicleInfo = "Vehículo: " + car.brand + " - Placa " + car.plate;
                                    break;
                                }
                            }
                        }

                        // Actualizado: Eliminada referencia al examen
                        String statusInfo = "Estado: " +
                                (user.paymentStatus ? "Pago completado" : "Pago pendiente");

                        tvUserInfo.setText(vehicleInfo + "\n" + statusInfo);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(UserDashboardActivity.this,
                                "Error al cargar vehículo: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Button click methods
    public void goToSchedule(View view) {
        // Primero necesitamos obtener el scheduleId del usuario
        mDatabase.child("users").child(userId).child("schedules")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot scheduleSnapshot : dataSnapshot.getChildren()) {
                                String scheduleId = scheduleSnapshot.getKey();
                                Intent intent = new Intent(UserDashboardActivity.this, UserScheduleActivity.class);
                                intent.putExtra("SCHEDULE_ID", scheduleId);
                                startActivity(intent);
                                return; // Solo tomamos el primer schedule si hay múltiples
                            }
                        }
                        // Si no hay schedules
                        Toast.makeText(UserDashboardActivity.this,
                                "No tienes horarios asignados", Toast.LENGTH_SHORT).show();
                        // No iniciamos la actividad, el usuario se queda en el dashboard
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(UserDashboardActivity.this,
                                "Error al verificar horarios", Toast.LENGTH_SHORT).show();
                        // En caso de error, también nos quedamos en el dashboard
                    }
                });
    }
    public void goToVehicleData(View view) {
        Intent intent = new Intent(this, UserVehicleActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    public void goToCircuit(View view) {
        Intent intent = new Intent(this, UserCircuitActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    public void goToPayments(View view) {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}