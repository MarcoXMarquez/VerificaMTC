package com.master.verificamtc.user.dashboard;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.master.verificamtc.user.circuit.UserCircuitActivity;
import com.master.verificamtc.database.AppDatabase;
import com.master.verificamtc.user.exam.UserExamActivity;
import com.master.verificamtc.user.payment.PaymentActivity;
import com.master.verificamtc.user.payment.UserPaymentActivity;
import com.master.verificamtc.R;
import com.master.verificamtc.user.vehicle.UserVehicleActivity;

public class UserDashboardActivity extends AppCompatActivity {
    private AppDatabase dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_lobby);

        // Obtener el ID del usuario del Intent
        userId = getIntent().getIntExtra("USER_ID", -1);
        dbHelper = new AppDatabase(this);

        // Cargar datos del usuario
        loadUserData();
    }

    private void loadUserData() {
        // 1. Obtener datos básicos del usuario
        Cursor userCursor = dbHelper.getReadableDatabase().query(
                AppDatabase.TABLE_AUTH,
                new String[]{AppDatabase.COLUMN_NAMES, AppDatabase.COLUMN_LASTNAMES},
                AppDatabase.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        // 2. Obtener datos del vehículo
        Cursor vehicleCursor = dbHelper.getVehicleByUserId(userId);

        // 3. Obtener estado del trámite
        Cursor statusCursor = dbHelper.getUserStatus(userId);

        // Configurar la UI con los datos
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvUserInfo = findViewById(R.id.tvUserInfo);
        TextView tvStatus = findViewById(R.id.tvStatus);

        if (userCursor != null && userCursor.moveToFirst()) {
            String firstName = userCursor.getString(userCursor.getColumnIndexOrThrow(AppDatabase.COLUMN_NAMES));
            String lastName = userCursor.getString(userCursor.getColumnIndexOrThrow(AppDatabase.COLUMN_LASTNAMES));
            tvWelcome.setText("Bienvenido, " + firstName + " " + lastName);
            userCursor.close();
        }

        String vehicleInfo = "Vehículo: No registrado";
        if (vehicleCursor != null && vehicleCursor.moveToFirst()) {
            String brand = vehicleCursor.getString(vehicleCursor.getColumnIndexOrThrow(AppDatabase.COLUMN_BRAND));
            String plate = vehicleCursor.getString(vehicleCursor.getColumnIndexOrThrow(AppDatabase.COLUMN_PLATE));
            vehicleInfo = "Vehículo: " + brand + " - Placa " + plate;
            vehicleCursor.close();
        }

        String statusInfo = "Estado: Pendiente de registro";
        if (statusCursor != null && statusCursor.moveToFirst()) {
            int hasPaid = statusCursor.getInt(statusCursor.getColumnIndexOrThrow(AppDatabase.COLUMN_HAS_PAID));
            int writtenPassed = statusCursor.getInt(statusCursor.getColumnIndexOrThrow(AppDatabase.COLUMN_WRITTEN_EXAM_PASSED));

            statusInfo = "Estado: " +
                    (hasPaid == 1 ? "Pago completado" : "Pago pendiente") + " | " +
                    (writtenPassed == 1 ? "Examen aprobado" : "Examen pendiente");
            statusCursor.close();
        }

        tvUserInfo.setText(vehicleInfo + "\n" + statusInfo);
        tvStatus.setText(statusInfo);
    }

    // Métodos para los clicks de los botones (actualizados para pasar userId)
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
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}