package com.master.verificamtc.user.payment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.master.verificamtc.R;
import java.util.HashMap;

public class PaymentActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private String userId;
    private String examType;
    private double examPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Obtener datos del Intent
        userId = getIntent().getStringExtra("USER_ID");
        examType = getIntent().getStringExtra("exam_type");
        examPrice = getIntent().getDoubleExtra("exam_price", 0.0);

        if (userId == null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        setupPaymentOptions();
    }

    private void setupPaymentOptions() {
        CardView cardVisa = findViewById(R.id.card_visa);
        CardView cardCash = findViewById(R.id.card_cash);

        cardVisa.setOnClickListener(v -> {
            Intent visaIntent = new Intent(this, UserPaymentActivity.class);
            visaIntent.putExtra("USER_ID", userId);
            visaIntent.putExtra("exam_type", examType);
            visaIntent.putExtra("exam_price", examPrice);
            startActivity(visaIntent);
        });

        cardCash.setOnClickListener(v -> {
            processPayment("EFECTIVO");
        });
    }

    private void processPayment(String method) {
        // Crear un nodo único para el pago
        String paymentId = mDatabase.child("payments").push().getKey();

        // Crear datos del pago
        HashMap<String, Object> paymentData = new HashMap<>();
        paymentData.put("amount", examPrice);
        paymentData.put("concept", "Pago de examen MTC");
        paymentData.put("date", System.currentTimeMillis());
        paymentData.put("exam_id", ""); // Puedes llenar esto después si es necesario
        paymentData.put("method", method.toLowerCase());
        paymentData.put("status", "completed");
        paymentData.put("user_id", userId);
        paymentData.put("exam_type", examType);

        // Guardar en /payments/{paymentId}
        mDatabase.child("payments").child(paymentId)
                .setValue(paymentData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Actualizar también la información básica en el usuario
                        updateUserPaymentStatus(method, paymentId);
                        Toast.makeText(this, "Pago registrado: " + method, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al procesar pago", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserPaymentStatus(String method, String paymentId) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("paymentStatus", true);
        updates.put("paymentMethod", method);
        updates.put("lastPaymentDate", System.currentTimeMillis());
        updates.put("exam_type", examType);
        updates.put("exam_status", "pending");
        // Opcional: guardar referencia al pago
        updates.put("lastPaymentId", paymentId);

        mDatabase.child("users").child(userId)
                .updateChildren(updates);
    }
}