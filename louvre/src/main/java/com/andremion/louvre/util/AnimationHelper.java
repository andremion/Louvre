/*
 * Copyright (c) 2017. André Mion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andremion.louvre.util;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import android.view.View;

public class AnimationHelper {

    public static void scaleView(@NonNull View view, @FloatRange(from = 0, to = 1) float scale) {
        if (view.getScaleX() != scale || view.getScaleY() != scale) {
            long duration = view.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
            view.animate().scaleX(scale).scaleY(scale).setDuration(duration).start();
        }
    }

}
