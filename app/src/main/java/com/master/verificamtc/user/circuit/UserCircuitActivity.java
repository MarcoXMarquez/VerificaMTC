package com.master.verificamtc.user.circuit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.master.verificamtc.R;

public class UserCircuitActivity extends AppCompatActivity {

    private PhotoView photoView;
    private OverlayView overlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_circuit);

        photoView = findViewById(R.id.circuitImageView);
        overlayView = findViewById(R.id.overlayView);

        photoView.setZoomable(true);
        photoView.setMaximumScale(5f);
        photoView.setMinimumScale(1f);

        photoView.post(() -> {
            photoView.setScale(1.5f, true);

            Matrix displayMatrix = new Matrix();
            photoView.getDisplayMatrix(displayMatrix);
            overlayView.setImageMatrix(displayMatrix);
        });

        photoView.setOnMatrixChangeListener(rect -> {
            Matrix displayMatrix = new Matrix();
            photoView.getDisplayMatrix(displayMatrix);
            overlayView.setImageMatrix(displayMatrix);
        });

        photoView.setOnPhotoTapListener((view, x, y) -> {
            float[] touchPoint = new float[]{x * photoView.getWidth(), y * photoView.getHeight()};

            Matrix displayMatrix = new Matrix();
            photoView.getDisplayMatrix(displayMatrix);

            Matrix inverse = new Matrix();
            if (displayMatrix.invert(inverse)) {
                inverse.mapPoints(touchPoint);
                float imageX = touchPoint[0];
                float imageY = touchPoint[1];

                if (isInEstacionamiento(imageX, imageY)) {
                    showQuestionnaireDialog("Paso peatonal, intersecciones, etc.", new String[]{
                            "¿Respetó la señalización?",
                            "¿Cedió el paso correctamente?",
                            "¿Mantiene distancia de seguridad?"
                    });
                } else if (isInCurva(imageX, imageY)) {
                    showQuestionnaireDialog("Curvas", new String[]{
                            "¿Redujo la velocidad al ingresar a la curva?",
                            "¿Mantuvo el control del vehículo durante la curva?",
                            "¿Usó adecuadamente los espejos y señalización?"
                    });
                }
            }
        });
    }

    private boolean isInCurva(float x, float y) {
        RectF zona = new RectF(150, 300, 250, 400);
        return zona.contains(x, y);
    }

    private boolean isInEstacionamiento(float x, float y) {
        RectF zona = new RectF(450, 600, 580, 700);
        return zona.contains(x, y);
    }

    private void showQuestionnaireDialog(String title, String[] questions) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_questionnaire, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(view)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    // Aquí puedes recoger las respuestas si lo necesitas.
                    dialog.dismiss();
                });

        RadioGroup[] radioGroups = new RadioGroup[questions.length];

        for (int i = 0; i < questions.length; i++) {
            int groupId = getResources().getIdentifier("group" + (i + 1), "id", getPackageName());
            radioGroups[i] = view.findViewById(groupId);
        }

        builder.show();
    }
}
