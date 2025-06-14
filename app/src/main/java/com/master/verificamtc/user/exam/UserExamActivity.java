package com.master.verificamtc.user.exam;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.master.verificamtc.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserExamActivity extends AppCompatActivity {

    private TextView questionText;
    private RadioGroup optionsGroup;
    private RadioButton option1, option2, option3, option4;
    private Button nextButton;
    private TextView scoreText;
    private TextView resultText;

    private List<Question> questionList;
    private int currentIndex = 0;
    private int score = 0;
    private final StringBuilder resultsSummary = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_exam);

        questionText = findViewById(R.id.questionText);
        optionsGroup = findViewById(R.id.optionsGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        nextButton = findViewById(R.id.nextButton);
        scoreText = findViewById(R.id.scoreText);
        resultText = findViewById(R.id.resultText); // NUEVO

        questionList = getSampleQuestions();
        Collections.shuffle(questionList);

        showQuestion();

        nextButton.setOnClickListener(v -> {
            int selectedId = optionsGroup.getCheckedRadioButtonId();
            if (selectedId == -1) return;

            RadioButton selected = findViewById(selectedId);
            String userAnswer = selected.getText().toString();
            Question currentQuestion = questionList.get(currentIndex);

            if (currentQuestion.getCorrectAnswer().equals(userAnswer)) {
                score++;
                resultsSummary.append("✔️ ").append(currentQuestion.getQuestion())
                        .append("\nTu respuesta: ").append(userAnswer).append(" (Correcta)\n\n");
            } else {
                resultsSummary.append("❌ ").append(currentQuestion.getQuestion())
                        .append("\nTu respuesta: ").append(userAnswer)
                        .append("\nRespuesta correcta: ").append(currentQuestion.getCorrectAnswer())
                        .append("\n\n");
            }

            currentIndex++;

            if (currentIndex < questionList.size()) {
                showQuestion();
            } else {
                showScore();
            }
        });
    }

    private void showQuestion() {
        optionsGroup.clearCheck();
        Question q = questionList.get(currentIndex);
        questionText.setText(q.getQuestion());
        List<String> shuffledOptions = new ArrayList<>(q.getOptions());
        Collections.shuffle(shuffledOptions);
        option1.setText(shuffledOptions.get(0));
        option2.setText(shuffledOptions.get(1));
        option3.setText(shuffledOptions.get(2));
        option4.setText(shuffledOptions.get(3));
    }

    private void showScore() {
        questionText.setVisibility(View.GONE);
        optionsGroup.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        scoreText.setVisibility(View.VISIBLE);
        resultText.setVisibility(View.VISIBLE); // Mostrar resultados

        scoreText.setText("Tu nota: " + score + " / " + questionList.size());
        resultText.setText(resultsSummary.toString());
    }

    private List<Question> getSampleQuestions() {
        List<Question> questions = new ArrayList<>();

        questions.add(new Question("¿Cuál es el límite máximo de velocidad en zona urbana, salvo señalización diferente?",
                List.of("50 km/h", "60 km/h", "70 km/h", "40 km/h"), "50 km/h"));

        questions.add(new Question("¿Qué debe hacer si el semáforo está en verde pero un policía le indica que se detenga?",
                List.of("Detenerse, obedeciendo al policía", "Avanzar porque el semáforo está en verde", "Tocar el claxon", "Esperar que el policía se retire"), "Detenerse, obedeciendo al policía"));

        questions.add(new Question("¿Qué significa una línea continua en el centro de la vía?",
                List.of("No adelantar", "Adelantar con precaución", "Zona de cruce", "Permitido adelantar si hay visibilidad"), "No adelantar"));

        questions.add(new Question("¿Qué debe hacer antes de cambiar de carril?",
                List.of("Señalizar e inspeccionar los espejos", "Cambiar directamente", "Acelerar", "Tocar el claxon"), "Señalizar e inspeccionar los espejos"));

        questions.add(new Question("¿Qué documento es obligatorio portar al conducir un vehículo?",
                List.of("Licencia de conducir", "Recibo de luz", "Carnet universitario", "Tarjeta del banco"), "Licencia de conducir"));

        questions.add(new Question("¿Cuál es el nivel máximo permitido de alcohol en sangre para un conductor particular?",
                List.of("0.5 gramos por litro", "1 gramo por litro", "0.1 gramos por litro", "0 gramos por litro"), "0.5 gramos por litro"));

        questions.add(new Question("¿Qué debe hacer si sufre un accidente sin heridos pero con daños materiales?",
                List.of("Mover el vehículo y reportar a la autoridad", "Abandonar el lugar", "Esperar sin mover el auto", "Irse sin informar"), "Mover el vehículo y reportar a la autoridad"));

        questions.add(new Question("¿Qué indica una luz roja intermitente en un semáforo?",
                List.of("Alto total y continuar con precaución", "Avanzar rápidamente", "Cruzar sin detenerse", "Cruce exclusivo para buses"), "Alto total y continuar con precaución"));

        questions.add(new Question("¿En qué casos se puede usar el claxon?",
                List.of("Para evitar un accidente", "Para apurar a otros", "Para saludar", "En túneles"), "Para evitar un accidente"));

        questions.add(new Question("¿Qué significa esta señal: ⚠️ (triángulo amarillo con borde rojo y un símbolo en el centro)?",
                List.of("Advertencia de peligro", "Prohibición", "Información turística", "Estacionamiento"), "Advertencia de peligro"));

        return questions;
    }

    // Clase interna
    static class Question {
        private final String question;
        private final List<String> options;
        private final String correctAnswer;

        public Question(String question, List<String> options, String correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestion() {
            return question;
        }

        public List<String> getOptions() {
            return options;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }
    }
}
