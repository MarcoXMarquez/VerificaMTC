package com.master.verificamtc.admin.dashboard;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

import java.util.ArrayList;

public class PaymentsListActivity extends AppCompatActivity {
    private ListView paymentsListView;
    private ArrayList<String> paymentsList;
    private ArrayAdapter<String> adapter;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments_list);

        paymentsListView = findViewById(R.id.paymentsListView);
        paymentsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                paymentsList
        );
        paymentsListView.setAdapter(adapter);

        // Vamos a leer /users en vez de /payments
        usersRef = FirebaseDatabase.getInstance()
                .getReference("users");

        loadPaymentStatuses();
    }

    private void loadPaymentStatuses() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                paymentsList.clear();
                if (!snap.exists()) {
                    Toast.makeText(PaymentsListActivity.this,
                            "No hay usuarios registrados", Toast.LENGTH_SHORT).show();
                } else {
                    for (DataSnapshot child : snap.getChildren()) {
                        User u = child.getValue(User.class);
                        if (u != null) {
                            String estadoPago = u.paymentStatus ? "Realizado" : "Pendiente";
                            String entry = "Usuario: "  + u.firstName + " " + u.lastName
                                    + "\nDNI: "      + u.dni
                                    + "\nPago: "     + estadoPago;
                            paymentsList.add(entry);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
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
