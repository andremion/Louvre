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

package com.andremion.louvre.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;

import com.andremion.louvre.home.GalleryActivity;
import com.andremion.louvre.util.ItemOffsetDecoration;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int LOUVRE_REQUEST_CODE = 0;

    private MainAdapter mAdapter;
    private List<Uri> mSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final int spacing = getResources().getDimensionPixelSize(R.dimen.gallery_item_offset);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(new ItemOffsetDecoration(spacing));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter = new MainAdapter());
        recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                int size = getResources().getDimensionPixelSize(R.dimen.gallery_item_size);
                int width = recyclerView.getMeasuredWidth();
                int columnCount = width / (size + spacing);
                recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, columnCount));
                return false;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NumberPickerDialog.show(getSupportFragmentManager(), LOUVRE_REQUEST_CODE, mSelection);
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //noinspection unchecked
        mSelection = (List<Uri>) getLastCustomNonConfigurationInstance();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mSelection;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOUVRE_REQUEST_CODE && resultCode == RESULT_OK) {
            mAdapter.swapData(mSelection = GalleryActivity.getSelection(data));
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
