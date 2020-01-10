/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.customization.model.theme;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a header coming from the resources of the theme bundle container APK.
 */
public class ThemeBundledHeaderInfo implements Parcelable {
    public static final Creator<ThemeBundledHeaderInfo> CREATOR =
            new Creator<ThemeBundledHeaderInfo>() {
                @Override
                public ThemeBundledHeaderInfo createFromParcel(Parcel in) {
                    return new ThemeBundledHeaderInfo(in);
                }

                @Override
                public ThemeBundledHeaderInfo[] newArray(int size) {
                    return new ThemeBundledHeaderInfo[size];
                }
            };

    private static final String TAG = "ThemeBundledHeaderInfo";

    private final String mPackageName;
    private final String mResName;
    private final String mCollectionId;
    private final String mDrawableResName;
    private Resources mResources;

    /**
     * Constructs a new theme-bundled static header model object.
     *
     * @param drawableResId  Resource ID of the raw header image.
     * @param resName        The unique name of the header resource, e.g. "z_wp001".
     * @param themeName   Unique name of the collection this header belongs in; used for logging.
     */
    public ThemeBundledHeaderInfo(String packageName, String resName, String themeName,
            String drawableResName) {
        mPackageName = packageName;
        mResName = resName;
        mCollectionId = themeName;
        mDrawableResName = drawableResName;
    }

    private ThemeBundledHeaderInfo(Parcel in) {
        mPackageName = in.readString();
        mResName = in.readString();
        mCollectionId = in.readString();
        mDrawableResName = in.readString();
    }

    public Drawable getDrawable(Context context) {
        Resources res = getPackageResources(context);
        int drawableResId = res.getIdentifier(mDrawableResName, "drawable", mPackageName);
        return res.getDrawable(drawableResId);
    }

    public String getCollectionId(Context unused) {
        return mCollectionId;
    }

    public String getHeaderId() {
        return mResName;
    }

    public String getResName() {
        return mResName;
    }

    public String getDrawableResName() {
        return mPackageName + "/" + mDrawableResName;
    }

    /**
     * Returns the {@link Resources} instance for the theme bundles stub APK.
     */
    private Resources getPackageResources(Context context) {
        if (mResources != null) {
            return mResources;
        }

        try {
            mResources = context.getPackageManager().getResourcesForApplication(mPackageName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not get app resources for " + mPackageName);
        }
        return mResources;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPackageName);
        dest.writeString(mResName);
        dest.writeString(mCollectionId);
        dest.writeString(mDrawableResName);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
