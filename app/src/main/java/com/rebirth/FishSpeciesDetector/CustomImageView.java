//package com.rebirth.FishSpeciesDetector;
//
//import android.content.Context;
//import android.graphics.Matrix;
//import android.graphics.PointF;
//import android.util.AttributeSet;
//import android.view.GestureDetector;
//import android.view.MotionEvent;
//import android.view.ScaleGestureDetector;
//
//import androidx.appcompat.widget.AppCompatImageView;
//
//public class CustomImageView extends AppCompatImageView {
//
//    // These matrices will be used to move and zoom image
//    Matrix matrix = new Matrix();
//    Matrix savedMatrix = new Matrix();
//
//    // We can be in one of these 3 states
//    static final int NONE = 0;
//    static final int DRAG = 1;
//    static final int ZOOM = 2;
//    int mode = NONE;
//
//    // Remember some things for zooming
//    PointF start = new PointF();
//    PointF mid = new PointF();
//    float oldDist = 1f;
//
//    // Gesture detectors
//    GestureDetector gestureDetector;
//    ScaleGestureDetector scaleGestureDetector;
//
//    public CustomImageView(Context context) {
//        super(context);
//        init(context);
//    }
//
//    public CustomImageView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        init(context);
//    }
//
//    public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init(context);
//    }
//
//    private void init(Context context) {
//        super.setClickable(true);
//        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
//            @Override
//            public boolean onDoubleTap(MotionEvent e) {
//                float x = e.getX();
//                float y = e.getY();
//                float currentScale = getScale();
//                float targetScale = (currentScale == 1f) ? 2f : 1f;
//                CustomImageView.this.post(new AnimatedZoomRunnable(currentScale, targetScale, x, y));
//                return true;
//            }
//        });
//        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener(){
//            @Override
//            public boolean onScaleBegin(ScaleGestureDetector detector) {
//                mode = ZOOM;
//                return true;
//            }
//        });
//        matrix.setTranslate(1f, 1f);
//        setImageMatrix(matrix);
//        setScaleType(ScaleType.MATRIX);
//    }
//
//    public float getScale() {
//        float[] values = new float[9];
//        matrix.getValues(values);
//        return values[Matrix.MSCALE_X];
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        scaleGestureDetector.onTouchEvent(event);
//        gestureDetector.onTouchEvent(event);
//
//        PointF curr = new PointF(event.getX(), event.getY());
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                savedMatrix.set(matrix);
//                start.set(curr);
//                mode = DRAG;
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//                if (mode == DRAG) {
//                    matrix.set(savedMatrix);
//                    float dx = curr.x - start.x;
//                    float dy = curr.y - start.y;
//                    matrix.postTranslate(dx, dy);
//                }
//                break;
//
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_POINTER_UP:
//                mode = NONE;
//                break;
//        }
//
//        setImageMatrix(matrix);
//        return true; // indicate event was handled
//    }
//
//    private class AnimatedZoomRunnable implements Runnable {
//        static final float ZOOM_TIME = 500f; // 500 ms
//        private long startTime;
//        private float startZoom, targetZoom;
//        private float centerX, centerY;
//
//        public AnimatedZoomRunnable(float startZoom, float targetZoom, float centerX, float centerY) {
//            this.startTime = System.currentTimeMillis();
//            this.startZoom = startZoom;
//            this.targetZoom = targetZoom;
//            this.centerX = centerX;
//            this.centerY = centerY;
//        }
//
//        @Override
//        public void run() {
//            float t = interpolate();
//            float scale = startZoom + t * (targetZoom - startZoom);
//            float deltaScale = scale / getScale();
//
//            matrix.postScale(deltaScale, deltaScale, centerX, centerY);
//            setImageMatrix(matrix);
//
//            // We haven't hit our target scale yet, so post ourselves again
//            if (t < 1f) {
//                post(this);
//            }
//        }
//
//        private float interpolate() {
//            long now = System.currentTimeMillis();
//            float elapsed = (now - startTime) / ZOOM_TIME;
//            elapsed = Math.min(1f, elapsed);
//            return elapsed;
//        }
//    }
//}


package com.rebirth.FishSpeciesDetector;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.appcompat.widget.AppCompatImageView;

public class CustomImageView extends AppCompatImageView {

    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    // Gesture detectors
    GestureDetector gestureDetector;
    ScaleGestureDetector scaleGestureDetector;

    public CustomImageView(Context context) {
        super(context);
        init(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        super.setClickable(true);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float x = e.getX();
                float y = e.getY();
                float currentScale = getScale();
                float targetScale = (currentScale == 1f) ? 2f : 1f;
                if (targetScale == 1f) { // Reset matrix if zooming out
                    matrix.reset();
                }
                CustomImageView.this.post(new AnimatedZoomRunnable(currentScale, targetScale, x, y));
                return true;
            }
        });
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                mode = ZOOM;
                return true;
            }
        });
        matrix.setTranslate(1f, 1f);
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);
    }

    public float getScale() {
        float[] values = new float[9];
        matrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        PointF curr = new PointF(event.getX(), event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(curr);
                if (getScale() > 1f) { // Check if the image is already zoomed in
                    mode = DRAG;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = curr.x - start.x;
                    float dy = curr.y - start.y;
                    matrix.postTranslate(dx, dy);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
        }

        setImageMatrix(matrix);
        return true; // indicate event was handled
    }


    private class AnimatedZoomRunnable implements Runnable {
        static final float ZOOM_TIME = 500f; // 500 ms
        private long startTime;
        private float startZoom, targetZoom;
        private float centerX, centerY;

        public AnimatedZoomRunnable(float startZoom, float targetZoom, float centerX, float centerY) {
            this.startTime = System.currentTimeMillis();
            this.startZoom = startZoom;
            this.targetZoom = targetZoom;
            this.centerX = centerX;
            this.centerY = centerY;
        }

        @Override
        public void run() {
            float t = interpolate();
            float scale = startZoom + t * (targetZoom - startZoom);
            float deltaScale = scale / getScale();

            matrix.postScale(deltaScale, deltaScale, centerX, centerY);
            setImageMatrix(matrix);

            // We haven't hit our target scale yet, so post ourselves again
            if (t < 1f) {
                post(this);
            }
        }

        private float interpolate() {
            long now = System.currentTimeMillis();
            float elapsed = (now - startTime) / ZOOM_TIME;
            elapsed = Math.min(1f, elapsed);
            return elapsed;
        }
    }
}

