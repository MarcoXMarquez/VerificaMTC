package com.master.verificamtc.user.payment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import android.widget.TextView;
import com.master.verificamtc.R;

public class UserPaymentActivity extends AppCompatActivity {

    // Vistas del formulario
    private TextInputEditText etCardNumber, etCardHolder, etExpiryDate, etCvv;
    private Button btnConfirmPayment, btnBackToOptions;

    // Vistas de la tarjeta interactiva
    private TextView cardNumberPreview, cardHolderPreview, cardExpiryPreview;

    // Firebase
    private final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private String userId;
    private String examType;
    private double examPrice;

    // Control de formato
    private boolean isFormatting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_visa_payment);

        // Obtener datos del intent
        userId = getIntent().getStringExtra("USER_ID");
        examType = getIntent().getStringExtra("exam_type");
        examPrice = getIntent().getDoubleExtra("exam_price", 0.0);

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupTextWatchers();
        setupButtons();
    }

    private void initViews() {
        etCardNumber = findViewById(R.id.et_card_number);
        etCardHolder = findViewById(R.id.et_card_holder);
        etExpiryDate = findViewById(R.id.et_expiry_date);
        etCvv = findViewById(R.id.et_cvv);
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment);
        btnBackToOptions = findViewById(R.id.btn_back_to_options);
        cardNumberPreview = findViewById(R.id.card_number_preview);
        cardHolderPreview = findViewById(R.id.card_holder_preview);
        cardExpiryPreview = findViewById(R.id.card_expiry_preview);
    }

    private void setupTextWatchers() {
        // Listener para número de tarjeta
        etCardNumber.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String digits = s.toString().replaceAll("[^\\d]", "");
                if (digits.length() > 16) digits = digits.substring(0, 16);

                // Formatear con espacios cada 4 dígitos
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i > 0 && i % 4 == 0) formatted.append(" ");
                    formatted.append(digits.charAt(i));
                }

                // Actualizar vista previa
                updateCardPreview(digits);

                // Actualizar campo
                etCardNumber.removeTextChangedListener(this);
                etCardNumber.setText(formatted.toString());
                etCardNumber.setSelection(formatted.length());
                etCardNumber.addTextChangedListener(this);

                isFormatting = false;
            }
        });

        // Listener para nombre del titular
        etCardHolder.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                cardHolderPreview.setText(s.toString().isEmpty() ? "NOMBRE DEL TITULAR" : s.toString().toUpperCase());
            }
        });

        // Listener para fecha de expiración
        etExpiryDate.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String digits = s.toString().replaceAll("[^\\d]", "");
                if (digits.length() > 4) digits = digits.substring(0, 4);

                // Formatear para campo de entrada (MM/AA)
                String formatted = digits;
                if (digits.length() >= 2) {
                    formatted = digits.substring(0, 2) + "/" + (digits.length() > 2 ? digits.substring(2) : "");
                }

                // Actualizar vista previa (siempre con slash)
                String previewText = formatted;
                if (digits.length() < 2) {
                    previewText = digits + "/AA".substring(digits.length());
                } else if (digits.length() < 4) {
                    previewText = digits.substring(0, 2) + "/" + (digits.length() > 2 ? digits.substring(2) : "AA");
                }
                cardExpiryPreview.setText(previewText.length() < 5 ? "MM/AA" : previewText);

                // Actualizar campo de entrada
                etExpiryDate.removeTextChangedListener(this);
                etExpiryDate.setText(formatted);
                etExpiryDate.setSelection(Math.min(formatted.length(), digits.length() + (digits.length() > 2 ? 1 : 0)));
                etExpiryDate.addTextChangedListener(this);

                isFormatting = false;
            }
        });

        // Listener para CVV
        etCvv.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 4) {
                    s.delete(4, s.length());
                }
            }
        });
    }

    private void updateCardPreview(String digits) {
        StringBuilder preview = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            if (i > 0 && i % 4 == 0) preview.append(" ");
            preview.append(i < digits.length() ? digits.charAt(i) : '•');
        }
        cardNumberPreview.setText(preview.toString());
    }

    private void setupButtons() {
        btnConfirmPayment.setOnClickListener(v -> {
            if (validateCardDetails()) {
                processVisaPayment();
            }
        });

        btnBackToOptions.setOnClickListener(v -> finish());
    }

    private boolean validateCardDetails() {
        String cardNumber = etCardNumber.getText().toString().replace(" ", "");
        String cardHolder = etCardHolder.getText().toString();
        String expiryDate = etExpiryDate.getText().toString();
        String cvv = etCvv.getText().toString();

        if (cardNumber.length() != 16) {
            etCardNumber.setError("Se requieren 16 dígitos");
            return false;
        }

        if (cardHolder.isEmpty()) {
            etCardHolder.setError("Ingrese el nombre del titular");
            return false;
        }

        if (!expiryDate.matches("^(0[1-9]|1[0-2])/?([0-9]{2})$")) {
            etExpiryDate.setError("Formato MM/AA inválido");
            return false;
        }

        if (cvv.length() < 3 || cvv.length() > 4) {
            etCvv.setError("CVV inválido (3-4 dígitos)");
            return false;
        }

        return true;
    }

    private void processVisaPayment() {
        String cardNumber = etCardNumber.getText().toString().replace(" ", "");
        String cardHolder = etCardHolder.getText().toString();
        String expiryDate = etExpiryDate.getText().toString();
        String cvv = etCvv.getText().toString();

        // Obtener ID de usuario si no estaba en el Intent
        if (userId == null && auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }

        // Generar ID único para el pago
        String paymentId = database.child("payments").push().getKey();

        // Crear datos del pago
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("amount", examPrice);
        paymentData.put("date", System.currentTimeMillis());
        paymentData.put("concept", "Pago de examen MTC - " + examType);
        paymentData.put("method", "visa");
        paymentData.put("card_last_four", cardNumber.substring(cardNumber.length() - 4));
        paymentData.put("status", "completed");
        paymentData.put("user_id", userId);
        paymentData.put("exam_type", examType);

        // Guardar en /payments/{paymentId}
        database.child("payments").child(paymentId)
                .setValue(paymentData)
                .addOnSuccessListener(aVoid -> {
                    // Actualizar estado del usuario
                    updateUserPaymentStatus(paymentId);
                    Toast.makeText(this, "Pago procesado exitosamente", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al procesar pago: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserPaymentStatus(String paymentId) {
        if (userId == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentStatus", true);
        updates.put("paymentMethod", "visa");
        updates.put("lastPaymentDate", System.currentTimeMillis());
        updates.put("exam_type", examType);
        updates.put("exam_status", "pending");
        updates.put("lastPaymentId", paymentId);

        database.child("users").child(userId)
                .updateChildren(updates);
    }
}