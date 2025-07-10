package com.master.verificamtc.admin.recognition;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.master.verificamtc.R;

public class FaceDetectionActivity extends AppCompatActivity {
    //public static HashMap<String, FaceClassifier.Recognition> registered = new HashMap<>();

    Button registerBtn,recognizeBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);

        registerBtn = findViewById(R.id.buttonregister);
        recognizeBtn = findViewById(R.id.buttonrecognize);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FaceDetectionActivity.this, RegisterActivity.class));
            }
        });

        recognizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FaceDetectionActivity.this, RecognitionActivity.class));
            }
        });
    }

}
