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

            // Crear estructura de datos para Realtime Database
            Map<String, Object> vehicle = new HashMap<>();
            vehicle.put("plate", plate);
            vehicle.put("color", color);
            vehicle.put("brand", brand);
            vehicle.put("model", model);
            vehicle.put("year", year);
            vehicle.put("userId", userId);
            vehicle.put("verificationStatus", "pending"); // Estado inicial

            // Guardar en Realtime Database (nodo "vehicles" con placa como clave)
            database.child("vehicles").child(plate.replace("-", "_")) // Reemplazar guiones por underscores
                    .setValue(vehicle)
                    .addOnSuccessListener(aVoid -> {
                        // Actualizar también referencia en el usuario
                        if (userId != null && !userId.isEmpty()) {
                            database.child("users").child(userId)
                                    .child("vehicles").child(plate.replace("-", "_"))
                                    .setValue(true);
                        }

                        Toast.makeText(this, "Vehículo guardado exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al guardar vehículo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "El año debe ser un número válido", Toast.LENGTH_SHORT).show();
        }
    }
}