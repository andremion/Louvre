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

package com.andremion.louvre.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andremion.counterfab.CounterFab;
import com.andremion.louvre.R;
import com.andremion.louvre.StoragePermissionActivity;
import com.andremion.louvre.preview.PreviewActivity;
import com.andremion.louvre.util.transition.TransitionCallback;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GalleryActivity extends StoragePermissionActivity implements GalleryFragment.Callbacks, View.OnClickListener {

    private static final String EXTRA_MAX_SELECTION = GalleryActivity.class.getPackage().getName() + ".extra.MAX_SELECTION";
    private static final String EXTRA_MEDIA_TYPE_FILTER = GalleryActivity.class.getPackage().getName() + ".extra.MEDIA_TYPE_FILTER";
    private static final String EXTRA_SELECTION = GalleryActivity.class.getPackage().getName() + ".extra.SELECTION";
    private static final int DEFAULT_MAX_SELECTION = 1;
    private static final String TITLE_STATE = "title_state";
    private static final int PREVIEW_REQUEST_CODE = 0;

    /**
     * Start the Gallery Activity with additional launch information.
     *
     * @param activity        Context to launch activity from.
     * @param requestCode     If >= 0, this code will be returned in onActivityResult() when the activity exits.
     * @param maxSelection    The max count of image selection
     * @param selection       The current image selection
     * @param mediaTypeFilter The media types that will display
     */
    public static void startActivity(@NonNull Activity activity, int requestCode,
                                     @IntRange(from = 0) int maxSelection,
                                     List<Uri> selection,
                                     String... mediaTypeFilter) {
        Intent intent = buildIntent(activity, maxSelection, selection, mediaTypeFilter);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Start the Gallery Activity with additional launch information.
     *
     * @param fragment        Context to launch fragment from.
     * @param requestCode     If >= 0, this code will be returned in onActivityResult() when the fragment exits.
     * @param maxSelection    The max count of image selection
     * @param selection       The current image selection
     * @param mediaTypeFilter The media types that will display
     */
    public static void startActivity(@NonNull Fragment fragment, int requestCode,
                                     @IntRange(from = 0) int maxSelection,
                                     List<Uri> selection,
                                     String... mediaTypeFilter) {
        Intent intent = buildIntent(fragment.getContext(), maxSelection, selection, mediaTypeFilter);
        fragment.startActivityForResult(intent, requestCode);
    }

    @NonNull
    private static Intent buildIntent(@NonNull Context context, @IntRange(from = 0) int maxSelection, List<Uri> selection, String[] mediaTypeFilter) {
        Intent intent = new Intent(context, GalleryActivity.class);
        if (maxSelection > 0) {
            intent.putExtra(EXTRA_MAX_SELECTION, maxSelection);
        }
        if (selection != null) {
            intent.putExtra(EXTRA_SELECTION, new LinkedList<>(selection));
        }
        if (mediaTypeFilter != null && mediaTypeFilter.length > 0) {
            intent.putExtra(EXTRA_MEDIA_TYPE_FILTER, mediaTypeFilter);
        }
        return intent;
    }

    public static List<Uri> getSelection(Intent data) {
        return data.getParcelableArrayListExtra(EXTRA_SELECTION);
    }

    private GalleryFragment mFragment;
    private ViewGroup mContentView;
    private CounterFab mFab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupTransition();

        mContentView = (ViewGroup) findViewById(R.id.coordinator_layout);

        mFab = (CounterFab) findViewById(R.id.fab_done);
        mFab.setOnClickListener(this);

        mFragment = (GalleryFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_gallery);
        mFragment.setMaxSelection(getIntent().getIntExtra(EXTRA_MAX_SELECTION, DEFAULT_MAX_SELECTION));
        if (getIntent().hasExtra(EXTRA_SELECTION)) {
            //noinspection unchecked
            mFragment.setSelection((List<Uri>) getIntent().getSerializableExtra(EXTRA_SELECTION));
        }
        if (getIntent().hasExtra(EXTRA_MEDIA_TYPE_FILTER)) {
            mFragment.setMediaTypeFilter(getIntent().getStringArrayExtra(EXTRA_MEDIA_TYPE_FILTER));
        }

        if (savedInstanceState == null) {
            setResult(RESULT_CANCELED);
            askForPermission();
        } else {
            setActionBarTitle(savedInstanceState.getString(TITLE_STATE));
        }
    }

    private void setupTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TransitionInflater inflater = TransitionInflater.from(this);
            Transition exitTransition = inflater.inflateTransition(R.transition.gallery_exit);
            exitTransition.addListener(new TransitionCallback() {
                @Override
                public void onTransitionStart(Transition transition) {
                    mFab.hide();
                }
            });
            getWindow().setExitTransition(exitTransition);
            Transition reenterTransition = inflater.inflateTransition(R.transition.gallery_reenter);
            reenterTransition.addListener(new TransitionCallback() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    mFab.show();
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    mFab.show();
                }
            });
            getWindow().setReenterTransition(reenterTransition);
        }
    }

    @Override
    public void onPermissionGranted() {
        mFragment.loadBuckets();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(TITLE_STATE, getSupportActionBar().getTitle());
    }

    @Override
    public void onBackPressed() {
        if (mFragment.onBackPressed()) {
            resetActionBarTitle();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        mFragment.onActivityReenter(resultCode, data);
    }

    @Override
    public void onClick(View v) {
        Intent data = new Intent();
        data.putExtra(EXTRA_SELECTION, (ArrayList<Uri>) mFragment.getSelection());
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onBucketClick(String label) {
        setActionBarTitle(label);
    }

    @Override
    public void onMediaClick(@NonNull View imageView, @NonNull View checkView, long bucketId, int position) {
        if (getIntent().hasExtra(EXTRA_MEDIA_TYPE_FILTER)) {
            PreviewActivity.startActivity(this, PREVIEW_REQUEST_CODE, imageView, checkView, bucketId, position, mFragment.getSelection(),
                    getIntent().getIntExtra(EXTRA_MAX_SELECTION, DEFAULT_MAX_SELECTION),
                    getIntent().getStringArrayExtra(EXTRA_MEDIA_TYPE_FILTER));
        } else {
            PreviewActivity.startActivity(this, PREVIEW_REQUEST_CODE, imageView, checkView, bucketId, position, mFragment.getSelection(),
                    getIntent().getIntExtra(EXTRA_MAX_SELECTION, DEFAULT_MAX_SELECTION));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Before Lollipop we don't have Activity.onActivityReenter() callback,
        // so we have to call GalleryFragment.onActivityReenter() here.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mFragment.onActivityReenter(resultCode, data);
        }

        if (requestCode == PREVIEW_REQUEST_CODE) {
            mFragment.setSelection(PreviewActivity.getSelection(data));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSelectionUpdated(int count) {
        mFab.setCount(count);
    }

    @Override
    public void onMaxSelectionReached() {
        Snackbar.make(mContentView, R.string.activity_gallery_max_selection_reached, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onWillExceedMaxSelection() {
        Snackbar.make(mContentView, R.string.activity_gallery_will_exceed_max_selection, Snackbar.LENGTH_SHORT).show();
    }

    @SuppressWarnings("ConstantConditions")
    private void setActionBarTitle(@Nullable CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    private void resetActionBarTitle() {
        setActionBarTitle(getTitle());
    }

}
