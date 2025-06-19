package com.master.verificamtc.user.payment

import com.master.verificamtc.R
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.master.verificamtc.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPaymentMethods()
        setupVisaCardForm()
    }

    private fun setupPaymentMethods() {
        binding.visaFormContainer.visibility = View.GONE
    }

    // Método para Visa
    fun selectVisa(view: View) {
        if (binding.visaFormContainer.visibility == View.VISIBLE) {
            hideVisaFormWithAnimation()
        } else {
            showVisaFormWithAnimation()
        }
    }

    // Método para Efectivo
    fun selectCash(view: View) {
        hideVisaFormWithAnimation()
        Toast.makeText(this, "Efectivo seleccionado", Toast.LENGTH_SHORT).show()
    }

    private fun showVisaFormWithAnimation() {
        binding.visaFormContainer.visibility = View.VISIBLE
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        binding.visaFormContainer.startAnimation(slideUp)
        setupFieldFormatters()
    }

    private fun hideVisaFormWithAnimation() {
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        slideDown.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                binding.visaFormContainer.visibility = View.GONE
            }
        })
        binding.visaFormContainer.startAnimation(slideDown)
    }

    private fun setupVisaCardForm() {
        binding.btnConfirmPayment.setOnClickListener {
            if (validateCardDetails()) {
                processVisaPayment()
            }
        }
    }

    private fun setupFieldFormatters() {
        binding.etCardNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Implementación del formateo...
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etCardExpiry.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Implementación del formateo...
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validateCardDetails(): Boolean {
        var isValid = true

        if (!isValidCardNumber(binding.etCardNumber.text.toString())) {
            binding.etCardNumber.error = "Número inválido"
            isValid = false
        }

        if (binding.etCardHolder.text.isNullOrEmpty()) {
            binding.etCardHolder.error = "Nombre requerido"
            isValid = false
        }

        if (!isValidExpiryDate(binding.etCardExpiry.text.toString())) {
            binding.etCardExpiry.error = "Fecha inválida"
            isValid = false
        }

        if (binding.etCardCvv.text?.length != 3) {
            binding.etCardCvv.error = "CVV inválido"
            isValid = false
        }

        return isValid
    }

    private fun isValidCardNumber(number: String): Boolean {
        // Implementación de validación...
        return TODO("Provide the return value")
    }

    private fun isValidExpiryDate(date: String): Boolean {
        // Implementación de validación...
        return TODO("Provide the return value")
    }

    private fun processVisaPayment() {
        // Lógica de pago...
    }
}