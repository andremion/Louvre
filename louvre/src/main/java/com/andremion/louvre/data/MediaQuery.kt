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

import android.net.Uri
import android.provider.MediaStore

/**
 * Helper properties used by [MediaLoader]
 */

internal val GALLERY_URI: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
internal val IMAGE_PROJECTION: Array<String> = arrayOf(
    MediaStore.Images.Media._ID,
    MediaStore.Images.Media.BUCKET_ID,
    MediaStore.Images.Media.DISPLAY_NAME,
    MediaStore.Images.Media.DATA
)
internal const val ALL_MEDIA_BUCKET_ID: Long = 0L
internal const val MEDIA_SORT_ORDER: String = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
internal val BUCKET_PROJECTION: Array<String> = arrayOf(
    MediaStore.Images.Media.BUCKET_ID,
    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
    MediaStore.Images.Media.DATA
)

// The template for "WHERE" parameter is like:
//    SELECT ... FROM ... WHERE (%s)
// and we make it look like:
//    SELECT ... FROM ... WHERE (1) GROUP BY (1)
// The "WHERE (1)" means true.
// The "GROUP BY (1)" means the first column specified after SELECT.
// Note that because there is a "(" and )" in the template, we use "1)" and "(1" to match it.
//
// *Hack pulled from https://android.googlesource.com/platform/packages/apps/Gallery2/+/android-4.4.2_r2/src/com/android/gallery3d/data/BucketHelper.java
//
// *Aggregation functions are not allowed from API 29 on
internal val BUCKET_SELECTION: String = if (isAllowedAggregatedFunctions) "1) GROUP BY (1" else "1"
internal val BUCKET_SORT_ORDER: String =
    if (isAllowedAggregatedFunctions) "MAX(${MediaStore.Images.Media.DATE_TAKEN}) DESC"
    else "${MediaStore.Images.Media.BUCKET_ID}, ${MediaStore.Images.Media.DATE_TAKEN} DESC"
