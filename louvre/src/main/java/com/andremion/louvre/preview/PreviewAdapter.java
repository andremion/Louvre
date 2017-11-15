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

package com.andremion.louvre.preview;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import com.andremion.louvre.R;
import com.andremion.louvre.util.transition.MediaSharedElementCallback;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static android.view.View.NO_ID;

class PreviewAdapter extends PagerAdapter {

    interface Callbacks {

        void onCheckedUpdated(boolean checked);

        void onMaxSelectionReached();
    }

    private final FragmentActivity mActivity;
    private final LayoutInflater mInflater;
    private final CheckedTextView mCheckbox;
    private final MediaSharedElementCallback mSharedElementCallback;
    private final List<Uri> mSelection;
    @Nullable
    private PreviewAdapter.Callbacks mCallbacks;
    private int mMaxSelection;
    private int mInitialPosition;
    @Nullable
    private Cursor mData;
    private boolean mDontAnimate;
    private int mCurrentPosition = RecyclerView.NO_POSITION;

    PreviewAdapter(@NonNull FragmentActivity activity, @NonNull CheckedTextView checkbox, @NonNull MediaSharedElementCallback sharedElementCallback, @NonNull List<Uri> selection) {
        mActivity = activity;
        mInflater = LayoutInflater.from(activity);
        mCheckbox = checkbox;
        mSharedElementCallback = sharedElementCallback;
        mSelection = selection;
        mDontAnimate = true;
    }

    void setCallbacks(@Nullable PreviewAdapter.Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    void setMaxSelection(@IntRange(from = 0) int maxSelection) {
        mMaxSelection = maxSelection;
    }

    void setInitialPosition(int position) {
        mInitialPosition = position;
    }

    void swapData(Cursor data) {
        if (data != mData) {
            mData = data;
            notifyDataSetChanged();
        }
    }

    void setDontAnimate(boolean dontAnimate) {
        mDontAnimate = dontAnimate;
    }

    @Override
    public int getCount() {
        if (mData != null && !mData.isClosed()) {
            return mData.getCount();
        }
        return 0;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mInflater.inflate(R.layout.page_item_preview, container, false);
        ViewHolder holder = new ViewHolder(view);
        Uri data = getData(position);
        onViewBound(holder, position, data);
        container.addView(holder.itemView);
        return holder;
    }

    @Nullable
    Uri getData(int position) {
        if (mData != null && !mData.isClosed()) {
            mData.moveToPosition(position);
            return Uri.fromFile(new File(mData.getString(mData.getColumnIndex(MediaStore.Images.Media.DATA))));
        }
        return null;
    }

    private long getItemId(int position) {
        if (mData != null && !mData.isClosed()) {
            mData.moveToPosition(position);
            return mData.getLong(mData.getColumnIndex(MediaStore.Images.ImageColumns._ID));
        }
        return NO_ID;
    }

    private void onViewBound(ViewHolder holder, int position, Uri data) {
        String imageTransitionName = holder.imageView.getContext().getString(R.string.activity_gallery_image_transition, data.toString());
        ViewCompat.setTransitionName(holder.imageView, imageTransitionName);

        RequestOptions options = new RequestOptions()
                .skipMemoryCache(true)
                .fitCenter();
        if (mDontAnimate) {
            options.dontAnimate();
        }
        Glide.with(mActivity)
                .load(data)
                .apply(options)
                .listener(new ImageLoadingCallback(position))
                .into(holder.imageView);
    }

    private boolean isSelected(int position) {
        Uri data = getData(position);
        return mSelection.contains(data);
    }

    private void startPostponedEnterTransition(int position) {
        if (position == mInitialPosition) {
            mActivity.supportStartPostponedEnterTransition();
        }
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (object instanceof ViewHolder) {
            mCurrentPosition = position;
            mSharedElementCallback.setSharedElementViews(((ViewHolder) object).imageView, mCheckbox);
            if (mCallbacks != null) {
                mCallbacks.onCheckedUpdated(isSelected(position));
            }
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object instanceof ViewHolder
                && view.equals(((ViewHolder) object).itemView);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = ((ViewHolder) object).itemView;
        container.removeView(view);
    }

    void selectCurrentItem() {
        boolean selectionChanged = handleChangeSelection(mCurrentPosition);
        if (selectionChanged) {
            notifyDataSetChanged();
        }
        if (mCallbacks != null) {
            if (selectionChanged) {
                mCallbacks.onCheckedUpdated(isSelected(mCurrentPosition));
            } else {
                mCallbacks.onMaxSelectionReached();
            }
        }
    }

    List<Uri> getSelection() {
        return new LinkedList<>(mSelection);
    }

    private boolean handleChangeSelection(int position) {
        Uri data = getData(position);
        if (!isSelected(position)) {
            if (mSelection.size() == mMaxSelection) {
                return false;
            }
            mSelection.add(data);
        } else {
            mSelection.remove(data);
        }
        return true;
    }

    private static class ViewHolder {

        final View itemView;
        final ImageView imageView;

        ViewHolder(View view) {
            itemView = view;
            imageView = view.findViewById(R.id.image);
        }

    }

    private class ImageLoadingCallback implements RequestListener<Drawable> {

        final int mPosition;

        ImageLoadingCallback(int position) {
            mPosition = position;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            startPostponedEnterTransition(mPosition);
            return false;
        }

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            startPostponedEnterTransition(mPosition);
            return false;
        }
    }

}
