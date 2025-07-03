package com.master.verificamtc.user.vehicle;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.master.verificamtc.R;
import java.util.HashMap;
import java.util.Map;

public class UserVehicleActivity extends AppCompatActivity {
    private TextInputEditText etPlate, etColor, etBrand, etModel, etYear;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_data);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        etPlate = findViewById(R.id.etPlate);
        etColor = findViewById(R.id.etColor);
        etBrand = findViewById(R.id.etBrand);
        etModel = findViewById(R.id.etModel);
        etYear = findViewById(R.id.etYear);

        findViewById(R.id.btnSaveVehicle).setOnClickListener(v -> saveVehicleToFirestore());
    }

    private boolean validatePlate(String plate) {
        plate = plate.toUpperCase(); // Normaliza a mayúsculas
        return plate.matches("^[A-Z0-9]{3}-\\d{3}$");
    }

    private void saveVehicleToFirestore() {
        String plate = etPlate.getText().toString().trim().toUpperCase();
        String color = etColor.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();

        // Validación de campos vacíos
        if (plate.isEmpty() || color.isEmpty() || brand.isEmpty() || model.isEmpty() || yearStr.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de placa
        if (!validatePlate(plate)) {
            etPlate.setError(getString(R.string.error_invalid_plate));
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);

            // Crear estructura de datos para Firestore
            Map<String, Object> vehicle = new HashMap<>();
            vehicle.put("plate", plate);
            vehicle.put("color", color);
            vehicle.put("brand", brand);
            vehicle.put("model", model);
            vehicle.put("year", year);
            vehicle.put("userId", getIntent().getStringExtra("USER_ID")); // Asegúrate de pasar el ID como String

            // Guardar en Firestore (colección "cars" con placa como ID)
            db.collection("cars").document(plate)
                    .set(vehicle)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, R.string.success_vehicle_saved, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, R.string.firestore_error, Toast.LENGTH_SHORT).show();
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "El año debe ser un número válido", Toast.LENGTH_SHORT).show();
        }
    }
}