package com.andremion.louvre;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v7.app.AppCompatDelegate;

import com.andremion.louvre.home.GalleryActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

    private final Activity mActivity;
    private int mRequestCode;
    private int mMaxSelection;
    private String[] mMediaTypeFilter;

    private Louvre(@NonNull Activity activity) {
        mActivity = activity;
    }

    public static Louvre init(@NonNull Activity activity) {
        return new Louvre(activity);
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
     * Set the media type to filter the query
     */
    public Louvre setMediaTypeFilter(@MediaType @NonNull String... mediaTypeFilter) {
        mMediaTypeFilter = mediaTypeFilter;
        return this;
    }

    public void open() {
        GalleryActivity.startActivity(mActivity, mRequestCode, mMaxSelection, mMediaTypeFilter);
    }

}
