package com.master.verificamtc.admin.dashboard;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.master.verificamtc.R;
import com.master.verificamtc.database.AppDatabase;
import java.util.ArrayList;

public class PaymentsListActivity extends AppCompatActivity {
    private ListView paymentsListView;
    private AppDatabase databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments_list);

        paymentsListView = findViewById(R.id.paymentsListView);
        databaseHelper = new AppDatabase(this);

        displayPaymentsList();
    }

    private void displayPaymentsList() {
        Cursor cursor = databaseHelper.getAllPayments();
        ArrayList<String> paymentsList = new ArrayList<>();
        String estado;

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No hay pagos registrados", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                if (cursor.getInt(2)==0){
                    estado = "Pendiente";
                }
                else{
                    estado = "Pagado";
                }
                String paymentData =
                        "ID Pago: "   + cursor.getString(0)
                                + "\nUsuario: "  + cursor.getString(1)
                                + "\nEstado de pago: "    + estado   // monto es integer (0 o 1) o bien real
                                + "\nFecha: "    + cursor.getString(3);
                paymentsList.add(paymentData);
            }
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                paymentsList
        );
        paymentsListView.setAdapter(adapter);
    }

}