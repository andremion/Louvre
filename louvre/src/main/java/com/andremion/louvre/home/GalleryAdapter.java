package com.andremion.louvre.home;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.andremion.louvre.R;
import com.andremion.louvre.util.AnimationHelper;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link RecyclerView.Adapter} subclass used to bind {@link Cursor} items from {@link MediaStore} into {@link RecyclerView}
 * <p>
 * We can have two types of {@link View} items: {@link #VIEW_TYPE_BUCKET} or {@link #VIEW_TYPE_MEDIA}
 */
class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    static final int VIEW_TYPE_BUCKET = 0;
    static final int VIEW_TYPE_MEDIA = 1;

    private static final String SELECTION_PAYLOAD = "selection";
    private static final float SELECTED_SCALE = .8f;
    private static final float UNSELECTED_SCALE = 1f;

    @IntDef({VIEW_TYPE_BUCKET, VIEW_TYPE_MEDIA})
    @Retention(RetentionPolicy.SOURCE)
    @interface ViewType {
    }

    interface Callbacks {

        void onBucketClick(long bucketId, String label);

        void onMediaClick(View imageView, View checkView, long bucketId, int position);

        void onSelectionUpdated(int count);

        void onMaxSelectionReached();

        void onWillExceedMaxSelection();
    }

    private final LinkedHashMap<Long, Uri> mSelection;

    @Nullable
    private Callbacks mCallbacks;
    private int mMaxSelection;
    @Nullable
    private LinearLayoutManager mLayoutManager;
    private int mViewType = VIEW_TYPE_BUCKET;
    @Nullable
    private Cursor mData;

    GalleryAdapter() {
        mSelection = new LinkedHashMap<>();
        setHasStableIds(true);
    }

    void setCallbacks(@Nullable Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    void setMaxSelection(@IntRange(from = 0) int maxSelection) {
        mMaxSelection = maxSelection;
    }

    public void setLayoutManager(@NonNull LinearLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    void swapData(@ViewType int viewType, @Nullable Cursor data) {
        if (viewType != mViewType) {
            mViewType = viewType;
        }
        if (data != mData) {
            mData = data;
            notifyDataSetChanged();
        }
    }

    @Override
    public long getItemId(int position) {
        if (mData != null && !mData.isClosed()) {
            mData.moveToPosition(position);
            if (VIEW_TYPE_MEDIA == mViewType) {
                return mData.getLong(mData.getColumnIndex(MediaStore.Images.ImageColumns._ID));
            } else {
                return mData.getLong(mData.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID));
            }
        }
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        if (mData != null && !mData.isClosed()) {
            return mData.getCount();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return mViewType;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        // TODO: 18/01/2017 onAttachedToRecyclerView
    }

    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, @ViewType int viewType) {
        if (VIEW_TYPE_MEDIA == viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_gallery_media, parent, false);
            return new MediaViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_gallery_bucket, parent, false);
            return new BucketViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryAdapter.ViewHolder holder, int position) {
        Uri data = getData(position);
        String imageTransitionName = holder.itemView.getContext().getString(R.string.activity_gallery_image_transition, data.toString());
        String checkboxTransitionName = holder.itemView.getContext().getString(R.string.activity_gallery_checkbox_transition, data.toString());
        ViewCompat.setTransitionName(holder.mImageView, imageTransitionName);
        Picasso.with(holder.mImageView.getContext())
                .load(data)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .fit()
                .centerCrop()
                .placeholder(R.color.gallery_item_background)
                .into(holder.mImageView);

        boolean selected = isSelected(position);
        if (selected) {
            holder.mImageView.setScaleX(SELECTED_SCALE);
            holder.mImageView.setScaleY(SELECTED_SCALE);
        } else {
            holder.mImageView.setScaleX(UNSELECTED_SCALE);
            holder.mImageView.setScaleY(UNSELECTED_SCALE);
        }

        if (VIEW_TYPE_MEDIA == getItemViewType(position)) {
            MediaViewHolder viewHolder = (MediaViewHolder) holder;
            ViewCompat.setTransitionName(viewHolder.mCheckView, checkboxTransitionName);
            viewHolder.mCheckView.setChecked(selected);
            holder.mImageView.setContentDescription(getLabel(position));
        } else {
            BucketViewHolder viewHolder = (BucketViewHolder) holder;
            viewHolder.mTextView.setText(getLabel(position));
        }
    }

    /**
     * Binding view holder with payloads is used to handle partial changes in item.
     */
    @Override
    public void onBindViewHolder(GalleryAdapter.ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) { // If doesn't have any payload then bind the fully item
            super.onBindViewHolder(holder, position, payloads);
        } else {
            for (Object payload : payloads) {
                boolean selected = isSelected(position);
                if (SELECTION_PAYLOAD.equals(payload)) {
                    if (VIEW_TYPE_MEDIA == getItemViewType(position)) {
                        MediaViewHolder viewHolder = (MediaViewHolder) holder;
                        viewHolder.mCheckView.setChecked(selected);
                        if (selected) {
                            AnimationHelper.scaleView(holder.mImageView, SELECTED_SCALE);
                        } else {
                            AnimationHelper.scaleView(holder.mImageView, UNSELECTED_SCALE);
                        }
                    }
                }
            }
        }
    }

    List<Uri> getSelection() {
        List<Uri> selected = new ArrayList<>();
        for (Map.Entry<Long, Uri> entry : mSelection.entrySet()) {
            selected.add(entry.getValue());
        }
        return selected;
    }

    LinkedHashMap<Long, Uri> getRawSelection() {
        return new LinkedHashMap<>(mSelection);
    }

    void setSelection(@NonNull HashMap<Long, Uri> selection) {
        if (!mSelection.equals(selection)) {
            mSelection.clear();
            mSelection.putAll(selection);
            notifySelectionChanged();
        }
    }

    void selectAll() {
        if (mData == null) {
            return;
        }
        LinkedHashMap<Long, Uri> selectionToAdd = new LinkedHashMap<>();
        int count = mData.getCount();
        for (int position = 0; position < count; position++) {
            if (!isSelected(position)) {
                long id = getItemId(position);
                Uri data = getData(position);
                selectionToAdd.put(id, data);
            }
        }
        if (mSelection.size() + selectionToAdd.size() > mMaxSelection) {
            if (mCallbacks != null) {
                mCallbacks.onWillExceedMaxSelection();
            }
        } else {
            mSelection.putAll(selectionToAdd);
            notifySelectionChanged();
        }
    }

    void clearSelection() {
        if (!mSelection.isEmpty()) {
            mSelection.clear();
            notifySelectionChanged();
        }
    }

    private void notifySelectionChanged() {
        if (mCallbacks != null) {
            mCallbacks.onSelectionUpdated(mSelection.size());
        }
        int from = 0, count = getItemCount();
        // If we have LinearLayoutManager we should just rebind the visible items
        if (mLayoutManager != null) {
            from = mLayoutManager.findFirstVisibleItemPosition();
            count = mLayoutManager.findLastVisibleItemPosition() - from + 1;
        }
        notifyItemRangeChanged(from, count, SELECTION_PAYLOAD);
    }

    private boolean isSelected(int position) {
        long itemId = getItemId(position);
        return mSelection.containsKey(itemId);
    }

    private String getLabel(int position) {
        assert mData != null; // It is supposed not be null here
        mData.moveToPosition(position);
        if (mViewType == VIEW_TYPE_MEDIA) {
            return mData.getString(mData.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
        } else {
            return mData.getString(mData.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
        }
    }

    private Uri getData(int position) {
        assert mData != null; // It is supposed not be null here
        mData.moveToPosition(position);
        return Uri.fromFile(new File(mData.getString(mData.getColumnIndex(MediaStore.Images.Media.DATA))));
    }

    private long getBucketId(int position) {
        assert mData != null; // It is supposed not be null here
        mData.moveToPosition(position);
        return mData.getLong(mData.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID));
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView mImageView;

        private ViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.image);
        }
    }

    private class BucketViewHolder extends ViewHolder implements View.OnClickListener {

        private final TextView mTextView;

        private BucketViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            // getAdapterPosition() returns RecyclerView.NO_POSITION if item has been removed from the adapter,
            // RecyclerView.Adapter.notifyDataSetChanged() has been called after the last layout pass
            // or the ViewHolder has already been recycled.
            if (position == RecyclerView.NO_POSITION) {
                return;
            }

            if (mCallbacks != null) {
                mCallbacks.onBucketClick(getItemId(), getLabel(position));
            }
        }

    }

    class MediaViewHolder extends ViewHolder implements View.OnClickListener {

        final CheckedTextView mCheckView;

        private MediaViewHolder(View itemView) {
            super(itemView);
            mCheckView = (CheckedTextView) itemView.findViewById(R.id.check);
            mCheckView.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            // getAdapterPosition() returns RecyclerView.NO_POSITION if item has been removed from the adapter,
            // RecyclerView.Adapter.notifyDataSetChanged() has been called after the last layout pass
            // or the ViewHolder has already been recycled.
            if (position == RecyclerView.NO_POSITION) {
                return;
            }

            if (v == mCheckView) {
                boolean selectionChanged = handleChangeSelection(position);
                if (selectionChanged) {
                    notifyItemChanged(position, SELECTION_PAYLOAD);
                }
                if (mCallbacks != null) {
                    if (selectionChanged) {
                        mCallbacks.onSelectionUpdated(mSelection.size());
                    } else {
                        mCallbacks.onMaxSelectionReached();
                    }
                }
            } else {
                if (mCallbacks != null) {
                    mCallbacks.onMediaClick(mImageView, mCheckView, getBucketId(position), position);
                }
            }
        }

    }

    private boolean handleChangeSelection(int position) {
        final long itemId = getItemId(position);
        if (!isSelected(position)) {
            if (mSelection.size() == mMaxSelection) {
                return false;
            }
            mSelection.put(itemId, getData(position));
        } else {
            mSelection.remove(itemId);
        }
        return true;
    }
}