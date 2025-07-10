package com.master.verificamtc.user.circuit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class OverlayView extends View {
    private Paint paint;
    private final Matrix imageMatrix = new Matrix();
    private OnZoneClickListener onZoneClickListener;
    private final RectF curva = new RectF(150, 300, 250, 400);
    private final RectF estacionamiento = new RectF(450, 600, 580, 700);

    public interface OnZoneClickListener {
        void onZoneClick(String zoneType);
    }

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
        paint.setColor(0x55FF0000);
        paint.setStyle(Paint.Style.FILL);
        setClickable(true);
    }

    public void setImageMatrix(Matrix matrix) {
        this.imageMatrix.set(matrix);
        invalidate();
    }

    public void setOnZoneClickListener(OnZoneClickListener listener) {
        this.onZoneClickListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.concat(imageMatrix);
        canvas.drawRect(curva, paint);
        canvas.drawRect(estacionamiento, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && onZoneClickListener != null) {
            float[] touchPoint = new float[]{event.getX(), event.getY()};
            Matrix inverse = new Matrix();
            imageMatrix.invert(inverse);
            inverse.mapPoints(touchPoint);

            if (curva.contains(touchPoint[0], touchPoint[1])) {
                onZoneClickListener.onZoneClick("curva");
                return true;
            } else if (estacionamiento.contains(touchPoint[0], touchPoint[1])) {
                onZoneClickListener.onZoneClick("estacionamiento");
                return true;
            }
        }
        return false;
    }
}