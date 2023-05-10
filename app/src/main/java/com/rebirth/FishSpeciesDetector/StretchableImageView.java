package com.rebirth.FishSpeciesDetector;
import android.content.Context;
import android.util.AttributeSet;


public class StretchableImageView extends androidx.appcompat.widget.AppCompatImageView {

    public StretchableImageView(Context context) {
        super(context);
    }

    public StretchableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StretchableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
