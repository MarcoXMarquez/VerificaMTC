package com.master.verificamtc.user.vehicle;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import com.master.verificamtc.R;
import com.master.verificamtc.helpers.FirebaseDatabaseHelper;

public class UserVehicleActivity extends AppCompatActivity {
    private TextInputEditText etPlate, etColor, etBrand, etModel, etYear;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_data);

        // Inicializar Realtime Database
        database = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        etPlate = findViewById(R.id.etPlate);
        etColor = findViewById(R.id.etColor);
        etBrand = findViewById(R.id.etBrand);
        etModel = findViewById(R.id.etModel);
        etYear = findViewById(R.id.etYear);

        findViewById(R.id.btnSaveVehicle).setOnClickListener(v -> saveVehicleToDatabase());
    }

    private boolean validatePlate(String plate) {
        plate = plate.toUpperCase(); // Normaliza a mayúsculas
        return plate.matches("^[A-Z0-9]{3}-\\d{3}$");
    }

    private void saveVehicleToDatabase() {
        String plate = etPlate.getText().toString().trim().toUpperCase();
        String color = etColor.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String userId = getIntent().getStringExtra("USER_ID");

        // Validación de campos vacíos
        if (plate.isEmpty() || color.isEmpty() || brand.isEmpty() || model.isEmpty() || yearStr.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de placa
        if (!validatePlate(plate)) {
            etPlate.setError("Formato de placa inválido. Use AAA-000");
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);

            // Crear objeto Car
            FirebaseDatabaseHelper.Car vehicle = new FirebaseDatabaseHelper.Car();
            vehicle.plate = plate;
            vehicle.color = color;
            vehicle.brand = brand;
            vehicle.model = model;
            vehicle.year = year;
            vehicle.userId = userId;
            vehicle.verificationStatus = false;

            // Usar el helper para guardar
            FirebaseDatabaseHelper dbHelper = new FirebaseDatabaseHelper(this);
            dbHelper.addVehicle(vehicle);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "El año debe ser un número válido", Toast.LENGTH_SHORT).show();
        }
    }
}