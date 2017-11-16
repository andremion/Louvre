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

package com.andremion.louvre.sample;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private final List<Uri> mData;

    MainAdapter() {
        mData = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_main, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Uri data = mData.get(position);
        Glide.with(holder.mImageView.getContext())
                .load(data)
                .apply(RequestOptions.skipMemoryCacheOf(true)
                        .centerCrop()
                        .placeholder(com.andremion.louvre.R.color.gallery_item_background))
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    void swapData(@Nullable List<Uri> data) {
        if (!mData.equals(data)) {
            mData.clear();
            if (data != null) {
                mData.addAll(data);
            }
            notifyDataSetChanged();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mImageView;

        private ViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView;
        }
    }


}
