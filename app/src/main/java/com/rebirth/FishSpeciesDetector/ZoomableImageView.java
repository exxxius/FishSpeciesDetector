package com.rebirth.FishSpeciesDetector;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.appcompat.widget.AppCompatImageView;

public class ZoomableImageView extends AppCompatImageView {

    private ScaleGestureDetector scaleGestureDetector;
    private Matrix matrix;
    private float scaleFactor = 1.0f;

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        matrix = new Matrix();
        setScaleType(ScaleType.MATRIX);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
            matrix.setScale(scaleFactor, scaleFactor);
            setImageMatrix(matrix);
            return true;
        }
    }
}
