package com.master.verificamtc.admin.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.master.verificamtc.R;
import com.master.verificamtc.models.User;

public class PaymentsListActivity extends AppCompatActivity {
    private LinearLayout paymentsContainer;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments_list);

        paymentsContainer = findViewById(R.id.paymentsContainer);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        loadPaymentStatuses();
    }

    private void loadPaymentStatuses() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                paymentsContainer.removeAllViews();
                if (!snap.exists()) {
                    Toast.makeText(PaymentsListActivity.this,
                            "No hay usuarios registrados", Toast.LENGTH_SHORT).show();
                } else {
                    LayoutInflater inflater = LayoutInflater.from(PaymentsListActivity.this);
                    for (DataSnapshot child : snap.getChildren()) {
                        User u = child.getValue(User.class);
                        if (u != null) {
                            View item = inflater.inflate(R.layout.item_payment, paymentsContainer, false);

                            TextView tvUser = item.findViewById(R.id.tvPaymentUser);
                            TextView tvDni = item.findViewById(R.id.tvPaymentDni);
                            TextView tvStatus = item.findViewById(R.id.tvPaymentStatus);
                            ImageView ivIcon = item.findViewById(R.id.ivPaymentIcon);

                            tvUser.setText(u.firstName + " " + u.lastName);
                            tvDni.setText("DNI: " + u.dni);
                            boolean paid = u.paymentStatus;
                            tvStatus.setText(paid ? "Pago realizado" : "Pago pendiente");
                            tvStatus.setTextColor(getResources().getColor(
                                    paid ? R.color.success_green : R.color.warning_orange
                            ));
                            ivIcon.setImageResource(
                                    paid ? R.drawable.user_payment_2 : R.drawable.user_payment_1
                            );

                            paymentsContainer.addView(item);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PaymentsListActivity.this,
                        "Error Firebase: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
