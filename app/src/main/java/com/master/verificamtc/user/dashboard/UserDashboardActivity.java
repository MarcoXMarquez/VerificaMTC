package com.master.verificamtc.user.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.master.verificamtc.R;
import com.master.verificamtc.database.FirebaseDatabaseHelper;
import com.master.verificamtc.user.circuit.UserCircuitActivity;
import com.master.verificamtc.user.exam.UserExamActivity;
import com.master.verificamtc.user.payment.UserPaymentActivity;
import com.master.verificamtc.user.vehicle.UserVehicleActivity;

public class UserDashboardActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_lobby);

        // Get user ID from Intent
        userId = getIntent().getStringExtra("USER_ID");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Load user data from Firebase
        loadUserData();
    }

    private void loadUserData() {
        // 1. Get basic user data
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    FirebaseDatabaseHelper.User user = userSnapshot.getValue(FirebaseDatabaseHelper.User.class);
                    if (user != null) {
                        TextView tvWelcome = findViewById(R.id.tvWelcome);
                        tvWelcome.setText("Bienvenido, " + user.firstName + " " + user.lastName);

                        // Now load vehicle data
                        loadVehicleData(user);
                    }
                } else {
                    Toast.makeText(UserDashboardActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UserDashboardActivity.this, "Error al cargar datos: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVehicleData(FirebaseDatabaseHelper.User user) {
        mDatabase.child("cars").orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot vehicleSnapshot) {
                TextView tvUserInfo = findViewById(R.id.tvUserInfo);
                TextView tvStatus = findViewById(R.id.tvStatus);

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

                String statusInfo = "Estado: " +
                        (user.paymentStatus ? "Pago completado" : "Pago pendiente") + " | " +
                        (user.writtenExamPassed ? "Examen aprobado" : "Examen pendiente");

                tvUserInfo.setText(vehicleInfo + "\n" + statusInfo);
                tvStatus.setText(statusInfo);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UserDashboardActivity.this, "Error al cargar vehículo: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Button click methods (updated to pass userId)
    public void goToExam(View view) {
        Intent intent = new Intent(this, UserExamActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
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
        Intent intent = new Intent(this, UserPaymentActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        // No need to close database connection in Firebase
        super.onDestroy();
    }
}