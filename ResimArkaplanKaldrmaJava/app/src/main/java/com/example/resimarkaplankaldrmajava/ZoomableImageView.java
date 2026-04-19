package com.example.resimarkaplankaldrmajava;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class ZoomableImageView extends View {

    public interface OnImageTapListener {
        void onImageTap(int bitmapX, int bitmapY);
    }

    private Bitmap bitmap;

    // Bitmap'i ekranda nasıl göstereceğimizi tutan matrix
    private final Matrix drawMatrix = new Matrix();

    // Matrix değerlerini okumak için yardımcı dizi
    private final float[] matrixValues = new float[9];

    // Gesture yardımcıları
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;

    private final PointF lastTouch = new PointF();

    private boolean isDragging = false;
    private boolean isMagnifierVisible = false;
    private boolean isLongPressMode = false;

    private float magnifierTouchX = 0f;
    private float magnifierTouchY = 0f;

    private OnImageTapListener onImageTapListener;

    private float minScale = 1f;
    private float maxScale = 5f;

    // Büyüteç ayarları
    private final float magnifierRadius = 170f;
    private final float magnifierZoom = 2.5f;
    private final float magnifierMargin = 28f;

    private final Paint magnifierBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint magnifierOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint magnifierCrossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint magnifierBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ZoomableImageView(Context context) {
        this(context, null);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());

        magnifierBorderPaint.setColor(Color.WHITE);
        magnifierBorderPaint.setStyle(Paint.Style.STROKE);
        magnifierBorderPaint.setStrokeWidth(6f);

        magnifierOuterPaint.setColor(Color.argb(120, 0, 0, 0));
        magnifierOuterPaint.setStyle(Paint.Style.STROKE);
        magnifierOuterPaint.setStrokeWidth(12f);

        magnifierCrossPaint.setColor(Color.RED);
        magnifierCrossPaint.setStyle(Paint.Style.STROKE);
        magnifierCrossPaint.setStrokeWidth(3f);

        magnifierBackgroundPaint.setColor(Color.WHITE);
        magnifierBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    public void setBitmap(Bitmap newBitmap) {
        boolean isFirstBitmap = (bitmap == null);
        bitmap = newBitmap;

        // İlk yüklemede ekrana sığdır.
        // Sonraki canlı güncellemelerde zoom'u bozma.
        if (isFirstBitmap) {
            resetZoom();
        } else {
            invalidate();
        }
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void forceResetZoom() {
        resetZoom();
    }

    public void setOnImageTapListener(OnImageTapListener listener) {
        this.onImageTapListener = listener;
    }

    public void resetZoom() {
        if (bitmap == null) return;
        if (getWidth() == 0 || getHeight() == 0) return;

        drawMatrix.reset();

        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float bmpWidth = bitmap.getWidth();
        float bmpHeight = bitmap.getHeight();

        float scale = Math.min(viewWidth / bmpWidth, viewHeight / bmpHeight);
        minScale = scale;

        float dx = (viewWidth - bmpWidth * scale) / 2f;
        float dy = (viewHeight - bmpHeight * scale) / 2f;

        drawMatrix.postScale(scale, scale);
        drawMatrix.postTranslate(dx, dy);

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (bitmap != null) {
            resetZoom();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (bitmap == null) return;

        canvas.drawBitmap(bitmap, drawMatrix, null);

        if (isMagnifierVisible) {
            drawMagnifier(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouch.set(event.getX(), event.getY());
                isDragging = false;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                hideMagnifier();
                break;

            case MotionEvent.ACTION_MOVE:
                if (isMagnifierVisible) {
                    magnifierTouchX = event.getX();
                    magnifierTouchY = event.getY();
                    invalidate();
                }

                if (!scaleDetector.isInProgress()
                        && event.getPointerCount() == 1
                        && !isLongPressMode) {

                    float dx = event.getX() - lastTouch.x;
                    float dy = event.getY() - lastTouch.y;

                    if (Math.abs(dx) > 2f || Math.abs(dy) > 2f) {
                        isDragging = true;

                        drawMatrix.postTranslate(dx, dy);
                        fixTranslation();
                        invalidate();

                        lastTouch.set(event.getX(), event.getY());
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                hideMagnifier();
                isDragging = false;
                isLongPressMode = false;
                break;

            case MotionEvent.ACTION_CANCEL:
                hideMagnifier();
                isDragging = false;
                isLongPressMode = false;
                break;
        }

        return true;
    }

    public Point mapTouchToBitmap(float touchX, float touchY) {
        if (bitmap == null) return null;

        Matrix inverse = new Matrix();
        if (!drawMatrix.invert(inverse)) return null;

        float[] pts = new float[]{touchX, touchY};
        inverse.mapPoints(pts);

        int bx = (int) pts[0];
        int by = (int) pts[1];

        if (bx < 0 || bx >= bitmap.getWidth() || by < 0 || by >= bitmap.getHeight()) {
            return null;
        }

        return new Point(bx, by);
    }

    private void fixTranslation() {
        if (bitmap == null) return;

        drawMatrix.getValues(matrixValues);

        float currentScaleX = matrixValues[Matrix.MSCALE_X];
        float transX = matrixValues[Matrix.MTRANS_X];
        float transY = matrixValues[Matrix.MTRANS_Y];

        float contentWidth = bitmap.getWidth() * currentScaleX;
        float contentHeight = bitmap.getHeight() * currentScaleX;

        float fixedX = transX;
        float fixedY = transY;

        if (contentWidth <= getWidth()) {
            fixedX = (getWidth() - contentWidth) / 2f;
        } else {
            float minX = getWidth() - contentWidth;
            float maxX = 0f;
            fixedX = clamp(fixedX, minX, maxX);
        }

        if (contentHeight <= getHeight()) {
            fixedY = (getHeight() - contentHeight) / 2f;
        } else {
            float minY = getHeight() - contentHeight;
            float maxY = 0f;
            fixedY = clamp(fixedY, minY, maxY);
        }

        matrixValues[Matrix.MTRANS_X] = fixedX;
        matrixValues[Matrix.MTRANS_Y] = fixedY;
        drawMatrix.setValues(matrixValues);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    private void hideMagnifier() {
        if (isMagnifierVisible) {
            isMagnifierVisible = false;
            invalidate();
        }
    }

    private void drawMagnifier(Canvas canvas) {
        if (bitmap == null) return;

        Point mapped = mapTouchToBitmap(magnifierTouchX, magnifierTouchY);
        if (mapped == null) return;

        float bitmapX = mapped.x;
        float bitmapY = mapped.y;

        float cx = getWidth() - magnifierRadius - magnifierMargin;
        float cy = magnifierRadius + magnifierMargin;

        Path circlePath = new Path();
        circlePath.addCircle(cx, cy, magnifierRadius, Path.Direction.CW);

        Matrix magnifierMatrix = new Matrix();
        magnifierMatrix.postTranslate(-bitmapX, -bitmapY);
        magnifierMatrix.postScale(magnifierZoom, magnifierZoom);
        magnifierMatrix.postTranslate(cx, cy);

        int saveCount = canvas.save();

        canvas.drawCircle(cx, cy, magnifierRadius, magnifierBackgroundPaint);
        canvas.clipPath(circlePath);
        canvas.drawBitmap(bitmap, magnifierMatrix, null);
        canvas.restoreToCount(saveCount);

        canvas.drawCircle(cx, cy, magnifierRadius, magnifierOuterPaint);
        canvas.drawCircle(cx, cy, magnifierRadius, magnifierBorderPaint);

        float crossHalf = 18f;
        canvas.drawLine(cx - crossHalf, cy, cx + crossHalf, cy, magnifierCrossPaint);
        canvas.drawLine(cx, cy - crossHalf, cx, cy + crossHalf, magnifierCrossPaint);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            hideMagnifier();
            isLongPressMode = false;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (bitmap == null) return false;

            drawMatrix.getValues(matrixValues);
            float currentScale = matrixValues[Matrix.MSCALE_X];

            float targetScale = currentScale * detector.getScaleFactor();
            targetScale = clamp(targetScale, minScale, maxScale);

            float factor = targetScale / currentScale;

            drawMatrix.postScale(
                    factor,
                    factor,
                    detector.getFocusX(),
                    detector.getFocusY()
            );

            fixTranslation();
            invalidate();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            isLongPressMode = true;
            isDragging = false;

            magnifierTouchX = e.getX();
            magnifierTouchY = e.getY();
            isMagnifierVisible = true;
            invalidate();
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (isDragging || isLongPressMode || isMagnifierVisible) {
                return true;
            }

            if (onImageTapListener != null) {
                Point mapped = mapTouchToBitmap(e.getX(), e.getY());
                if (mapped != null) {
                    onImageTapListener.onImageTap(mapped.x, mapped.y);
                }
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            hideMagnifier();
            isLongPressMode = false;

            drawMatrix.getValues(matrixValues);
            float currentScale = matrixValues[Matrix.MSCALE_X];

            float newScale;
            if (currentScale < (minScale * 2f)) {
                newScale = Math.min(currentScale * 2f, maxScale);
            } else {
                newScale = minScale;
            }

            float factor = newScale / currentScale;
            drawMatrix.postScale(factor, factor, e.getX(), e.getY());
            fixTranslation();
            invalidate();
            return true;
        }
    }

    public static class Point {
        public final int x;
        public final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
