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

package com.andremion.louvre;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

/**
 * {@link AppCompatActivity} that manages request for {@link Manifest.permission#READ_EXTERNAL_STORAGE} Permission
 */
public abstract class StoragePermissionActivity extends AppCompatActivity {

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 4321;
    private static final int REQUEST_APP_SETTINGS = 1234;

    public void askForPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously*
                showExplanation();

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_EXTERNAL_STORAGE);
            }

        } else {

            onPermissionGranted();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionGranted();
                } else {
                    showExplanation();
                }
                break;
            }
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_APP_SETTINGS) {
            askForPermission();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Callback when permission is granted
     */
    public abstract void onPermissionGranted();

    /**
     * Show UI with rationale for requesting a storage permission.
     * <p>
     * "You should do this only if you do not have the permission and the context in
     * which the permission is requested does not clearly communicate to the user
     * what would be the benefit from granting this permission."
     * </p>
     */
    private void showExplanation() {
        Snackbar.make(findSuitableView(), getString(R.string.activity_gallery_permission_request_explanation), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.activity_gallery_permission_request_settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getPackageName(), null));
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intent, REQUEST_APP_SETTINGS);
                    }
                })
                .show();
    }

    private View findSuitableView() {
        View view = findViewById(R.id.coordinator_layout);
        if (view == null) {
            view = getWindow().getDecorView();
        }
        return view;
    }

}
