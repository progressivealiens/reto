package com.techsalt.reto.crop_slider.util;

import android.content.res.Resources;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import com.techsalt.reto.R;

public class PaintUtil {

    // Public Methods //////////////////////////////////////////////////////////

    /**
     * Creates the Paint object for drawing the crop window border.
     */
    public static Paint newBorderPaint(@NonNull Resources resources) {

        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(resources.getDimension(R.dimen.border_thickness));
        paint.setColor(resources.getColor(R.color.border));

        return paint;
    }

    /**
     * Creates the Paint object for drawing the crop window guidelines.
     */
    public static Paint newGuidelinePaint(@NonNull Resources resources) {

        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(resources.getDimension(R.dimen.guideline_thickness));
        paint.setColor(resources.getColor(R.color.guideline));

        return paint;
    }

    /**
     * Creates the Paint object for drawing the translucent overlay outside the crop window.
     *
     * @return the new Paint object
     */
    public static Paint newSurroundingAreaOverlayPaint(@NonNull Resources resources) {

        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(resources.getColor(R.color.surrounding_area));

        return paint;
    }

    /**
     * Creates the Paint object for drawing the corners of the border
     */
    public static Paint newCornerPaint(@NonNull Resources resources) {

        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(resources.getDimension(R.dimen.corner_thickness));
        paint.setColor(resources.getColor(R.color.corner));

        return paint;
    }
}
