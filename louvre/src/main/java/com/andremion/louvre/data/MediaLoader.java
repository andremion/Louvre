package com.andremion.louvre.data;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.andremion.louvre.R;

import static com.andremion.louvre.data.MediaQuery.ALL_IMAGE_PROJECTION;
import static com.andremion.louvre.data.MediaQuery.BUCKET_PROJECTION;
import static com.andremion.louvre.data.MediaQuery.BUCKET_SELECTION;
import static com.andremion.louvre.data.MediaQuery.BUCKET_SORT_ORDER;
import static com.andremion.louvre.data.MediaQuery.GALLERY_URI;
import static com.andremion.louvre.data.MediaQuery.IMAGE_PROJECTION;
import static com.andremion.louvre.data.MediaQuery.MEDIA_SORT_ORDER;

/**
 * {@link Loader} for media and bucket data
 */
public class MediaLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TIME_LOADER = 0;
    private static final int BUCKET_LOADER = 1;
    private static final int MEDIA_LOADER = 2;

    static final long ALL_MEDIA_BUCKET_ID = 0;
    private static final String BUCKET_ID = MediaStore.Images.Media.BUCKET_ID;

    public interface Callbacks {

        void onBucketLoadFinished(@Nullable Cursor data);

        void onMediaLoadFinished(@Nullable Cursor data);
    }

    private FragmentActivity mActivity;
    private Callbacks mCallbacks;
    private String mTypeFilter;

    public MediaLoader() {
        // 1 means all media type.
        mTypeFilter = "1";
    }

    @Override
    public final Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == TIME_LOADER) {
            return new CursorLoader(mActivity,
                    GALLERY_URI,
                    ALL_IMAGE_PROJECTION,
                    mTypeFilter,
                    null,
                    MEDIA_SORT_ORDER);
        }
        if (id == BUCKET_LOADER) {
            return new CursorLoader(mActivity,
                    GALLERY_URI,
                    BUCKET_PROJECTION,
                    String.format("%s AND %s", mTypeFilter, BUCKET_SELECTION),
                    null,
                    BUCKET_SORT_ORDER);
        }
        // id == MEDIA_LOADER
        return new CursorLoader(mActivity,
                GALLERY_URI,
                IMAGE_PROJECTION,
                String.format("%s=%s AND %s", MediaStore.Images.Media.BUCKET_ID, args.getLong(BUCKET_ID), mTypeFilter),
                null,
                MEDIA_SORT_ORDER);
    }

    @Override
    public final void onLoadFinished(@NonNull Loader<Cursor> loader, @Nullable Cursor data) {
        if (mCallbacks != null) {
            if (loader.getId() == BUCKET_LOADER) {
                mCallbacks.onBucketLoadFinished(addAllMediaBucketItem(data));
            } else {
                mCallbacks.onMediaLoadFinished(data);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // no-op
    }

    public void onAttach(@NonNull FragmentActivity activity, @NonNull Callbacks callbacks) {
        mActivity = activity;
        mCallbacks = callbacks;
    }

    public void onDetach() {
        mActivity = null;
        mCallbacks = null;
    }

    public void setMediaTypes(@NonNull String[] mediaTypes) {
        StringBuilder filter = new StringBuilder();
        for (String type : mediaTypes) {
            if (filter.length() > 0) {
                filter.append(",");
            }
            filter.append(String.format("'%s'", type));
        }
        if (filter.length() > 0) {
            mTypeFilter = MediaStore.Images.ImageColumns.MIME_TYPE + " IN (" + filter + ")";
        }
    }

    public void loadBuckets() {
        ensureActivityAttached();
        mActivity.getSupportLoaderManager().restartLoader(BUCKET_LOADER, null, this);
    }

    public void loadByBucket(@IntRange(from = 0) long bucketId) {
        ensureActivityAttached();
        if (ALL_MEDIA_BUCKET_ID == bucketId) {
            mActivity.getSupportLoaderManager().restartLoader(TIME_LOADER, null, this);
        } else {
            Bundle args = new Bundle();
            args.putLong(BUCKET_ID, bucketId);
            mActivity.getSupportLoaderManager().restartLoader(MEDIA_LOADER, args, this);
        }
    }

    /**
     * Ensure that a FragmentActivity is attached to this loader.
     */
    private void ensureActivityAttached() {
        if (mActivity == null) {
            throw new IllegalStateException("The FragmentActivity was not attached!");
        }
    }

    /**
     * Add "All Media" item as the first row of bucket items.
     *
     * @param cursor The original data of all bucket items
     * @return The data with "All Media" item added
     */
    private Cursor addAllMediaBucketItem(@Nullable Cursor cursor) {
        if (cursor == null || !cursor.moveToPosition(0)) {
            return null;
        }
        ensureActivityAttached();
        long id = ALL_MEDIA_BUCKET_ID;
        String label = mActivity.getString(R.string.activity_gallery_bucket_all_media);
        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        MatrixCursor allMediaRow = new MatrixCursor(BUCKET_PROJECTION);
        allMediaRow.newRow()
                .add(id)
                .add(label)
                .add(data);
        return new MergeCursor(new Cursor[]{allMediaRow, cursor});
    }

}
