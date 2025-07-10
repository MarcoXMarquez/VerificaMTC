package com.master.verificamtc.user.payment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.master.verificamtc.R;
import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Obtener ID de usuario
        userId = getIntent().getStringExtra("USER_ID");
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
            try {
                Log.d("PaymentActivity", "Attempting to open UserPaymentActivity");
                Intent visaIntent = new Intent(PaymentActivity.this, UserPaymentActivity.class);
                visaIntent.putExtra("USER_ID", userId);
                startActivity(visaIntent);
            } catch (ActivityNotFoundException e) {
                Log.e("PaymentActivity", "UserPaymentActivity not found", e);
                Toast.makeText(PaymentActivity.this, "Payment form not available", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("PaymentActivity", "Error opening payment form", e);
                Toast.makeText(PaymentActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        cardCash.setOnClickListener(v -> {
            // Procesar pago en efectivo directamente
            processPayment("EFECTIVO");
        });
    }

    private void processPayment(String method) {
        mDatabase.child("users").child(userId).child("payment")
                .setValue(createPaymentData(method))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updatePaymentStatus(method);
                        Toast.makeText(this, "Pago registrado: " + method, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al procesar pago", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private HashMap<String, Object> createPaymentData(String method) {
        HashMap<String, Object> payment = new HashMap<>();
        payment.put("method", method);
        payment.put("timestamp", System.currentTimeMillis());
        payment.put("amount", 150.00);
        payment.put("status", "completed");
        return payment;
    }

    private void updatePaymentStatus(String method) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("paymentStatus", true);
        updates.put("paymentMethod", method);
        updates.put("lastPaymentDate", System.currentTimeMillis());

        mDatabase.child("users").child(userId)
                .updateChildren(updates);
    }
}