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

package com.andremion.louvre;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;

import com.andremion.louvre.home.GalleryActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * A small customizable image picker. Useful to handle an image pick action built-in
 */
public class Louvre {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static final String IMAGE_TYPE_BMP = "image/bmp";
    public static final String IMAGE_TYPE_JPEG = "image/jpeg";
    public static final String IMAGE_TYPE_PNG = "image/png";
    public static final String[] IMAGE_TYPES = {IMAGE_TYPE_BMP, IMAGE_TYPE_JPEG, IMAGE_TYPE_PNG};

    @StringDef({IMAGE_TYPE_BMP, IMAGE_TYPE_JPEG, IMAGE_TYPE_PNG})
    @Retention(RetentionPolicy.SOURCE)
    @interface MediaType {
    }

    private Activity mActivity;
    private Fragment mFragment;
    private int mRequestCode;
    private int mMaxSelection;
    private List<Uri> mSelection;
    private String[] mMediaTypeFilter;

    private Louvre(@NonNull Activity activity) {
        mActivity = activity;
        mRequestCode = -1;
    }

    private Louvre(@NonNull Fragment fragment) {
        mFragment = fragment;
        mRequestCode = -1;
    }

    public static Louvre init(@NonNull Activity activity) {
        return new Louvre(activity);
    }

    public static Louvre init(@NonNull Fragment fragment) {
        return new Louvre(fragment);
    }

    /**
     * Set the request code to return on {@link Activity#onActivityResult(int, int, Intent)}
     */
    public Louvre setRequestCode(int requestCode) {
        mRequestCode = requestCode;
        return this;
    }

    /**
     * Set the max images allowed to pick
     */
    public Louvre setMaxSelection(@IntRange(from = 0) int maxSelection) {
        mMaxSelection = maxSelection;
        return this;
    }

    /**
     * Set the current selected items
     */
    public Louvre setSelection(@NonNull List<Uri> selection) {
        mSelection = selection;
        return this;
    }

    /**
     * Set the media type to filter the query with a combination of one of these types: {@link #IMAGE_TYPE_BMP}, {@link #IMAGE_TYPE_JPEG}, {@link #IMAGE_TYPE_PNG}
     */
    public Louvre setMediaTypeFilter(@MediaType @NonNull String... mediaTypeFilter) {
        mMediaTypeFilter = mediaTypeFilter;
        return this;
    }

    public void open() {
        if (mRequestCode == -1) {
            throw new IllegalArgumentException("You need to define a request code in setRequestCode(int) method");
        }
        if (mActivity != null) {
            GalleryActivity.startActivity(mActivity, mRequestCode, mMaxSelection, mSelection, mMediaTypeFilter);
        } else {
            GalleryActivity.startActivity(mFragment, mRequestCode, mMaxSelection, mSelection, mMediaTypeFilter);
        }
    }

}
