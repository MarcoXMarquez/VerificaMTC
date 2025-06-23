package com.master.verificamtc.admin.dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.master.verificamtc.R;
import com.master.verificamtc.database.AppDatabase;

public class AddScheduleActivity extends AppCompatActivity {
    private EditText etFecha, etHora;
    private Button btnSaveSchedule;
    private AppDatabase databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        etFecha = findViewById(R.id.etFecha);
        etHora = findViewById(R.id.etHora);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);
        databaseHelper = new AppDatabase(this);

        btnSaveSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fecha = etFecha.getText().toString().trim();
                String hora = etHora.getText().toString().trim();
                if (fecha.isEmpty() || hora.isEmpty()) {
                    Toast.makeText(AddScheduleActivity.this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                long id = databaseHelper.addSchedule(fecha, hora);
                if (id > 0) {
                    Toast.makeText(AddScheduleActivity.this, "Horario agregado", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra y vuelve a ScheduleManagementActivity
                } else {
                    Toast.makeText(AddScheduleActivity.this, "Error al agregar horario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}