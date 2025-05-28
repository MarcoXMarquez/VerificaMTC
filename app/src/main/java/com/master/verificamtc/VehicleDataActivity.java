package com.master.verificamtc;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class VehicleDataActivity extends AppCompatActivity {
    private TextInputEditText etPlate, etColor, etBrand, etModel, etYear;
    private Button btnSave;
    private DatabaseScheme dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_data);

        // Obtener el ID del usuario del Intent
        userId = getIntent().getIntExtra("USER_ID", -1);
        dbHelper = new DatabaseScheme(this);

        // Inicializar vistas
        etPlate = findViewById(R.id.etPlate);
        etColor = findViewById(R.id.etColor);
        etBrand = findViewById(R.id.etBrand);
        etModel = findViewById(R.id.etModel);
        etYear = findViewById(R.id.etYear);
        btnSave = findViewById(R.id.btnSaveVehicle);

        // Cargar datos existentes si existen
        loadExistingVehicleData();

        //btnSave.setOnClickListener(v -> saveVehicleData());
    }

    private void loadExistingVehicleData() {
        Cursor cursor = dbHelper.getVehicleByUserId(userId);
        if (cursor != null && cursor.moveToFirst()) {
            etPlate.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseScheme.COLUMN_PLATE)));
            etColor.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseScheme.COLUMN_COLOR)));
            etBrand.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseScheme.COLUMN_BRAND)));
            etModel.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseScheme.COLUMN_MODEL)));
            etYear.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseScheme.COLUMN_YEAR))));
            cursor.close();
        }
    }

    /*private void saveVehicleData() {
        String plate = etPlate.getText().toString().trim().toUpperCase();
        String color = etColor.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();

        if (plate.isEmpty() || color.isEmpty() || brand.isEmpty() || model.isEmpty() || yearStr.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);

            // Verificar si ya existe un vehículo para actualizar o crear nuevo
            boolean success;
            if (dbHelper.getVehicleByUserId(userId).getCount() > 0) {
                //success = dbHelper.updateVehicle(userId, color, plate, brand, model, year);
            } else {
                success = dbHelper.addVehicle(userId, color, plate, brand, model, year);
            }

            if (success) {
                Toast.makeText(this, "Datos del vehículo guardados", Toast.LENGTH_SHORT).show();
                finish(); // Cierra la actividad después de guardar
            } else {
                Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El año debe ser un número válido", Toast.LENGTH_SHORT).show();
        }
    }*/

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}