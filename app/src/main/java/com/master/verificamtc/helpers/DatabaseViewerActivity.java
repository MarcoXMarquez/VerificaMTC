package com.master.verificamtc.helpers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.master.verificamtc.R;

public class DatabaseViewerActivity extends AppCompatActivity {

    private FirebaseDatabaseHelper dbHelper;
    private TextView tvDatabaseContent;
    private TextView tvDataCount;
    private Button btnLoadData;
    private Button btnSyncData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_viewer);

        dbHelper = new FirebaseDatabaseHelper(this);
        tvDatabaseContent = findViewById(R.id.tvDatabaseContent);
        tvDataCount = findViewById(R.id.tvDataCount);
        btnLoadData = findViewById(R.id.btnLoadData);
        btnSyncData = findViewById(R.id.btnSyncData);

        // Botón para cargar datos locales
        btnLoadData.setOnClickListener(v -> displayDatabaseContent());

        // Botón para sincronizar con Firebase
        btnSyncData.setOnClickListener(v -> syncWithFirebase());
    }

    private void syncWithFirebase() {
        btnSyncData.setEnabled(false);
        btnSyncData.setText("Sincronizando...");
        tvDatabaseContent.setText("Sincronizando con Firebase...");

        dbHelper.getAllFaces(new FirebaseDatabaseHelper.SyncCompletionListener() {
            @Override
            public void onSyncComplete(boolean success) {
                runOnUiThread(() -> {
                    btnSyncData.setEnabled(true);
                    btnSyncData.setText("Sincronizar con Firebase");

                    if (success) {
                        Toast.makeText(DatabaseViewerActivity.this,
                                "Sincronización exitosa", Toast.LENGTH_SHORT).show();
                        displayDatabaseContent();
                    } else {
                        Toast.makeText(DatabaseViewerActivity.this,
                                "Error en sincronización", Toast.LENGTH_SHORT).show();
                        tvDatabaseContent.setText("Error al sincronizar con Firebase");
                    }
                });
            }
        });
    }

    private void displayDatabaseContent() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        StringBuilder content = new StringBuilder();
        int recordCount = 0;

        // Consultar la tabla de caras (faces)
        Cursor cursor = db.query(
                FirebaseDatabaseHelper.TABLE_FACES,
                null,
                null,
                null,
                null,
                null,
                null
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                content.append("=== Datos de la tabla FACES ===\n\n");

                do {
                    String userId = cursor.getString(cursor.getColumnIndexOrThrow(FirebaseDatabaseHelper.COLUMN_USER_ID));
                    String embedding = cursor.getString(cursor.getColumnIndexOrThrow(FirebaseDatabaseHelper.COLUMN_EMBEDDING));

                    content.append("User ID: ").append(userId).append("\n");
                    content.append("Embedding: ").append(embedding).append("\n");
                    content.append("----------------------------\n");

                    recordCount++;
                } while (cursor.moveToNext());
            } else {
                content.append("No hay datos en la base de datos local.\n");
                content.append("Presiona 'Sincronizar con Firebase' para obtener datos.");
            }
        } catch (Exception e) {
            content.append("Error al leer la base de datos: ").append(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        tvDataCount.setText(recordCount + " registros encontrados");
        tvDatabaseContent.setText(content.toString());
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}