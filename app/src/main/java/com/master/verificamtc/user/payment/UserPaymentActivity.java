package com.master.verificamtc.user.payment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.master.verificamtc.databinding.FormVisaPaymentBinding;
import java.util.HashMap;
import java.util.Map;

public class UserPaymentActivity extends AppCompatActivity {

    private FormVisaPaymentBinding binding;
    private boolean isFormattingCardNumber = false;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private String examId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FormVisaPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        examId = getIntent().getStringExtra("exam_id");
        if (examId == null) {
            examId = "";
        }

        setupCardPreview();
        setupFieldFormatters();
        setupButtons();
    }

    private void setupButtons() {
        binding.btnConfirmPayment.setOnClickListener(v -> {
            if (validateCardDetails()) {
                processVisaPayment();
            }
        });

        binding.btnBackToOptions.setOnClickListener(v -> finish());
    }

    private boolean validateCardDetails() {
        String cardNumber = binding.etCardNumber.getText().toString().replace(" ", "");
        String cardHolder = binding.etCardHolder.getText().toString();
        String expiryDate = binding.etExpiryDate.getText().toString();
        String cvv = binding.etCvv.getText().toString();

        if (cardNumber.length() != 16) {
            binding.etCardNumber.setError("Número de tarjeta inválido");
            return false;
        }

        if (cardHolder.isEmpty()) {
            binding.etCardHolder.setError("Ingrese el nombre del titular");
            return false;
        }

        if (!expiryDate.matches("^(0[1-9]|1[0-2])/?([0-9]{2})$")) {
            binding.etExpiryDate.setError("Formato MM/AA inválido");
            return false;
        }

        if (cvv.length() < 3 || cvv.length() > 4) {
            binding.etCvv.setError("CVV inválido");
            return false;
        }

        return true;
    }

    private void setupCardPreview() {
        // Listener para número de tarjeta
        binding.etCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormattingCardNumber) return;

                isFormattingCardNumber = true;

                // Formatear número de tarjeta
                String digits = s.toString().replaceAll("[^\\d]", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < digits.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(digits.charAt(i));
                }

                // Actualizar campo de entrada
                binding.etCardNumber.removeTextChangedListener(this);
                binding.etCardNumber.setText(formatted.toString());
                binding.etCardNumber.setSelection(formatted.length());
                binding.etCardNumber.addTextChangedListener(this);

                // Actualizar vista previa
                if (digits.isEmpty()) {
                    binding.cardNumber.setText("1234 5678 9012 3456");
                } else {
                    binding.cardNumber.setText(formatted.toString());
                }

                isFormattingCardNumber = false;
            }
        });

        // Listener para nombre del titular
        binding.etCardHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String cardHolder = s.toString();
                if (cardHolder.isEmpty()) {
                    binding.cardHolder.setText("NOMBRE DEL TITULAR");
                } else {
                    binding.cardHolder.setText(cardHolder.toUpperCase());
                }
            }
        });

        // Listener para fecha de expiración
        binding.etExpiryDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String expiryDate = s.toString();
                if (expiryDate.isEmpty()) {
                    binding.cardExpiry.setText("MM/AA");
                } else {
                    binding.cardExpiry.setText(expiryDate);
                }
            }
        });

        // Listener para CVV (opcional)
        binding.etCvv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String cvv = s.toString();
                if (cvv.isEmpty()) {
                    binding.cardCvv.setText("•••");
                } else {
                    binding.cardCvv.setText("•".repeat(cvv.length()));
                }
            }
        });
    }

    private void setupFieldFormatters() {
        // Formateador para fecha de expiración
        binding.etExpiryDate.addTextChangedListener(new TextWatcher() {
            private static final int MAX_LENGTH = 5; // MM/AA

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && (s.length() % 3) == 0) {
                    char c = s.charAt(s.length() - 1);
                    if ('/' == c) {
                        s.delete(s.length() - 1, s.length());
                    } else if (Character.isDigit(c) && s.length() == 2) {
                        s.insert(2, "/");
                    }
                }

                if (s.length() > MAX_LENGTH) {
                    s.delete(MAX_LENGTH, s.length());
                }
            }
        });

        // Formateador para CVV
        binding.etCvv.addTextChangedListener(new TextWatcher() {
            private static final int MAX_LENGTH = 4;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > MAX_LENGTH) {
                    s.delete(MAX_LENGTH, s.length());
                }
            }
        });
    }

    private void processVisaPayment() {
        String cardNumber = binding.etCardNumber.getText().toString().replace(" ", "");
        String cardHolder = binding.etCardHolder.getText().toString();
        String expiryDate = binding.etExpiryDate.getText().toString();
        String cvv = binding.etCvv.getText().toString();

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("amount", 150.00);
        paymentData.put("date", System.currentTimeMillis());
        paymentData.put("concept", "Pago de examen MTC");
        paymentData.put("method", "visa");
        paymentData.put("card_last_four", cardNumber.substring(cardNumber.length() - 4));
        paymentData.put("status", "completed");
        paymentData.put("exam_id", examId);

        db.collection("payments")
                .add(paymentData)
                .addOnSuccessListener(documentReference -> {
                    updateUserPaymentStatus(documentReference.getId());
                    Toast.makeText(this, "Pago procesado: •••• " + cardNumber.substring(cardNumber.length() - 4), Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al procesar pago: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserPaymentStatus(String paymentId) {
        String userEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;
        if (userEmail == null) return;

        String userDni = userEmail.split("@")[0];

        Map<String, Object> paymentStatus = new HashMap<>();
        paymentStatus.put("payment_id", paymentId);
        paymentStatus.put("exam_id", examId);
        paymentStatus.put("status", "paid");
        paymentStatus.put("payment_date", System.currentTimeMillis());

        db.collection("users")
                .document(userDni)
                .collection("payment_status")
                .document(examId)
                .set(paymentStatus);
    }
}