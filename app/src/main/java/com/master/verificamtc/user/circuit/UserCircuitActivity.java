package com.master.verificamtc.user.circuit;

import android.app.AlertDialog;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.master.verificamtc.R;
import com.master.verificamtc.helpers.FirebaseDatabaseHelper;

import java.util.HashMap;
import java.util.Map;

public class UserCircuitActivity extends AppCompatActivity implements OverlayView.OnZoneClickListener {

    private PhotoView photoView;
    private OverlayView overlayView;
    private final Matrix displayMatrix = new Matrix();
    private FirebaseDatabaseHelper dbHelper;
    private String userId; // ID del usuario obtenido del Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_circuit);

        // Obtener userId del Intent
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Error: No se identificó al usuario", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        dbHelper = new FirebaseDatabaseHelper(this);

        // Configurar vistas
        photoView = findViewById(R.id.circuitImageView);
        overlayView = findViewById(R.id.overlayView);
        overlayView.setOnZoneClickListener(this);

        // Configuración del PhotoView
        photoView.setZoomable(true);
        photoView.setMaximumScale(5f);
        photoView.setMinimumScale(1f);

        photoView.setOnMatrixChangeListener(rect -> {
            photoView.getDisplayMatrix(displayMatrix);
            overlayView.setImageMatrix(displayMatrix);
        });

        photoView.post(() -> {
            photoView.setScale(1.5f, true);
            photoView.getDisplayMatrix(displayMatrix);
            overlayView.setImageMatrix(displayMatrix);
        });
    }

    @Override
    public void onZoneClick(String zoneType) {
        Log.d("ZoneClick", "Usuario: " + userId + " | Zona: " + zoneType);

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Error: No se pudo identificar al usuario", Toast.LENGTH_LONG).show();
            return;
        }

        String firebaseZoneType = zoneType.equals("curva") ? "curve" : "parking";
        String dialogTitle = zoneType.equals("curva") ? "Curvas" : "Estacionamiento";

        // Primero cargar las respuestas existentes
        dbHelper.getAnswers(userId, firebaseZoneType, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot answersSnapshot) {
                Map<String, Integer> existingAnswers = new HashMap<>();
                if (answersSnapshot.exists()) {
                    // Verificamos la estructura de datos en Firebase
                    for (DataSnapshot answerSnapshot : answersSnapshot.getChildren()) {
                        String key = answerSnapshot.getKey();
                        if (key.startsWith("q")) {
                            // Dos posibles estructuras: objeto Answer o valor directo
                            if (answerSnapshot.hasChild("rating")) {
                                // Es un objeto Answer
                                existingAnswers.put(key, answerSnapshot.child("rating").getValue(Integer.class));
                            } else {
                                // Es un valor directo (compatibilidad con versión anterior)
                                existingAnswers.put(key, answerSnapshot.getValue(Integer.class));
                            }
                        }
                    }
                }

                // Luego cargar las preguntas
                dbHelper.getQuestions(firebaseZoneType, new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot questionsSnapshot) {
                        try {
                            if (questionsSnapshot.exists()) {
                                String[] questions = new String[3];
                                int i = 0;
                                for (DataSnapshot questionSnapshot : questionsSnapshot.getChildren()) {
                                    if (i < 3) {
                                        String questionText = questionSnapshot.getValue(String.class);
                                        if (questionText != null) {
                                            questions[i++] = questionText;
                                        }
                                    }
                                }

                                if (i >= 3) {
                                    showQuestionnaireDialog(dialogTitle, firebaseZoneType, questions, existingAnswers);
                                } else {
                                    Toast.makeText(UserCircuitActivity.this,
                                            "Configuración incompleta de preguntas",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(UserCircuitActivity.this,
                                        "No hay preguntas para esta zona",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("ZoneClick", "Error procesando preguntas", e);
                            Toast.makeText(UserCircuitActivity.this,
                                    "Error al cargar preguntas",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(UserCircuitActivity.this,
                                "Error de conexión: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UserCircuitActivity.this,
                        "Error cargando respuestas: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuestionnaireDialog(String title, String zoneType, String[] questions, Map<String, Integer> existingAnswers) {
        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.dialog_questionnaire, null);

            // Configurar preguntas
            ((TextView) view.findViewById(R.id.question1)).setText(questions[0]);
            ((TextView) view.findViewById(R.id.question2)).setText(questions[1]);
            ((TextView) view.findViewById(R.id.question3)).setText(questions[2]);

            // Obtener RadioGroups
            RadioGroup group1 = view.findViewById(R.id.group1);
            RadioGroup group2 = view.findViewById(R.id.group2);
            RadioGroup group3 = view.findViewById(R.id.group3);

            // Marcar respuestas existentes si las hay
            if (existingAnswers != null) {
                Log.d("ExistingAnswers", "Respuestas existentes: " + existingAnswers.toString());

                if (existingAnswers.containsKey("q1")) {
                    int rating = existingAnswers.get("q1");
                    Log.d("RadioCheck", "Marcando q1 con rating: " + rating);
                    checkRadioButton(group1, rating);
                }
                if (existingAnswers.containsKey("q2")) {
                    int rating = existingAnswers.get("q2");
                    Log.d("RadioCheck", "Marcando q2 con rating: " + rating);
                    checkRadioButton(group2, rating);
                }
                if (existingAnswers.containsKey("q3")) {
                    int rating = existingAnswers.get("q3");
                    Log.d("RadioCheck", "Marcando q3 con rating: " + rating);
                    checkRadioButton(group3, rating);
                }
            }

            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setView(view)
                    .setPositiveButton("Guardar", (dialog, which) -> {
                        Map<String, Object> answers = new HashMap<>();
                        answers.put("q1", getSelectedRating(group1));
                        answers.put("q2", getSelectedRating(group2));
                        answers.put("q3", getSelectedRating(group3));
                        answers.put("timestamp", System.currentTimeMillis());

                        dbHelper.saveAnswers(userId, zoneType, answers);
                        Toast.makeText(UserCircuitActivity.this,
                                "Respuestas guardadas",
                                Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

        } catch (Exception e) {
            Log.e("DialogError", "Error mostrando diálogo", e);
            Toast.makeText(this, "Error al mostrar preguntas", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkRadioButton(RadioGroup group, Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            Log.d("RadioCheck", "Rating inválido: " + rating);
            return;
        }

        // Los IDs de los RadioButtons siguen el patrón: radioButton1, radioButton2, etc. para el primer grupo
        // radioButton6, radioButton7, etc. para el segundo grupo, y así sucesivamente
        int baseId;
        if (group.getId() == R.id.group1) {
            baseId = R.id.radioButton1;
        } else if (group.getId() == R.id.group2) {
            baseId = R.id.radioButton6;
        } else if (group.getId() == R.id.group3) {
            baseId = R.id.radioButton11;
        } else {
            return;
        }

        int radioButtonId = baseId + (rating - 1);
        Log.d("RadioCheck", "Intentando marcar ID: " + radioButtonId + " para rating: " + rating);

        RadioButton radioButton = group.findViewById(radioButtonId);
        if (radioButton != null) {
            radioButton.setChecked(true);
            Log.d("RadioCheck", "RadioButton encontrado y marcado");
        } else {
            Log.d("RadioCheck", "RadioButton no encontrado para ID: " + radioButtonId);
        }
    }

    private int getSelectedRating(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        if (selectedId == -1) return 0;

        RadioButton radioButton = group.findViewById(selectedId);
        try {
            return Integer.parseInt(radioButton.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}