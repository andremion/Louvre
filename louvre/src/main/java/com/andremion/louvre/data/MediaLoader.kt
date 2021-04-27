/*
 * Copyright (c) 2020. André Mion
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
package com.andremion.louvre.data

import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.IntRange
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.andremion.louvre.R

private const val TIME_LOADER = 0
private const val BUCKET_LOADER = 1
private const val MEDIA_LOADER = 2
private const val ARG_BUCKET_ID = MediaStore.Images.Media.BUCKET_ID

/**
 * [Loader] for media and bucket data
 */
class MediaLoader : LoaderManager.LoaderCallbacks<Cursor?> {

    interface Callbacks {
        fun onBucketLoadFinished(data: Cursor?)
        fun onMediaLoadFinished(data: Cursor?)
    }

    private var activity: FragmentActivity? = null
    private var callbacks: Callbacks? = null
    private var typeFilter = "1" // Means all media type.

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> =
        ensureActivityAttached().let { activity ->
            when (id) {
                TIME_LOADER -> CursorLoader(
                    activity,
                    GALLERY_URI,
                    IMAGE_PROJECTION,
                    typeFilter,
                    null,
                    MEDIA_SORT_ORDER
                )
                BUCKET_LOADER -> CursorLoader(
                    activity,
                    GALLERY_URI,
                    BUCKET_PROJECTION,
                    "$typeFilter AND $BUCKET_SELECTION",
                    null,
                    BUCKET_SORT_ORDER
                )
                // id == MEDIA_LOADER
                else -> CursorLoader(
                    activity,
                    GALLERY_URI,
                    IMAGE_PROJECTION,
                    "${MediaStore.Images.Media.BUCKET_ID}=${args?.getLong(ARG_BUCKET_ID) ?: 0} AND $typeFilter",
                    null,
                    MEDIA_SORT_ORDER
                )
            }
        }

    override fun onLoadFinished(loader: Loader<Cursor?>, data: Cursor?) {
        callbacks?.let { callbacks ->
            if (loader.id == BUCKET_LOADER) {
                callbacks.onBucketLoadFinished(finishUpBuckets(data))
            } else {
                callbacks.onMediaLoadFinished(data)
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor?>) {
        // no-op
    }

    fun onAttach(activity: FragmentActivity, callbacks: Callbacks) {
        this.activity = activity
        this.callbacks = callbacks
    }

    fun onDetach() {
        activity = null
        callbacks = null
    }

    fun setMediaTypes(mediaTypes: Array<String>) {
        val filter = mediaTypes.joinToString { "'$it'" }
        if (filter.isNotEmpty()) {
            typeFilter = "${MediaStore.Images.Media.MIME_TYPE} IN ($filter)"
        }
    }

    fun loadBuckets() {
        LoaderManager.getInstance(ensureActivityAttached())
            .restartLoader(BUCKET_LOADER, null, this)
    }

    fun loadByBucket(@IntRange(from = 0) bucketId: Long) {
        ensureActivityAttached().let { activity ->
            if (ALL_MEDIA_BUCKET_ID == bucketId) {
                LoaderManager.getInstance(activity).restartLoader(TIME_LOADER, null, this)
            } else {
                val args = Bundle()
                args.putLong(ARG_BUCKET_ID, bucketId)
                LoaderManager.getInstance(activity).restartLoader(MEDIA_LOADER, args, this)
            }
        }
    }

    /**
     * Ensure that a FragmentActivity is attached to this loader.
     */
    private fun ensureActivityAttached(): FragmentActivity =
        requireNotNull(activity) { "The FragmentActivity was not attached!" }

    private fun finishUpBuckets(cursor: Cursor?): Cursor? =
        MergeCursor(
            arrayOf(
                addAllMediaBucketItem(cursor),
                if (isAllowedAggregatedFunctions) cursor
                else aggregateBuckets(cursor)
            )
        )

    /**
     * Add "All Media" item as the first row of bucket items.
     *
     * @param cursor The original data of all bucket items
     * @return The data with "All Media" item added
     */
    private fun addAllMediaBucketItem(cursor: Cursor?): Cursor? =
        cursor?.run {
            if (!moveToPosition(0)) return null
            val id = ALL_MEDIA_BUCKET_ID
            val label = ensureActivityAttached().getString(R.string.activity_gallery_bucket_all_media)
            val data = getString(getColumnIndex(MediaStore.Images.Media.DATA))
            MatrixCursor(BUCKET_PROJECTION).apply {
                newRow()
                    .add(id)
                    .add(label)
                    .add(data)
            }
        }

    /**
     * Since we are not allowed to use SQL aggregation functions we need to do that on code
     *
     * @param cursor The original data of all bucket items
     * @return The data aggregated by buckets
     */
    private fun aggregateBuckets(cursor: Cursor?): Cursor? =
        cursor?.run {
            val idIndex = getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
            val labelIndex = getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val dataIndex = getColumnIndex(MediaStore.Images.Media.DATA)

            val aggregatedBucket = MatrixCursor(BUCKET_PROJECTION)
            var previousId = 0L

            for (position in 0 until cursor.count) {
                moveToPosition(position)
                val id = getLong(idIndex)
                val label = getString(labelIndex)
                val data = getString(dataIndex)

                if (id != previousId) {
                    aggregatedBucket.newRow()
                        .add(id)
                        .add(label)
                        .add(data)
                }
                previousId = id
            }

            aggregatedBucket
        }
}

internal val isAllowedAggregatedFunctions = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
