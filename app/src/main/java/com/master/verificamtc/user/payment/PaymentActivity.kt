package com.master.verificamtc.user.payment

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.master.verificamtc.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var examId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        examId = intent.getStringExtra("exam_id") ?: ""

        setupPaymentOptions()
    }

    private fun setupPaymentOptions() {
        binding.cardVisa.setOnClickListener {
            val intent = Intent(this, UserPaymentActivity::class.java).apply {
                putExtra("exam_id", examId)
            }
            startActivity(intent)
        }

        binding.cardCash.setOnClickListener {
            processCashPayment()
        }
    }

    private fun processCashPayment() {
        val paymentData = hashMapOf(
            "amount" to 150.00, // Monto fijo para el examen
            "date" to System.currentTimeMillis(),
            "concept" to "Pago de examen MTC",
            "method" to "cash",
            "status" to "pending",
            "exam_id" to examId
        )

        db.collection("payments")
            .add(paymentData)
            .addOnSuccessListener { documentReference ->
                updateUserPaymentStatus(documentReference.id)
                Toast.makeText(this, "Pago en efectivo registrado. Diríjase a caja.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al registrar pago: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserPaymentStatus(paymentId: String) {
        val userDni = auth.currentUser?.email?.split("@")?.first() ?: return

        val paymentStatus = hashMapOf(
            "payment_id" to paymentId,
            "exam_id" to examId,
            "status" to "pending_verification",
            "last_update" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userDni)
            .collection("payment_status")
            .document(examId)
            .set(paymentStatus)
    }
}