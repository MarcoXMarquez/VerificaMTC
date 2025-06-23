package com.master.verificamtc.admin.dashboard;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.master.verificamtc.R;
import com.master.verificamtc.database.AppDatabase;
import java.util.ArrayList;

public class ScheduleManagementActivity extends AppCompatActivity {
    private ListView scheduleListView;
    private Button btnAddSchedule;
    private AppDatabase databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_management);

        scheduleListView = findViewById(R.id.scheduleListView);
        btnAddSchedule = findViewById(R.id.btnAddSchedule);
        databaseHelper = new AppDatabase(this);

        displayScheduleList();

        btnAddSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lanzar actividad o diálogo para agregar nuevo horario (placeholder)
                Intent intent = new Intent(ScheduleManagementActivity.this, AddScheduleActivity.class);
                startActivity(intent);
            }
        });
    }

    private void displayScheduleList() {
        Cursor cursor = databaseHelper.getAllSchedules();
        ArrayList<String> scheduleList = new ArrayList<>();

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No hay horarios disponibles", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                // Ajusta índices según tu esquema de schedules table
                String scheduleData = "ID: " + cursor.getString(0)
                        + "\nFecha: " + cursor.getString(1)
                        + "\nHora: " + cursor.getString(2);
                scheduleList.add(scheduleData);
            }
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                scheduleList
        );
        scheduleListView.setAdapter(adapter);
    }
}