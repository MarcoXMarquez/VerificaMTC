package com.master.verificamtc.admin.recognition;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.master.verificamtc.R;
import com.master.verificamtc.helpers.facerecognition.FaceClassifier;
import com.master.verificamtc.helpers.facerecognition.TFLiteFaceRecognition;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.List;


public class RecognitionActivity extends AppCompatActivity {
    CardView galleryCard,cameraCard;
    ImageView imageView;
    Uri image_uri;
    public static final int PERMISSION_CODE = 100;
    private String dniDelUsuario;

    //TODO declare face detector
    // High-accuracy landmark detection and face classification
    FaceDetectorOptions highAccuracyOpts =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .build();
    FaceDetector detector;


    //TODO declare face recognizer
    FaceClassifier faceClassifier;

    //TODO get the image from gallery and display it
    ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        image_uri = result.getData().getData();
                        Bitmap inputImage = uriToBitmap(image_uri);
                        Bitmap rotated = rotateBitmap(inputImage);
                        imageView.setImageBitmap(rotated);
                        performFaceDetection(rotated);
                    }
                }
            });

    //TODO capture the image using camera and display it
    ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bitmap inputImage = uriToBitmap(image_uri);
                        Bitmap rotated = rotateBitmap(inputImage);
                        imageView.setImageBitmap(rotated);
                        performFaceDetection(rotated);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);

        //TODO handling permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){
                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, PERMISSION_CODE);
            }
        }

        //TODO initialize views
        galleryCard = findViewById(R.id.gallerycard);
        cameraCard = findViewById(R.id.cameracard);
        imageView = findViewById(R.id.imageView2);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("DNI_USUARIO")) {
            dniDelUsuario = intent.getStringExtra("DNI_USUARIO");
        } else {
            // Manejar el caso donde el DNI no se pasó correctamente
            // Podrías mostrar un error y cerrar la actividad, o usar un valor por defecto
            Log.e("RecognitionActivity", "DNI_USUARIO no encontrado en el Intent.");
            Toast.makeText(this, "Error: DNI no proporcionado.", Toast.LENGTH_LONG).show();
            finish(); // Cierra la actividad si el DNI es esencial
            return; // Evita continuar si el DNI no está presente
        }

        //TODO code for choosing images from gallery
        galleryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryActivityResultLauncher.launch(galleryIntent);
            }
        });

        //TODO code for capturing images using camera
        cameraCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        //TODO initialize face detector
        detector = FaceDetection.getClient(highAccuracyOpts);

        //TODO initialize face recognition model
        try {
            faceClassifier = TFLiteFaceRecognition.create(getAssets(),"facenet.tflite",160,false,getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO opens camera so that user can capture image
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        cameraActivityResultLauncher.launch(cameraIntent);
    }

    //TODO takes URI of the image and returns bitmap
    private Bitmap uriToBitmap(Uri selectedFileUri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(selectedFileUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

            parcelFileDescriptor.close();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    //TODO rotate image if image captured on samsung devices
    //TODO Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
    @SuppressLint("Range")
    public Bitmap rotateBitmap(Bitmap input){
        String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cur = getContentResolver().query(image_uri, orientationColumn, null, null, null);
        int orientation = -1;
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
        }
        Log.d("tryOrientation",orientation+"");
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate(orientation);
        Bitmap cropped = Bitmap.createBitmap(input,0,0, input.getWidth(), input.getHeight(), rotationMatrix, true);
        return cropped;
    }

    //TODO perform face detection
    Canvas canvas;
    public void performFaceDetection(Bitmap input){
        Bitmap mutableBmp = input.copy(Bitmap.Config.ARGB_8888,true);
        canvas = new Canvas(mutableBmp);
        InputImage image = InputImage.fromBitmap(input, 0);
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        // Task completed successfully
                                        Log.d("tryFace","Len = "+faces.size());
                                        for (Face face : faces) {
                                            Rect bounds = face.getBoundingBox();

                                            Paint p1 = new Paint();
                                            p1.setColor(Color.RED);
                                            p1.setStyle(Paint.Style.STROKE);
                                            p1.setStrokeWidth(5);
                                            performFaceRecognition(bounds,input);
                                            canvas.drawRect(bounds,p1);
                                        }
                                        imageView.setImageBitmap(mutableBmp);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
    }


    //TODO perform face recognition
    public void performFaceRecognition(Rect bound, Bitmap input) {
        if(bound.top<0){
            bound.top=0;
        }
        if(bound.left<0){
            bound.left=0;
        }
        if(bound.right>input.getWidth()){
            bound.right=input.getWidth()-1;
        }
        if (bound.bottom>input.getHeight()){
            bound.bottom=input.getHeight()-1;
        }
        Bitmap croppedFace = Bitmap.createBitmap(input, bound.left, bound.top, bound.width(), bound.height());
        croppedFace = Bitmap.createScaledBitmap(croppedFace, 160, 160, false);
        FaceClassifier.Recognition recognition = faceClassifier.recognizeImage(croppedFace, false);

        if (recognition != null) {
            Log.d("tryFR", recognition.getTitle() + "    " + recognition.getDistance());

            // Validar la distancia Y que el título coincida con el DNI
            if (recognition.getDistance() < 1 && dniDelUsuario != null && dniDelUsuario.equals(recognition.getTitle())) {
                Paint p1 = new Paint();
                p1.setColor(Color.WHITE);
                p1.setTextSize(30);
                canvas.drawText(recognition.getTitle(), bound.left, bound.top, p1);

                // Si el reconocimiento fue exitoso Y el DNI coincide, regresamos a AuthRegisterActivity
                Intent returnIntent = new Intent();
                // Opcionalmente, puedes pasar algún dato de vuelta si es necesario
                // returnIntent.putExtra("RECONOCIMIENTO_EXITOSO", true);
                setResult(RESULT_OK, returnIntent);
                finish(); // Cierra RecognitionActivity y regresa a AuthRegisterActivity
            } else {
                // El reconocimiento falló (distancia muy alta o DNI no coincide)
                String mensajeError;
                if (recognition.getDistance() >= 1) {
                    mensajeError = "No se pudo reconocer el rostro. Intente con otra foto.";
                } else {
                    mensajeError = "El rostro no coincide con el DNI registrado. Intente con otra foto.";
                }
                Log.d("tryFR", "Error de reconocimiento: " + mensajeError);
                Toast.makeText(this, mensajeError, Toast.LENGTH_LONG).show();

            }
        } else {
            // No se detectó ningún rostro o hubo un error en el reconocimiento
            Toast.makeText(this, "No se pudo procesar el rostro. Intente con otra foto.", Toast.LENGTH_LONG).show();
            // imageView.setImageBitmap(null); // Opcional
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}