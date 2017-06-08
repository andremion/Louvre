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

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom {@link FloatingActionButton.Behavior} to hide {@link FloatingActionButton} when user is scrolling
 */
public class FabBehavior extends FloatingActionButton.Behavior {

    public FabBehavior() {
    }

    public FabBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                                       View directTargetChild, View target, int nestedScrollAxes) {
        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes)
                || ((nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
                && target instanceof RecyclerView);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (Math.abs(dyConsumed) > 0 && child.getVisibility() == View.VISIBLE) {
            child.hide();
        }
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target) {
        super.onStopNestedScroll(coordinatorLayout, child, target);
        child.show();
    }

}
