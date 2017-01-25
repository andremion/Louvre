package com.andremion.louvre.util;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.view.View;

public class AnimationHelper {

    public static void scaleView(@NonNull View view, @FloatRange(from = 0, to = 1) float scale) {
        if (view.getScaleX() != scale || view.getScaleY() != scale) {
            long duration = view.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
            view.animate().scaleX(scale).scaleY(scale).setDuration(duration).start();
        }
    }

}
