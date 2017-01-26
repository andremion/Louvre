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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.widget.NumberPicker;

public class NumberPickerDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String TAG = NumberPickerDialog.class.getSimpleName();
    private static final String ARG_REQUEST_CODE = "request_code_arg";
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 100;
    private static final int INITIAL_VALUE = 10;

    public static void show(@NonNull FragmentManager fragmentManager, int requestCode) {
        NumberPickerDialog dialog = new NumberPickerDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_REQUEST_CODE, requestCode);
        dialog.setArguments(args);
        dialog.show(fragmentManager, TAG);
    }

    private NumberPicker mNumberPicker;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mNumberPicker = new NumberPicker(getContext());
        mNumberPicker.setMinValue(MIN_VALUE);
        mNumberPicker.setMaxValue(MAX_VALUE);
        mNumberPicker.setValue(INITIAL_VALUE);
        mNumberPicker.setWrapSelectorWheel(true);
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.max_select_prompt)
                .setView(mNumberPicker)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        MediaTypeFilterDialog.show(getFragmentManager(),
                getArguments().getInt(ARG_REQUEST_CODE),
                mNumberPicker.getValue());
    }
}
