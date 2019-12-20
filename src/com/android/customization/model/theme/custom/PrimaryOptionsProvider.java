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
package com.android.customization.model.theme.custom;

import static com.android.customization.model.ResourceConstants.PRIMARY_COLOR_NAME;
import static com.android.customization.model.ResourceConstants.ANDROID_PACKAGE;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_ANDROID_THEME;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_PRIMARY;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_ICON_ANDROID;
import static com.android.customization.model.ResourceConstants.PATH_SIZE;
import static com.android.customization.model.ResourceConstants.SYSUI_PACKAGE;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.graphics.PathParser;

import com.android.customization.model.ResourceConstants;
import com.android.customization.model.theme.OverlayManagerCompat;
import com.android.customization.model.theme.custom.ThemeComponentOption.PrimaryOption;
import com.android.wallpaper.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link ThemeComponentOptionProvider} that reads {@link PrimaryOption}s from
 */
public class PrimaryOptionsProvider extends ThemeComponentOptionProvider<PrimaryOption> {

    private static final String TAG = "PrimaryOptionsProvider";
    private final CustomThemeManager mCustomThemeManager;
    private final String mDefaultThemePackage;

    public PrimaryOptionsProvider(Context context, OverlayManagerCompat manager,
            CustomThemeManager customThemeManager) {
        super(context, manager, OVERLAY_CATEGORY_PRIMARY);
        mCustomThemeManager = customThemeManager;
        // System color is set with a static overlay for android.theme category, so let's try to
        // find that first, and if that's not present, we'll default to System resources.
        // (see #addDefault())
        List<String> themePackages = manager.getOverlayPackagesForCategory(
                OVERLAY_CATEGORY_ANDROID_THEME, UserHandle.myUserId(), ANDROID_PACKAGE);
        mDefaultThemePackage = themePackages.isEmpty() ? null : themePackages.get(0);
    }

    @Override
    protected void loadOptions() {
        int accentColor = mCustomThemeManager.getColorAccentDark();
        addDefault();
        for (String overlayPackage : mOverlayPackages) {
            try {
                Resources overlayRes = getOverlayResources(overlayPackage);
                int primaryColor = overlayRes.getColor(
                        overlayRes.getIdentifier(PRIMARY_COLOR_NAME, "color", overlayPackage),
                        null);
                PackageManager pm = mContext.getPackageManager();
                String label = pm.getApplicationInfo(overlayPackage, 0).loadLabel(pm).toString();
                PrimaryOption option = new PrimaryOption(overlayPackage, label, primaryColor, accentColor);
                mOptions.add(option);
            } catch (NameNotFoundException | NotFoundException e) {
                Log.w(TAG, String.format("Couldn't load primary overlay %s, will skip it",
                        overlayPackage), e);
            }
        }
    }

    private void addDefault() {
        int primaryColor;
        Resources system = Resources.getSystem();
        int accentColor = mCustomThemeManager.getColorAccentDark();
        try {
            Resources r = getOverlayResources(mDefaultThemePackage);
            primaryColor = r.getColor(
                    r.getIdentifier(PRIMARY_COLOR_NAME, "color", mDefaultThemePackage),
                    null);
        } catch (NotFoundException | NameNotFoundException e) {
            Log.d(TAG, "Didn't find default color, will use system option", e);

            primaryColor = system.getColor(
                    system.getIdentifier(PRIMARY_COLOR_NAME, "color", ANDROID_PACKAGE), null);
        }
        PrimaryOption option = new PrimaryOption(null,
                mContext.getString(R.string.default_theme_title), primaryColor, accentColor);
        mOptions.add(option);
    }
}
