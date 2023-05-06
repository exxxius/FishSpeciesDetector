package com.rebirth.FishSpeciesDetector;

public class BoundingBox {
    private final float left;
    private final float top;
    private final float right;
    private final float bottom;

    public BoundingBox(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public float getLeft() {
        return left;
    }

    public float getTop() {
        return top;
    }

    public float getRight() {
        return right;
    }

    public float getBottom() {
        return bottom;
    }
}
