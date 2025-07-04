package com.master.verificamtc.user.circuit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View {
    private Paint paint;
    private final Matrix imageMatrix = new Matrix();

    public OverlayView(Context context) {
        super(context);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(0x55FF0000); // Rojo semi-transparente
        paint.setStyle(Paint.Style.FILL);
    }

    // Recibe la matriz de transformación de la imagen
    public void setImageMatrix(Matrix matrix) {
        this.imageMatrix.set(matrix);
        invalidate(); // Redibuja cuando cambia
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Aplica la transformación para escalar y desplazar los rectángulos junto con la imagen
        canvas.concat(imageMatrix);

        // Coordenadas relativas a la imagen original
        RectF curva = new RectF(150, 300, 250, 400);
        RectF estacionamiento = new RectF(450, 600, 580, 700);

        canvas.drawRect(curva, paint);
        canvas.drawRect(estacionamiento, paint);
    }
}
