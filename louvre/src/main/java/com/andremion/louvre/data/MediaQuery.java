package com.andremion.louvre.data;

import android.net.Uri;
import android.provider.MediaStore;

/**
 * Helper class used by {@link MediaLoader}
 */
class MediaQuery {

    private MediaQuery() {
    }

    static final Uri GALLERY_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    static final String[] IMAGE_PROJECTION = {
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.BUCKET_ID,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATA
    };
    static final String[] ALL_IMAGE_PROJECTION = {
            MediaStore.Images.ImageColumns._ID,
            MediaLoader.ALL_MEDIA_BUCKET_ID + " AS " + MediaStore.Images.ImageColumns.BUCKET_ID,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATA
    };
    static final String MEDIA_SORT_ORDER = MediaStore.Images.Media.DATE_TAKEN + " DESC";

    static final String[] BUCKET_PROJECTION = {
            MediaStore.Images.ImageColumns.BUCKET_ID,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATA
    };
    // The template for "WHERE" parameter is like:
    //    SELECT ... FROM ... WHERE (%s)
    // and we make it look like:
    //    SELECT ... FROM ... WHERE (1) GROUP BY (1)
    // The "WHERE (1)" means true.
    // The "GROUP BY (1)" means the first column specified after SELECT.
    // Note that because there is a "(" and )" in the template, we use "1)" and "(1" to match it.
    //
    // *Hack pulled from https://android.googlesource.com/platform/packages/apps/Gallery2/+/android-4.4.2_r2/src/com/android/gallery3d/data/BucketHelper.java
    static final String BUCKET_SELECTION = "1) GROUP BY (1";
    static final String BUCKET_SORT_ORDER = "MAX(" + MediaStore.Images.Media.DATE_TAKEN + ") DESC";

}
