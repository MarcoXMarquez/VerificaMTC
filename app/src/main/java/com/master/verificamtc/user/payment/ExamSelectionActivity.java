// ExamSelectionActivity.java
package com.master.verificamtc.user.payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.master.verificamtc.R;

public class ExamSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.examen_selection);

        // Obtener el ID de usuario del Intent
        String userId = getIntent().getStringExtra("USER_ID");

        // Configurar botones
        Button btnRules = findViewById(R.id.btn_select_rules);
        Button btnDriving = findViewById(R.id.btn_select_driving);

        btnRules.setOnClickListener(v -> {
            Intent paymentIntent = new Intent(this, PaymentActivity.class);
            paymentIntent.putExtra("USER_ID", userId);
            paymentIntent.putExtra("exam_type", "rules");
            paymentIntent.putExtra("exam_price", 37.20);
            startActivity(paymentIntent);
        });

        btnDriving.setOnClickListener(v -> {
            Intent paymentIntent = new Intent(this, PaymentActivity.class);
            paymentIntent.putExtra("USER_ID", userId);
            paymentIntent.putExtra("exam_type", "driving");
            paymentIntent.putExtra("exam_price", 43.80);
            startActivity(paymentIntent);
        });
    }
}
